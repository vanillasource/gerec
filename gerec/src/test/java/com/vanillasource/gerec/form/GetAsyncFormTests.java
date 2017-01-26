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
import com.vanillasource.gerec.AsyncResourceReference;
import java.util.function.Function;
import java.net.URI;

@Test
public class GetAsyncFormTests {
   private AsyncResourceReference resolvedReference;
   private Function<URI, AsyncResourceReference> referenceResolver;

   public void testGetAsyncFormMakesAGetRequestToResolvedReference() {
      GetAsyncForm form = new GetAsyncForm(URI.create("/root"), referenceResolver);

      form.submit(null);

      verify(resolvedReference).get(null);
   }

   @SuppressWarnings("unchecked")
   public void testGetAsyncFormPutsFormValuesIntoQueryParameters() {
      GetAsyncForm form = new GetAsyncForm(URI.create("/root"), referenceResolver);
      form.put("q", "search");

      form.submit(null);

      verify(referenceResolver).apply(URI.create("/root?q=search"));
   }

   @SuppressWarnings("unchecked")
   public void testGetAsyncFormPutsFormValuesIntoQueryParametersSeparatedWithAmp() {
      GetAsyncForm form = new GetAsyncForm(URI.create("/root"), referenceResolver);
      form.put("q", "search");
      form.put("lang", "en");

      form.submit(null);

      verify(referenceResolver).apply(URI.create("/root?q=search&lang=en"));
   }

   @SuppressWarnings("unchecked")
   public void testGetAsyncFormPutsFormValuesIntoQueryParametersWithAmpIfQueryPresent() {
      GetAsyncForm form = new GetAsyncForm(URI.create("/root?ni=nu"), referenceResolver);
      form.put("q", "search");

      form.submit(null);

      verify(referenceResolver).apply(URI.create("/root?ni=nu&q=search"));
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      resolvedReference = mock(AsyncResourceReference.class);
      referenceResolver = mock(Function.class);
      when(referenceResolver.apply(any())).thenReturn(resolvedReference);
   }
}
