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

package com.vanillasource.gerec.mediatype;

import com.vanillasource.gerec.http.HttpRequest;
import com.vanillasource.gerec.http.HttpResponse;
import com.vanillasource.gerec.http.HttpStatusCode;
import com.vanillasource.gerec.http.SingleHeaderValue;
import com.vanillasource.gerec.http.Header;
import com.vanillasource.gerec.resource.ResourceReference;
import com.vanillasource.gerec.resource.Response;
import java.util.function.Supplier;
import java.net.URI;

public abstract class MediaTypeAwareResourceReference implements ResourceReference {
   private Supplier<MediaTypeCatalog> catalogSupplier;

   /**
    * @param catalogSupplier Object that can supply a catalog. It does not directly depend
    * on a catalog to be able to decouple it from serialization. The supplier has to be
    * serializable.
    */
   public MediaTypeAwareResourceReference(Supplier<MediaTypeCatalog> catalogSupplier) {
      this.catalogSupplier = catalogSupplier;
   }

   protected Supplier<MediaTypeCatalog> getCatalogSupplier() {
      return catalogSupplier;
   }

   @Override
   public <T> Response<T> get(Class<T> type, HttpRequest.HttpRequestChange change) {
      MediaTypes<T> types = catalogSupplier.get().getMediaTypesFor(type);
      HttpResponse response = get(change.and(types));
      T media = types.deserialize(response);
      return new Response<T>() {
         @Override
         public HttpStatusCode getStatusCode() {
            return response.getStatusCode();
         }

         @Override
         public boolean hasIdentity() {
            return response.hasHeader(Header.ETAG);
         }

         @Override
         public HttpRequest.HttpRequestChange ifMatch() {
            return new SingleHeaderValue(Header.IF_MATCH, response.getHeader(Header.ETAG));
         }

         @Override
         public HttpRequest.HttpRequestChange ifNotMatch() {
            return new SingleHeaderValue(Header.IF_NONE_MATCH, response.getHeader(Header.ETAG));
         }

         @Override
         public HttpRequest.HttpRequestChange ifModifiedSince() {
            return new SingleHeaderValue(Header.IF_MODIFIED_SINCE, response.getHeader(Header.DATE));
         }

         @Override
         public HttpRequest.HttpRequestChange ifUnmodifiedSince() {
            return new SingleHeaderValue(Header.IF_UNMODIFIED_SINCE, response.getHeader(Header.DATE));
         }

         @Override
         public boolean hasLastModified() {
            return response.hasHeader(Header.LAST_MODIFIED);
         }

         @Override
         public HttpRequest.HttpRequestChange ifModifiedSinceLastModified() {
            return new SingleHeaderValue(Header.IF_MODIFIED_SINCE, response.getHeader(Header.LAST_MODIFIED));
         }

         @Override
         public HttpRequest.HttpRequestChange ifUnmodifiedSinceLastModified() {
            return new SingleHeaderValue(Header.IF_UNMODIFIED_SINCE, response.getHeader(Header.LAST_MODIFIED));
         }

         @Override
         public boolean hasLocation() {
            return response.hasHeader(Header.LOCATION);
         }

         @Override
         public ResourceReference followLocation() {
            return follow(URI.create(response.getHeader(Header.LOCATION)));
         }

         @Override
         public T getContent() {
            return media;
         }
      };
   }

   protected abstract ResourceReference follow(URI link);

   protected abstract HttpResponse get(HttpRequest.HttpRequestChange change);
}

