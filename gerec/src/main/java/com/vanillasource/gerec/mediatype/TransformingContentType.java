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

import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.gerec.HttpRequest;
import java.util.function.Function;

/**
 * Content media type that transforms from another content media type.
 */
public class TransformingContentType<T, R> implements ContentMediaType<T> {
   private final ContentMediaType<R> delegate;
   private final Function<T, R> transform;

   public TransformingContentType(ContentMediaType<R> delegate, Function<T, R> transform) {
      this.delegate = delegate;
      this.transform = transform;
   }

   @Override
   public void applyAsContent(HttpRequest request) {
      delegate.applyAsContent(request);
   }

   @Override
   public void serialize(T object, HttpRequest request) {
      delegate.serialize(transform.apply(object), request);
   }
}



