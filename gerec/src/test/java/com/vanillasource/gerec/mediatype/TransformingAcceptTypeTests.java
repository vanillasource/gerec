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
import com.vanillasource.gerec.AcceptMediaType;
import java.util.concurrent.CompletableFuture;

@Test
public class TransformingAcceptTypeTests {
   @SuppressWarnings("unchecked")
   public void testDeserializeAppliesTransformation() throws Exception {
      AcceptMediaType<String> delegate = mock(AcceptMediaType.class);
      AcceptMediaType<Integer> type = new TransformingAcceptType<>(delegate, Integer::valueOf);
      when(delegate.deserialize(any(), any())).thenReturn(CompletableFuture.completedFuture("12"));

      int content = type.deserialize(null, null).get();

      assertEquals(content, 12);
   }
}
