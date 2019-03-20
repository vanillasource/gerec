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
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Channel leader that supplies bytes from an input stream. Note that this channel may be blocking
 * iff the supplied stream blocks.
 */
public final class InputStreamReadableByteChannelMaster extends PassiveAioMaster implements ReadableByteChannelMaster {
   private final InputStream input;

   public InputStreamReadableByteChannelMaster(InputStream input) {
      this.input = input;
   }

   @Override
   public void close() {
      super.close();
      try {
         input.close();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public int read(ByteBuffer buffer) throws IOException {
      byte[] tmp = new byte[buffer.remaining()];
      int length = input.read(tmp);
      if (length < 0) {
         close();
      } else {
         buffer.put(tmp, 0, length);
      }
      return length;
   }
}


