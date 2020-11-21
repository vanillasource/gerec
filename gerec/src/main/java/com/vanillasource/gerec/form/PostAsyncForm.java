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
import com.vanillasource.gerec.mediatype.MediaTypes;
import com.vanillasource.gerec.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.Base64;
import java.util.List;

/**
 * A form that posts all parameters as form encoded, just like a HTML Form with POST method.
 */
public final class PostAsyncForm implements AsyncForm {
   private final AsyncResourceReference target;
   private final FormParameters parameters;

   public PostAsyncForm(AsyncResourceReference target) {
      this(target, new FormParameters());
   }

   private PostAsyncForm(AsyncResourceReference target, FormParameters parameters) {
      this.target = target;
      this.parameters = parameters;
   }

   @Override
   public PostAsyncForm put(String key, String value) {
      return new PostAsyncForm(target, parameters.put(key, value));
   }

   @Override
   public AsyncForm putInt(String key, int value) {
      return new PostAsyncForm(target, parameters.put(key, ""+value));
   }

   @Override
   public AsyncForm putLong(String key, long value) {
      return new PostAsyncForm(target, parameters.put(key, ""+value));
   }

   @Override
   public AsyncForm putBytes(String key, byte[] value) {
      return new PostAsyncForm(target, parameters.put(key, Base64.getEncoder().encodeToString(value)));
   }

   @Override
   public AsyncForm putBytes(String key, List<byte[]> values) {
      return new PostAsyncForm(target,
            values.stream().reduce(parameters,
               (p, v) -> p.put(key, Base64.getEncoder().encodeToString(v)),
               FormParameters::merge));
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> submit(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return target.post(MediaTypes.formUrlEncoded(), parameters.aggregate(), acceptType, change);
   }

   @Override
   public byte[] suspend(AcceptMediaType<?> acceptType, HttpRequest.HttpRequestChange change) {
      return target.suspend(ref -> ref.post(MediaTypes.formUrlEncoded(), parameters.aggregate(), acceptType, change));
   }

}


