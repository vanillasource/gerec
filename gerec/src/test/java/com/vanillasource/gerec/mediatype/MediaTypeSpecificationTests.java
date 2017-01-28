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
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.http.ValueWithParameter;
import static com.vanillasource.gerec.mediatype.MediaTypeSpecification.*;
import static java.util.Arrays.asList;

@Test
public class MediaTypeSpecificationTests {
   private HttpRequest request;
   private HttpResponse response;

   public void testAcceptsTypeIfNonePresentYet() {
      MediaTypeSpecification type = mediaType("text/html", 1);

      type.addAsAcceptedTo(request);

      verify(request).setHeader(Headers.ACCEPT, asList(new ValueWithParameter("text/html", "q", "1")));
   }

   public void testAppendsAcceptTypeIfAlreadyPresent() {
      MediaTypeSpecification type = mediaType("text/html", 1);
      when(request.hasHeader(Headers.ACCEPT)).thenReturn(true);
      when(request.getHeader(Headers.ACCEPT)).thenReturn(asList(new ValueWithParameter("image/png")));

      type.addAsAcceptedTo(request);

      verify(request).setHeader(Headers.ACCEPT, asList(
               new ValueWithParameter("image/png"), new ValueWithParameter("text/html", "q", "1")));
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testDoesNotAllowZeroQuality() {
      mediaType("text/html", 0d);
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testDoesNotAllowMoreThanOneQuality() {
      mediaType("text/html", 1.0001d);
   }

   public void testQualityWillBeScaledToMaximumThreeDigits() {
      MediaTypeSpecification type = mediaType("text/html", 0.5012d);

      type.addAsAcceptedTo(request);

      verify(request).setHeader(Headers.ACCEPT, asList(new ValueWithParameter("text/html", "q", "0.502")));
   }

   public void testDoesNotHandleResponsesWithNoContentType() {
      MediaTypeSpecification type = mediaType("text/html");

      assertFalse(type.isIn(response));
   }

   public void testDoesNotHandleResponsesWithOtherContentType() {
      MediaTypeSpecification type = mediaType("text/html");
      when(response.hasHeader(Headers.CONTENT_TYPE)).thenReturn(true);
      when(response.getHeader(Headers.CONTENT_TYPE)).thenReturn(new ValueWithParameter("image/png"));

      assertFalse(type.isIn(response));
   }

   public void testDoesHandleResponsesWithSameContentType() {
      MediaTypeSpecification type = mediaType("text/html");
      when(response.hasHeader(Headers.CONTENT_TYPE)).thenReturn(true);
      when(response.getHeader(Headers.CONTENT_TYPE)).thenReturn(new ValueWithParameter("text/html"));

      assertTrue(type.isIn(response));
   }

   public void testWillSetContentType() {
      MediaTypeSpecification type = mediaType("text/html");

      type.addAsContentTo(request);

      verify(request).setHeader(Headers.CONTENT_TYPE, new ValueWithParameter("text/html"));
   }

   @BeforeMethod
   protected void setUp() {
      request = mock(HttpRequest.class);
      response = mock(HttpResponse.class);
   }
}

