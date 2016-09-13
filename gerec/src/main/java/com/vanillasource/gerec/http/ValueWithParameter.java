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

import java.util.Map;
import java.util.HashMap;

/**
 * A "standard" header value, which is a string with
 * one or more parameters associated. The parameters themselves
 * are composed of key-value pairs, some without explicit value.
 */
public final class ValueWithParameter {
   private String value;
   private Map<String, String> parameters;

   public ValueWithParameter(String value) {
      this(value, new HashMap<>());
   }

   public ValueWithParameter(String value, String parameterKey, String parameterValue) {
      this(value, parameters(parameterKey, parameterValue));
   }

   private static Map<String, String> parameters(String key, String value) {
      Map<String, String> parameters = new HashMap<>();
      parameters.put(key, value);
      return parameters;
   }

   public ValueWithParameter(String value, Map<String, String> parameters) {
      this.value = value;
      this.parameters = parameters;
   }

   public boolean matchesValue(String value) {
      return this.value.equalsIgnoreCase(value);
   }

   public boolean hasParameter(String parameterName) {
      return parameters.containsKey(parameterName.toLowerCase());
   }

   public String getParameterValue(String parameterName, String defaultValue) {
      String parameterValue = parameters.get(parameterName.toLowerCase());
      if (parameterValue == null) {
         return defaultValue;
      }
      return parameterValue;
   }

   @Override
   public String toString() {
      return FORMAT.serialize(this);
   }

   @Override
   public int hashCode() {
      return 11*value.hashCode() + 13*parameters.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if ((o == null) || (!(o instanceof ValueWithParameter))) {
         return false;
      }
      ValueWithParameter other = (ValueWithParameter) o;
      return other.value.equals(value) && other.parameters.equals(parameters);
   }

   /**
    * The standard header values with parameters are case-insensitive, in both the value
    * and the parameter keys. In addition both the value and the parameter values may be 
    * in quotes. The deserialized object has the value and parameter keys in lower case,
    * parameter values are left as-is.
    * Serialization does not produce quotes at the moment, so values which would require
    * quotes will not work.
    */
   public static final Headers.ValueFormat<ValueWithParameter> FORMAT = new Headers.ValueFormat<ValueWithParameter>() {
      @Override
      public ValueWithParameter deserialize(String headerValue) {
         String[] split = headerValue.split(";");
         String value = null;
         Map<String, String> parameters = new HashMap<>();
         for (int i=0; i<split.length; i++) {
            if (i == 0) {
               value = removeQuotes(split[i].trim().toLowerCase());
            } else {
               String[] keyValue = split[i].trim().split("=");
               if (keyValue.length == 1) {
                  parameters.put(keyValue[0].toLowerCase(), null);
               } else {
                  parameters.put(keyValue[0].toLowerCase(), removeQuotes(keyValue[1]));
               }
            }
         }
         return new ValueWithParameter(value, parameters);
      }

      private String removeQuotes(String value) {
         if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length()-1);
         } else {
            return value;
         }
      }

      @Override
      public String serialize(ValueWithParameter object) {
         StringBuilder builder = new StringBuilder();
         builder.append(object.value);
         for (Map.Entry<String, String> entry: object.parameters.entrySet()) {
            builder.append("; ");
            builder.append(entry.getKey());
            if (entry.getValue() != null) {
               builder.append("=");
               builder.append(entry.getValue());
            }
         }
         return builder.toString();
      }
   };
}

