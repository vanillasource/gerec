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

import com.vanillasource.gerec.form.AsyncForm;
import com.vanillasource.gerec.form.GetAsyncForm;
import com.vanillasource.gerec.form.PostAsyncForm;
import com.vanillasource.gerec.AsyncResourceReference;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.mediatype.NamedMediaType;
import com.vanillasource.gerec.mediatype.ByteArrayAcceptType;
import java.util.function.Consumer;
import com.vanillasource.gerec.nio.ByteBufferProducer;
import java.net.URI;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.UncheckedIOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.JsonParseException;
import java.util.concurrent.CompletableFuture;

public class JacksonMediaType<T> extends NamedMediaType<T> {
   private Class<T> type;
   private Consumer<ObjectMapper> mapperCustomizer;

   public JacksonMediaType(Class<T> type, String mediaTypeName) {
      this(type, mediaTypeName, mapper -> {});
   }

   /**
    * @param mapperCustomizer A consumer that can customize object mappers created by this media type.
    */
   public JacksonMediaType(Class<T> type, String mediaTypeName, Consumer<ObjectMapper> mapperCustomizer) {
      super(mediaTypeName);
      this.type = type;
      this.mapperCustomizer = mapperCustomizer;
   }

   @Override
   public CompletableFuture<T> deserialize(HttpResponse response, DeserializationContext context) {
      return new ByteArrayAcceptType().deserialize(response, context)
         .thenApply(content -> {
            try {
               return createDeserializerObjectMapper(context).readValue(content, type);
            } catch (IOException e) {
               throw new UncheckedIOException(e);
            }
         });
   }

   private ObjectMapper createDeserializerObjectMapper(DeserializationContext context) {
      ObjectMapper mapper = new ObjectMapper();
      mapperCustomizer.accept(mapper);
      SimpleModule module = new SimpleModule();
      module.addDeserializer(AsyncResourceReference.class, new JsonDeserializer<AsyncResourceReference>() {
         @Override
         public AsyncResourceReference deserialize(JsonParser jp, com.fasterxml.jackson.databind.DeserializationContext jacksonContext) throws IOException {
            // Parse { "href": "<uri>" }, current token is the start of the object
            if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
               throw new JsonParseException("tried to read a link, but it was not an object", jp.getCurrentLocation());
            }
            jp.nextFieldName(new SerializedString("href"));
            String uri = jp.nextTextValue();
            JsonToken token = jp.nextToken();
            if (token != JsonToken.END_OBJECT) {
               throw new JsonParseException("tried to read a link, but it was not finished after reading href", jp.getCurrentLocation());
            }
            return context.resolve(URI.create(uri));
         }
      });
      module.addDeserializer(AsyncForm.class, new JsonDeserializer<AsyncForm>() {
         @Override
         public AsyncForm deserialize(JsonParser jp, com.fasterxml.jackson.databind.DeserializationContext jacksonContext) throws IOException {
            // Parse { "target": "<uri>", "method": "GET" }, current token is the start of the object
            if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
               throw new JsonParseException("tried to read a form, but it was not an object", jp.getCurrentLocation());
            }
            jp.nextFieldName(new SerializedString("target"));
            String target = jp.nextTextValue();
            jp.nextFieldName(new SerializedString("method"));
            String method = jp.nextTextValue();
            JsonToken token = jp.nextToken();
            if (token != JsonToken.END_OBJECT) {
               throw new JsonParseException("tried to read a form, but it was not finished after reading target and method", jp.getCurrentLocation());
            }
            if (method.equals("GET")) {
               return new GetAsyncForm(URI.create(target), context::resolve);
            } else if (method.equals("POST")) {
               return new PostAsyncForm(context.resolve(URI.create(target)));
            } else {
               throw new JsonParseException("unknown method for form '"+method+"'", jp.getCurrentLocation());
            }
         }
      });
      mapper.registerModule(module);
      return mapper;
   }

   @Override
   public void serialize(T object, HttpRequest request) {
      try {
         byte[] objectAsBytes = createSerializerObjectMapper().writeValueAsBytes(object);
         request.setByteProducer(channel -> new ByteBufferProducer(ByteBuffer.wrap(objectAsBytes), channel), objectAsBytes.length);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   private ObjectMapper createSerializerObjectMapper() {
      ObjectMapper mapper = new ObjectMapper();
      mapperCustomizer.accept(mapper);
      return mapper;
   }
}

