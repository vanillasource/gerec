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

/**
 * The part of a media-type for sending the given type. This includes adding the necessary
 * modifications to the request, and serializing the object to the request.
 */
public interface ContentMediaType<T> {
   /**
    * Apply this media type as being the content's media type for the given request.
    */
   void applyAsContent(HttpRequest request);

   /**
    * Serialize the given object to the request.
    */
   void serialize(T object, HttpRequest request);
}
