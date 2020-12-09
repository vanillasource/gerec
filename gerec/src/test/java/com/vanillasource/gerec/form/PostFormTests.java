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
import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.ContentResponse;
import java.util.concurrent.CompletableFuture;

@Test
public class PostFormTests {
   private ResourceReference target;

   @SuppressWarnings("unchecked")
   public void testPostFormMakesPostToTarget() {
      PostForm form = new PostForm(target);

      form.submit(null);

      verify(target).postResponse(any(ContentMediaType.class), eq(""), any(AcceptMediaType.class), eq(HttpRequest.HttpRequestChange.NO_CHANGE));
   }

   @SuppressWarnings("unchecked")
   public void testPostPostsParametersAsContent() {
      PostForm form = new PostForm(target);

      form
         .put("q", "search")
         .put("lang", "en")
         .submit(null);

      verify(target).postResponse(any(ContentMediaType.class), eq("q=search&lang=en"), any(AcceptMediaType.class), eq(HttpRequest.HttpRequestChange.NO_CHANGE));
   }

   @SuppressWarnings("unchecked")
   public void testQueryParametersAreEncoded() {
      PostForm form = new PostForm(target);

      form
         .put("q", "a+b&c d")
         .submit(null);

      verify(target).postResponse(any(ContentMediaType.class), eq("q=a%2Bb%26c+d"), any(AcceptMediaType.class), eq(HttpRequest.HttpRequestChange.NO_CHANGE));
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      target = mock(ResourceReference.class);
      when(target.postResponse(any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(mock(ContentResponse.class)));
   }
}
