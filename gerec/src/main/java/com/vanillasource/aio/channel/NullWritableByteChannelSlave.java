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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.WritableByteChannel;

/**
 * A follower of <code>WritableByteChannel</code> that doesn't write anything, and
 * closes the channel as soon as it's allocated.
 */
public final class NullWritableByteChannelSlave implements AioSlave<Void> {
   private final WritableByteChannel channel;

   public NullWritableByteChannelSlave(WritableByteChannel channel) {
      this.channel = channel;
   }

   @Override
   public void onReady() {
      try {
         channel.close();
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   @Override
   public Void onCompleted() {
      return null;
   }
}




