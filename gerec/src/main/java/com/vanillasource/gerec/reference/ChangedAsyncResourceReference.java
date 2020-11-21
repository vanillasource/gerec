/**
 * Copyright (C) 2019 VanillaSource
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

package com.vanillasource.gerec.reference;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.net.URI;
import com.vanillasource.gerec.AsyncResourceReference;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.Response;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.ContentMediaType;
import java.util.function.Consumer;

/**
 * A <code>ResourceReference</code> that applies a default change to all methods
 * of this reference.
 */
public final class ChangedAsyncResourceReference implements AsyncResourceReference {
   private final AsyncResourceReference delegate;
   private final HttpRequest.HttpRequestChange defaultChange;

   public ChangedAsyncResourceReference(AsyncResourceReference delegate, HttpRequest.HttpRequestChange defaultChange) {
      this.delegate = delegate;
      this.defaultChange = defaultChange;
   }

   @Override
   public CompletableFuture<Response> head(HttpRequest.HttpRequestChange change) {
      return delegate.head(defaultChange.and(change));
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return delegate.get(acceptType, defaultChange.and(change));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return delegate.post(contentType, content, acceptType, defaultChange.and(change));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return delegate.put(contentType, content, acceptType, defaultChange.and(change));
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return delegate.delete(acceptType, defaultChange.and(change));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return delegate.options(contentType, content, acceptType);
   }

   @Override
   public byte[] suspend(Consumer<AsyncResourceReference> call) {
      return delegate.suspend(suspendedDelegate ->
            call.accept(new ChangedAsyncResourceReference(suspendedDelegate, defaultChange)));
   }

   @Override
   public CompletableFuture<Response> execute(byte[] suspendedCall) {
      return delegate.execute(suspendedCall);
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> execute(byte[] suspendedCall, AcceptMediaType<T> acceptType) {
      return delegate.execute(suspendedCall, acceptType);
   }

}

