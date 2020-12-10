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
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.Response;
import com.vanillasource.gerec.Request;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.ContentMediaType;
import java.util.function.Consumer;

/**
 * A <code>ResourceReference</code> that applies a default change to all methods
 * of this reference.
 */
public final class ChangedResourceReference implements ResourceReference {
   private final ResourceReference delegate;
   private final HttpRequest.HttpRequestChange defaultChange;

   public ChangedResourceReference(ResourceReference delegate, HttpRequest.HttpRequestChange defaultChange) {
      this.delegate = delegate;
      this.defaultChange = defaultChange;
   }

   @Override
   public Request prepareHead(HttpRequest.HttpRequestChange change) {
      return delegate.prepareHead(defaultChange.and(change));
   }

   @Override
   public Request prepareGet(HttpRequest.HttpRequestChange change) {
      return delegate.prepareGet(defaultChange.and(change));
   }

   @Override
   public <R> Request preparePost(ContentMediaType<R> contentType, R content, HttpRequest.HttpRequestChange change) {
      return delegate.preparePost(contentType, content, defaultChange.and(change));
   }

   @Override
   public <R> Request preparePut(ContentMediaType<R> contentType, R content, HttpRequest.HttpRequestChange change) {
      return delegate.preparePut(contentType, content, defaultChange.and(change));
   }

   @Override
   public Request prepareDelete(HttpRequest.HttpRequestChange change) {
      return delegate.prepareDelete(defaultChange.and(change));
   }

   @Override
   public <R> Request prepareOptions(ContentMediaType<R> contentType, R content, HttpRequest.HttpRequestChange change) {
      return delegate.prepareOptions(contentType, content, change);
   }

   @Override
   public Request prepareResume(byte[] suspendedRequest) {
      return delegate.prepareResume(suspendedRequest);
   }
}

