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

package com.vanillasource.aio.channel;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.OutputStream;

/**
 * Channel leader that writes bytes to an output stream. Note that this channel may be blocking
 * iff the supplied stream blocks.
 */
public final class OutputStreamWritableByteChannelMaster extends PassiveAioMaster implements WritableByteChannelMaster {
   private final OutputStream output;

   public OutputStreamWritableByteChannelMaster(OutputStream output) {
      this.output = output;
   }

   @Override
   public void close() {
      super.close();
      try {
         output.close();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public int write(ByteBuffer buffer) throws IOException {
      byte[] tmp = new byte[buffer.remaining()];
      buffer.get(tmp);
      output.write(tmp);
      return tmp.length;
   }
}


