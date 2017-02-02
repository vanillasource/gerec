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

import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.gerec.DeserializationContext;
import java.util.concurrent.CompletableFuture;
import java.util.List;

/**
 * A collection of media-types that all produce the same java type. May be used in cases where
 * the same object may have different representations, such as XML and JSON representations at the same
 * time, or different versions of the same representation mapping to the same java class.
 * The content type is the first media type given.
 */
public class SameTypesMediaType<T> implements MediaType<T> {
   private PolymorphicAcceptType<T> acceptType;
   private ContentMediaType<T> contentType;

   public SameTypesMediaType(List<MediaType<T>> mediaTypes) {
      if (mediaTypes.isEmpty()) {
         throw new IllegalArgumentException("can not construct with no actual media types");
      }
      this.acceptType = new PolymorphicAcceptType<>(mediaTypes);
      this.contentType = mediaTypes.get(0);
   }

   @Override
   public void applyAsContent(HttpRequest request) {
      contentType.applyAsContent(request);
   }

   @Override
   public void serialize(T object, HttpRequest request) {
      contentType.serialize(object, request);
   }

   @Override
   public void applyAsOption(HttpRequest request) {
      acceptType.applyAsOption(request);
   }

   @Override
   public boolean isHandling(HttpResponse response) {
      return acceptType.isHandling(response);
   }

   @Override
   public CompletableFuture<T> deserialize(HttpResponse response, DeserializationContext context) {
      return acceptType.deserialize(response, context);
   }
}

