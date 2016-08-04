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

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.net.URI;

/**
 * References a remote HTTP resource which can be accessed by given methods.
 */
public interface ResourceReference extends Serializable {
   URI toURI();

   <T> Response<T> get(MediaType<T> type, HttpRequest.HttpRequestChange change);

   default <T> Response<T> get(MediaType<T> type) {
      return get(type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <T> CompletableFuture<Response<T>> getAsync(MediaType<T> type) {
      return CompletableFuture.supplyAsync(() -> get(type));
   }

   default <T> CompletableFuture<Response<T>> getAsync(MediaType<T> type, HttpRequest.HttpRequestChange change) {
      return CompletableFuture.supplyAsync(() -> get(type, change));
   }

   // TODO: different standard types (list of Ts, other methods: POST, DELETE, etc.)
}
