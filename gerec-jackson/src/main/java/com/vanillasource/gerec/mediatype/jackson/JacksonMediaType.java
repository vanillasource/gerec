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

import com.vanillasource.gerec.form.Form;
import com.vanillasource.gerec.form.GetForm;
import com.vanillasource.gerec.form.PostForm;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.mediatype.ByteArrayAcceptType;
import com.vanillasource.gerec.mediatype.ByteArrayContentType;
import com.vanillasource.gerec.mediatype.MediaTypeSpecification;
import java.util.function.Consumer;
import java.net.URI;
import java.io.IOException;
import java.io.UncheckedIOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.JsonParseException;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

public class JacksonMediaType<T> implements MediaType<T> {
   private final Class<T> type;
   private final Consumer<ObjectMapper> mapperCustomizer;
   private final MediaTypeSpecification mediaType;

   public JacksonMediaType(Class<T> type, String mediaTypeName) {
      this(type, MediaTypeSpecification.mediaType(mediaTypeName), mapper -> {});
   }

   /**
    * @param mapperCustomizer A consumer that can customize object mappers created by this media type.
    */
   public JacksonMediaType(Class<T> type, MediaTypeSpecification mediaType, Consumer<ObjectMapper> mapperCustomizer) {
      this.type = type;
      this.mediaType = mediaType;
      this.mapperCustomizer = mapperCustomizer;
   }

   @Override
   public void applyAsOption(HttpRequest request) {
      mediaType.addAsAcceptedTo(request);
   }

   @Override
   public boolean isHandling(HttpResponse response) {
      return mediaType.isIn(response);
   }

   @Override
   public void applyAsContent(HttpRequest request) {
      mediaType.addAsContentTo(request);
   }

   @Override
   public CompletableFuture<T> deserialize(HttpResponse response, DeserializationContext context) {
      return new ByteArrayAcceptType(MediaTypeSpecification.WILDCARD).deserialize(response, context)
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
      mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
      mapperCustomizer.accept(mapper);
      SimpleModule module = new SimpleModule();
      module.addDeserializer(ResourceReference.class, new ResourceReferenceDeserializer(context));
      module.addDeserializer(Form.class, new FormDeserializer(context));
      mapper.registerModule(module);
      return mapper;
   }

   @Override
   public void serialize(T object, HttpRequest request) {
      try {
         byte[] objectAsBytes = createSerializerObjectMapper().writeValueAsBytes(object);
         new ByteArrayContentType(MediaTypeSpecification.WILDCARD).serialize(objectAsBytes, request);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   private ObjectMapper createSerializerObjectMapper() {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
      mapperCustomizer.accept(mapper);
      return mapper;
   }

   private static class ResourceReferenceDeserializer extends JsonDeserializer<ResourceReference> {
      private final DeserializationContext context;

      public ResourceReferenceDeserializer(DeserializationContext context) {
         this.context = context;
      }

      @Override
      public ResourceReference deserialize(JsonParser jp, com.fasterxml.jackson.databind.DeserializationContext jacksonContext) throws IOException {
         // Parse { "href": "<uri>" }, current token is the start of the object
         if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new JsonParseException("tried to read a link, but it was not an object", jp.getCurrentLocation());
         }
         jp.nextFieldName(new SerializedString("href"));
         String uri = jp.nextTextValue();
         skipToObjectEnd(jp, jp.nextToken());
         return context.resolve(URI.create(uri));
      }
   }

   private static void skipToObjectEnd(JsonParser jp, JsonToken prereadToken) throws IOException {
      JsonToken token = prereadToken;
      while (token != null && token != JsonToken.END_OBJECT) {
         token = jp.nextToken();
      }
   }

   private static class FormDeserializer extends JsonDeserializer<Form> {
      private final DeserializationContext context;

      public FormDeserializer(DeserializationContext context) {
         this.context = context;
      }

      @Override
      public Form deserialize(JsonParser jp, com.fasterxml.jackson.databind.DeserializationContext jacksonContext) throws IOException {
         // Parse { "href": "<uri>", "method": "GET" }, current token is the start of the object
         if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new JsonParseException("tried to read a form, but it was not an object", jp.getCurrentLocation());
         }
         jp.nextFieldName(new SerializedString("href"));
         String target = jp.nextTextValue();
         JsonToken nextToken = jp.nextToken();
         String method = "GET";
         if (nextToken == JsonToken.FIELD_NAME && jp.getCurrentName().equals("method")) {
            method = jp.nextTextValue();
         }
         skipToObjectEnd(jp, nextToken);
         if (method.equals("GET")) {
            return new GetForm(URI.create(target), context::resolve);
         } else if (method.equals("POST")) {
            return new PostForm(context.resolve(URI.create(target)));
         } else {
            throw new JsonParseException("unknown method for form '"+method+"'", jp.getCurrentLocation());
         }
      }
   }
}

