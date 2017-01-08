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
import java.util.function.Supplier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletionException;

/**
 * An asynchronous version of <code>ResourceReference</code>, with exactly the
 * same functionality, except it returns a <code>CompletableFuture</code> for all operations.
 */
public interface AsyncResourceReference extends Serializable {
   CompletableFuture<Response> head(HttpRequest.HttpRequestChange change);

   default CompletableFuture<Response> head() {
      return head(HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <T> CompletableFuture<ContentResponse<T>> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> CompletableFuture<ContentResponse<T>> get(AcceptMediaType<T> acceptType) {
      return get(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R, T> CompletableFuture<ContentResponse<T>> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> CompletableFuture<ContentResponse<T>> post(MediaType<T> type, T content) {
      return post(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> CompletableFuture<ContentResponse<T>> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return post(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <R, T> CompletableFuture<ContentResponse<T>> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> CompletableFuture<ContentResponse<T>> put(MediaType<T> type, T content) {
      return put(type, content, type, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default <R, T> CompletableFuture<ContentResponse<T>> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return put(contentType, content, acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   <T> CompletableFuture<ContentResponse<T>> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> CompletableFuture<ContentResponse<T>> delete(AcceptMediaType<T> acceptType) {
      return delete(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default CompletableFuture<? extends Response> delete() {
      return delete(MediaType.NONE);
   }

   <R, T> CompletableFuture<ContentResponse<T>> options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType);
   
   default <T> CompletableFuture<ContentResponse<T>> options(MediaType<T> type, T content) {
      return options(type, content, type);
   }

   default CompletableFuture<? extends Response> options() {
      return options(MediaType.NONE, null);
   }

   default ResourceReference sync() {
      return new ResourceReference() {
         @Override
         public Response head(HttpRequest.HttpRequestChange change) {
            return wrapAsync(() -> AsyncResourceReference.this.head(change));
         }

         @Override
         public <T> ContentResponse<T> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
            return wrapAsync(() -> AsyncResourceReference.this.get(acceptType, change));
         }

         @Override
         public <R, T> ContentResponse<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
            return wrapAsync(() -> AsyncResourceReference.this.post(contentType, content, acceptType, change));
         }

         @Override
         public <R, T> ContentResponse<T> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
            return wrapAsync(() -> AsyncResourceReference.this.put(contentType, content, acceptType, change));
         }

         @Override
         public <T> ContentResponse<T> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
            return wrapAsync(() -> AsyncResourceReference.this.delete(acceptType, change));
         }

         @Override
         public <R, T> ContentResponse<T> options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
            return wrapAsync(() -> AsyncResourceReference.this.options(contentType, content, acceptType));
         }

         private <T> T wrapAsync(Supplier<CompletableFuture<T>> asyncSupplier) {
            try {
               return asyncSupplier.get().get();
            } catch (InterruptedException|ExecutionException e) {
               throw new CompletionException(e);
            }
         }

         @Override
         public AsyncResourceReference async() {
            return AsyncResourceReference.this;
         }

      };
   }
}

