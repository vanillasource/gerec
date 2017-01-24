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

package com.vanillasource.gerec.nio;

import com.vanillasource.gerec.HttpRequest;
import java.nio.channels.WritableByteChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Produces bytes from a byte buffer to the output channel until all bytes are trasmitted.
 */
public class ByteBufferProducer implements HttpRequest.ByteProducer {
   private final ByteBuffer buffer;
   private final WritableByteChannel output;

   public ByteBufferProducer(ByteBuffer buffer, WritableByteChannel output) {
      this.buffer = buffer;
      this.output = output;
   }

   @Override
   public void onReady() {
      try {
         if (buffer.hasRemaining()) {
            output.write(buffer);
         }
         if (!buffer.hasRemaining()) {
            onCompleted();
         }
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public void onCompleted() {
      try {
         output.close();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }
}


