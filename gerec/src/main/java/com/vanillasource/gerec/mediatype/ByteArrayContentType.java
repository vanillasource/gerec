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

import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.aio.channel.ByteArrayWritableByteChannelSlave;

/**
 * Serializes a byte array into the request.
 */
public class ByteArrayContentType implements ContentMediaType<byte[]> {
   private final MediaTypeSpecification mediaType;

   public ByteArrayContentType(MediaTypeSpecification mediaType) {
      this.mediaType = mediaType;
   }

   @Override
   public void applyAsContent(HttpRequest request) {
      mediaType.addAsContentTo(request);
   }

   @Override
   public void serialize(byte[] content, HttpRequest request) {
      request.setByteProducer(output -> new ByteArrayWritableByteChannelSlave(output, content), content.length);
   }
}
