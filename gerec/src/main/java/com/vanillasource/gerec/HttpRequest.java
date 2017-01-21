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

   void setByteProducer(Function<WritableByteChannel, ByteProducer> producerFactory);

   void setByteProducer(Function<WritableByteChannel, ByteProducer> producerFactory, long length);

   public interface ByteProducer {
      /**
       * Producer is notified that channel is ready to accept more bytes.
       */
      void onReady();

      void onCompleted();
   }

   public interface HttpRequestChange {
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

