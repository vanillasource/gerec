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
import com.vanillasource.gerec.resource.*;
import com.vanillasource.gerec.http.*;
import com.vanillasource.gerec.mediatype.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.net.URI;
import java.io.IOException;
import java.io.InputStream;

public final class HttpClientResourceReference extends MediaTypeAwareResourceReference {
   private final Supplier<HttpClient> httpClientSupplier;
   private final URI resourceUri;

   public HttpClientResourceReference(Supplier<MediaTypeCatalog> catalogSupplier, Supplier<HttpClient> httpClientSupplier, URI resourceUri) {
      super(catalogSupplier);
      this.httpClientSupplier = httpClientSupplier;
      this.resourceUri = resourceUri;
   }

   @Override
   protected ResourceReference follow(URI link) {
      return new HttpClientResourceReference(getCatalogSupplier(), httpClientSupplier, resourceUri.resolve(link));
   }

   @Override
   protected HttpResponse get(HttpRequest.HttpRequestChange change) {
      return execute(new HttpGet(resourceUri), change);
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
      });
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
            public void processContent(Consumer<InputStream> contentProcessor) {
               try {
                  try (InputStream input = httpResponse.getEntity().getContent()) {
                     contentProcessor.accept(input);
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
}


