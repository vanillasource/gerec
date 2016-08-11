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
import com.vanillasource.gerec.HttpResponse;
import java.util.function.Consumer;
import java.util.function.Function;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

@Test
public class LineBasedCollectionTypeTests {
   private HttpResponse response;
   private LineBasedCollectionType<String> type;
   private Consumer<String> consumer;

   public void testDeserializationDoesNotConsumeStream() {
      type.deserialize(response, null);

      verifyNoMoreInteractions(response);
   }

   public void testProcessingDoesConsumeStreamItems() {
      responseContent("a\nb\n");

      type.deserialize(response, null).process(consumer);

      verify(consumer).accept("a");
      verify(consumer).accept("b");
   }

   public void testEmptyLinesAreDiscarded() {
      responseContent("a\n\n\n\n");

      type.deserialize(response, null).process(consumer);

      verify(consumer).accept("a");
      verifyNoMoreInteractions(consumer);
   }

   @SuppressWarnings("unchecked")
   private void responseContent(String content) {
      doAnswer(invocation -> {
         ((Consumer<InputStream>)invocation.getArguments()[0]).accept(
            new ByteArrayInputStream(content.getBytes("UTF-8"))
         );
         return null;
      }).when(response).processContent(any(Consumer.class));
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      response = mock(HttpResponse.class);
      consumer = mock(Consumer.class);
      type = new LineBasedCollectionType<String>("application/vnd.vanillasource.lines", MediaTypes.TEXT_PLAIN);
   }
}
