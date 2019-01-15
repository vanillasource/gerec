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

import com.vanillasource.gerec.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Authorization request changes. Note: these only apply immediately to the
 * request and are not permanent.
 */
public class Authorization {
   private Authorization() {
   }

   /**
    * Adds the BASIC HTTP authorization credentials: username and password.
    */
   public static HttpRequest.HttpRequestChange basic(String username, String password) {
      return new SingleHeaderValueSet(Headers.AUTHORIZATION, "Basic "+Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8)));
   }

   /**
    * Adds a bearer token to the request.
    */
   public static HttpRequest.HttpRequestChange bearing(String token) {
      return new SingleHeaderValueSet(Headers.AUTHORIZATION, "Bearer "+token);
   }
}

