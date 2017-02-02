/**
 * Copyright (C) 2017 VanillaSource
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

package com.vanillasource.aio.channel;

import com.vanillasource.aio.AioSlave;
import java.nio.channels.WritableByteChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Writes all bytes given to the writable channel, without pausing.
 */
public class ByteArrayWritableByteChannelSlave implements AioSlave<Void> {
   private final WritableByteChannel channel;
   private final ByteBuffer content;

   public ByteArrayWritableByteChannelSlave(WritableByteChannel channel, byte[] bytes) {
      this.channel = channel;
      this.content = ByteBuffer.wrap(bytes);
   }

   @Override
   public void onReady() {
      try {
         if (content.hasRemaining()) {
            channel.write(content);
         }
         if (!content.hasRemaining()) {
            channel.close();
         }
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public Void onCompleted() {
      return null;
   }
}
