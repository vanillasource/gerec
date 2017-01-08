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

import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.AcceptMediaType;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * A form that puts data together into a string.
 */
public abstract class StringDataForm implements Form {
   private Map<String, String> values = new HashMap<>();

   @Override
   public void put(String key, String value) {
      values.put(key, value);
   }

   @Override
   public <T> ContentResponse<T> submit(AcceptMediaType<T> acceptType) {
      return submit(asString(), acceptType);
   }

   protected abstract <T> ContentResponse<T> submit(String data, AcceptMediaType<T> acceptType);

   @Override
   public <T> CompletableFuture<ContentResponse<T>> submitAsync(AcceptMediaType<T> acceptType) {
      return submitAsync(asString(), acceptType);
   }

   protected abstract <T> CompletableFuture<ContentResponse<T>> submitAsync(String data, AcceptMediaType<T> acceptType);

   private String asString() {
      StringBuilder builder = new StringBuilder();
      for (Map.Entry<String, String> parameter : values.entrySet()) {
         if (builder.length() > 0) {
            builder.append("&");
         }
         builder.append(parameter.getKey());
         builder.append("=");
         builder.append(parameter.getValue());
      }
      return builder.toString();
   }
}



