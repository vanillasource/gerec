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

package com.vanillasource.gerec;

import java.util.function.Function;
import java.nio.channels.ReadableByteChannel;

public interface HttpResponse {
   HttpStatusCode getStatusCode();

   boolean hasHeader(Header<?> header);

   <T> T getHeader(Header<T> header);

   void consumeContent(Function<ControllableReadableByteChannel, ByteConsumer> consumerFactory);

   interface ByteConsumer {
      /**
       * Consumer is notified that the readable channel has bytes waiting to be read.
       */
      void onReady();

      /**
       * Consumer is notified that there are no more bytes, <code>onReady()</code> will
       * not be called anymore.
       */
      void onCompleted();

      /**
       * There was an exception during processing. Exceptions from <code>onReady()</code>
       * and <code>onCompleted()</code> are also delivered here.
       */
      void onException(Exception e);
   }

   interface ControllableReadableByteChannel extends ReadableByteChannel {
      /**
       * Pause delivering <code>onReady()</code> events. Consumer notifies the channel
       * that it has no capacity to consume data, therefore it should not call <code>onReady()</code>.
       */
      void pause();

      /**
       * Resume delivering <code>onReady()</code> events. Consumer notifies channel that
       * it is ready to accept bytes from the channel whenever the channel is ready.
       */
      void resume();
   }

}
