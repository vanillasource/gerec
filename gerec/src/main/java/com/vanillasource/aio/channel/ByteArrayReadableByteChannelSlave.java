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
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Reads all bytes into an internal buffer. Note, this has to copy the bytes
 * multiple times, and consumes as much memory as all the bytes. Use only
 * when the size of the input is limited/known.
 */
public class ByteArrayReadableByteChannelSlave implements AioSlave<byte[]> {
   private static final int BUFFER_SIZE = 4096;
   private final ReadableByteChannel channel;
   private final byte[] bytes = new byte[BUFFER_SIZE];
   private final ByteBuffer buffer = ByteBuffer.wrap(bytes);
   private final ByteArrayOutputStream output;

   public ByteArrayReadableByteChannelSlave(ReadableByteChannel channel, int initialSize) {
      this.channel = channel;
      this.output = new ByteArrayOutputStream(initialSize);
   }

   public ByteArrayReadableByteChannelSlave(ReadableByteChannel channel) {
      this(channel, BUFFER_SIZE);
   }

   @Override
   public void onReady() {
      try {
         buffer.clear();
         int readLength;
         while ((readLength = channel.read(buffer)) > 0) {
            output.write(bytes, 0, readLength);
            buffer.clear();
         }
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public byte[] onCompleted() {
      return output.toByteArray();
   }
}
