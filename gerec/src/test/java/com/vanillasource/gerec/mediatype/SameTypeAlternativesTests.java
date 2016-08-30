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

package com.vanillasource.gerec.mediatype;

import com.vanillasource.gerec.*;
import com.vanillasource.gerec.http.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static java.util.Collections.*;

@Test
public class SameTypeAlternativesTests {
   private MediaType<String> mediaType;
   private HttpResponse response = mock(HttpResponse.class);
   private HttpRequest request = mock(HttpRequest.class);

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testNoTypesThrowException() {
      new SameTypeAlternatives<>(emptyList());
   }

   public void testAllTypesAreApplied() {
      SameTypeAlternatives<String> types = new SameTypeAlternatives<>(singletonList(mediaType));

      types.applyAsOption(request);

      verify(mediaType).applyAsOption(request);
   }

   public void testTypesDoesNotHandleResponseIfTypeIsNotHandling() {
      SameTypeAlternatives<String> types = new SameTypeAlternatives<>(singletonList(mediaType));

      assertFalse(types.isHandling(response));
   }

   public void testTypesDoHandleResponseIfTypeDoes() {
      SameTypeAlternatives<String> types = new SameTypeAlternatives<>(singletonList(mediaType));
      when(mediaType.isHandling(response)).thenReturn(true);

      assertTrue(types.isHandling(response));
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testIfTypeNotHandlingResponseDeserializationThrowsException() {
      SameTypeAlternatives<String> types = new SameTypeAlternatives<>(singletonList(mediaType));

      types.deserialize(response, null);
   }

   @SuppressWarnings("unchecked")
   public void testHandlingTypeDeserializesForTypes() {
      SameTypeAlternatives<String> types = new SameTypeAlternatives<>(singletonList(mediaType));
      when(mediaType.isHandling(response)).thenReturn(true);
      when(mediaType.deserialize(response, null)).thenReturn("abc");

      assertEquals(types.deserialize(response, null), "abc");
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   public void setUp() {
      mediaType = mock(MediaType.class);
   }
}

