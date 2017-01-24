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

package com.vanillasource.gerec;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

/**
 * A full media-type which can be both serialized to make requests and deserialized back from
 * responses.
 */
public interface MediaType<T> extends ContentMediaType<T>, AcceptMediaType<T> {
   /**
    * A MediaType that simply discards any response content if there is any, and does not
    * have any content as a request.
    */
   MediaType<Void> NONE = new MediaType<Void>() {
      @Override
      public void applyAsOption(HttpRequest request) {
      }

      @Override
      public void applyAsContent(HttpRequest request) {
      }

      @Override
      public boolean isHandling(HttpResponse response) {
         return true;
      }

      @Override
      public CompletableFuture<Void> deserialize(HttpResponse response, DeserializationContext context) {
         CompletableFuture<Void> result = new CompletableFuture<>();
         response.consumeContent(input -> new HttpResponse.ByteConsumer() {
            private ByteBuffer nothing = ByteBuffer.allocateDirect(4096);

            @Override
            public void onReady() {
               try {
                  while (input.read(nothing) > 0) {
                     nothing.clear();
                  }
               } catch (IOException e) {
                  throw new UncheckedIOException(e);
               }
            }

            @Override
            public void onCompleted() {
               result.complete(null);
            }

            @Override
            public void onException(Exception e) {
               result.completeExceptionally(e);
            }
         });
         return result;
      }

      @Override
      public void serialize(Void object, HttpRequest request) {
         request.setByteProducer(output -> new HttpRequest.ByteProducer() {
            @Override
            public void onReady() {
               onCompleted();
            }

            @Override
            public void onCompleted() {
               try {
                  output.close();
               } catch (IOException e) {
                  throw new UncheckedIOException(e);
               }
            }
         }, 0);
      }
   };
}
