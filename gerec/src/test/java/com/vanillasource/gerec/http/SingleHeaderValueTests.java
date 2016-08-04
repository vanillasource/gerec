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
import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

@Test
public class SingleHeaderValueTests {
   private HttpRequest request;

   public void testHeaderValueIsSetIfNotPresent() {
      SingleHeaderValue value = new SingleHeaderValue(Header.ETAG, "123");

      value.applyTo(request);

      verify(request).setHeader(Header.ETAG, "123");
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testThrowsExceptionIfAlreadyExists() {
      SingleHeaderValue value = new SingleHeaderValue(Header.ETAG, "123");
      when(request.hasHeader(Header.ETAG)).thenReturn(true);

      value.applyTo(request);
   }

   @BeforeMethod
   protected void setUp() {
      request = mock(HttpRequest.class);
   }
}

