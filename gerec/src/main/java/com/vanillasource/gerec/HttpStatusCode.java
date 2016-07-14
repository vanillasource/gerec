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

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

/**
 * A list of HTTP status codes. This is not an enum, because there might be extensions to HTTP at any later point
 * in time, which should not break any code using this quasi-enumeration. The operator '==' and switch statements
 * work with these objects.
 */
public final class HttpStatusCode implements Serializable {
   private static Map<Integer, HttpStatusCode> CODES = new HashMap<>();

   // --- 1xx Informational ---

   public static final HttpStatusCode CONTINUE = item(100);
   public static final HttpStatusCode SWITCHING_PROTOCOLS = item(101);
   public static final HttpStatusCode PROCESSING = item(102);

   // --- 2xx Success ---

   public static final HttpStatusCode OK = item(200);
   public static final HttpStatusCode CREATED = item(201);
   public static final HttpStatusCode ACCEPTED = item(202);
   public static final HttpStatusCode NON_AUTHORITATIVE_INFORMATION = item(203);
   public static final HttpStatusCode NO_CONTENT = item(204);
   public static final HttpStatusCode RESET_CONTENT = item(205);
   public static final HttpStatusCode PARTIAL_CONTENT = item(206);
   public static final HttpStatusCode MULTI_STATUS = item(207);

   // --- 3xx Redirection ---

   public static final HttpStatusCode MULTIPLE_CHOICES = item(300);
   public static final HttpStatusCode MOVED_PERMANENTLY = item(301);
   public static final HttpStatusCode MOVED_TEMPORARILY = item(302);
   public static final HttpStatusCode SEE_OTHER = item(303);
   public static final HttpStatusCode NOT_MODIFIED = item(304);
   public static final HttpStatusCode USE_PROXY = item(305);
   public static final HttpStatusCode TEMPORARY_REDIRECT = item(307);

   // --- 4xx Client Error ---

   public static final HttpStatusCode BAD_REQUEST = item(400);
   public static final HttpStatusCode UNAUTHORIZED = item(401);
   public static final HttpStatusCode PAYMENT_REQUIRED = item(402);
   public static final HttpStatusCode FORBIDDEN = item(403);
   public static final HttpStatusCode NOT_FOUND = item(404);
   public static final HttpStatusCode METHOD_NOT_ALLOWED = item(405);
   public static final HttpStatusCode NOT_ACCEPTABLE = item(406);
   public static final HttpStatusCode PROXY_AUTHENTICATION_REQUIRED = item(407);
   public static final HttpStatusCode REQUEST_TIMEOUT = item(408);
   public static final HttpStatusCode CONFLICT = item(409);
   public static final HttpStatusCode GONE = item(410);
   public static final HttpStatusCode LENGTH_REQUIRED = item(411);
   public static final HttpStatusCode PRECONDITION_FAILED = item(412);
   public static final HttpStatusCode REQUEST_TOO_LONG = item(413);
   public static final HttpStatusCode REQUEST_URI_TOO_LONG = item(414);
   public static final HttpStatusCode UNSUPPORTED_MEDIA_TYPE = item(415);
   public static final HttpStatusCode REQUESTED_RANGE_NOT_SATISFIABLE = item(416);
   public static final HttpStatusCode EXPECTATION_FAILED = item(417);
   public static final HttpStatusCode INSUFFICIENT_SPACE_ON_RESOURCE = item(419);
   public static final HttpStatusCode METHOD_FAILURE = item(420);
   public static final HttpStatusCode UNPROCESSABLE_ENTITY = item(422);
   public static final HttpStatusCode LOCKED = item(423);
   public static final HttpStatusCode FAILED_DEPENDENCY = item(424);

   // --- 5xx Server Error ---

   public static final HttpStatusCode INTERNAL_SERVER_ERROR = item(500);
   public static final HttpStatusCode NOT_IMPLEMENTED = item(501);
   public static final HttpStatusCode BAD_GATEWAY = item(502);
   public static final HttpStatusCode SERVICE_UNAVAILABLE = item(503);
   public static final HttpStatusCode GATEWAY_TIMEOUT = item(504);
   public static final HttpStatusCode HTTP_VERSION_NOT_SUPPORTED = item(505);
   public static final HttpStatusCode INSUFFICIENT_STORAGE = item(507);

   private static HttpStatusCode item(int value) {
      HttpStatusCode code = new HttpStatusCode(value);
      CODES.put(value, code);
      return code;
   }

   public static HttpStatusCode valueOf(int value) {
      HttpStatusCode code = CODES.get(value);
      if (code == null) {
         code = new HttpStatusCode(value);
      }
      return code;
   }

   private int value;

   private HttpStatusCode(int value) {
      this.value = value;
   }

   public int value() {
      return value;
   }

   @Override
   public int hashCode() {
      return value;
   }

   @Override
   public boolean equals(Object o) {
      if ((o==null)||(!(o instanceof HttpStatusCode))) {
         return false;
      }
      return value == ((HttpStatusCode) o).value;
   }
}
