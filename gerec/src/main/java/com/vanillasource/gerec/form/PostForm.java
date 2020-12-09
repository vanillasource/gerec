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
import com.vanillasource.gerec.mediatype.MediaTypes;
import com.vanillasource.gerec.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.Base64;
import java.util.List;

/**
 * A form that posts all parameters as form encoded, just like a HTML Form with POST method.
 */
public final class PostForm implements Form {
   private final ResourceReference target;
   private final FormParameters parameters;

   public PostForm(ResourceReference target) {
      this(target, new FormParameters());
   }

   private PostForm(ResourceReference target, FormParameters parameters) {
      this.target = target;
      this.parameters = parameters;
   }

   @Override
   public PostForm put(String key, String value) {
      return new PostForm(target, parameters.put(key, value));
   }

   @Override
   public Form putInt(String key, int value) {
      return new PostForm(target, parameters.put(key, ""+value));
   }

   @Override
   public Form putLong(String key, long value) {
      return new PostForm(target, parameters.put(key, ""+value));
   }

   @Override
   public Form putBytes(String key, byte[] value) {
      return new PostForm(target, parameters.put(key, Base64.getEncoder().encodeToString(value)));
   }

   @Override
   public Form putBytes(String key, List<byte[]> values) {
      return new PostForm(target,
            values.stream().reduce(parameters,
               (p, v) -> p.put(key, Base64.getEncoder().encodeToString(v)),
               FormParameters::merge));
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> submitResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return target.postResponse(MediaTypes.formUrlEncoded(), parameters.aggregate(), acceptType, change);
   }

   @Override
   public byte[] suspend(AcceptMediaType<?> acceptType, HttpRequest.HttpRequestChange change) {
      return target.suspend(ref -> ref.post(MediaTypes.formUrlEncoded(), parameters.aggregate(), acceptType, change));
   }

}


