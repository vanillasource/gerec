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
 * An AIO (Asynchronous-IO) Master represents a peer in an AIO communication
 * which drives the conversation. A leader may be either producing or consuming
 * data. Its key attribute is that it drives the conversation, essentially owns
 * the Thread the communication is executed in. The follower may ask the Master
 * to pause or resume the conversation.
 */
public interface AioMaster extends AutoCloseable {
   /**
    * Instructs the leader to temporarily pause the production or consumption
    * of data. No events will be delivered after this call.
    */
   void pause();

   /**
    * Instructs the leader to resume producing or consuming data.
    */
   void resume();

   /**
    * Instructs the leader to stop all I/O, and free all resources.
    */
   @Override
   void close();
}

