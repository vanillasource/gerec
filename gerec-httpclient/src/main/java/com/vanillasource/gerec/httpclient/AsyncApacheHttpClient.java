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

import com.vanillasource.aio.channel.WritableByteChannelMaster;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.ReadableByteChannelMaster;
import com.vanillasource.gerec.reference.AsyncHttpClient;
import com.vanillasource.gerec.*;
import org.apache.http.client.methods.*;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.entity.BasicHttpEntity;
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
import java.io.UncheckedIOException;
import java.nio.channels.ReadableByteChannel;
import org.apache.http.nio.ContentDecoderChannel;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AsyncApacheHttpClient implements AsyncHttpClient {
   private static final Logger logger = LoggerFactory.getLogger(AsyncApacheHttpClient.class);
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

   private CompletableFuture<HttpResponse> execute(HttpRequestBase httpRequest, HttpRequest.HttpRequestChange change) {
      AsyncHttpRequest request = new AsyncHttpRequest(httpRequest);
      change.applyTo(request);
      return execute(request);
   }

   private CompletableFuture<HttpResponse> execute(AsyncHttpRequest request) {
      CompletableFuture<HttpResponse> result = new CompletableFuture<>();
      logger.debug("making the async http request "+request);
      httpClientSupplier.get().execute(
            request.getProducer(result),
            AsyncHttpResponse.getConsumer(request, result),
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
      private Function<WritableByteChannelMaster, AioSlave<Void>> followerFactory;
      private HttpRequestBase request;

      private AsyncHttpRequest(HttpRequestBase request) {
         super(request);
         this.request = request;
      }

      @Override
      public String toString() {
         return request.toString();
      }

      public HttpAsyncRequestProducer getProducer(CompletableFuture<HttpResponse> responseFuture) {
         return new HttpAsyncRequestProducer() {
            private AioSlave<Void> follower;

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
               if (follower == null) {
                  follower = followerFactory.apply(new WritableByteChannelMaster() {
                     @Override
                     public void pause() {
                        logger.debug("suspending output during request submission");
                        control.suspendOutput();
                     }

                     @Override
                     public void resume() {
                        logger.debug("resuming output during request submission");
                        control.requestOutput();
                     }

                     @Override
                     public int write(ByteBuffer buffer) throws IOException {
                        int length = contentEncoder.write(buffer);
                        if (logger.isDebugEnabled()) {
                           logger.debug("written "+length+" bytes of request");
                        }
                        return length;
                     }

                     @Override
                     public void close() {
                        logger.debug("completing request "+request);
                        try {
                           contentEncoder.complete();
                        } catch (IOException e) {
                           throw new UncheckedIOException(e);
                        }
                     }

                     @Override
                     public boolean isOpen() {
                        return true;
                     }
                  });
               }
               follower.onReady();
            }

            @Override
            public void requestCompleted(HttpContext context) {
               logger.debug("request "+request+" completed");
               if (follower != null) {
                  follower.onCompleted();
               }
            }

            @Override
            public void resetRequest() {
            }
         };
      }

      @Override
      public void setByteProducer(Function<WritableByteChannelMaster, AioSlave<Void>> followerFactory) {
         this.followerFactory = followerFactory;
         if (request instanceof HttpEntityEnclosingRequest) {
            ((HttpEntityEnclosingRequest)request).setEntity(new BasicHttpEntity());
         }
      }

      @Override
      public void setByteProducer(Function<WritableByteChannelMaster, AioSlave<Void>> followerFactory, long length) {
         this.followerFactory = followerFactory;
         if (request instanceof HttpEntityEnclosingRequest) {
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContentLength(length);
            ((HttpEntityEnclosingRequest)request).setEntity(entity);
         }
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

      public static HttpAsyncResponseConsumer<Void> getConsumer(AsyncHttpRequest request, CompletableFuture<HttpResponse> responseFuture) {
         return new HttpAsyncResponseConsumer<Void>() {
            private boolean done = false;
            private Exception e;
            private volatile Function<ReadableByteChannelMaster, AioSlave<Object>> followerFactory;
            private AioSlave<Object> follower;
            private volatile ReadableByteChannelMaster master;
            private CompletableFuture<Object> result;

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
               } else if (result != null) {
                  result.completeExceptionally(e);
               } else {
                  throw new IllegalStateException("something's wrong, there was no consumer, but there was an error with request "+request, e);
               }
            }

            @Override
            public void responseReceived(org.apache.http.HttpResponse response) {
               responseFuture.complete(new AsyncHttpResponse(response) {
                  @Override
                  @SuppressWarnings("unchecked")
                  public <R> CompletableFuture<R> consumeContent(Function<ReadableByteChannelMaster, AioSlave<R>> consumerFactory) {
                     // Note: this method will be called synchronously with this completion, therefore thread-safe
                     if (followerFactory != null) {
                        throw new IllegalStateException("can only consume response once");
                     }
                     followerFactory = (Function<ReadableByteChannelMaster, AioSlave<Object>>)(Object) consumerFactory;
                     result = new CompletableFuture<>();
                     logger.debug("client will consume content");
                     if (master != null) {
                        initializeFollower();
                        master.resume();
                     }
                     return (CompletableFuture<R>) result;
                  }
               });
            }

            private void initializeFollower() {
               if (follower == null) {
                  follower = followerFactory.apply(master);
               }
               follower.onReady();
            }

            @Override
            public void responseCompleted(HttpContext context) {
               logger.debug("response completed");
               this.done = true;
               if (follower == null) {
                  follower = followerFactory.apply(ReadableByteChannelMaster.NULL);
               }
               result.complete(follower.onCompleted());
            }

            @Override
            public void consumeContent(ContentDecoder contentDecoder, IOControl control) {
               logger.debug("consuming content");
               if (master == null) {
                  ReadableByteChannel delegate = new ContentDecoderChannel(contentDecoder);
                  master = new ReadableByteChannelMaster() {
                     @Override
                     public void pause() {
                        control.suspendInput();
                     }

                     @Override
                     public void resume() {
                        control.requestInput();
                     }

                     @Override
                     public int read(ByteBuffer buffer) throws IOException {
                        return delegate.read(buffer);
                     }

                     @Override
                     public void close() {
                        try {
                           delegate.close();
                        } catch (IOException e) {
                           throw new UncheckedIOException(e);
                        }
                     }

                     @Override
                     public boolean isOpen() {
                        return delegate.isOpen();
                     }
                  };
               }
               if (followerFactory == null) {
                  master.pause();
               } else {
                  initializeFollower();
               }
            }
         };
      }
   }
}


