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

import java.util.concurrent.CompletableFuture;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.AioMaster;
import java.util.concurrent.Executor;

/**
 * A master that is appropriate for passive implementations that do not have control
 * or backpressure functionality on their own.
 */
public abstract class PassiveAioMaster implements AioMaster {
   private volatile boolean closed = false; // Initially we don't know, so assume false
   private boolean paused = false;
   private Object pauseMutex = new Object();

   /**
    * Execute a given slave against this master. This will potentially spawn another thread
    * to fulfill the request. This method is guaranteed not to block.
    */
   public <T> CompletableFuture<T> execute(AioSlave<T> slave, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            while (!closed) {
               synchronized (pauseMutex) {
                  while (paused) {
                     pauseMutex.wait();
                  }
               }
               slave.onReady();
            }
            return slave.onCompleted();
         } catch (InterruptedException e) {
            throw new IllegalStateException("master interrupted", e);
         }
      }, executor);
   }

   public boolean isOpen() {
      return !closed;
   }

   @Override
   public void close() {
      closed = true;
   }

   @Override
   public void pause() {
      synchronized (pauseMutex) {
         paused = true;
      }
   }

   @Override
   public void resume() {
      synchronized (pauseMutex) {
         paused = false;
         pauseMutex.notify();
      }
   }
}

