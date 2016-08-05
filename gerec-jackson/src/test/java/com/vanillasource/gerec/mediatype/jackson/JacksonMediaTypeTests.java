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
import java.util.function.Consumer;
import java.net.URI;
import java.util.function.Function;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

@Test
public class JacksonMediaTypeTests {
   private HttpRequest request;
   private HttpResponse response;
   private String content;

   public void testTestObjectGetsSerialized() {
      JacksonMediaType<TestObject> mediaType = new JacksonMediaType<>(TestObject.class, "application/vnd.vanillasource.testobject+json");
      mediaType.serialize(new TestObject("John", 34), request);

      assertEquals(content, "{\"name\":\"John\",\"age\":34}");
   }

   public void testTestObjectCanBeDeserialized() {
      JacksonMediaType<TestObject> mediaType = new JacksonMediaType<>(TestObject.class, "application/vnd.vanillasource.testobject+json");
      content = "{\"name\":\"John\",\"age\":34}";
      
      TestObject object = mediaType.deserialize(response, uri -> null);

      assertEquals(object.getName(), "John");
      assertEquals(object.getAge(), 34);
   }

   public void testReferencesGetSerializedAsURIs() {
      JacksonMediaType<ReferenceObject> mediaType = new JacksonMediaType<>(ReferenceObject.class, "application/vnd.vanillasource.referenceobject+json");
      ResourceReference reference = mock(ResourceReference.class);
      when(reference.toURI()).thenReturn(URI.create("/relative/uri"));

      mediaType.serialize(new ReferenceObject(reference), request);

      assertEquals(content, "{\"reference\":\"/relative/uri\"}");
   }

   @SuppressWarnings("unchecked")
   public void testReferencesGetDeserializedUsedReferenceFactory() {
      JacksonMediaType<ReferenceObject> mediaType = new JacksonMediaType<>(ReferenceObject.class, "application/vnd.vanillasource.referenceobject+json");
      content = "{\"reference\":\"/relative/uri\"}";
      Function<URI, ResourceReference> referenceFactory = mock(Function.class);
      ResourceReference reference = mock(ResourceReference.class);
      when(referenceFactory.apply(URI.create("/relative/uri"))).thenReturn(reference);

      ReferenceObject object = mediaType.deserialize(response, referenceFactory);

      assertSame(object.getReference(), reference);
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      request = mock(HttpRequest.class);
      content = null;
      doAnswer(invocation -> {
         Consumer<OutputStream> processor = (Consumer<OutputStream>)invocation.getArguments()[0];
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         try {
            processor.accept(byteArrayOutputStream);
         } finally {
            byteArrayOutputStream.close();
         }
         content = byteArrayOutputStream.toString("UTF-8");
         return null;
      }).when(request).processContent(any());
      response = mock(HttpResponse.class);
      when(response.processContent(any())).thenAnswer(invocation -> {
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

