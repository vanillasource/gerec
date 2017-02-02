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

package com.vanillasource.aio.channel;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A channel leader that can not be controlled. Use only in cases where the follower can write
 * all data on a single call to <code>onReady()</code>.
 */
public final class UncontrollableByteArrayWritableByteChannelMaster implements WritableByteChannelMaster {
   private final ByteArrayOutputStream output;

   public UncontrollableByteArrayWritableByteChannelMaster() {
      this.output = new ByteArrayOutputStream();
   }

   public byte[] toByteArray() {
      return output.toByteArray();
   }

   @Override
   public void close() {
   }

   @Override
   public boolean isOpen() {
      return true;
   }

   @Override
   public int write(ByteBuffer buffer) throws IOException {
      int count = 0;
      while (buffer.hasRemaining()) {
         output.write(buffer.get());
         count++;
      }
      return count;
   }

   @Override
   public void pause() {
      throw new UnsupportedOperationException("pause not supported in uncontrollable leader");
   }

   @Override
   public void resume() {
      throw new UnsupportedOperationException("resume not supported in uncontrollable leader");
   }
}

