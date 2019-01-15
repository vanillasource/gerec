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

/**
 * A <code>ResourceReference</code> that applies a constant change to this requests
 * and all follow-up requests while the URI predicate holds.
 */
public final class PermanentChangeAsyncResourceReference implements AsyncResourceReference {
   private final AsyncResourceReference delegate;
   private final Predicate<URI> uriPredicate;
   private final HttpRequest.HttpRequestChange permanentChange;

   public PermanentChangeAsyncResourceReference(AsyncResourceReference delegate, HttpRequest.HttpRequestChange permanentChange) {
      this(delegate, uri -> true, permanentChange);
   }

   public PermanentChangeAsyncResourceReference(AsyncResourceReference delegate, Predicate<URI> uriPredicate,
         HttpRequest.HttpRequestChange permanentChange) {
      this.delegate = delegate;
      this.uriPredicate = uriPredicate;
      this.permanentChange = permanentChange;
   }

   @Override
   public CompletableFuture<Response> head(HttpRequest.HttpRequestChange change) {
      return delegate.head(permanentChange.and(change));
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return delegate.get(passChange(acceptType), permanentChange.and(change));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return delegate.post(contentType, content, passChange(acceptType), permanentChange.and(change));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return delegate.put(contentType, content, passChange(acceptType), permanentChange.and(change));
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return delegate.delete(passChange(acceptType), permanentChange.and(change));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return delegate.options(contentType, content, passChange(acceptType));
   }

   private <T> AcceptMediaType<T> passChange(AcceptMediaType<T> delegateType) {
      return new AcceptMediaType<T>() {
         @Override
         public void applyAsOption(HttpRequest request) {
            delegateType.applyAsOption(request);
         }

         @Override
         public boolean isHandling(HttpResponse response) {
            return delegateType.isHandling(response);
         }

         @Override
         public CompletableFuture<T> deserialize(HttpResponse response, DeserializationContext context) {
            return delegateType.deserialize(response, uri -> {
               if (uriPredicate.test(uri)) {
                  return new PermanentChangeAsyncResourceReference(context.resolve(uri), uriPredicate, permanentChange);
               } else {
                  return context.resolve(uri);
               }
            });
         }
      };
   }
}

