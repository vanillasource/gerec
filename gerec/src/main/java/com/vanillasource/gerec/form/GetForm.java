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

import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpRequest;
import java.net.URI;
import java.util.List;
import java.util.Base64;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;

/**
 * A form that adds all parameters to the URI, just like a HTML Form with GET method.
 */
public final class GetForm implements Form {
   private final URI target;
   private final Function<URI, ResourceReference> referenceResolver;
   private final FormParameters parameters;

   public GetForm(URI target, Function<URI, ResourceReference> referenceResolver) {
      this(target, referenceResolver, new FormParameters());
   }

   private GetForm(URI target, Function<URI, ResourceReference> referenceResolver, FormParameters parameters) {
      this.target = target;
      this.referenceResolver = referenceResolver;
      this.parameters = parameters;
   }

   @Override
   public GetForm put(String key, String value) {
      return new GetForm(target, referenceResolver, parameters.put(key, value));
   }

   @Override
   public Form putInt(String key, int value) {
      return new GetForm(target, referenceResolver, parameters.put(key, ""+value));
   }

   @Override
   public Form putLong(String key, long value) {
      return new GetForm(target, referenceResolver, parameters.put(key, ""+value));
   }

   @Override
   public Form putBytes(String key, byte[] value) {
      return new GetForm(target, referenceResolver, parameters.put(key, Base64.getEncoder().encodeToString(value)));
   }

   @Override
   public Form putBytes(String key, List<byte[]> values) {
      return new GetForm(target, referenceResolver,
            values.stream().reduce(parameters,
               (p, v) -> p.put(key, Base64.getEncoder().encodeToString(v)),
               FormParameters::merge));
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> submitResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return referenceResolver
         .apply(resolveTarget(parameters.aggregate()))
         .getResponse(acceptType, change);
   }

   @Override
   public byte[] suspend(AcceptMediaType<?> acceptType, HttpRequest.HttpRequestChange change) {
      return referenceResolver
         .apply(resolveTarget(parameters.aggregate()))
         .suspend(ref -> ref.get(acceptType, change));
   }

   private URI resolveTarget(String data) {
      String parameterConcatenator = "?";
      if (target.getQuery() != null) {
         parameterConcatenator = "&";
      }
      return URI.create(target.toString()+parameterConcatenator+data);
   }
}

