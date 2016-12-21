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

import com.vanillasource.gerec.*;
import com.vanillasource.gerec.reference.MediaTypeAwareResourceReference;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.entity.InputStreamEntity;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public final class HttpClientResourceReference extends MediaTypeAwareResourceReference {
   private final Supplier<HttpClient> httpClientSupplier;
   private final URI resourceUri;

   public HttpClientResourceReference(Supplier<HttpClient> httpClientSupplier, URI resourceUri) {
      this.httpClientSupplier = httpClientSupplier;
      this.resourceUri = resourceUri;
   }

   @Override
   protected ResourceReference follow(URI link) {
      return new HttpClientResourceReference(httpClientSupplier, resourceUri.resolve(link));
   }

   @Override
   protected HttpResponse doHead(HttpRequest.HttpRequestChange change) {
      return execute(new HttpHead(resourceUri), change);
   }

   @Override
   protected HttpResponse doGet(HttpRequest.HttpRequestChange change) {
      return execute(new HttpGet(resourceUri), change);
   }

   @Override
   protected HttpResponse doPost(HttpRequest.HttpRequestChange change) {
      return execute(new HttpPost(resourceUri), change);
   }

   @Override
   protected HttpResponse doPut(HttpRequest.HttpRequestChange change) {
      return execute(new HttpPut(resourceUri), change);
   }

   @Override
   protected HttpResponse doDelete(HttpRequest.HttpRequestChange change) {
      return execute(new HttpDelete(resourceUri), change);
   }

   @Override
   protected HttpResponse doOptions(HttpRequest.HttpRequestChange change) {
      return execute(new HttpOptions(resourceUri), change);
   }

   private HttpResponse execute(HttpEntityEnclosingRequestBase request, HttpRequest.HttpRequestChange change) {
      change.applyTo(new HeaderAwareHttpRequest(request) {
         @Override
         public void setContent(Supplier<InputStream> contentSupplier, long length) {
            request.setEntity(new SuppliedInputStreamHttpEntity(contentSupplier, length));
         }

         @Override
         public void setContent(Supplier<InputStream> contentSupplier) {
            request.setEntity(new SuppliedInputStreamHttpEntity(contentSupplier));
         }
      });
      return execute(request);
   }

   private HttpResponse execute(HttpRequestBase request, HttpRequest.HttpRequestChange change) {
      change.applyTo(new HeaderAwareHttpRequest(request) {
         @Override
         public void setContent(Supplier<InputStream> contentSupplier, long length) {
            throw new UnsupportedOperationException("can not modify outpustream for this operation");
         }

         @Override
         public void setContent(Supplier<InputStream> contentSupplier) {
            throw new UnsupportedOperationException("can not modify outpustream for this operation");
         }
      });
      return execute(request);
   }

   private HttpResponse execute(HttpRequestBase request) {
      try {
         org.apache.http.HttpResponse httpResponse = httpClientSupplier.get().execute(request);
         return new HeaderAwareHttpResponse(httpResponse) {
            @Override
            public HttpStatusCode getStatusCode() {
               return HttpStatusCode.valueOf(httpResponse.getStatusLine().getStatusCode());
            }

            @Override
            public <T> T processContent(Function<InputStream, T> contentProcessor) {
               try {
                  try (InputStream input = httpResponse.getEntity().getContent()) {
                     return contentProcessor.apply(input);
                  }
               } catch (IOException e) {
                  throw new UncheckedIOException(e);
               }
            }
         };
      } catch (IOException e) {
         throw new UncheckedIOException(e);
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

   private static abstract class HeaderAwareHttpRequest extends HeaderAwareMessage implements HttpRequest {
      private HeaderAwareHttpRequest(HttpMessage message) {
         super(message);
      }
   }

   private static abstract class HeaderAwareHttpResponse extends HeaderAwareMessage implements HttpResponse {
      private HeaderAwareHttpResponse(HttpMessage message) {
         super(message);
      }
   }

   private static class SuppliedInputStreamHttpEntity implements HttpEntity {
      private final Supplier<InputStream> inputStreamSupplier;
      private final long length;
      private InputStream inputStream;

      public SuppliedInputStreamHttpEntity(Supplier<InputStream> inputStreamSupplier, long length) {
         this.inputStreamSupplier = inputStreamSupplier;
         this.length = length;
      }

      public SuppliedInputStreamHttpEntity(Supplier<InputStream> inputStreamSupplier) {
         this(inputStreamSupplier, -1);
      }

      @Override
      @SuppressWarnings("deprecation")
      public void consumeContent() throws IOException {
         if (inputStream != null) {
            inputStream.close();
         }
      }

      @Override
      public InputStream getContent() {
         if (inputStream == null) {
            inputStream = inputStreamSupplier.get();
         }
         return inputStream;
      }

      @Override
      public org.apache.http.Header getContentEncoding() {
         return null;
      }

      @Override
      public long getContentLength() {
         return length;
      }

      @Override
      public org.apache.http.Header getContentType() {
         return null;
      }

      @Override
      public boolean isChunked() {
         return false;
      }

      @Override
      public boolean isRepeatable() {
         return false;
      }

      @Override
      public boolean isStreaming() {
         return true;
      }

      @Override
      public void writeTo(OutputStream outputStream) throws IOException {
         new InputStreamEntity(getContent(), getContentLength()).writeTo(outputStream);
      }
   }
}


