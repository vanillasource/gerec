/**
 * Copyright (C) 2020 VanillaSource
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

package com.vanillasource.gerec.mediatype;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public final class ShutdownTimer {
   private static final Logger logger = LoggerFactory.getLogger(ShutdownTimer.class);
   private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, runnable -> {
      Thread thread = Executors.defaultThreadFactory().newThread(runnable);
      thread.setDaemon(true);
      return thread;
   });
   private final Runnable action;
   private final long timeout;
   private ScheduledFuture<?> future;

   public ShutdownTimer(Runnable action, long timeout) {
      this.action = action;
      this.timeout = timeout;
      reset();
   }

   public void reset() {
      if (future != null) {
         future.cancel(false);
      }
      if (executor.isShutdown()) {
         return;
      }
      if (timeout != 0L) {
         logger.debug("shutdown timer reset to "+timeout+" ms from now");
         future = executor.schedule(this::execute, timeout, TimeUnit.MILLISECONDS);
      } else {
         logger.debug("shutdown timer disabled");
      }
   }

   private void execute() {
      logger.debug("shutdown timer expired, executing action");
      cancel();
      action.run();
   }

   public void cancel() {
      executor.shutdownNow();
   }
}
