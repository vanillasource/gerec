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

import com.vanillasource.gerec.*;
import java.util.function.Supplier;

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
      Hypermedia<T> media = types.deserialize(response);
      return new Response<T>() {
         @Override
         public HttpStatusCode getStatusCode() {
            return response.getStatusCode();
         }

         @Override
         public Condition ifMatch() {
            return response.ifMatch();
         }

         @Override
         public Condition ifNoneMatch() {
            return response.ifNoneMatch();
         }

         @Override
         public Condition ifModifiedSince() {
            return response.ifModifiedSince();
         }

         @Override
         public Condition ifUnmodifiedSince() {
            return response.ifUnmodifiedSince();
         }

         @Override
         public boolean hasLocation() {
            return response.hasLocation();
         }

         @Override
         public ResourceReference followLocation() {
            return response.followLocation();
         }

         @Override
         public boolean hasLink(String relationName) {
            return media.hasLink(relationName);
         }

         @Override
         public ResourceReference follow(String relationName) {
            return media.follow(relationName);
         }

         @Override
         public T getContent() {
            return media.getContent();
         }
      };
   }

   protected abstract HttpResponse get(HttpRequest.HttpRequestChange change);
}

