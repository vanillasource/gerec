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

package com.vanillasource.gerec.javanet;

import com.vanillasource.gerec.reference.AsyncHttpClient;
import com.vanillasource.gerec.*;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import java.net.URI;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.WritableByteChannelMaster;
import java.util.stream.Collectors;
import com.vanillasource.aio.channel.OutputStreamWritableByteChannelMaster;
import com.vanillasource.aio.channel.ReadableByteChannelMaster;
import java.util.List;
import java.util.Map;
import com.vanillasource.aio.channel.InputStreamReadableByteChannelMaster;
import java.util.concurrent.Executor;
import com.vanillasource.aio.channel.NullWritableByteChannelSlave;

/**
 * Implement the <code>AsyncHttpClient</code> with standard java.net
 * classes, specifically <code>HttpURLConnection</code>. This is suitable
 * to be used in Android. As these classes do not support non-blocking
 * operation, an appropriate executor has to be supplied on which operations
 * will be pushed to.
 */
public final class AsyncJavanetHttpClient implements AsyncHttpClient {
   private final Executor executor;
   private final Function<URL, HttpURLConnection> connectionFactory;

   /**
    * Create with an executor service, and not additional parameters for the connection.
    */
   public AsyncJavanetHttpClient(Executor executor) {
      this(executor, url -> {
         try {
            return (HttpURLConnection) url.openConnection();
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      });
   }

   /**
    * Create with executor and potentially custom configuration of the <code>HttpURLConnection</code>.
    */
   public AsyncJavanetHttpClient(Executor executor, Function<URL, HttpURLConnection> connectionFactory) {
      this.executor = executor;
      this.connectionFactory = connectionFactory;
   }

   @Override
   public CompletableFuture<HttpResponse> doHead(URI uri, HttpRequest.HttpRequestChange change) {
      return executeAsync(uri, "HEAD", change);
   }

   @Override
   public CompletableFuture<HttpResponse> doGet(URI uri, HttpRequest.HttpRequestChange change) {
      return executeAsync(uri, "GET", change);
   }

   @Override
   public CompletableFuture<HttpResponse> doPost(URI uri, HttpRequest.HttpRequestChange change) {
      return executeAsync(uri, "POST", change);
   }

   @Override
   public CompletableFuture<HttpResponse> doPut(URI uri, HttpRequest.HttpRequestChange change) {
      return executeAsync(uri, "PUT", change);
   }

   @Override
   public CompletableFuture<HttpResponse> doDelete(URI uri, HttpRequest.HttpRequestChange change) {
      return executeAsync(uri, "DELETE", change);
   }

   @Override
   public CompletableFuture<HttpResponse> doOptions(URI uri, HttpRequest.HttpRequestChange change) {
      return executeAsync(uri, "OPTIONS", change);
   }

   private CompletableFuture<HttpResponse> executeAsync(URI uri, String method, HttpRequest.HttpRequestChange change) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            return execute(uri, method, change);
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      }, executor);
   }

   private HttpResponse execute(URI uri, String method, HttpRequest.HttpRequestChange change) throws IOException {
      HttpURLConnection connection = connectionFactory.apply(uri.toURL());
      HttpRequestAdapter request = new HttpRequestAdapter(method, connection);
      change.applyTo(request);
      return request.execute();
   }

   private final class HttpRequestAdapter implements HttpRequest {
      private final String method;
      private final HttpURLConnection connection;
      private Function<WritableByteChannelMaster, AioSlave<Void>> producerFactory = NullWritableByteChannelSlave::new;
      private long length = -1;

      public HttpRequestAdapter(String method, HttpURLConnection connection) {
         this.method = method;
         this.connection = connection;
      }

      public HttpResponse execute() throws IOException {
         writeContentIfNeeded();
         return new HttpResponseAdapter(connection);
      }

      private void writeContentIfNeeded() throws IOException {
         connection.setRequestMethod(method);
         if (!"GET".equals(method) && !"DELETE".equals(method)) {
            connection.setDoOutput(true);
            if (length >= 0) {
               connection.setFixedLengthStreamingMode(length);
            } else {
               connection.setChunkedStreamingMode(0);
            }
            OutputStreamWritableByteChannelMaster master = new OutputStreamWritableByteChannelMaster(connection.getOutputStream());
            AioSlave<Void> slave = producerFactory.apply(master);
            master.execute(slave, Runnable::run).join();
         }
      }

      @Override
      public boolean hasHeader(Header<?> header) {
         return connection.getRequestProperties().containsKey(header.getName());
      }

      @Override
      public <T> T getHeader(Header<T> header) {
         return header.deserialize(connection.getRequestProperties().get(header.getName()));
      }

      @Override
      public <T> void setHeader(Header<T> header, T value) {
         connection.setRequestProperty(header.getName(), header.serialize(value).stream().collect(Collectors.joining(",")));
      }

      @Override
      public void setByteProducer(Function<WritableByteChannelMaster, AioSlave<Void>> producerFactory) {
         this.producerFactory = producerFactory;
      }

      @Override
      public void setByteProducer(Function<WritableByteChannelMaster, AioSlave<Void>> producerFactory, long length) {
         this.producerFactory = producerFactory;
         this.length = length;
      }
   }

   private final class HttpResponseAdapter implements HttpResponse {
      private final HttpURLConnection connection;
      private final Map<String,List<String>> headers;

      public HttpResponseAdapter(HttpURLConnection connection) {
         this.connection = connection;
         this.headers = connection.getHeaderFields();
      }

      @Override
      public HttpStatusCode getStatusCode() {
         try {
            return HttpStatusCode.valueOf(connection.getResponseCode());
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      }

      @Override
      public boolean hasHeader(Header<?> header) {
         return headers.containsKey(header.getName());
      }

      @Override
      public <T> T getHeader(Header<T> header) {
         return header.deserialize(headers.get(header.getName()));
      }

      @Override
      public <R> CompletableFuture<R> consumeContent(Function<ReadableByteChannelMaster, AioSlave<R>> consumerFactory) {
         // Content can not be consumed of non-success connections (for some reason)
         try {
            if (connection.getResponseCode() >= 300) {
               connection.disconnect();
               return CompletableFuture.completedFuture(null);
            } else {
               InputStreamReadableByteChannelMaster master = new InputStreamReadableByteChannelMaster(connection.getInputStream());
               AioSlave<R> slave = consumerFactory.apply(master);
               return master.execute(slave, executor)
                  .whenComplete((result, exception) -> connection.disconnect());
            }
         } catch (IOException e) {
            connection.disconnect();
            throw new UncheckedIOException(e);
         }
      }
   }
}

