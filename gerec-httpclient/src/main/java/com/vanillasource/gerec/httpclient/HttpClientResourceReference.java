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
import com.vanillasource.gerec.mediatype.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.Header;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.net.URI;
import java.io.IOException;
import java.io.InputStream;

public final class HttpClientResourceReference extends MediaTypeAwareResourceReference {
   private Supplier<HttpClient> httpClientSupplier;
   private URI resourceUri;

   public HttpClientResourceReference(Supplier<MediaTypeCatalog> catalogSupplier, Supplier<HttpClient> httpClientSupplier, URI resourceUri) {
      super(catalogSupplier);
      this.httpClientSupplier = httpClientSupplier;
      this.resourceUri = resourceUri;
   }

   @Override
   protected HttpResponse get(HttpRequest.HttpRequestChange change) {
      return execute(new HttpGet(resourceUri), change);
   }

   private HttpResponse execute(HttpRequestBase request, HttpRequest.HttpRequestChange change) {
      change.applyTo(new HttpRequest() {
      });
      try {
         org.apache.http.HttpResponse httpResponse = httpClientSupplier.get().execute(request);
         HttpResponse response = new HttpResponse() {
            @Override
            public HttpStatusCode getStatusCode() {
               return HttpStatusCode.valueOf(httpResponse.getStatusLine().getStatusCode());
            }

            @Override
            public Condition ifMatch() {
               return Condition.TRUE; // TODO
            }

            @Override
            public Condition ifNoneMatch() {
               return Condition.TRUE; // TODO
            }

            @Override
            public Condition ifModifiedSince() {
               return Condition.TRUE; // TODO
            }

            @Override
            public Condition ifUnmodifiedSince() {
               return Condition.TRUE; // TODO
            }

            @Override
            public boolean hasLocation() {
               return httpResponse.containsHeader("Location");
            }

            @Override
            public ResourceReference followLocation() {
               Header locationHeader = httpResponse.getFirstHeader("Location");
               if (locationHeader == null) {
                  throw new GerecException("there was no 'Location' header present, can not follow");
               }
               return new HttpClientResourceReference(getCatalogSupplier(), httpClientSupplier, resourceUri.resolve(URI.create(locationHeader.getValue())));
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


