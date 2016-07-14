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
import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static java.util.Collections.*;

@Test
public class MediaTypesTests {
   private MediaType<String> mediaType;
   private HttpResponse response = mock(HttpResponse.class);
   private HttpRequest request = mock(HttpRequest.class);

   public void testNoTypesDontApply() {
      MediaTypes<String> types = new MediaTypes<>(emptyList());

      types.applyTo(request);
   }

   public void testAllTypesAreApplied() {
      MediaTypes<String> types = new MediaTypes<>(singletonList(mediaType));

      types.applyTo(request);

      verify(mediaType).applyTo(request);
   }

   public void testNoTypesDontHandleResponse() {
      MediaTypes<String> types = new MediaTypes<>(emptyList());

      assertFalse(types.isHandling(response));
   }

   public void testTypesDoesNotHandleResponseIfTypeIsNotHandling() {
      MediaTypes<String> types = new MediaTypes<>(singletonList(mediaType));

      assertFalse(types.isHandling(response));
   }

   public void testTypesDoHandleResponseIfTypeDoes() {
      MediaTypes<String> types = new MediaTypes<>(singletonList(mediaType));
      when(mediaType.isHandling(response)).thenReturn(true);

      assertTrue(types.isHandling(response));
   }

   @Test(expectedExceptions = GerecException.class)
   public void testNoTypesThrowsExceptionOnDeserialize() {
      MediaTypes<String> types = new MediaTypes<>(emptyList());

      types.deserialize(response);
   }

   @Test(expectedExceptions = GerecException.class)
   public void testIfTypeNotHandlingResponseDeserializationThrowsException() {
      MediaTypes<String> types = new MediaTypes<>(singletonList(mediaType));

      types.deserialize(response);
   }

   @SuppressWarnings("unchecked")
   public void testHandlingTypeDeserializesForTypes() {
      MediaTypes<String> types = new MediaTypes<>(singletonList(mediaType));
      when(mediaType.isHandling(response)).thenReturn(true);
      Hypermedia<String> media = mock(Hypermedia.class);
      when(mediaType.deserialize(response)).thenReturn(media);

      assertEquals(types.deserialize(response), media);
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   public void setUp() {
      mediaType = mock(MediaType.class);
   }
}

