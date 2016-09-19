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

import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.DeserializationContext;
import java.util.function.Supplier;
import java.net.URI;
import java.util.function.Function;
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

   public void testTestObjectCanBeDeserialized() {
      JacksonMediaType<TestObject> mediaType = new JacksonMediaType<>(TestObject.class, "application/vnd.vanillasource.testobject+json");
      content = "{\"name\":\"John\",\"age\":34}";
      
      TestObject object = mediaType.deserialize(response, context);

      assertEquals(object.getName(), "John");
      assertEquals(object.getAge(), 34);
   }

   public void testReferencesGetSerializedAsURIs() {
      JacksonMediaType<ReferenceObject> mediaType = new JacksonMediaType<>(ReferenceObject.class, "application/vnd.vanillasource.referenceobject+json");
      ResourceReference reference = mock(ResourceReference.class);
      when(reference.toURI()).thenReturn(URI.create("/relative/uri"));

      mediaType.serialize(new ReferenceObject(reference), request);

      assertEquals(content, "{\"reference\":{\"href\":\"/relative/uri\"}}");
   }

   @SuppressWarnings("unchecked")
   public void testReferencesGetDeserializedUsedReferenceResolver() {
      JacksonMediaType<ReferenceObject> mediaType = new JacksonMediaType<>(ReferenceObject.class, "application/vnd.vanillasource.referenceobject+json");
      content = "{\"reference\":{\"href\":\"/relative/uri\"}}";
      ResourceReference reference = mock(ResourceReference.class);
      when(context.resolve(URI.create("/relative/uri"))).thenReturn(reference);

      ReferenceObject object = mediaType.deserialize(response, context);

      assertSame(object.getReference(), reference);
   }

   public void testPostProcessingIsInvokedForDeserializedObject() {
      JacksonMediaType<TestObject> mediaType = new JacksonMediaType<>(TestObject.class, "application/vnd.vanillasource.testobject+json");
      content = "{\"name\":\"John\",\"age\":34}";
      
      TestObject object = mediaType.deserialize(response, context);

      verify(context).postProcess(object);
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      context = mock(DeserializationContext.class);
      request = mock(HttpRequest.class);
      content = null;
      doAnswer(invocation -> {
         Supplier<InputStream> inputStreamSupplier = (Supplier<InputStream>)invocation.getArguments()[0];
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStreamSupplier.get()));
         content = reader.readLine();
         return null;
      }).when(request).setContent(any(), anyLong());
      response = mock(HttpResponse.class);
      when(response.processContent(any(Function.class))).thenAnswer(invocation -> {
         Function<InputStream, Object> processor = (Function<InputStream, Object>)invocation.getArguments()[0];
         try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
            return processor.apply(inputStream);
         }
      });
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

