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

package com.vanillasource.gerec.mediatype.minimaljson;

import com.vanillasource.gerec.AsyncResourceReference;
import com.vanillasource.gerec.mediatype.MediaTypeSpecification;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.form.AsyncForm;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.*;
import java.util.function.BiFunction;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.Json;
import com.vanillasource.aio.AioSlave;
import com.vanillasource.aio.channel.InputStreamReadableByteChannelMaster;
import com.vanillasource.aio.channel.ReadableByteChannelMaster;
import java.net.URI;
import java.io.ByteArrayInputStream;

@Test
public final class MinimalJsonAcceptTypeTests {
   private DeserializationContext context;
   private HttpResponse response;
   private MinimalJsonAcceptType type;
   private BiFunction<JsonValue, MinimalJsonAcceptType.JsonContext, Void> deserializer;

   public void testReceivesEmptyFromEmptyJson() {
      responseContent("{}");

      type.deserialize(response, context);

      verify(deserializer).apply(eq(Json.parse("{}")), any());
   }

   public void testReceivesElementFromContent() {
      responseContent("{\"element\":\"value\"}");

      type.deserialize(response, context);

      verify(deserializer).apply(eq(Json.parse("{\"element\":\"value\"}")), any());
   }

   public void testDeserializesLinks() throws Exception {
      responseContent("{\"href\":\"link\"}");
      withDeserializer((json, context) -> {
         context.parseLink(json.asObject());
      });

      type.deserialize(response, context);

      verify(context).resolve(new URI("link"));
   }

   public void testDeserializesGetForms() throws Exception {
      AsyncForm[] form = new AsyncForm[1];
      responseContent("{\"href\":\"form\", \"method\":\"get\"}");
      withDeserializer((json, context) -> {
         form[0] = context.parseForm(json.asObject());
      });
      type.deserialize(response, context);

      form[0].submitResponse(null);

      verify(context).resolve(new URI("form?"));
   }

   public void testDeserializedDefaultFormElements() throws Exception {
      AsyncForm[] form = new AsyncForm[1];
      responseContent("{\"href\":\"form\", \"method\":\"get\", \"controls\":{\"name\":{\"default\":\"defaultName\"}}}");
      withDeserializer((json, context) -> {
         form[0] = context.parseForm(json.asObject());
      });
      type.deserialize(response, context);

      form[0].submitResponse(null);

      verify(context).resolve(new URI("form?name=defaultName"));
   }

   private void withDeserializer(BiConsumer<JsonValue, MinimalJsonAcceptType.JsonContext> deserializer) {
      this.deserializer = (json, context) -> {
         deserializer.accept(json, context);
         return null;
      };
      type = new MinimalJsonAcceptType<>(MediaTypeSpecification.WILDCARD, this.deserializer);
   }

   @SuppressWarnings("unchecked")
   private void responseContent(String content) {
      doAnswer(invocation -> {
         InputStreamReadableByteChannelMaster master = new InputStreamReadableByteChannelMaster(new ByteArrayInputStream(content.getBytes()));
         AioSlave<String> follower = ((Function<ReadableByteChannelMaster, AioSlave<String>>) invocation.getArguments()[0])
            .apply(master);
         return master.execute(follower, Runnable::run);
      }).when(response).consumeContent(any(Function.class));
   }   

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      deserializer = mock(BiFunction.class);
      type = new MinimalJsonAcceptType(MediaTypeSpecification.WILDCARD, deserializer);
      context = mock(DeserializationContext.class);
      when(context.resolve(any())).thenReturn(mock(AsyncResourceReference.class));
      response = mock(HttpResponse.class);
   }
}

