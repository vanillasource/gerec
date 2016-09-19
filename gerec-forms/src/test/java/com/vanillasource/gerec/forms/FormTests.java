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

package com.vanillasource.gerec.forms;

import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.forms.Form.Method;
import static java.util.Collections.emptyList;
import static java.util.Arrays.asList;
import com.vanillasource.gerec.mediatype.MediaTypes;
import java.util.function.Function;
import java.net.URI;

@Test
public class FormTests {
   private ResourceReference target;

   public void testGetFormMakesAGetRequestToResolvedReference() {
      Form form = new Form(target, Method.GET, emptyList());
      when(target.toURI()).thenReturn(URI.create("http://localhost"));
      ResourceReference resolved = mock(ResourceReference.class);
      form.setReferenceResolver(uri -> resolved);

      form.submit(null);

      verify(resolved).get(null);
   }

   @SuppressWarnings("unchecked")
   public void testGetFormPutsFormValuesIntoQueryParameters() {
      FormComponent qComponent = new FormComponent("q");
      qComponent.setSelectedValue("search");
      Form form = new Form(target, Method.GET, asList(qComponent));
      when(target.toURI()).thenReturn(URI.create("http://localhost"));
      ResourceReference resolved = mock(ResourceReference.class);
      Function<URI, ResourceReference> resolver = mock(Function.class);
      when(resolver.apply(any())).thenReturn(resolved);
      form.setReferenceResolver(resolver);

      form.submit(null);

      verify(resolver).apply(URI.create("http://localhost?q=search"));
   }

   @SuppressWarnings("unchecked")
   public void testGetFormPutsFormValuesIntoQueryParametersSeparatedWithAmp() {
      FormComponent qComponent = new FormComponent("q");
      qComponent.setSelectedValue("search");
      FormComponent langComponent = new FormComponent("lang");
      langComponent.setSelectedValue("en");
      Form form = new Form(target, Method.GET, asList(qComponent, langComponent));
      when(target.toURI()).thenReturn(URI.create("http://localhost"));
      ResourceReference resolved = mock(ResourceReference.class);
      Function<URI, ResourceReference> resolver = mock(Function.class);
      when(resolver.apply(any())).thenReturn(resolved);
      form.setReferenceResolver(resolver);

      form.submit(null);

      verify(resolver).apply(URI.create("http://localhost?q=search&lang=en"));
   }

   @SuppressWarnings("unchecked")
   public void testGetFormPutsFormValuesIntoQueryParametersWithAmpIfQueryPresent() {
      FormComponent qComponent = new FormComponent("q");
      qComponent.setSelectedValue("search");
      Form form = new Form(target, Method.GET, asList(qComponent));
      when(target.toURI()).thenReturn(URI.create("http://localhost?ni=nu"));
      ResourceReference resolved = mock(ResourceReference.class);
      Function<URI, ResourceReference> resolver = mock(Function.class);
      when(resolver.apply(any())).thenReturn(resolved);
      form.setReferenceResolver(resolver);

      form.submit(null);

      verify(resolver).apply(URI.create("http://localhost?ni=nu&q=search"));
   }

   public void testPostFormMakesPostToTarget() {
      Form form = new Form(target, Method.POST, emptyList());
      when(target.toURI()).thenReturn(URI.create("http://localhost"));

      form.submit(null);

      verify(target).post(MediaTypes.FORM_URLENCODED, "", null);
   }

   public void testPostPostsParametersAsContent() {
      FormComponent qComponent = new FormComponent("q");
      qComponent.setSelectedValue("search");
      FormComponent langComponent = new FormComponent("lang");
      langComponent.setSelectedValue("en");
      Form form = new Form(target, Method.POST, asList(qComponent, langComponent));
      when(target.toURI()).thenReturn(URI.create("http://localhost"));

      form.submit(null);

      verify(target).post(MediaTypes.FORM_URLENCODED, "q=search&lang=en", null);
   }

   @BeforeMethod
   protected void setUp() {
      target = mock(ResourceReference.class);
   }
}
