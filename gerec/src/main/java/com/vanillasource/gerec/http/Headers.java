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

package com.vanillasource.gerec.http;

import com.vanillasource.gerec.Header;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * List of HTTP header names as used in requests and responses.
 */
public class Headers {
   public static final Header<List<String>> CACHE_CONTROL = csvStringHeader("Cache-Control");
   public static final Header<String> ETAG = singleStringHeader("ETag");
   public static final Header<String> DATE = singleStringHeader("Date");
   public static final Header<String> ALLOW = singleStringHeader("Allow");
   public static final Header<String> CONTENT_TYPE = singleStringHeader("Content-Type");
   public static final Header<List<String>> ACCEPT = csvStringHeader("Accept");
   public static final Header<String> IF_MATCH = singleStringHeader("If-Match");
   public static final Header<String> IF_NONE_MATCH = singleStringHeader("If-None-Match");
   public static final Header<String> IF_MODIFIED_SINCE = singleStringHeader("If-Modified-Since");
   public static final Header<String> IF_UNMODIFIED_SINCE = singleStringHeader("If-Unmodified-Since");
   public static final Header<String> LAST_MODIFIED = singleStringHeader("Last-Modified");
   public static final Header<String> LOCATION = singleStringHeader("Location");

   private Headers() {
   }

   public static Header<String> singleStringHeader(String name) {
      return new Header<String>() {
         @Override
         public String getName() {
            return name;
         }

         @Override
         public String deserialize(List<String> headerValues) {
            if (headerValues.size() != 1) {
               throw new IllegalStateException("header values were: "+headerValues+", but expecting a single one for header: "+name);
            }
            return headerValues.get(0);
         }

         @Override
         public List<String> serialize(String value) {
            return Collections.singletonList(value);
         }
      };
   }

   public static Header<List<String>> csvStringHeader(String name) {
      return new Header<List<String>>() {
         @Override
         public String getName() {
            return name;
         }

         @Override
         public List<String> deserialize(List<String> headerValues) {
            List<String> values = new LinkedList<>();
            for (String headerValue: headerValues) {
               for (String value: headerValue.split(",")) {
                  values.add(value.trim());
               }
            }
            return values;
         }

         @Override
         public List<String> serialize(List<String> value) {
            return Collections.singletonList(value.stream().collect(Collectors.joining(", ")));
         }
      };
   }
}
