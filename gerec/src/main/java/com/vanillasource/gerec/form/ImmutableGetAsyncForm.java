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
import com.vanillasource.gerec.HttpRequest;
import java.net.URI;
import java.util.List;
import java.util.Base64;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;

/**
 * An immutable form submitted with GET.
 */
public final class ImmutableGetAsyncForm implements AsyncForm {
   private final AsyncResourceReference reference;

   public ImmutableGetAsyncForm(AsyncResourceReference reference) {
      this.reference = reference;
   }

   @Override
   public GetAsyncForm put(String key, String value) {
      throw new UnsupportedOperationException("can not modify immutable form");
   }

   @Override
   public AsyncForm putInt(String key, int value) {
      throw new UnsupportedOperationException("can not modify immutable form");
   }

   @Override
   public AsyncForm putLong(String key, long value) {
      throw new UnsupportedOperationException("can not modify immutable form");
   }

   @Override
   public AsyncForm putBytes(String key, byte[] value) {
      throw new UnsupportedOperationException("can not modify immutable form");
   }

   @Override
   public AsyncForm putBytes(String key, List<byte[]> values) {
      throw new UnsupportedOperationException("can not modify immutable form");
   }

   @Override
   public String serialize() {
      return "GET\n" + reference.serialize();
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> submit(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return reference
         .get(acceptType, change);
   }
}

