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
import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

@Test
public class MultiValueHeaderAddTests {
   private HttpRequest request;

   public void testHeaderValueIsSetIfNotPresent() {
      MultiValueHeaderAdd value = new MultiValueHeaderAdd(Headers.CACHE_CONTROL, "123");

      value.applyTo(request);

      verify(request).setHeader(Headers.CACHE_CONTROL, asList("123"));
   }

   public void testHeaderValueWillBeCommaSeparatedIfAlreadyPresent() {
      MultiValueHeaderAdd value = new MultiValueHeaderAdd(Headers.CACHE_CONTROL, "123");
      when(request.hasHeader(Headers.CACHE_CONTROL)).thenReturn(true);
      when(request.getHeader(Headers.CACHE_CONTROL)).thenReturn(asList("abc"));

      value.applyTo(request);

      verify(request).setHeader(Headers.CACHE_CONTROL, asList("abc", "123"));
   }

   @BeforeMethod
   protected void setUp() {
      request = mock(HttpRequest.class);
   }
}

