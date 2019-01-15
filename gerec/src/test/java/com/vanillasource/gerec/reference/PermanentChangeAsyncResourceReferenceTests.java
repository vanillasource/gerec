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
import com.vanillasource.gerec.AsyncResourceReference;
import com.vanillasource.gerec.http.Authorization;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.http.Headers;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.net.URI;

@Test
public final class PermanentChangeAsyncResourceReferenceTests {
   private AsyncResourceReference reference;
   private PermanentChangeAsyncResourceReference changedReference;
   private Predicate<URI> predicate;
   private HttpRequest request;
   private AcceptMediaType<String> acceptType;
   private AsyncResourceReference nextReference;

   public void testAppliesChangeToCurrentReference() {
      changedReference.get(acceptType);

      verify(request).setHeader(Headers.AUTHORIZATION, "Bearer token");
   }

   public void testAppliesToNextRequestIfPredicateHolds() {
      when(predicate.test(any())).thenReturn(true);
      changedReference.get(new AcceptMediaType<String>() {
         @Override
         public void applyAsOption(HttpRequest request) {
         }

         @Override
         public boolean isHandling(HttpResponse response) {
            return true;
         }

         @Override
         public CompletableFuture<String> deserialize(HttpResponse response, DeserializationContext context) {
            try {
               nextReference = context.resolve(new URI("test"));
            } catch (Exception e) {
            }
            return CompletableFuture.completedFuture(null);
         }
      });

      nextReference.get(acceptType);

      verify(request, times(2)).setHeader(Headers.AUTHORIZATION, "Bearer token");
   }

   public void testDoesNotApplyToNextRequestIfPredicateDoesNotHold() {
      changedReference.get(new AcceptMediaType<String>() {
         @Override
         public void applyAsOption(HttpRequest request) {
         }

         @Override
         public boolean isHandling(HttpResponse response) {
            return true;
         }

         @Override
         public CompletableFuture<String> deserialize(HttpResponse response, DeserializationContext context) {
            try {
               nextReference = context.resolve(new URI("test"));
            } catch (Exception e) {
            }
            return CompletableFuture.completedFuture(null);
         }
      });

      nextReference.get(acceptType);

      verify(request).setHeader(Headers.AUTHORIZATION, "Bearer token");
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      acceptType = mock(AcceptMediaType.class);
      request = mock(HttpRequest.class);
      reference = mock(AsyncResourceReference.class);
      predicate = mock(Predicate.class);
      DeserializationContext context = mock(DeserializationContext.class);
      when(context.resolve(any())).thenReturn(reference);
      when(reference.get(any(), any())).thenAnswer(invocation -> {
         ((HttpRequest.HttpRequestChange) invocation.getArguments()[1]).applyTo(request);
         AcceptMediaType<String> acceptType = (AcceptMediaType<String>) invocation.getArguments()[0];
         acceptType.deserialize(mock(HttpResponse.class), context);
         return null; // TODO
      });
      changedReference = new PermanentChangeAsyncResourceReference(reference,
            predicate, Authorization.bearing("token"));
   }
}

