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

import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A channel leader that can not be controlled. Use only in cases where the follower can process
 * the <code>onReady()</code> call completely on first call.
 */
public final class UncontrollableReadableByteChannelLeader implements ReadableByteChannelLeader {
   private final ReadableByteChannel delegate;

   public UncontrollableReadableByteChannelLeader(ReadableByteChannel delegate) {
      this.delegate = delegate;
   }

   @Override
   public boolean isOpen() {
      return delegate.isOpen();
   }

   @Override
   public void close() {
      try {
         delegate.close();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public int read(ByteBuffer buffer) throws IOException {
      return delegate.read(buffer);
   }

   @Override
   public void pause() {
      throw new UnsupportedOperationException("can not pause uncontrollable channel");
   }

   @Override
   public void resume() {
      throw new UnsupportedOperationException("can not resume uncontrollable channel");
   }
}

