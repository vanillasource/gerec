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

package com.vanillasource.gerec;

import java.util.concurrent.CompletableFuture;

/**
 * A request that is already assembled with the destination URI, possible
 * changes to the <code>HttpRequest</code>.
 */
public interface Request {
   <T> CompletableFuture<ContentResponse<T>> send(AcceptMediaType<T> acceptType, HttpRequest.HttpRequestChange change);

   default <T> CompletableFuture<ContentResponse<T>> send(AcceptMediaType<T> acceptType) {
      return send(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);
   }

   default CompletableFuture<Response> send() {
      return send(MediaType.NONE, HttpRequest.HttpRequestChange.NO_CHANGE)
         .thenApply(response -> response);
   }

   /**
    * Suspend this reqest into a serialized form.
    */
   byte[] suspend();
}

