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

import java.util.concurrent.CompletableFuture;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.Request;
import com.vanillasource.gerec.mediatype.PolymorphicAcceptType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Define navigation to reach a certain result or overall processing state. 
 * With this object you can completely
 * dissociate the handling of the known media types from the actual URIs
 * you expect these media types to appear.
 * <p>
 * Instead, define a Navigation for each <i>intent</i> or <i>use-case</i>
 * using individual decisions for all media types, and this object
 * takes case of navigating until the intent
 * is fulfilled.
 */
public final class Navigation<R> {
   private final ResourceReference startReference;
   private final List<AcceptMediaType<Function<NavigationContext<R>, CompletableFuture<Optional<R>>>>> types;

   public Navigation(ResourceReference startReference) {
      this.startReference = startReference;
      this.types = new ArrayList<>();
   }

   public <T> Navigation<R> navigate(AcceptMediaType<T> type, BiFunction<T, NavigationContext<R>, CompletableFuture<Optional<R>>> decision) {
      types.add(type.map(value -> context -> decision.apply(value, context)));
      return this;
   }

   public CompletableFuture<Optional<R>> execute() {
      AcceptMediaType<Function<NavigationContext<R>, CompletableFuture<Optional<R>>>> acceptType =
         new PolymorphicAcceptType<>(types);
      NavigationContext<R> context = new NavigationContext<>(acceptType);
      return startReference.get(acceptType)
         .thenCompose(rule -> rule.apply(context));
   }
}

