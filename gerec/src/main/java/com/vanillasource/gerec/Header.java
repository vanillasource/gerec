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

public enum Header {
   ETAG("ETag"),
   DATE("Date"),
   CONTENT_TYPE("Content-Type"),
   ACCEPT("Accept"),
   IF_MATCH("If-Match"),
   IF_NONE_MATCH("If-None-Match"),
   IF_MODIFIED_SINCE("If-Modified-Since"),
   IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
   LAST_MODIFIED("Last-Modified"),
   LOCATION("Location");

   private final String value;

   private Header(String value) {
      this.value = value;
   }
   
   public String value() {
      return value;
   }
}
