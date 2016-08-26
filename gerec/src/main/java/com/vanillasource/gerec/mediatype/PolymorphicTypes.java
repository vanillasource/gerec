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
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.GerecException;
import java.util.List;
import java.net.URI;
import java.util.function.Function;

/**
 * An accept-type that can await several media types that are mapping to a 
 * class hierarchy, returning the base class. This means this type can actually
 * map representations into an inheritance hierarchy. 
 */
public class PolymorphicTypes<T> implements AcceptMediaType<T> {
   private List<? extends AcceptMediaType<? extends T>> acceptTypes;

   public PolymorphicTypes(List<? extends AcceptMediaType<? extends T>> acceptTypes) {
      this.acceptTypes = acceptTypes;
   }

   @Override
   public void applyAsOption(HttpRequest request) {
      acceptTypes.forEach(acceptType -> acceptType.applyAsOption(request));
   }

   @Override
   public boolean isHandling(HttpResponse response) {
      return acceptTypes.stream().anyMatch(acceptType -> acceptType.isHandling(response));
   }

   @Override
   public T deserialize(HttpResponse response, Function<URI, ResourceReference> referenceProducer) {
      return acceptTypes.stream()
         .filter(acceptType -> acceptType.isHandling(response))
         .findFirst()
         .orElseThrow(() -> new GerecException("no matching media types found for "+response+", possible media types were: "+acceptTypes))
         .deserialize(response, referenceProducer);
   }
}

