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

import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.GerecException;
import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.ContentMediaType;
import java.util.List;
import java.util.function.Function;
import java.net.URI;

/**
 * A collection of media-types that all produce the same java type. The content type is the first media type given.
 */
public class SameTypeAlternatives<T> extends PolymorphicTypes<T> implements MediaType<T> {
   private ContentMediaType<T> contentType;

   public SameTypeAlternatives(List<MediaType<T>> mediaTypes) {
      super(mediaTypes);
      if (mediaTypes.isEmpty()) {
         throw new IllegalArgumentException("can not construct with no actual media types");
      }
      contentType = mediaTypes.get(0);
   }

   @Override
   public void applyAsContent(HttpRequest request) {
      contentType.applyAsContent(request);
   }

   @Override
   public void serialize(T object, HttpRequest request) {
      contentType.serialize(object, request);
   }
}

