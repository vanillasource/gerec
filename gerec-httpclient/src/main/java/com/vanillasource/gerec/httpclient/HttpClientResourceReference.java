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

import com.vanillasource.gerec.GerecException;
import com.vanillasource.gerec.*;
import com.vanillasource.gerec.reference.MediaTypeAwareResourceReference;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.net.URI;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class HttpClientResourceReference extends MediaTypeAwareResourceReference {
   private final Supplier<HttpClient> httpClientSupplier;
   private final URI resourceUri;

   public HttpClientResourceReference(Supplier<HttpClient> httpClientSupplier, URI resourceUri) {
      this.httpClientSupplier = httpClientSupplier;
      this.resourceUri = resourceUri;
   }

   @Override
   public URI toURI() {
      return resourceUri;
   }

   @Override
   protected ResourceReference follow(URI link) {
      return new HttpClientResourceReference(httpClientSupplier, resourceUri.resolve(link));
   }

   @Override
   protected HttpResponse get(HttpRequest.HttpRequestChange change) {
      return execute(new HttpGet(resourceUri), change);
   }

   @Override
   protected HttpResponse post(HttpRequest.HttpRequestChange change) {
      return execute(new HttpPost(resourceUri), change);
   }

   @Override
   protected HttpResponse put(HttpRequest.HttpRequestChange change) {
      return execute(new HttpPut(resourceUri), change);
   }

   @Override
   protected HttpResponse delete(HttpRequest.HttpRequestChange change) {
      return execute(new HttpDelete(resourceUri), change);
   }

   private HttpResponse execute(HttpEntityEnclosingRequestBase request, HttpRequest.HttpRequestChange change) {
      change.applyTo(new HttpRequest() {
         @Override
         public boolean hasHeader(Header header) {
            return request.containsHeader(header.value());
         }

         @Override
         public String getHeader(Header header) {
            return request.getFirstHeader(header.value()).getValue();
         }

         @Override
         public void setHeader(Header header, String value) {
            request.setHeader(header.value(), value);
         }

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
      change.applyTo(new HttpRequest() {
         @Override
         public boolean hasHeader(Header header) {
            return request.containsHeader(header.value());
         }

         @Override
         public String getHeader(Header header) {
            return request.getFirstHeader(header.value()).getValue();
         }

         @Override
         public void setHeader(Header header, String value) {
            request.setHeader(header.value(), value);
         }

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
         HttpResponse response = new HttpResponse() {
            @Override
            public HttpStatusCode getStatusCode() {
               return HttpStatusCode.valueOf(httpResponse.getStatusLine().getStatusCode());
            }

            @Override
            public boolean hasHeader(Header header) {
               return httpResponse.containsHeader(header.value());
            }

            @Override
            public String getHeader(Header header) {
               return httpResponse.getFirstHeader(header.value()).getValue();
            }

            @Override
            public <T> T processContent(Function<InputStream, T> contentProcessor) {
               try {
                  try (InputStream input = httpResponse.getEntity().getContent()) {
                     return contentProcessor.apply(input);
                  }
               } catch (IOException e) {
                  throw new GerecException("exception while reading http response", e);
               }
            }
         };
         if (response.getStatusCode().isError()) {
            throw new HttpGerecException("received status code: "+response.getStatusCode(), response);
         }
         return response;
      } catch (IOException e) {
         throw new GerecException("error while making http call", e);
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


