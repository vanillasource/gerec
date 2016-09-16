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

import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.mediatype.NamedMediaType;
import java.util.function.Function;
import java.util.function.Consumer;
import java.net.URI;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.JsonParseException;

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
   public T deserialize(HttpResponse response, Function<URI, ResourceReference> referenceProducer) {
      return response.processContent(inputStream -> {
         try {
            return createDeserializerObjectMapper(referenceProducer).readValue(inputStream, type);
         } catch (IOException e) {
            throw new UncheckedIOException("error deserializing object of type: "+type, e);
         }
      });
   }

   private ObjectMapper createDeserializerObjectMapper(Function<URI, ResourceReference> referenceProducer) {
      ObjectMapper mapper = new ObjectMapper();
      mapperCustomizer.accept(mapper);
      SimpleModule module = new SimpleModule();
      module.addDeserializer(ResourceReference.class, new JsonDeserializer<ResourceReference>() {
         @Override
         public ResourceReference deserialize(JsonParser jp, DeserializationContext context) throws IOException {
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
            return referenceProducer.apply(URI.create(uri));
         }
      });
      mapper.registerModule(module);
      return mapper;
   }

   @Override
   public void serialize(T object, HttpRequest request) {
      try {
         byte[] objectAsBytes = createSerializerObjectMapper().writeValueAsBytes(object);
         request.setContent(() -> new ByteArrayInputStream(objectAsBytes), objectAsBytes.length);
      } catch (IOException e) {
         throw new UncheckedIOException("error serializing object: "+object, e);
      }
   }

   private ObjectMapper createSerializerObjectMapper() {
      ObjectMapper mapper = new ObjectMapper();
      mapperCustomizer.accept(mapper);
      SimpleModule module = new SimpleModule();
      module.addSerializer(ResourceReference.class, new JsonSerializer<ResourceReference>() {
         @Override
         public void serialize(ResourceReference reference, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            // { "href": "<uri>" }
            jgen.writeStartObject();
            jgen.writeStringField("href", reference.toURI().toString());
            jgen.writeEndObject();
         }
      });
      mapper.registerModule(module);
      return mapper;
   }
}

