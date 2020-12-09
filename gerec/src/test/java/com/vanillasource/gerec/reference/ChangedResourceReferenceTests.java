/**
 * Copyright (C) 2019 VanillaSource
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

package com.vanillasource.gerec.reference;

import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.http.Authorization;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.http.Headers;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.net.URI;

@Test
public final class ChangedResourceReferenceTests {
   private ResourceReference reference;
   private ChangedResourceReference changedReference;
   private HttpRequest request;
   private AcceptMediaType<String> acceptType;

   public void testAppliesChangeToCurrentReference() {
      changedReference.getResponse(acceptType, HttpRequest.HttpRequestChange.NO_CHANGE);

      verify(request).setHeader(Headers.AUTHORIZATION, "Bearer token");
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      acceptType = mock(AcceptMediaType.class);
      request = mock(HttpRequest.class);
      reference = mock(ResourceReference.class);
      DeserializationContext context = mock(DeserializationContext.class);
      when(context.resolve(any())).thenReturn(reference);
      when(reference.getResponse(any(), any())).thenAnswer(invocation -> {
         ((HttpRequest.HttpRequestChange) invocation.getArguments()[1]).applyTo(request);
         AcceptMediaType<String> acceptType = (AcceptMediaType<String>) invocation.getArguments()[0];
         acceptType.deserialize(mock(HttpResponse.class), context);
         return null; // TODO
      });
      changedReference = new ChangedResourceReference(reference, Authorization.bearing("token"));
   }
}

