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
import com.vanillasource.gerec.Header;

/**
 * Cache control directives which can be combined and attached to requests to
 * control privacy, freshness or staleness of responses.
 */
public class CacheControl {
   private CacheControl() {
   }

   /**
    * Specifies that the client is unwilling to accept a response that has an 'age'
    * of more than the given amount.
    */
   public static HttpRequest.HttpRequestChange maxAge(int seconds) {
      return new CommaSeparatedHeaderValue(Header.CACHE_CONTROL, "max-age="+seconds);
   }

   /**
    * Specifies that the client is willing to accept a response which is stale,
    * but only the given seconds at most.
    */
   public static HttpRequest.HttpRequestChange maxStale(int seconds) {
      return new CommaSeparatedHeaderValue(Header.CACHE_CONTROL, "max-stale="+seconds);
   }

   /**
    * Specifies that the client is only willing to accept a response that is still
    * fresh for the given amount of seconds.
    */
   public static HttpRequest.HttpRequestChange minFresh(int seconds) {
      return new CommaSeparatedHeaderValue(Header.CACHE_CONTROL, "min-fresh="+seconds);
   }

   /**
    * Specifies that the client is only willing to accept validated responses. That is,
    * either straight from the server, or if the responding cache first validates with
    * the server.
    */
   public static HttpRequest.HttpRequestChange noCache() {
      return new CommaSeparatedHeaderValue(Header.CACHE_CONTROL, "no-cache");
   }

   /**
    * Specifies that request and response should not be stored by intermediate caches. Has no effect if
    * response is already cached.
    */
   public static HttpRequest.HttpRequestChange noStore() {
      return new CommaSeparatedHeaderValue(Header.CACHE_CONTROL, "no-store");
   }

   public static HttpRequest.HttpRequestChange noTransform() {
      return new CommaSeparatedHeaderValue(Header.CACHE_CONTROL, "no-transform");
   }

   /**
    * Specifies that the client is only interested in cached responses. The request will fail
    * if there is no cached response.
    */
   public static HttpRequest.HttpRequestChange onlyIfCached() {
      return new CommaSeparatedHeaderValue(Header.CACHE_CONTROL, "only-if-cached");
   }
}

