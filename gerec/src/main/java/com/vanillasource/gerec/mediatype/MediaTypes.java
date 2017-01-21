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

import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.DeserializationContext;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.gerec.nio.ByteBufferProducer;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

/**
 * A collections of some basic media types and their serializers/deserializers.
 */
public final class MediaTypes {
   private static final String DEFAULT_ENCODING = "US-ASCII";

   private MediaTypes() {
   }

   /**
    * Used for submitting information in HTML FORM POST format, using UTF-8 encoding.
    */
   public static final ContentMediaType<String> FORM_URLENCODED = new NamedMediaType<String>("application/x-www-form-urlencoded") {
      @Override
      public CompletableFuture<String> deserialize(HttpResponse response, DeserializationContext context) {
         throw new UnsupportedOperationException("deserialization unsupported, this is a content media type");
      }

      @Override
      public void serialize(String object, HttpRequest request) {
         try {
            byte[] bytes = object.getBytes("UTF-8");
            request.setByteProducer(
                  output -> new ByteBufferProducer(ByteBuffer.wrap(bytes), output),
                  bytes.length);
         } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("utf-8 encoding not supported", e);
         }
      }
   };

   /**
    * The media-type "text/plain" which is mapped to type <code>String</code>. The charset when using with
    * a request will always be UTF-8. The charset from the response will be used to deserialize messages.
    * Please note that the default charset for text/plain is US-ASCII if not explicitly defined otherwise.
    */
   public static final MediaType<String> TEXT_PLAIN = new NamedMediaType<String>("text/plain", "charset", "UTF-8") {
      @Override
      public CompletableFuture<String> deserialize(HttpResponse response, DeserializationContext context) {
         return new ByteArrayAcceptType().deserialize(response, context)
            .thenApply(content -> {
               try {
                  String encoding = DEFAULT_ENCODING;
                  if (response.hasHeader(Headers.CONTENT_TYPE)) {
                     encoding = response.getHeader(Headers.CONTENT_TYPE).getParameterValue("charset", DEFAULT_ENCODING);
                  }
                  return new String(content, encoding);
               } catch (UnsupportedEncodingException e) {
                  throw new IllegalStateException("utf-8 encoding not supported", e);
               }
            });
      }

      @Override
      public void serialize(String object, HttpRequest request) {
         try {
            byte[] bytes = object.getBytes("UTF-8");
            request.setByteProducer(
                  output -> new ByteBufferProducer(ByteBuffer.wrap(bytes), output),
                  bytes.length);
         } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("utf-8 encoding not supported", e);
         }
      }
   };
}

