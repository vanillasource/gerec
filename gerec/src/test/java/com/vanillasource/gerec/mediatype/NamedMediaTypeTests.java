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

import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import java.util.function.Function;
import java.net.URI;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.Header;

@Test
public class NamedMediaTypeTests {
   private HttpRequest request;
   private HttpResponse response;

   public void testAcceptsTypeIfNonePresentYet() {
      TestType type = new TestType("text/html");

      type.getAcceptType().applyTo(request);

      verify(request).setHeader(Header.ACCEPT, "text/html;q=1");
   }

   public void testAppendsAcceptTypeIfAlreadyPresent() {
      TestType type = new TestType("text/html");
      when(request.hasHeader(Header.ACCEPT)).thenReturn(true);
      when(request.getHeader(Header.ACCEPT)).thenReturn("image/png");

      type.getAcceptType().applyTo(request);

      verify(request).setHeader(Header.ACCEPT, "image/png, text/html;q=1");
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testDoesNotAllowZeroQuality() {
      new TestType("text/html", 0d);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testDoesNotAllowMoreThanOneQuality() {
      new TestType("text/html", 1.0001d);
   }

   public void testQualityWillBeScaledToMaximumThreeDigits() {
      TestType type = new TestType("text/html", 0.5012d);

      type.getAcceptType().applyTo(request);

      verify(request).setHeader(Header.ACCEPT, "text/html;q=0.502");
   }

   public void testDoesNotHandleResponsesWithNoContentType() {
      TestType type = new TestType("text/html");

      assertFalse(type.getAcceptType().isHandling(response));
   }

   public void testDoesNotHandleResponsesWithOtherContentType() {
      TestType type = new TestType("text/html");
      when(response.hasHeader(Header.CONTENT_TYPE)).thenReturn(true);
      when(response.getHeader(Header.CONTENT_TYPE)).thenReturn("image/png");

      assertFalse(type.getAcceptType().isHandling(response));
   }

   public void testDoesHandleResponsesWithSameContentType() {
      TestType type = new TestType("text/html");
      when(response.hasHeader(Header.CONTENT_TYPE)).thenReturn(true);
      when(response.getHeader(Header.CONTENT_TYPE)).thenReturn("text/html");

      assertTrue(type.getAcceptType().isHandling(response));
   }

   public void testWillSetContentType() {
      TestType type = new TestType("text/html");

      type.getContentType().applyTo(request);

      verify(request).setHeader(Header.CONTENT_TYPE, "text/html");
   }

   @BeforeMethod
   protected void setUp() {
      request = mock(HttpRequest.class);
      response = mock(HttpResponse.class);
   }

   public class TestType extends NamedMediaType<String> {
      public TestType(String name) {
         super(name);
      }

      public TestType(String name, double quality) {
         super(name, quality);
      }

      @Override
      protected String deserialize(HttpResponse response, Function<URI, ResourceReference> referenceProducer) {
         return null;
      }

      @Override
      protected void serialize(String object, HttpRequest request) {
      }
   }
}

