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
import com.vanillasource.gerec.Header;
import static com.vanillasource.gerec.http.Headers.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.UncheckedIOException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.WritableByteChannelMaster;
import java.util.function.Function;
import java.util.List;
import com.vanillasource.aio.channel.OutputStreamWritableByteChannelMaster;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.URISyntaxException;
import com.vanillasource.aio.channel.ByteArrayWritableByteChannelSlave;
import java.util.ArrayList;

/**
 * An http client which can suspend and re-execute a single call.
 */
public final class SuspendingAsyncHttpClient implements AsyncHttpClient {
   private byte[] suspendedCall;

   public SuspendingAsyncHttpClient() {
   }

   public SuspendingAsyncHttpClient(byte[] suspendedCall) {
      this.suspendedCall = suspendedCall;
   }

   public CompletableFuture<HttpResponse> execute(AsyncHttpClient client) {
      try {
         DataInputStream dis = new DataInputStream(new ByteArrayInputStream(suspendedCall));
         int actionCode = dis.readByte();
         URI uri = new URI(dis.readUTF());
         HttpRequest.HttpRequestChange change = HttpRequest.HttpRequestChange.NO_CHANGE;
         while (true) {
            int contentCode = dis.readByte();
            if (contentCode == 10) {
               String headerName = dis.readUTF();
               int size = dis.readInt();
               List<String> headerValues = new ArrayList<String>(size);
               for (int i=0; i<size; i++) {
                  headerValues.add(dis.readUTF());
               }
               change = change.and(request -> request.setHeader(csvStringHeader(headerName), headerValues));
            } else if (contentCode == 11) {
               int length = dis.readInt();
               byte[] content = new byte[length];
               dis.readFully(content);
               change = change.and(request -> {
                  request.setByteProducer(output -> new ByteArrayWritableByteChannelSlave(output, content), content.length);
               });
            } else {
               break;
            }
         }
         switch (actionCode) {
            case 1:
               return client.doHead(uri, change);
            case 2:
               return client.doOptions(uri, change);
            case 3:
               return client.doGet(uri, change);
            case 4:
               return client.doPost(uri, change);
            case 5:
               return client.doPut(uri, change);
            case 6:
               return client.doDelete(uri, change);
            default:
               throw new IllegalArgumentException("action code "+actionCode+" in suspended call is unknown");
         }
      } catch (IOException e) {
         throw new UncheckedIOException("unable to parse suspended call", e);
      } catch (URISyntaxException e) {
         throw new IllegalArgumentException("could not parse uri in suspended call", e);
      }
   }

   public byte[] suspend() {
      if (suspendedCall == null) {
         throw new IllegalStateException("there was no suspended call yet");
      }
      return suspendedCall;
   }

   private void suspend(int actionCode, URI uri, HttpRequest.HttpRequestChange change) {
      if (suspendedCall != null) {
         throw new IllegalStateException("there is already a suspended call, can not suspend more then one call");
      }
      try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(bos);
         dos.writeByte(actionCode);
         dos.writeUTF(uri.toString());
         change.applyTo(new HttpRequest() {
            private final Map<String, Object> headers = new HashMap<>();

            @Override
            public boolean hasHeader(Header<?> header) {
               return headers.containsKey(header.getName().toUpperCase());
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T getHeader(Header<T> header) {
               return (T) headers.get(header.getName().toUpperCase());
            }

            @Override
            public <T> void setHeader(Header<T> header, T value) {
               headers.put(header.getName().toUpperCase(), value);
               try {
                  dos.writeByte(10);
                  dos.writeUTF(header.getName());
                  List<String> values = header.serialize(value);
                  dos.writeInt(values.size());
                  for (String headerValue: values) {
                     dos.writeUTF(headerValue);
                  }
               } catch (IOException e) {
                  throw new UncheckedIOException("can not write header", e);
               }
            }

            @Override
            public void setByteProducer(Function<WritableByteChannelMaster, AioSlave<Void>> producerFactory) {
               ByteArrayOutputStream bos = new ByteArrayOutputStream();
               OutputStreamWritableByteChannelMaster master = new OutputStreamWritableByteChannelMaster(bos);
               AioSlave<Void> follower = producerFactory.apply(master);
               master.execute(follower, Runnable::run);
               byte[] content = bos.toByteArray();
               try {
                  dos.writeByte(11);
                  dos.writeInt(content.length);
                  dos.write(content);
               } catch (IOException e) {
                  throw new UncheckedIOException("can not write content", e);
               }
            }

            @Override
            public void setByteProducer(Function<WritableByteChannelMaster, AioSlave<Void>> producerFactory, long length) {
               setByteProducer(producerFactory);
            }
         });
         dos.writeByte(0x7F);
         dos.close();
         suspendedCall = bos.toByteArray();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }


   @Override
   public CompletableFuture<HttpResponse> doHead(URI uri, HttpRequest.HttpRequestChange change) {
      suspend(1, uri, change);
      return new CompletableFuture<>();
   }

   @Override
   public CompletableFuture<HttpResponse> doOptions(URI uri, HttpRequest.HttpRequestChange change) {
      suspend(2, uri, change);
      return new CompletableFuture<>();
   }

   @Override
   public CompletableFuture<HttpResponse> doGet(URI uri, HttpRequest.HttpRequestChange change) {
      suspend(3, uri, change);
      return new CompletableFuture<>();
   }

   @Override
   public CompletableFuture<HttpResponse> doPost(URI uri, HttpRequest.HttpRequestChange change) {
      suspend(4, uri, change);
      return new CompletableFuture<>();
   }

   @Override
   public CompletableFuture<HttpResponse> doPut(URI uri, HttpRequest.HttpRequestChange change) {
      suspend(5, uri, change);
      return new CompletableFuture<>();
   }

   @Override
   public CompletableFuture<HttpResponse> doDelete(URI uri, HttpRequest.HttpRequestChange change) {
      suspend(6, uri, change);
      return new CompletableFuture<>();
   }
}
