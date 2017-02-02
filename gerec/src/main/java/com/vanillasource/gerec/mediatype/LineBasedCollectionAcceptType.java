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

package com.vanillasource.gerec.mediatype;

import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpStatusCode;
import com.vanillasource.gerec.Header;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.ReadableByteChannelMaster;
import com.vanillasource.aio.channel.UncontrollableByteArrayReadableByteChannelMaster;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * An accept type that with another (delegate) accept type will lazily deserialize
 * single lines into single items. This is suitable for messages where each (non-empty)
 * line in the response corresponds to one deserializable object, which is done by the delegate
 * accept type. 
 * Because the response is processed lazily (with the help of a callback mechanism),
 * this type can be used for long-polling basically indefinitely.
 * Because only non-empty lines are deserialized, empty lines (line breaks) can be used by
 * the server to keep the connection alive.
 */
public class LineBasedCollectionAcceptType<T> implements AcceptMediaType<Void> {
   private static final int READ_BUFFER_SIZE = 4096;
   private final MediaTypeSpecification mediaType;
   private final AcceptMediaType<T> acceptType;
   private final Consumer<T> consumer;

   public LineBasedCollectionAcceptType(MediaTypeSpecification mediaType, AcceptMediaType<T> acceptType, Consumer<T> consumer) {
      this.mediaType = mediaType;
      this.acceptType = acceptType;
      this.consumer = consumer;
   }

   @Override
   public void applyAsOption(HttpRequest request) {
      mediaType.addAsAcceptedTo(request);
   }

   @Override
   public boolean isHandling(HttpResponse response) {
      return mediaType.isIn(response);
   }

   @Override
   public CompletableFuture<Void> deserialize(HttpResponse response, DeserializationContext context) {
      return response.consumeContent(input -> new AioSlave<Void>() {
         private final ByteBuffer inputBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
         private final StringBuilder lineBuilder = new StringBuilder();

         @Override
         public void onReady() {
            try {
               while (input.read(inputBuffer) > 0) {
                  inputBuffer.flip();
                  while (inputBuffer.hasRemaining()) {
                     int c = inputBuffer.get();
                     if (c == '\n') {
                        String line = lineBuilder.toString();
                        if (!line.isEmpty()) {
                           processLine(line);
                        }
                        lineBuilder.setLength(0);
                     } else {
                        lineBuilder.append((char) c);
                     }
                  }
                  inputBuffer.clear();
               }
            } catch (IOException e) {
               throw new UncheckedIOException(e);
            }
         }

         private void processLine(String line) {
            acceptType.deserialize(new HttpResponse() {
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
               public <T> CompletableFuture<T> consumeContent(Function<ReadableByteChannelMaster, AioSlave<T>> consumerFactory) {
                  AioSlave<T> follower = consumerFactory.apply(new UncontrollableByteArrayReadableByteChannelMaster(line.getBytes()));
                  follower.onReady();
                  return CompletableFuture.completedFuture(follower.onCompleted());
               }
            }, context)
            .thenAccept(consumer);
         }

         @Override
         public Void onCompleted() {
            return null;
         }
      });
   }
}
