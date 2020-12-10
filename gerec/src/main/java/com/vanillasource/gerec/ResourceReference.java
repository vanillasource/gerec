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
import java.util.function.Consumer;

/**
 * An asynchronous version of <code>ResourceReference</code>, with exactly the
 * same functionality, except it returns a <code>CompletableFuture</code> for all operations.
 */
public interface ResourceReference extends Serializable {
   Request prepareHead(HttpRequest.HttpRequestChange change);

   default Request prepareHead() {
      return prepareHead(HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default CompletableFuture<Response> headResponse(HttpRequest.HttpRequestChange change) {
      return prepareHead(change).sendResponse(MediaType.NONE)
         .thenApply(response -> response);
   }

   default CompletableFuture<Response> headResponse() {
      return headResponse(HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   Request prepareGet(HttpRequest.HttpRequestChange change);

   default Request prepareGet() {
      return prepareGet(HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <T> CompletableFuture<ContentResponse<T>> getResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return prepareGet(change).sendResponse(acceptType);
   }

   default <T> CompletableFuture<ContentResponse<T>> getResponse(AcceptMediaType<T> acceptType) {
      return getResponse(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <T> CompletableFuture<T> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return getResponse(acceptType, change)
         .thenApply(ContentResponse::getContent);
   }

   default <T> CompletableFuture<T> get(AcceptMediaType<T> acceptType) {
      return get(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R> Request preparePost(ContentMediaType<R> contentType, R content, HttpRequest.HttpRequestChange change);

   default <R> Request preparePost(ContentMediaType<R> contentType, R content) {
      return preparePost(contentType, content, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> CompletableFuture<ContentResponse<T>> postResponse(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return preparePost(contentType, content, change).sendResponse(acceptType);
   }

   default <R, T> CompletableFuture<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return postResponse(contentType, content, acceptType, change)
         .thenApply(ContentResponse::getContent);
   }

   default <T> CompletableFuture<T> post(MediaType<T> type, T content) {
      return post(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> CompletableFuture<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return post(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R> Request preparePut(ContentMediaType<R> contentType, R content, HttpRequest.HttpRequestChange change);

   default <R> Request preparePut(ContentMediaType<R> contentType, R content) {
      return preparePut(contentType, content, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> CompletableFuture<ContentResponse<T>> putResponse(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return preparePut(contentType, content, change).sendResponse(acceptType);
   }

   default <R, T> CompletableFuture<T> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return putResponse(contentType, content, acceptType, change)
         .thenApply(ContentResponse::getContent);
   }

   default <T> CompletableFuture<T> put(MediaType<T> type, T content) {
      return put(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> CompletableFuture<T> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return put(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   Request prepareDelete(HttpRequest.HttpRequestChange change);

   default Request prepareDelete() {
      return prepareDelete(HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <T> CompletableFuture<ContentResponse<T>> deleteResponse(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return prepareDelete(change).sendResponse(acceptType);
   }

   default <T> CompletableFuture<T> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return deleteResponse(acceptType, change)
         .thenApply(ContentResponse::getContent);
   }

   default <T> CompletableFuture<T> delete(AcceptMediaType<T> acceptType) {
      return delete(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default CompletableFuture<Void> delete() {
      return delete(MediaType.NONE, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R> Request prepareOptions(ContentMediaType<R> contentType, R content, HttpRequest.HttpRequestChange change);

   default <R> Request prepareOptions() {
      return prepareOptions(MediaType.NONE, null, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> CompletableFuture<ContentResponse<T>> optionsResponse(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return prepareOptions(contentType, content, change).sendResponse(acceptType);
   }

   default CompletableFuture<ContentResponse<Void>> optionsResponse() {
      return optionsResponse(MediaType.NONE, null, MediaType.NONE, HttpRequest.HttpRequestChange.NO_CHANGE);
   }
   
   /**
    * Resume a request previously suspended.
    */
   Request prepareResume(byte[] suspendedRequest);

   default <T> CompletableFuture<ContentResponse<T>> resumeResponse(byte[] suspendedRequest, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return prepareResume(suspendedRequest).sendResponse(acceptType, change);
   }

   default <T> CompletableFuture<T> resume(byte[] suspendedRequest, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return resumeResponse(suspendedRequest, acceptType, change)
         .thenApply(ContentResponse::getContent);
   }

   default <T> CompletableFuture<T> resume(byte[] suspendedRequest, AcceptMediaType<T> acceptType) {
      return resume(suspendedRequest, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }
}

