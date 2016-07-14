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

import java.io.Serializable;

/**
 * The hypermedia aspects of a certain representation. The returned content may or may not
 * be equal to the full representation, it may have the links stripped out for example.
 */
public interface Hypermedia<T> extends Serializable {
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
    * Get the content.
    */
   T getContent();
}
