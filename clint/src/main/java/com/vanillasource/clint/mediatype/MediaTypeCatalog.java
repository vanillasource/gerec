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

package com.vanillasource.clint.mediatype;

import java.util.Collection;

/**
 * A catalog of all known media-types.
 */
public interface MediaTypeCatalog {
   /**
    * Get all matching media-types for the given class. There may be multiple media-types for
    * each class, for example a json and an xml representation, or multiple versions of the same.
    */
   <T> Collection<MediaType<T>> getMediaTypesFor(Class<T> type);
}

