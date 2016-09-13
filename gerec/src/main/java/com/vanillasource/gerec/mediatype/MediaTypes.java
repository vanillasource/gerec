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
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.http.Headers;
import java.util.function.Function;
import java.net.URI;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A collections of some basic media types and their serializers/deserializers.
 */
public final class MediaTypes {
   private static final String DEFAULT_ENCODING = "US-ASCII";

   private MediaTypes() {
   }

   /**
    * The media-type "text/plain" which is mapped to type <code>String</code>. The charset when using with
    * a request will always be UTF-8.
    */
   public static final MediaType<String> TEXT_PLAIN = new NamedMediaType<String>("text/plain") {
      @Override
      public String deserialize(HttpResponse response, Function<URI, ResourceReference> referenceProducer) {
         return response.processContent(inputStream -> {
            try {
               ByteArrayOutputStream result = new ByteArrayOutputStream();
               byte[] buffer = new byte[1024];
               int length;
               while ((length = inputStream.read(buffer)) >= 0) {
                      result.write(buffer, 0, length);
               }
               String encoding = DEFAULT_ENCODING;
               if (response.hasHeader(Headers.CONTENT_TYPE)) {
                  encoding = response.getHeader(Headers.CONTENT_TYPE).getParameterValue("charset", DEFAULT_ENCODING);
               }
               return result.toString(encoding);
            } catch (IOException e) {
               throw new UncheckedIOException(e);
            }
         });
      }

      @Override
      public void serialize(String object, HttpRequest request) {
         try {
            byte[] bytes = object.getBytes("UTF-8"); // TODO: set encoding to http request
            request.setContent(() -> new ByteArrayInputStream(bytes), bytes.length);
         } catch (IOException e) {
            throw new UncheckedIOException(e);
         }
      }
   };
}

