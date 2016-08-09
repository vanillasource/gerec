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

   <T> Response<T> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> Response<T> get(AcceptMediaType<T> acceptType) {
      return get(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R, T> Response<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> Response<T> post(MediaType<T> type, T content) {
      return post(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> Response<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return post(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   // TODO: different standard types (list of Ts, other methods: POST, DELETE, etc.)
}
