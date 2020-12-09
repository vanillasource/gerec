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
import com.vanillasource.gerec.ExceptionTransparentCall;
import com.vanillasource.gerec.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.List;

/**
 * A generic form that can be filled out with string
 * keys and values and submitted.
 * Note: forms are immutable, always work with returned Form.
 */
public interface AsyncForm {
   AsyncForm put(String key, String value);

   AsyncForm putInt(String key, int value);

   AsyncForm putLong(String key, long value);

   AsyncForm putBytes(String key, byte[] value);

   AsyncForm putBytes(String key, List<byte[]> values);

   <T> CompletableFuture<ContentResponse<T>> submitResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> CompletableFuture<ContentResponse<T>> submitResponse(AcceptMediaType<T> acceptType) {
      return submitResponse(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <T> CompletableFuture<T> submit(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return submitResponse(acceptType, change)
         .thenApply(ContentResponse::getContent);
   }

   default <T> CompletableFuture<T> submit(AcceptMediaType<T> acceptType) {
      return submit(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default byte[] suspend(AcceptMediaType<?> acceptType) {
      return suspend(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   byte[] suspend(AcceptMediaType<?> acceptType, HttpRequest.HttpRequestChange change);

   default Form sync() {
      return new Form() {
         @Override
         public Form put(String key, String value) {
            return AsyncForm.this.put(key, value).sync();
         }

         @Override
         public Form putInt(String key, int value) {
            return AsyncForm.this.putInt(key, value).sync();
         }

         @Override
         public Form putLong(String key, long value) {
            return AsyncForm.this.putLong(key, value).sync();
         }

         @Override
         public Form putBytes(String key, byte[] value) {
            return AsyncForm.this.putBytes(key, value).sync();
         }

         @Override
         public <T> ContentResponse<T> submitResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
            return new ExceptionTransparentCall<>(AsyncForm.this.submitResponse(acceptType, change)).get();
         }

         @Override
         public AsyncForm async() {
            return AsyncForm.this;
         }
      };
   }
}


