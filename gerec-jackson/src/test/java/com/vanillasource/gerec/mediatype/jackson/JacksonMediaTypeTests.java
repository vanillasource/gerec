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

package com.vanillasource.gerec.mediatype.jackson;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.aio.channel.WritableByteChannelMaster;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.ReadableByteChannelMaster;
import com.vanillasource.aio.channel.InputStreamReadableByteChannelMaster;
import java.util.concurrent.CompletableFuture;
import java.net.URI;
import java.util.function.Function;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.nio.ByteBuffer;
import java.io.*;

@Test
public class JacksonMediaTypeTests {
   private HttpRequest request;
   private HttpResponse response;
   private DeserializationContext context;
   private String content;

   public void testTestObjectGetsSerialized() {
      JacksonMediaType<TestObject> mediaType = new JacksonMediaType<>(TestObject.class, "application/vnd.vanillasource.testobject+json");
      mediaType.serialize(new TestObject("John", 34), request);

      assertEquals(content, "{\"name\":\"John\",\"age\":34}");
   }

   public void testTestObjectCanBeDeserialized() throws Exception {
      JacksonMediaType<TestObject> mediaType = new JacksonMediaType<>(TestObject.class, "application/vnd.vanillasource.testobject+json");
      content = "{\"name\":\"John\",\"age\":34}";
      
      TestObject object = mediaType.deserialize(response, context).get();

      assertEquals(object.getName(), "John");
      assertEquals(object.getAge(), 34);
   }

   @SuppressWarnings("unchecked")
   public void testReferencesGetDeserializedUsedReferenceResolver() throws Exception {
      JacksonMediaType<ReferenceObject> mediaType = new JacksonMediaType<>(ReferenceObject.class, "application/vnd.vanillasource.referenceobject+json");
      content = "{\"reference\":{\"href\":\"/relative/uri\"}}";
      ResourceReference reference = mock(ResourceReference.class);
      when(context.resolve(URI.create("/relative/uri"))).thenReturn(reference);

      ReferenceObject object = mediaType.deserialize(response, context).get();

      assertSame(object.getReference(), reference);
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      context = mock(DeserializationContext.class);
      request = mock(HttpRequest.class);
      content = null;
      doAnswer(invocation -> {
         Function<WritableByteChannelMaster, AioSlave<Void>> producerFactory =
            (Function<WritableByteChannelMaster, AioSlave<Void>>)invocation.getArguments()[0];
         AioSlave<Void> producer = producerFactory.apply(new StringWritableByteChannel());
         producer.onReady();
         return CompletableFuture.completedFuture(producer.onCompleted());
      }).when(request).setByteProducer(any(), anyLong());
      response = mock(HttpResponse.class);
      doAnswer(invocation -> {
         InputStreamReadableByteChannelMaster master = new InputStreamReadableByteChannelMaster(new ByteArrayInputStream(content.getBytes()));
         Function<ReadableByteChannelMaster, AioSlave<Void>> consumerFactory =
            (Function<ReadableByteChannelMaster, AioSlave<Void>>) invocation.getArguments()[0];
         AioSlave<Void> consumer = consumerFactory.apply(master);
         return master.execute(consumer, Runnable::run);
      }).when(response).consumeContent(any(Function.class));
   }

   public class StringWritableByteChannel implements WritableByteChannelMaster {
      private StringBuilder builder = new StringBuilder();

      @Override
      public void resume() {
      }

      @Override
      public void pause() {
      }

      @Override
      public int write(ByteBuffer buffer) {
         int count = 0;
         while (buffer.hasRemaining()) {
            builder.append((char) buffer.get());
            count++;
         }
         return count;
      }

      @Override
      public void close() {
         content = builder.toString();
      }

      @Override
      public boolean isOpen() {
         return true;
      }
   }

   public static final class TestObject {
      private String name;
      private int age;

      protected TestObject() {
      }

      public TestObject(String name, int age) {
         this.name = name;
         this.age = age;
      }

      public String getName() {
            return name;
      }

      public int getAge() {
            return age;
      }
   }

   public static final class ReferenceObject {
      private ResourceReference reference;

      protected ReferenceObject() {
      }

      public ReferenceObject(ResourceReference reference) {
         this.reference = reference;
      }

      public ResourceReference getReference() {
         return reference;
      }
   }
}

