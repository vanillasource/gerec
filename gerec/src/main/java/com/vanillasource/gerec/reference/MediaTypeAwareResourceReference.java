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

package com.vanillasource.gerec.reference;

import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.HttpStatusCode;
import com.vanillasource.gerec.http.SingleHeaderValue;
import com.vanillasource.gerec.Header;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.Response;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.ContentMediaType;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.net.URI;

/**
 * Implement all media-type related functionality (serialization, deserialization) and request
 * modification. Subclasses "only" need to implement the direct HTTP related functionality.
 */
public abstract class MediaTypeAwareResourceReference implements ResourceReference {
   @Override
   public Response head(HttpRequest.HttpRequestChange change) {
      HttpResponse response = doHead(change);
      return new HttpContentResponse<>(response, null);
   }

   @Override
   public <T> ContentResponse<T> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      HttpResponse response = doGet(change.and(acceptType::applyAsOption));
      T media = acceptType.deserialize(response, this::follow);
      return new HttpContentResponse<>(response, media);
   }

   @Override
   public <R, T> ContentResponse<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      HttpResponse response = doPost(change.and(acceptType::applyAsOption).and(contentType::applyAsContent).and(
            request -> contentType.serialize(content, request)));
      T media = acceptType.deserialize(response, this::follow);
      return new HttpContentResponse<>(response, media);
   }

   @Override
   public <R, T> ContentResponse<T> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      HttpResponse response = doPut(change.and(acceptType::applyAsOption).and(contentType::applyAsContent).and(
            request -> contentType.serialize(content, request)));
      T media = acceptType.deserialize(response, this::follow);
      return new HttpContentResponse<>(response, media);
   }

   @Override
   public <T> ContentResponse<T> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      HttpResponse response = doDelete(change.and(acceptType::applyAsOption));
      T media = acceptType.deserialize(response, this::follow);
      return new HttpContentResponse<>(response, media);
   }

   @Override
   public <R, T> ContentResponse<T> options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      HttpResponse response = doOptions(HttpRequest.HttpRequestChange.NO_CHANGE.and(acceptType::applyAsOption));
      T media = acceptType.deserialize(response, this::follow);
      return new HttpContentResponse<>(response, media);
   }

   protected abstract ResourceReference follow(URI link);

   protected abstract HttpResponse doHead(HttpRequest.HttpRequestChange change);

   protected abstract HttpResponse doOptions(HttpRequest.HttpRequestChange change);

   protected abstract HttpResponse doGet(HttpRequest.HttpRequestChange change);

   protected abstract HttpResponse doPost(HttpRequest.HttpRequestChange change);

   protected abstract HttpResponse doPut(HttpRequest.HttpRequestChange change);

   protected abstract HttpResponse doDelete(HttpRequest.HttpRequestChange change);

   private class HttpContentResponse<T> implements ContentResponse<T> {
      private HttpResponse response;
      private T media;

      public HttpContentResponse(HttpResponse response, T media) {
         this.response = response;
         this.media = media;
      }

      @Override
      public HttpStatusCode getStatusCode() {
         return response.getStatusCode();
      }

      @Override
      public boolean hasIdentity() {
         return response.hasHeader(Header.ETAG);
      }

      @Override
      public HttpRequest.HttpRequestChange ifMatch() {
         return new SingleHeaderValue(Header.IF_MATCH, response.getHeader(Header.ETAG));
      }

      @Override
      public HttpRequest.HttpRequestChange ifNotMatch() {
         return new SingleHeaderValue(Header.IF_NONE_MATCH, response.getHeader(Header.ETAG));
      }

      @Override
      public HttpRequest.HttpRequestChange ifModifiedSince() {
         return new SingleHeaderValue(Header.IF_MODIFIED_SINCE, response.getHeader(Header.DATE));
      }

      @Override
      public HttpRequest.HttpRequestChange ifUnmodifiedSince() {
         return new SingleHeaderValue(Header.IF_UNMODIFIED_SINCE, response.getHeader(Header.DATE));
      }

      @Override
      public boolean hasLastModified() {
         return response.hasHeader(Header.LAST_MODIFIED);
      }

      @Override
      public HttpRequest.HttpRequestChange ifModifiedSinceLastModified() {
         return new SingleHeaderValue(Header.IF_MODIFIED_SINCE, response.getHeader(Header.LAST_MODIFIED));
      }

      @Override
      public HttpRequest.HttpRequestChange ifUnmodifiedSinceLastModified() {
         return new SingleHeaderValue(Header.IF_UNMODIFIED_SINCE, response.getHeader(Header.LAST_MODIFIED));
      }

      @Override
      public boolean hasLocation() {
         return response.hasHeader(Header.LOCATION);
      }

      @Override
      public ResourceReference followLocation() {
         return follow(URI.create(response.getHeader(Header.LOCATION)));
      }

      @Override
      public boolean hasAllow() {
         return response.hasHeader(Header.ALLOW);
      }

      @Override
      public boolean isGetAllowed() {
         return isMethodAllowed("GET");
      }

      @Override
      public boolean isPostAllowed() {
         return isMethodAllowed("POST");
      }

      @Override
      public boolean isPutAllowed() {
         return isMethodAllowed("PUT");
      }

      @Override
      public boolean isDeleteAllowed() {
         return isMethodAllowed("DELETE");
      }

      @Override
      public boolean isHeadAllowed() {
         return isMethodAllowed("HEAD");
      }

      private boolean isMethodAllowed(String method) {
         return response.getHeader(Header.ALLOW).contains(method);
      }

      @Override
      public T getContent() {
         return media;
      }
   }
}

