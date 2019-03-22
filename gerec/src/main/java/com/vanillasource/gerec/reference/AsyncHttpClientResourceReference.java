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

import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.HttpStatusCode;
import com.vanillasource.gerec.HttpErrorException;
import com.vanillasource.gerec.mediatype.ByteArrayAcceptType;
import com.vanillasource.gerec.mediatype.MediaTypeSpecification;
import com.vanillasource.gerec.http.SingleHeaderValueSet;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.Header;
import com.vanillasource.aio.channel.InputStreamReadableByteChannelMaster;
import com.vanillasource.gerec.AsyncResourceReference;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.ErrorResponse;
import com.vanillasource.gerec.Response;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.ReadableByteChannelMaster;
import java.net.URI;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;

/**
 * Implement a resource reference using a HTTP Client.
 */
public class AsyncHttpClientResourceReference implements AsyncResourceReference {
   private static final Logger logger = LoggerFactory.getLogger(AsyncHttpClientResourceReference.class);
   private final AsyncHttpClient asyncHttpClient;
   private final URI uri;

   public AsyncHttpClientResourceReference(AsyncHttpClient asyncHttpClient, URI uri) {
      this.asyncHttpClient = asyncHttpClient;
      this.uri = uri;
   }

   @Override
   public CompletableFuture<Response> head(HttpRequest.HttpRequestChange change) {
      return asyncHttpClient.doHead(uri, change)
         .thenCompose(response -> createResponse(response, MediaType.NONE))
         .thenApply(response -> response); // Don't want to change signature to CompletableFuture<? extends Response>
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return asyncHttpClient.doGet(uri, change.and(acceptType::applyAsOption))
         .thenCompose(response -> createResponse(response, acceptType));
   }

   private <T> CompletableFuture<ContentResponse<T>> createResponse(HttpResponse response, AcceptMediaType<T> acceptType) {
      if (response.getStatusCode().isError()) {
         if (logger.isDebugEnabled()) {
            logger.debug("received status code "+response.getStatusCode()+", throwing error");
         }
         return error(response, null);
      } else {
         if (acceptType.isHandling(response)) {
            try {
               return acceptType.deserialize(response, this::follow)
                  .thenApply(content -> new HttpContentResponse<>(response, content));
            } catch (RuntimeException e) {
               return error(response, e);
            }
         } else {
            logger.warn("accept type is not handling response, so returning error");
            return error(response, null);
         }
      }
   }

   private <T> CompletableFuture<ContentResponse<T>> error(HttpResponse response, Exception cause) {
      return new ByteArrayAcceptType(MediaTypeSpecification.WILDCARD).deserialize(response, this::follow)
         .thenApply(content -> {
            logger.debug("cached full error contents, returning with exception", cause);
            throw new HttpErrorException("error in response, status code: "+response.getStatusCode(), new HttpErrorResponse(response, content));
         });
   }

   private AsyncResourceReference follow(URI linkUri) {
      return new AsyncHttpClientResourceReference(asyncHttpClient, uri.resolve(linkUri));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return asyncHttpClient.doPost(uri, change.and(acceptType::applyAsOption).and(contentType::applyAsContent).and(
            request -> contentType.serialize(content, request)))
         .thenCompose(response -> createResponse(response, acceptType));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return asyncHttpClient.doPut(uri, change.and(acceptType::applyAsOption).and(contentType::applyAsContent).and(
            request -> contentType.serialize(content, request)))
         .thenCompose(response -> createResponse(response, acceptType));
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      return asyncHttpClient.doDelete(uri, change.and(acceptType::applyAsOption))
         .thenCompose(response -> createResponse(response, acceptType));
   }

   @Override
   public <R, T> CompletableFuture<ContentResponse<T>> options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      return asyncHttpClient.doOptions(uri, HttpRequest.HttpRequestChange.NO_CHANGE.and(acceptType::applyAsOption))
         .thenCompose(response -> createResponse(response, acceptType));
   }

   private class HttpBaseResponse<T> implements Response {
      protected HttpResponse response;

      public HttpBaseResponse(HttpResponse response) {
         this.response = response;
      }

      @Override
      public HttpStatusCode getStatusCode() {
         return response.getStatusCode();
      }

      @Override
      public boolean hasIdentity() {
         return response.hasHeader(Headers.ETAG);
      }

      @Override
      public HttpRequest.HttpRequestChange ifMatch() {
         return new SingleHeaderValueSet(Headers.IF_MATCH, response.getHeader(Headers.ETAG));
      }

      @Override
      public HttpRequest.HttpRequestChange ifNotMatch() {
         return new SingleHeaderValueSet(Headers.IF_NONE_MATCH, response.getHeader(Headers.ETAG));
      }

      @Override
      public HttpRequest.HttpRequestChange ifModifiedSince() {
         return new SingleHeaderValueSet(Headers.IF_MODIFIED_SINCE, response.getHeader(Headers.DATE));
      }

      @Override
      public HttpRequest.HttpRequestChange ifUnmodifiedSince() {
         return new SingleHeaderValueSet(Headers.IF_UNMODIFIED_SINCE, response.getHeader(Headers.DATE));
      }

      @Override
      public boolean hasLastModified() {
         return response.hasHeader(Headers.LAST_MODIFIED);
      }

      @Override
      public HttpRequest.HttpRequestChange ifModifiedSinceLastModified() {
         return new SingleHeaderValueSet(Headers.IF_MODIFIED_SINCE, response.getHeader(Headers.LAST_MODIFIED));
      }

      @Override
      public HttpRequest.HttpRequestChange ifUnmodifiedSinceLastModified() {
         return new SingleHeaderValueSet(Headers.IF_UNMODIFIED_SINCE, response.getHeader(Headers.LAST_MODIFIED));
      }

      @Override
      public boolean hasLocation() {
         return response.hasHeader(Headers.LOCATION);
      }

      @Override
      public AsyncResourceReference followLocation() {
         return follow(URI.create(response.getHeader(Headers.LOCATION)));
      }

      @Override
      public boolean hasAllow() {
         return response.hasHeader(Headers.ALLOW);
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
         return response.getHeader(Headers.ALLOW).contains(method);
      }
   }

   private class HttpContentResponse<T> extends HttpBaseResponse implements ContentResponse<T> {
      private T media;

      public HttpContentResponse(HttpResponse response, T media) {
         super(response);
         this.media = media;
      }

      @Override
      public T getContent() {
         return media;
      }
   }

   /**
    * Makes it possible to read the error content from an exception. Note: this
    * implementation copies the full error content into memory, to avoid unclosed
    * resources in case the user is not interested in the contents.
    */
   private class HttpErrorResponse extends HttpBaseResponse implements ErrorResponse {
      private final byte[] errorBody;

      public HttpErrorResponse(HttpResponse response, byte[] errorBody) {
         super(response);
         this.errorBody = errorBody;
      }

      @Override
      public boolean hasBody() {
         return response.hasHeader(Headers.CONTENT_TYPE);
      }

      @Override
      public boolean hasBody(AcceptMediaType<?> acceptType) {
         return acceptType.isHandling(response);
      }

      @Override
      public <T> T getBody(AcceptMediaType<T> acceptType) {
         try {
            return acceptType.deserialize(new HttpResponse() {
               @Override
               public HttpStatusCode getStatusCode() {
                  return response.getStatusCode();
               }

               @Override
               public boolean hasHeader(Header<?> header) {
                  return response.hasHeader(header);
               }

               @Override
               public <T> T getHeader(Header<T> header) {
                  return response.getHeader(header);
               }

               @Override
               public <T> CompletableFuture<T> consumeContent(Function<ReadableByteChannelMaster, AioSlave<T>> followerFactory) {
                  InputStreamReadableByteChannelMaster master = new InputStreamReadableByteChannelMaster(new ByteArrayInputStream(errorBody));
                  AioSlave<T> follower = followerFactory.apply(master);
                  return master.execute(follower, Runnable::run); // Safe to be sync, because slave reads everything without blocking
               }
            }, AsyncHttpClientResourceReference.this::follow).get();
         } catch (InterruptedException e) {
            throw new IllegalStateException("interrupted reading error message", e);
         } catch (ExecutionException e) {
            throw new IllegalStateException("exception while reading error message", e.getCause());
         }
      }
   }

   @Override
   public String toString() {
      return uri.toString();
   }
}

