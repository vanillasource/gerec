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
   public static final Header<List<String>> ALLOW = csvStringHeader("Allow");
   public static final Header<ValueWithParameter> CONTENT_TYPE = singleValueHeader("Content-Type", ValueWithParameter.FORMAT);
   public static final Header<List<ValueWithParameter>> ACCEPT = csvValueHeader("Accept", ValueWithParameter.FORMAT);
   public static final Header<String> IF_MATCH = singleStringHeader("If-Match");
   public static final Header<String> IF_NONE_MATCH = singleStringHeader("If-None-Match");
   public static final Header<String> IF_MODIFIED_SINCE = singleStringHeader("If-Modified-Since");
   public static final Header<String> IF_UNMODIFIED_SINCE = singleStringHeader("If-Unmodified-Since");
   public static final Header<String> LAST_MODIFIED = singleStringHeader("Last-Modified");
   public static final Header<String> LOCATION = singleStringHeader("Location");
   public static final Header<Long> CONTENT_LENGTH = singleLongHeader("Content-Length");

   private Headers() {
   }

   public static Header<String> singleStringHeader(String name) {
      return singleValueHeader(name, new StringFormat());
   }

   public static Header<Long> singleLongHeader(String name) {
      return singleValueHeader(name, new LongFormat());
   }

   public static <T> Header<T> singleValueHeader(String name, ValueFormat<T> format) {
      return new Header<T>() {
         @Override
         public String getName() {
            return name;
         }

         @Override
         public T deserialize(List<String> headerValues) {
            if (headerValues.size() != 1) {
               throw new IllegalArgumentException("header values were: "+headerValues+", but expecting a single one for header: "+name);
            }
            return format.deserialize(headerValues.get(0));
         }

         @Override
         public List<String> serialize(T object) {
            return Collections.singletonList(format.serialize(object));
         }
      };
   }

   public static Header<List<String>> csvStringHeader(String name) {
      return csvValueHeader(name, new StringFormat());
   }

   public static <T> Header<List<T>> csvValueHeader(String name, ValueFormat<T> format) {
      return new Header<List<T>>() {
         @Override
         public String getName() {
            return name;
         }

         @Override
         public List<T> deserialize(List<String> headerValues) {
            List<T> values = new LinkedList<>();
            for (String headerValue: headerValues) {
               for (String value: headerValue.split(",")) {
                  values.add(format.deserialize(value.trim()));
               }
            }
            return values;
         }

         @Override
         public List<String> serialize(List<T> value) {
            return Collections.singletonList(value.stream().map(format::serialize).collect(Collectors.joining(", ")));
         }
      };
   }

   public interface ValueFormat<T> {
      T deserialize(String value);

      String serialize(T object);
   }

   public static class StringFormat implements ValueFormat<String> { 
      @Override
      public String deserialize(String value) {
         return value;
      }

      @Override
      public String serialize(String object) {
         return object;
      }
   }

   public static class LongFormat implements ValueFormat<Long> { 
      @Override
      public Long deserialize(String value) {
         return Long.valueOf(value);
      }

      @Override
      public String serialize(Long object) {
         return object.toString();
      }
   }
}
