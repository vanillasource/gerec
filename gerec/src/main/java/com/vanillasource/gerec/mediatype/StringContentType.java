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

import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.gerec.HttpRequest;
import java.io.UnsupportedEncodingException;
import java.io.UncheckedIOException;

/**
 * Serializes a string into the request with the given charset, which
 * is also added to the given media-type as the 'charset' attribute.
 * Default encoding is UTF-8.
 */
public class StringContentType implements ContentMediaType<String> {
   private final ByteArrayContentType delegate;
   private final String encoding;

   public StringContentType(MediaTypeSpecification mediaType, String encoding) {
      this.delegate = new ByteArrayContentType(mediaType.withParameter("charset", encoding));
      this.encoding = encoding;
   }

   public StringContentType(MediaTypeSpecification mediaType) {
      this(mediaType, "UTF-8");
   }

   @Override
   public void applyAsContent(HttpRequest request) {
      delegate.applyAsContent(request);
   }

   @Override
   public void serialize(String content, HttpRequest request) {
      try {
         byte[] bytes = content.getBytes(encoding);
         delegate.serialize(bytes, request);
      } catch (UnsupportedEncodingException e) {
         throw new UncheckedIOException(encoding+" encoding not supported", e);
      }
   }
}

