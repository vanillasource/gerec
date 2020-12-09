/**
 * Copyright (C) 2020 VanillaSource
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

import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.vanillasource.gerec.Header;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.WritableByteChannelMaster;
import java.util.function.Function;
import java.io.ByteArrayOutputStream;
import com.vanillasource.aio.channel.OutputStreamWritableByteChannelMaster;

public final class RecordingHttpClient implements HttpClient {
   public RecordedRequest lastRequest;

   @Override
   public CompletableFuture<HttpResponse> doHead(URI uri, HttpRequest.HttpRequestChange change) {
      return doMethod("HEAD", uri, change);
   }

   @Override
   public CompletableFuture<HttpResponse> doOptions(URI uri, HttpRequest.HttpRequestChange change) {
      return doMethod("OPTIONS", uri, change);
   }

   @Override
   public CompletableFuture<HttpResponse> doGet(URI uri, HttpRequest.HttpRequestChange change) {
      return doMethod("GET", uri, change);
   }

   @Override
   public CompletableFuture<HttpResponse> doPost(URI uri, HttpRequest.HttpRequestChange change) {
      return doMethod("POST", uri, change);
   }

   @Override
   public CompletableFuture<HttpResponse> doPut(URI uri, HttpRequest.HttpRequestChange change) {
      return doMethod("PUT", uri, change);
   }

   @Override
   public CompletableFuture<HttpResponse> doDelete(URI uri, HttpRequest.HttpRequestChange change) {
      return doMethod("DELETE", uri, change);
   }

   private CompletableFuture<HttpResponse> doMethod(String method, URI uri, HttpRequest.HttpRequestChange change) {
      Map<String, List<String>> headers = new HashMap<>();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      change.applyTo(new HttpRequest() {
         @Override
         public boolean hasHeader(Header<?> header) {
            return headers.containsKey(header.getName().toUpperCase());
         }

         @Override
         public <T> T getHeader(Header<T> header) {
            return header.deserialize(headers.get(header.getName().toUpperCase()));
         }

         @Override
         public <T> void setHeader(Header<T> header, T value) {
            headers.put(header.getName().toUpperCase(), header.serialize(value));
         }

         @Override
         public void setByteProducer(Function<WritableByteChannelMaster, AioSlave<Void>> producerFactory) {
            OutputStreamWritableByteChannelMaster master = new OutputStreamWritableByteChannelMaster(bos);
            AioSlave<Void> follower = producerFactory.apply(master);
            master.execute(follower, Runnable::run);
         }

         @Override
         public void setByteProducer(Function<WritableByteChannelMaster, AioSlave<Void>> producerFactory, long length) {
            setByteProducer(producerFactory);
         }
      });
      this.lastRequest = new RecordedRequest(method, uri.toString(), headers, new String(bos.toByteArray()));
      return CompletableFuture.completedFuture(null);
   }

   public static final class RecordedRequest {
      private String method;
      private String uri;
      private Map<String, List<String>> headers;
      private String content;

      public RecordedRequest(String method, String uri, Map<String, List<String>> headers, String content) {
         this.method = method;
         this.uri = uri;
         this.headers = headers;
         this.content = content;
      }

      @Override
      public String toString() {
         return method+" "+uri+" "+headers+": "+content;
      }

      @Override
      public boolean equals(Object o) {
         if ((o == null) || (!(o instanceof RecordedRequest))) {
            return false;
         }
         RecordedRequest r = (RecordedRequest) o;
         return method.equals(r.method) && uri.equals(r.uri) && headers.equals(r.headers)
            && content.equals(r.content);
      }
   }
}
