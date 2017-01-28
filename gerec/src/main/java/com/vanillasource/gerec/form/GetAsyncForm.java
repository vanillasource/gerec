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

package com.vanillasource.gerec.form;

import com.vanillasource.gerec.AsyncResourceReference;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.AcceptMediaType;
import java.net.URI;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;

/**
 * A form that adds all parameters to the URI, just like a HTML Form with GET method.
 */
public final class GetAsyncForm implements AsyncForm {
   private final URI target;
   private final Function<URI, AsyncResourceReference> referenceResolver;
   private final FormParameters parameters = new FormParameters();

   public GetAsyncForm(URI target, Function<URI, AsyncResourceReference> referenceResolver) {
      this.target = target;
      this.referenceResolver = referenceResolver;
   }

   @Override
   public void put(String key, String value) {
      parameters.put(key, value);
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> submit(AcceptMediaType<T> acceptType) {
      return referenceResolver
         .apply(resolveTarget(parameters.aggregate()))
         .get(acceptType);
   }

   private URI resolveTarget(String data) {
      String parameterConcatenator = "?";
      if (target.getQuery() != null) {
         parameterConcatenator = "&";
      }
      return URI.create(target.toString()+parameterConcatenator+data);
   }
}

