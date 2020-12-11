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

package com.vanillasource.gerec.navigation;

import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.Request;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;
import java.util.function.Function;
import java.util.List;

public final class NavigationContext<R> {
   private final AcceptMediaType<Function<NavigationContext<R>, CompletableFuture<Optional<R>>>> acceptType;

   public NavigationContext(AcceptMediaType<Function<NavigationContext<R>, CompletableFuture<Optional<R>>>> acceptType) {
      this.acceptType = acceptType;
   }

   public CompletableFuture<Optional<R>> back() {
      return CompletableFuture.completedFuture(Optional.empty());
   }

   public CompletableFuture<Optional<R>> finish(R value) {
      return CompletableFuture.completedFuture(Optional.of(value));
   }

   public CompletableFuture<Optional<R>> follow(Request request) {
      return request.send(acceptType)
         .thenCompose(rule -> rule.apply(this));
   }

   public CompletableFuture<Optional<R>> first(List<Request> requests) {
      CompletableFuture<Optional<R>> result = back();
      for (Request request: requests) {
         result = result.thenCompose(value -> {
            if (value.isPresent()) {
               return finish(value.get());
            } else {
               return follow(request);
            }
         });
      }
      return result;
   }
}
