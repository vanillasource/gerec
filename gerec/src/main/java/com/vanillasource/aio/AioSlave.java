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

package com.vanillasource.aio;

/**
 * An AIO (Asynchronous IO) Slave is a peer in an AIO communication
 * which is driven by the Master to produce or consume data to- or from-
 * the Master.
 */
public interface AioSlave<T> {
   /**
    * Slave is notified by the leader to read or write data. I/O is
    * only allowed during this method call.
    */
   void onReady();

   /**
    * Called by the leader to indicate that the I/O conversation completed.
    * There will be no more calls to <code>onReady()</code>.
    * @return An object that is either the result of reading the data, or
    * an object indicating completion of writing all data.
    */
   T onCompleted();
}
