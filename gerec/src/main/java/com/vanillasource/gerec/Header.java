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

import java.util.List;

/**
 * Represents a logical header. This is different from a physical header in that
 * it contains all values to the header "name", even if those are read from multiple
 * header lines.
 * @param T The type of value expected in the header.
 */
public interface Header<T> {
   /**
    * Get the name/key of this header. This is the "field-name" of the header. This value
    * will be used case-insensitively.
    */
   String getName();

   /**
    * Deserialize multiple physical header lines into a single value.
    */
   T deserialize(List<String> headerValues);

   /**
    * Serialize the given value to possibly multiple header values.
    */
   List<String> serialize(T value);
}

