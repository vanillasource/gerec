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
public class ValueWithParameter {
   private String value;
   private Map<String, String> parameters;

   public ValueWithParameter(String value, Map<String, String> parameters) {
      this.value = value;
      this.parameters = parameters;
   }

   public String getValue() {
      return value;
   }

   public Map<String, String> getParameters() {
      return parameters;
   }

   public boolean hasParameter(String parameterName) {
      return parameters.containsKey(parameterName);
   }

   public String getParameterValue(String parameterName) {
      return parameters.get(parameterName);
   }
}

