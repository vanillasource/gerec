/**
 * Copyright (C) 2018 VanillaSource
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
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.UncontrollableByteArrayReadableByteChannelMaster;
import com.vanillasource.aio.channel.ReadableByteChannelMaster;
import static com.vanillasource.gerec.mediatype.MediaTypeSpecification.*;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

@Test
public class PacketBasedCollectionAcceptTypeTests {
   private HttpResponse response;
   private PacketBasedCollectionAcceptType<String> type;
   private Consumer<String> consumer;

   public void testProcessingDoesConsumeStreamItems() throws Exception {
      responseContent(new byte[] { 0,0,0,1, 'a', 0,0,0,2, 'b', 'c' });

      type.deserialize(response, null).get();

      verify(consumer).accept("a");
      verify(consumer).accept("bc");
   }

   public void testEmptyPacketsAreDiscarded() throws Exception {
      responseContent(new byte[] { 0,0,0,1, 'a', 0,0,0,0, 0,0,0,0, 0,0,0,0 });

      type.deserialize(response, null).get();

      verify(consumer).accept("a");
      verifyNoMoreInteractions(consumer);
   }

   @SuppressWarnings("unchecked")
   private void responseContent(byte[] content) {
      doAnswer(invocation -> {
         AioSlave<String> follower = ((Function<ReadableByteChannelMaster, AioSlave<String>>) invocation.getArguments()[0])
            .apply(new UncontrollableByteArrayReadableByteChannelMaster(content));
         follower.onReady();
         return CompletableFuture.completedFuture(follower.onCompleted());
      }).when(response).consumeContent(any(Function.class));
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      response = mock(HttpResponse.class);
      consumer = mock(Consumer.class);
      type = new PacketBasedCollectionAcceptType<String>(mediaType("application/vnd.vanillasource.packets"), MediaTypes.textPlain(), consumer);
   }
}
