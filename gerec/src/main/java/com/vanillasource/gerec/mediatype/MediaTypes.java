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

package com.vanillasource.gerec.mediatype;

import static com.vanillasource.gerec.mediatype.MediaTypeSpecification.*;
import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.ContentMediaType;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

/**
 * A factory of some basic media types and their serializers/deserializers.
 */
public final class MediaTypes {
   private MediaTypes() {
   }

   /**
    * Used for submitting information in HTML FORM POST format, using UTF-8 encoding.
    */
   public static ContentMediaType<String> formUrlEncoded() {
      return textPlain(mediaType("application/x-www-form-urlencoded"));
   }

   /**
    * A text/plain with UTF-8 and no quality indicator.
    */
   public static MediaType<String> textPlain() {
      return textPlain(mediaType("text/plain"));
   }

   /**
    * An UTF-8 text/plain with accept quality indicator.
    */
   public static MediaType<String> textPlain(double quality) {
      return textPlain(mediaType("text/plain", quality));
   }

   /**
    * The media-type "text/plain" which is mapped to type <code>String</code>. The charset when using with
    * a request will always be UTF-8. The charset from the response will be used to deserialize messages.
    * Please note that the default charset for text/plain is US-ASCII if not explicitly defined otherwise.
    */
   public static MediaType<String> textPlain(MediaTypeSpecification mediaType) {
      return new MediaType<String>() {
         private final StringAcceptType acceptType = new StringAcceptType(mediaType, "US-ASCII");
         private final StringContentType contentType = new StringContentType(mediaType, "UTF-8");

         @Override
         public void applyAsOption(HttpRequest request) {
            acceptType.applyAsOption(request);
         }

         @Override
         public boolean isHandling(HttpResponse response) {
            return acceptType.isHandling(response);
         }

         @Override
         public void applyAsContent(HttpRequest request) {
            contentType.applyAsContent(request);
         }

         @Override
         public CompletableFuture<String> deserialize(HttpResponse response, DeserializationContext context) {
            return acceptType.deserialize(response, context);
         }

         @Override
         public void serialize(String content, HttpRequest request) {
            contentType.serialize(content, request);
         }
      };
   }
}

