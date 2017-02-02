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

import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.DeserializationContext;
import java.util.concurrent.CompletableFuture;
import java.io.UnsupportedEncodingException;
import java.io.UncheckedIOException;

/**
 * Accept media type that copies all input into a string, with the characterset
 * determined from the "charset" attribute of the media type.
 */
public class StringAcceptType implements AcceptMediaType<String> {
   private final ByteArrayAcceptType delegate;
   private final String defaultEncoding;

   public StringAcceptType(MediaTypeSpecification mediaType, String defaultEncoding) {
      this.delegate = new ByteArrayAcceptType(mediaType);
      this.defaultEncoding = defaultEncoding;
   }

   public StringAcceptType(MediaTypeSpecification mediaType) {
      this(mediaType, "UTF-8");
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
   public CompletableFuture<String> deserialize(HttpResponse response, DeserializationContext context) {
      return new ByteArrayAcceptType(MediaTypeSpecification.WILDCARD).deserialize(response, context)
         .thenApply(content -> {
            String encoding = defaultEncoding;
            if (response.hasHeader(Headers.CONTENT_TYPE)) {
               encoding = response.getHeader(Headers.CONTENT_TYPE).getParameterValue("charset", defaultEncoding);
            }
            try {
               return new String(content, encoding);
            } catch (UnsupportedEncodingException e) {
               throw new UncheckedIOException(encoding+" encoding not supported", e);
            }
         });
   }
}


