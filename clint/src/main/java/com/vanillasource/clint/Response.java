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

package com.vanillasource.clint;

import java.util.List;

/**
 * A server response after calling a HTTP Method.
 */
public interface Response<T> {
   HttpStatusCode getStatusCode();

   /**
    * @return Whether the response content has a link with the given relation. The media-type
    * defines how links are parsed (if at all) in the given type.
    */
   boolean hasLink(String relationName);

   /**
    * Follow the link with the given relation.
    */
   ResourceReference follow(String relationName);

   /**
    * @return The updated reference to the returned state.
    */
   ResourceReference self();

   // How to process content: streams, http/2, multipart, but still easy
}

