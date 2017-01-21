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

import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.DeserializationContext;
import java.util.concurrent.CompletableFuture;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Accept media type that copies all input into a byte array. This can
 * not be directly applied to any request, but can be applied to
 * any response. Note: this implementation copies all bytes at least
 * 4 times, it should be only used in bounded length messages.
 */
public class ByteArrayAcceptType implements AcceptMediaType<byte[]> {
   private static final int BUFFER_SIZE = 4096;

   @Override
   public void applyAsOption(HttpRequest request) {
      throw new UnsupportedOperationException("byte array can not be directly applied to requests");
   }

   @Override
   public boolean isHandling(HttpResponse response) {
      return true;
   }

   @Override
   public CompletableFuture<byte[]> deserialize(HttpResponse response, DeserializationContext context) {
      ByteArrayOutputStream output;
      if (response.hasHeader(Headers.CONTENT_LENGTH)) {
         output = new ByteArrayOutputStream(response.getHeader(Headers.CONTENT_LENGTH));
      } else {
         output = new ByteArrayOutputStream();
      }
      CompletableFuture<byte[]> result = new CompletableFuture<>();
      response.consumeContent(channel -> new HttpResponse.ByteConsumer() {
         private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
         private final byte[] byteBuffer = new byte[BUFFER_SIZE];

         @Override
         public void onReady() {
            try {
               buffer.clear();
               int readLength = channel.read(buffer);
               buffer.flip();
               buffer.get(byteBuffer, 0, readLength);
               output.write(byteBuffer, 0, readLength);
            } catch (IOException e) {
               throw new UncheckedIOException(e);
            }
         }

         @Override
         public void onCompleted() {
            result.complete(output.toByteArray());
         }

         @Override
         public void onException(Exception e) {
            result.completeExceptionally(e);
         }
      });
      return result;
   }
}

