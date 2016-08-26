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
import java.net.URI;

/**
 * The part of a media-type for receiving the given type. This includes adding the necessary
 * modifications to a request, as parsing it in the response.
 */
public interface AcceptMediaType<T> {
   /**
    * Apply this media-type as a possible option for a server to respond with. Note: the server may still
    * decide to answer with a different media-type.
    */
   void applyAsOption(HttpRequest request);

   /**
    * Determine whether the given response contains this accepted media type.
    */
   boolean isHandling(HttpResponse response);

   /**
    * Deserialize the contents of the response to the given object type.
    * @param referenceProducer Used by the deserialization process to resolve potentially relative URIs
    * to full resource references.
    */
   T deserialize(HttpResponse response, Function<URI, ResourceReference> referenceProducer);
}
