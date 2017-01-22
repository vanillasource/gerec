/**
 * Copyright (C) 2016 VanillaSource
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.vanillasource.gerec.httpclient;

import org.apache.http.nio.client.HttpAsyncClient;
import com.vanillasource.gerec.reference.AsyncHttpClient;
import com.vanillasource.gerec.*;
import com.vanillasource.gerec.http.Headers;
import org.apache.http.client.methods.*;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpMessage;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.HttpHost;
import java.util.function.Supplier;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.net.URI;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.nio.ContentDecoderChannel;
import java.util.function.Function;

public final class AsyncApacheHttpClient implements AsyncHttpClient {
   private final Supplier<HttpAsyncClient> httpClientSupplier;

   /**
    * Create an instance that references the real http client only indirectly, useful for
    * serialization.
    */
   public AsyncApacheHttpClient(Supplier<HttpAsyncClient> httpClientSupplier) {
      this.httpClientSupplier = httpClientSupplier;
   }

   /**
    * Create an instance which directly references the http client. This is normally non-serializable.
    */
   public AsyncApacheHttpClient(HttpAsyncClient httpClient) {
      this(() -> httpClient);
   }

   @Override
   public CompletableFuture<HttpResponse> doHead(URI uri, HttpRequest.HttpRequestChange change) {
      return execute(new HttpHead(uri), change);
   }

   @Override
   public CompletableFuture<HttpResponse> doGet(URI uri, HttpRequest.HttpRequestChange change) {
      return execute(new HttpGet(uri), change);
   }

   @Override
   public CompletableFuture<HttpResponse> doPost(URI uri, HttpRequest.HttpRequestChange change) {
      return execute(new HttpPost(uri), change);
   }

   @Override
   public CompletableFuture<HttpResponse> doPut(URI uri, HttpRequest.HttpRequestChange change) {
      return execute(new HttpPut(uri), change);
   }

   @Override
   public CompletableFuture<HttpResponse> doDelete(URI uri, HttpRequest.HttpRequestChange change) {
      return execute(new HttpDelete(uri), change);
   }

   @Override
   public CompletableFuture<HttpResponse> doOptions(URI uri, HttpRequest.HttpRequestChange change) {
      return execute(new HttpOptions(uri), change);
   }

   private CompletableFuture<HttpResponse> execute(HttpEntityEnclosingRequestBase request, HttpRequest.HttpRequestChange change) {
      request.setEntity(null); // TODO
      return execute(new AsyncHttpRequest(request), change);
   }

   private CompletableFuture<HttpResponse> execute(HttpRequestBase request, HttpRequest.HttpRequestChange change) {
      return execute(new AsyncHttpRequest(request), change);
   }

   private CompletableFuture<HttpResponse> execute(AsyncHttpRequest request, HttpRequest.HttpRequestChange change) {
      change.applyTo(request);
      return execute(request);
   }

   private CompletableFuture<HttpResponse> execute(AsyncHttpRequest request) {
      CompletableFuture<HttpResponse> result = new CompletableFuture<>();
      httpClientSupplier.get().execute(
            request.getProducer(result),
            AsyncHttpResponse.getConsumer(result),
            new NopFutureCallback());
      return result;
   }

   private static class NopFutureCallback implements FutureCallback<Void> {
      @Override
      public void cancelled() {
      }

      @Override
      public void completed(Void result) {
      }

      @Override
      public void failed(Exception e) {
      }
   }

   private static class HeaderAwareMessage {
      private HttpMessage message;

      public HeaderAwareMessage(HttpMessage message) {
         this.message = message;
      }

      public boolean hasHeader(Header<?> header) {
         return message.containsHeader(header.getName());
      }

      public <T> T getHeader(Header<T> header) {
         org.apache.http.Header[] httpHeaders = message.getHeaders(header.getName());
         List<String> headerValues = new ArrayList<>(httpHeaders.length);
         for (org.apache.http.Header httpHeader: httpHeaders) {
            headerValues.add(httpHeader.getValue());
         }
         return header.deserialize(headerValues);
      }

      public <T> void setHeader(Header<T> header, T value) {
         message.removeHeaders(header.getName());
         for (String headerValue: header.serialize(value)) {
            message.addHeader(header.getName(), headerValue);
         }
      }
   }

   private static class AsyncHttpRequest extends HeaderAwareMessage implements HttpRequest {
      private Function<ControllableWritableByteChannel, ByteProducer> producerFactory;
      private HttpRequestBase request;

      private AsyncHttpRequest(HttpRequestBase request) {
         super(request);
         this.request = request;
      }

      public HttpAsyncRequestProducer getProducer(CompletableFuture<HttpResponse> responseFuture) {
         return new HttpAsyncRequestProducer() {
            private ByteProducer producer;

            @Override
            public void close() {
            }

            @Override
            public HttpHost getTarget() {
               return new HttpHost(request.getURI().getHost(), request.getURI().getPort(), request.getURI().getScheme());
            }

            @Override
            public org.apache.http.HttpRequest generateRequest() {
               return request;
            }

            @Override
            public void failed(Exception e) {
               responseFuture.completeExceptionally(e);
            }

            @Override
            public boolean isRepeatable() {
               return false;
            }

            @Override
            public void produceContent(ContentEncoder contentEncoder, IOControl control) {
               if (producer == null) {
                  producer = producerFactory.apply(new ControllableWritableByteChannel() {
                     @Override
                     public void pause() {
                        control.suspendOutput();
                     }

                     @Override
                     public void resume() {
                        control.requestOutput();
                     }

                     @Override
                     public int write(ByteBuffer buffer) throws IOException {
                        return contentEncoder.write(buffer);
                     }

                     @Override
                     public void close() throws IOException {
                        contentEncoder.complete();
                     }

                     @Override
                     public boolean isOpen() {
                        return true;
                     }
                  });
               }
               producer.onReady();
            }

            @Override
            public void requestCompleted(HttpContext context) {
               if (producer != null) {
                  producer.onCompleted();
               }
            }

            @Override
            public void resetRequest() {
               throw new UnsupportedOperationException("reset of request producing not supported");
            }
         };
      }

      @Override
      public void setByteProducer(Function<ControllableWritableByteChannel, ByteProducer> producerFactory) {
         this.producerFactory = producerFactory;
      }

      @Override
      public void setByteProducer(Function<ControllableWritableByteChannel, ByteProducer> producerFactory, long length) {
         this.producerFactory = producerFactory;
         setHeader(Headers.CONTENT_LENGTH, length);
      }
   }

   private static abstract class AsyncHttpResponse extends HeaderAwareMessage implements HttpResponse {
      private org.apache.http.HttpResponse response;

      private AsyncHttpResponse(org.apache.http.HttpResponse response) {
         super(response);
         this.response = response;
      }

      @Override
      public HttpStatusCode getStatusCode() {
         return HttpStatusCode.valueOf(response.getStatusLine().getStatusCode());
      }

      public static HttpAsyncResponseConsumer<Void> getConsumer(CompletableFuture<HttpResponse> responseFuture) {
         return new HttpAsyncResponseConsumer<Void>() {
            private volatile boolean done = false;
            private volatile Exception e;
            private volatile Function<ReadableByteChannel, ByteConsumer> responseConsumerFactory;
            private volatile ByteConsumer consumer;
            private volatile IOControl ioControl;

            @Override
            public void close() {
            }

            @Override
            public boolean cancel() {
               return false;
            }

            @Override
            public boolean isDone() {
               return done;
            }

            @Override
            public Void getResult() {
               return null;
            }

            @Override
            public Exception getException() {
               return e;
            }

            @Override
            public void failed(Exception e) {
               this.e = e;
               this.done = true;
               if (!responseFuture.isDone()) {
                  responseFuture.completeExceptionally(e);
               } else if (consumer != null) {
                  consumer.onException(e);
               }
            }

            @Override
            public void responseReceived(org.apache.http.HttpResponse response) {
               responseFuture.complete(new AsyncHttpResponse(response) {
                  @Override
                  public void consumeContent(Function<ReadableByteChannel, ByteConsumer> consumerFactory) {
                     if (responseConsumerFactory != null) {
                        throw new IllegalStateException("can only consume response once");
                     }
                     responseConsumerFactory = consumerFactory;
                     if (ioControl != null) {
                        ioControl.requestInput();
                     }
                  }
               });
            }

            @Override
            public void responseCompleted(HttpContext context) {
               this.done = true;
               if (consumer != null) {
                  consumer.onCompleted();
               }
            }

            @Override
            public void consumeContent(ContentDecoder contentDecoder, IOControl control) {
               if (responseConsumerFactory == null) {
                  control.suspendInput();
                  ioControl = control;
               } else {
                  if (consumer == null) {
                     consumer = responseConsumerFactory.apply(new ContentDecoderChannel(contentDecoder));
                  }
                  consumer.onReady();
               }
            }
         };
      }
   }
}


