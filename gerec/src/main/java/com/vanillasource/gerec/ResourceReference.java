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

/**
 * References to resources are the main method by which users of this library should interact
 * with RESTful HTTP resources. All actions to the resources should go through classes from this
 * interface. All implementations should be serializable, without serializing any underlying
 * infrastructure, like caches.
 */
public interface ResourceReference extends Serializable {
   Response headResponse(HttpRequest.HttpRequestChange change);

   default Response headResponse() {
      return headResponse(HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <T> ContentResponse<T> getResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> ContentResponse<T> getResponse(AcceptMediaType<T> acceptType) {
      return getResponse(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <T> T get(AcceptMediaType<T> acceptType) {
      return getResponse(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE)
         .getContent();
   }

   default <T> T get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return get(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R, T> ContentResponse<T> postResponse(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> T post(MediaType<T> type, T content) {
      return postResponse(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE)
         .getContent();
   }

   default <R, T> T post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return postResponse(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE)
         .getContent();
   }

   default <R, T> T post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return postResponse(contentType, content, acceptType, change)
         .getContent();
   }

   <R, T> ContentResponse<T> putResponse(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <R, T> T put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return putResponse(contentType, content, acceptType, change)
         .getContent();
   }

   default <T> T put(MediaType<T> type, T content) {
      return put(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> T put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return put(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <T> ContentResponse<T> deleteResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> T delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return deleteResponse(acceptType, change)
         .getContent();
   }

   default <T> T delete(AcceptMediaType<T> acceptType) {
      return delete(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default void delete() {
      delete(MediaType.NONE);
   }

   <R, T> ContentResponse<T> optionsResponse(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType);

   default <R, T> T options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return optionsResponse(contentType, content, acceptType)
         .getContent();
   }
   
   default Response optionsResponse() {
      return optionsResponse(MediaType.NONE, null, MediaType.NONE);
   }

   AsyncResourceReference async();
}

