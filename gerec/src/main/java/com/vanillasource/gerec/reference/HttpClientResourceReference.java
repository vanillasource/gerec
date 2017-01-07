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
import com.vanillasource.gerec.HttpErrorException;
import com.vanillasource.gerec.http.SingleHeaderValueSet;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.Header;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.ErrorResponse;
import com.vanillasource.gerec.Response;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.ContentMediaType;
import java.net.URI;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.util.function.Function;
import java.io.InputStream;

/**
 * Implement a resource reference using a HTTP Client.
 */
public class HttpClientResourceReference implements ResourceReference {
   private final HttpClient httpClient;
   private final URI uri;

   public HttpClientResourceReference(HttpClient httpClient, URI uri) {
      this.httpClient = httpClient;
      this.uri = uri;
   }

   @Override
   public Response head(HttpRequest.HttpRequestChange change) {
      HttpResponse response = httpClient.doHead(uri, change);
      return createResponse(response, null);
   }

   @Override
   public <T> ContentResponse<T> get(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      HttpResponse response = httpClient.doGet(uri, change.and(acceptType::applyAsOption));
      return createResponse(response, acceptType);
   }

   private <T> ContentResponse<T> createResponse(HttpResponse response, AcceptMediaType<T> acceptType) {
      if (response.getStatusCode().isError()) {
         throw new HttpErrorException("error in response, status code: "+response.getStatusCode(),
               new HttpErrorResponse(response));
      }
      T media = null;
      if (acceptType != null) {
         media = acceptType.deserialize(response, this::follow);
      }
      return new HttpContentResponse<>(response, media);
   }

   private ResourceReference follow(URI linkUri) {
      return new HttpClientResourceReference(httpClient, uri.resolve(linkUri));
   }

   @Override
   public <R, T> ContentResponse<T> post(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      HttpResponse response = httpClient.doPost(uri, change.and(acceptType::applyAsOption).and(contentType::applyAsContent).and(
            request -> contentType.serialize(content, request)));
      return createResponse(response, acceptType);
   }

   @Override
   public <R, T> ContentResponse<T> put(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      HttpResponse response = httpClient.doPut(uri, change.and(acceptType::applyAsOption).and(contentType::applyAsContent).and(
            request -> contentType.serialize(content, request)));
      return createResponse(response, acceptType);
   }

   @Override
   public <T> ContentResponse<T> delete(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change) {
      HttpResponse response = httpClient.doDelete(uri, change.and(acceptType::applyAsOption));
      return createResponse(response, acceptType);
   }

   @Override
   public <R, T> ContentResponse<T> options(ContentMediaType<R> contentType, R content, AcceptMediaType<T> acceptType) {
      HttpResponse response = httpClient.doOptions(uri, HttpRequest.HttpRequestChange.NO_CHANGE.and(acceptType::applyAsOption));
      return createResponse(response, acceptType);
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
      public ResourceReference followLocation() {
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
      private byte[] errorBody;

      public HttpErrorResponse(HttpResponse response) {
         super(response);
         consumeResponse();
      }

      private void consumeResponse() {
            response.processContent(input -> {
               try {
                  ByteArrayOutputStream output = new ByteArrayOutputStream();
                  byte[] buffer = new byte[2048];
                  int len = 0;
                  while ( (len = input.read(buffer)) >= 0) {
                     output.write(buffer, 0, len);
                  }
                  output.close();
                  errorBody = output.toByteArray();
               } catch (IOException e) {
                  throw new UncheckedIOException(e);
               }
            });
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
            public <T> T processContent(Function<InputStream, T> contentProcessor) {
               try {
                  try (InputStream input = new ByteArrayInputStream(errorBody)) {
                     return contentProcessor.apply(input);
                  }
               } catch (IOException e) {
                  throw new UncheckedIOException(e);
               }
            }
         }, HttpClientResourceReference.this::follow);
      }
   }
}

