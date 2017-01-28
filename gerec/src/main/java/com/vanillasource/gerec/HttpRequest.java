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
import java.nio.channels.WritableByteChannel;

public interface HttpRequest {
   boolean hasHeader(Header<?> header);

   <T> T getHeader(Header<T> header);

   <T> void setHeader(Header<T> header, T value);

   void setByteProducer(Function<ControllableWritableByteChannel, ByteProducer> producerFactory);

   void setByteProducer(Function<ControllableWritableByteChannel, ByteProducer> producerFactory, long length);

   interface ByteProducer {
      /**
       * Producer is notified that channel is ready to accept more bytes.
       */
      void onReady();

      /**
       * Producer is notified that channel will not call <code>onReady()</code> anymore.
       */
      void onCompleted();
   }

   interface ControllableWritableByteChannel extends WritableByteChannel {
      /**
       * Pause delivering <code>onReady()</code> events. Producer notifies the channel
       * that there is no data to be written, therefore it should not call <code>onReady()</code>.
       */
      void pause();

      /**
       * Resume delivering <code>onReady()</code> events. Producer notifies channel that
       * it is ready to deliver bytes to the channel whenever the channel is ready.
       */
      void resume();
   }

   interface HttpRequestChange {
      HttpRequestChange NO_CHANGE = new HttpRequestChange() {
         @Override
         public void applyTo(HttpRequest request) {
         }
      };

      void applyTo(HttpRequest request);

      default HttpRequestChange and(HttpRequestChange that) {
         return request -> {
            this.applyTo(request);
            that.applyTo(request);
         };
      }
   }
}

