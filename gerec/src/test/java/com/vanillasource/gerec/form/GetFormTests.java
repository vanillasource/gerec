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

package com.vanillasource.gerec.form;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.Request;
import java.util.function.Function;
import java.net.URI;
import com.vanillasource.gerec.ContentResponse;
import java.util.concurrent.CompletableFuture;

@Test
public class GetFormTests {
   private ResourceReference resolvedReference;
   private Function<URI, ResourceReference> referenceResolver;

   public void testGetFormMakesAGetRequestToResolvedReference() {
      GetForm form = new GetForm(URI.create("/root"), referenceResolver);

      form.submit(null);

      verify(resolvedReference).prepareGet(any(HttpRequest.HttpRequestChange.class));
   }

   @SuppressWarnings("unchecked")
   public void testGetFormPutsFormValuesIntoQueryParameters() {
      GetForm form = new GetForm(URI.create("/root"), referenceResolver);

      form
         .put("q", "search")
         .submit(null);

      verify(referenceResolver).apply(URI.create("/root?q=search"));
   }

   @SuppressWarnings("unchecked")
   public void testGetFormPutsFormValuesIntoQueryParametersSeparatedWithAmp() {
      GetForm form = new GetForm(URI.create("/root"), referenceResolver);

      form
         .put("q", "search")
         .put("lang", "en")
         .submit(null);

      verify(referenceResolver).apply(URI.create("/root?q=search&lang=en"));
   }

   @SuppressWarnings("unchecked")
   public void testGetFormPutsFormValuesIntoQueryParametersWithAmpIfQueryPresent() {
      GetForm form = new GetForm(URI.create("/root?ni=nu"), referenceResolver);

      form
         .put("q", "search")
         .submit(null);

      verify(referenceResolver).apply(URI.create("/root?ni=nu&q=search"));
   }

   public void testQueryParametersAreEncoded() {
      GetForm form = new GetForm(URI.create("/root"), referenceResolver);

      form
         .put("q", "a+b&c d")
         .submit(null);

      verify(referenceResolver).apply(URI.create("/root?q=a%2Bb%26c+d"));
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      resolvedReference = mock(ResourceReference.class);
      referenceResolver = mock(Function.class);
      when(referenceResolver.apply(any())).thenReturn(resolvedReference);
      Request request = mock(Request.class);
      when(resolvedReference.prepareGet(any())).thenReturn(request);
      when(request.send(any())).thenReturn(CompletableFuture.completedFuture(null));
   }
}
