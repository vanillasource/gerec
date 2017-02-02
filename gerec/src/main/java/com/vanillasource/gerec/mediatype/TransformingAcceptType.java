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

import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.DeserializationContext;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Accept media type that transforms to another accept media type.
 */
public class TransformingAcceptType<T, R> implements AcceptMediaType<T> {
   private final AcceptMediaType<R> delegate;
   private final Function<R, T> transform;

   public TransformingAcceptType(AcceptMediaType<R> delegate, Function<R, T> transform) {
      this.delegate = delegate;
      this.transform = transform;
   }

   @Override
   public void applyAsOption(HttpRequest request) {
      delegate.applyAsOption(request);
   }

   @Override
   public boolean isHandling(HttpResponse response) {
      return delegate.isHandling(response);
   }

   @Override
   public CompletableFuture<T> deserialize(HttpResponse response, DeserializationContext context) {
      return delegate.deserialize(response, context)
         .thenApply(transform);
   }
}



