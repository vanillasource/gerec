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
import com.vanillasource.gerec.Request;
import java.util.concurrent.CompletableFuture;
import java.util.List;

/**
 * A generic form that can be filled out with string
 * keys and values and submitted.
 * Note: forms are immutable, always work with returned Form.
 */
public interface Form {
   Form put(String key, String value);

   Form putInt(String key, int value);

   Form putLong(String key, long value);

   Form putBytes(String key, byte[] value);

   Form putBytes(String key, List<byte[]> values);

   Request prepareSubmit(HttpRequest.HttpRequestChange change);

   default Request prepareSubmit() {
      return prepareSubmit(HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <T> CompletableFuture<ContentResponse<T>> submitResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return prepareSubmit(change).send(acceptType);
   }

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

}

