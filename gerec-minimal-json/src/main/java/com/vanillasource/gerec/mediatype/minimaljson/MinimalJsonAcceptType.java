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
import com.vanillasource.gerec.reference.ChangedAsyncResourceReference;
import com.vanillasource.gerec.AsyncResourceReference;
import com.vanillasource.gerec.form.AsyncForm;
import com.vanillasource.gerec.form.PostAsyncForm;
import com.vanillasource.gerec.form.GetAsyncForm;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.Json;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
         .thenApply(jsonString -> deserializer.apply(Json.parse(jsonString), new JsonContext() {
            @Override
            public AsyncForm parseForm(JsonObject object) {
               LOGGER.debug("parsing form");
               return withControls(object, createForm(object));
            }

            private AsyncForm createForm(JsonObject object) {
               if (object.getString("method","get").equalsIgnoreCase("post")) {
                  return new PostAsyncForm(parseLink(object));
               } else {
                  return new GetAsyncForm(URI.create(object.getString("href","")), uri -> parseLink(object, uri));
               }
            }

            private AsyncForm withControls(JsonObject object, AsyncForm form) {
               AsyncForm result = form;
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
            public AsyncResourceReference parseLink(JsonObject object) {
               LOGGER.debug("parsing link");
               URI uri = URI.create(object.getString("href",""));
               return parseLink(object, uri);
            }

            private AsyncResourceReference parseLink(JsonObject object, URI uri) {
               AsyncResourceReference original = context.resolve(uri);
               JsonValue bearing = object.get("bearing");
               if (bearing != null) {
                  LOGGER.debug("parsing link bearing token");
                  return new ChangedAsyncResourceReference(original,
                        bearing(bearing.asString()));
               } else {
                  return original;
               }
            }
         }));
   }

   public interface JsonContext {
      AsyncForm parseForm(JsonObject object);

      AsyncResourceReference parseLink(JsonObject object);
   }
}


