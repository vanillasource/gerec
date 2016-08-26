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
 * References to resources are the main method by which users of this library should interact
 * with RESTful HTTP resources. All actions to the resources should go through classes from this
 * interface. All implementations should be serializable, without serializing any underlying
 * infrastructure, like caches.
 */
public interface ResourceReference extends Serializable {
   URI toURI();

   Response head(HttpRequest.HttpRequestChange change);

   default Response head() {
      return head(HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <T> ContentResponse<T> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> ContentResponse<T> get(AcceptMediaType<T> acceptType) {
      return get(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R, T> ContentResponse<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> ContentResponse<T> post(MediaType<T> type, T content) {
      return post(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> ContentResponse<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return post(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R, T> ContentResponse<T> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> ContentResponse<T> put(MediaType<T> type, T content) {
      return put(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> ContentResponse<T> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return put(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <T> ContentResponse<T> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> ContentResponse<T> delete(AcceptMediaType<T> acceptType) {
      return delete(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default Response delete() {
      return delete(MediaType.NONE);
   }

   <R, T> ContentResponse<T> options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType);
   
   default <T> ContentResponse<T> options(MediaType<T> type, T content) {
      return options(type, content, type);
   }

   default Response options() {
      return options(MediaType.NONE, null);
   }
}

