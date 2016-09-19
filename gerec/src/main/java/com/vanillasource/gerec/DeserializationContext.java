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

package com.vanillasource.gerec;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DeserializationContext {
   /**
    * Resolve the given uri into a resource reference.
    */
   ResourceReference resolve(URI uri);

   /**
    * Process an object after it was instantiated from the representation.
    */
   void postProcess(Object object);

   /**
    * Generate a completely new context based on resolving only.
    */
   static DeserializationContext fromResolver(Function<URI, ResourceReference> resolver) {
      return new DeserializationContext() {
         @Override
         public ResourceReference resolve(URI uri) {
            return resolver.apply(uri);
         }
         
         @Override
         public void postProcess(Object object) {
         }
      };
   }

   /**
    * Generate a new context based on this one with the processing of the given type
    * added.
    */
   default <T> DeserializationContext addPostProcessing(Class<T> type, Consumer<T> postProcessor) {
      DeserializationContext parent = this;
      return new DeserializationContext() {
         @Override
         public ResourceReference resolve(URI uri) {
            return parent.resolve(uri);
         }

         @Override
         @SuppressWarnings("unchecked")
         public void postProcess(Object object) {
            parent.postProcess(object);
            if (object.getClass().equals(type)) {
               postProcessor.accept((T) object);
            }
         }
      };
   }
}

