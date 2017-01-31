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

import com.vanillasource.aio.channel.ByteArrayReadableByteChannelFollower;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.DeserializationContext;
import java.util.concurrent.CompletableFuture;

/**
 * Accept media type that copies all input into a byte array.
 */
public class ByteArrayAcceptType implements AcceptMediaType<byte[]> {
   private static final int DEFAULT_INITIAL_SIZE = 4096;
   private final MediaTypeSpecification mediaType;

   public ByteArrayAcceptType(MediaTypeSpecification mediaType) {
      this.mediaType = mediaType;
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
   public CompletableFuture<byte[]> deserialize(HttpResponse response, DeserializationContext context) {
      int initialSize;
      if (response.hasHeader(Headers.CONTENT_LENGTH)) {
         initialSize = response.getHeader(Headers.CONTENT_LENGTH).intValue();
      } else {
         initialSize = DEFAULT_INITIAL_SIZE;
      }
      return response.consumeContent(channel ->
            new ByteArrayReadableByteChannelFollower(channel, initialSize));
   }
}

