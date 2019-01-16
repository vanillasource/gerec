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

package com.vanillasource.gerec.reference;

import java.util.function.Predicate;
import java.util.Optional;
import java.net.URI;
import java.net.InetAddress;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class UriPredicates {
   private UriPredicates() {
   }

   /**
    * Always push permanent changes to the next request.
    */
   public static Predicate<URI> always() {
      return uri -> true;
   }

   /**
    * Never push permanent changes to the next request.
    */
   public static Predicate<URI> never() {
      return uri -> false;
   }

   /**
    * Push permanent changes only if the request domain ends with the specified
    * exact partial hostname.
    */
   public static Predicate<URI> endsWith(String partialHost) {
      return uri -> uri.getHost().endsWith(partialHost);
   }

   /**
    * Push permanent changes only if next request is the same domain as given URI.
    * @param prototype The uri containing the hostname as prototype. Note: it is assumed that
    * the host in the uri is given as a name not an IP address.
    */
   public static Predicate<URI> sameSecondLevelDomain(URI prototype) {
      return extractSecondLevelDomain(prototype)
         .map(UriPredicates::endsWith)
         .orElse(never());
   }

   private static Optional<String> extractSecondLevelDomain(URI prototype) {
      String host = prototype.getHost();
      int lastDot = host.lastIndexOf('.');
      if (lastDot <= 0) {
         return Optional.empty();
      }
      int secondLastDot = host.lastIndexOf('.', lastDot-1);
      if (secondLastDot <= 0) {
         return Optional.empty();
      }
      return Optional.of(host.substring(secondLastDot));
   }

   /**
    * Push permanent changes only if next request is localhost.
    */
   public static Predicate<URI> localhost() {
      return uri -> {
         try {
            return InetAddress.getByName(uri.getHost()).isLoopbackAddress();
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      };
   }
}
