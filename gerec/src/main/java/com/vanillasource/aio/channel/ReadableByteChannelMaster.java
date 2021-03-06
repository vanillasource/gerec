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

import com.vanillasource.aio.AioMaster;
import java.nio.channels.ReadableByteChannel;
import java.nio.ByteBuffer;

public interface ReadableByteChannelMaster extends ReadableByteChannel, AioMaster {
   ReadableByteChannelMaster NULL = new ReadableByteChannelMaster() {
      @Override
      public void pause() {
      }

      @Override
      public void resume() {
      }

      @Override
      public int read(ByteBuffer buffer) {
         return -1;
      }

      @Override
      public void close() {
      }

      @Override
      public boolean isOpen() {
         return false;
      }
   };
}


