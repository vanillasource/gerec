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

import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.mediatype.MediaTypeSpecification;
import com.vanillasource.gerec.mediatype.StringAcceptType;
import static com.vanillasource.gerec.http.Authorization.bearing;
import com.vanillasource.gerec.reference.ChangedResourceReference;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.form.Form;
import com.vanillasource.gerec.form.PostForm;
import com.vanillasource.gerec.form.GetForm;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.Json;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.eclipsesource.json.ParseException;

/**
 * Accept media type that processes incoming json documents with
 * minimal-json.
 */
public class MinimalJsonAcceptType<T> implements AcceptMediaType<T> {
   private static final Logger LOGGER = LoggerFactory.getLogger(MinimalJsonAcceptType.class);
   private final AcceptMediaType<String> delegate;
   private final BiFunction<JsonValue, JsonContext, T> deserializer;

   public MinimalJsonAcceptType(MediaTypeSpecification mediaType, BiFunction<JsonValue, JsonContext, T> deserializer) {
      this.delegate = new StringAcceptType(mediaType);
      this.deserializer = deserializer;
   }

   @Override
   public void applyAsOption(HttpRequest request) {
      delegate.applyAsOption(request);
   }

   @Override
   public boolean isHandling(HttpResponse response) {
      return delegate.isHandling(response);
   }

   @Override
   public CompletableFuture<T> deserialize(HttpResponse response, DeserializationContext context) {
      return delegate.deserialize(response, context)
         .thenApply(jsonString -> {
            JsonValue jsonValue;
            try {
               jsonValue = Json.parse(jsonString);
            } catch (ParseException e) {
               LOGGER.error("there was a parse exception (follows) for message:\n"+jsonString);
               throw e;
            }
            return deserializer.apply(jsonValue, new JsonContext() {
               @Override
               public Form parseForm(JsonObject object) {
                  LOGGER.debug("parsing form");
                  return withControls(object, createForm(object));
               }

               private Form createForm(JsonObject object) {
                  if (object.getString("method","get").equalsIgnoreCase("post")) {
                     return new PostForm(parseLink(object));
                  } else {
                     return new GetForm(URI.create(object.getString("href","")), uri -> parseLink(object, uri));
                  }
               }

               private Form withControls(JsonObject object, Form form) {
                  Form result = form;
                  JsonValue controls = object.get("controls");
                  if (controls != null) {
                     LOGGER.debug("parsing form controls");
                     for (JsonObject.Member member: controls.asObject()) {
                        JsonValue defaultValue = member.getValue().asObject().get("default");
                        if (defaultValue != null) {
                           result = result.put(member.getName(), defaultValue.asString());
                        }
                     }
                  }
                  return result;
               }

               @Override
               public ResourceReference parseLink(JsonObject object) {
                  LOGGER.debug("parsing link");
                  URI uri = URI.create(object.getString("href",""));
                  return parseLink(object, uri);
               }

               private ResourceReference parseLink(JsonObject object, URI uri) {
                  ResourceReference original = context.resolve(uri);
                  JsonValue bearing = object.get("bearing");
                  if (bearing != null) {
                     LOGGER.debug("parsing link bearing token");
                     return new ChangedResourceReference(original,
                           bearing(bearing.asString()));
                  } else {
                     return original;
                  }
               }
            });
         });
   }

   public interface JsonContext {
      Form parseForm(JsonObject object);

      ResourceReference parseLink(JsonObject object);
   }
}


