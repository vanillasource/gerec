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
import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.gerec.AcceptMediaType;

@Test
public class PostAsyncFormTests {
   private AsyncResourceReference target;

   @SuppressWarnings("unchecked")
   public void testPostAsyncFormMakesPostToTarget() {
      PostAsyncForm form = new PostAsyncForm(target);

      form.submit(null);

      verify(target).post(any(ContentMediaType.class), eq(""), any(AcceptMediaType.class));
   }

   @SuppressWarnings("unchecked")
   public void testPostPostsParametersAsContent() {
      PostAsyncForm form = new PostAsyncForm(target);

      form
         .put("q", "search")
         .put("lang", "en")
         .submit(null);

      verify(target).post(any(ContentMediaType.class), eq("q=search&lang=en"), any(AcceptMediaType.class));
   }

   @SuppressWarnings("unchecked")
   public void testQueryParametersAreEncoded() {
      PostAsyncForm form = new PostAsyncForm(target);

      form
         .put("q", "a+b&c d")
         .submit(null);

      verify(target).post(any(ContentMediaType.class), eq("q=a%2Bb%26c+d"), any(AcceptMediaType.class));
   }

   @BeforeMethod
   protected void setUp() {
      target = mock(AsyncResourceReference.class);
   }
}
