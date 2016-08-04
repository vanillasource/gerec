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

import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.http.SingleHeaderValue;
import com.vanillasource.gerec.http.CommaSeparatedHeaderValue;
import com.vanillasource.gerec.Header;
import com.vanillasource.gerec.MediaType;
import java.util.function.Function;
import java.net.URI;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A media type that has a static name and quality value.
 */
public abstract class NamedMediaType<T> implements MediaType {
   private final String mediaTypeName;
   private final double qualityValue;
   private final String headerValue;

   public NamedMediaType(String mediaTypeName, double qualityValue) {
      this.mediaTypeName = mediaTypeName;
      this.qualityValue = qualityValue;
      if (qualityValue <= 0 || qualityValue > 1) {
         throw new IllegalArgumentException("quality value most be between 0 (exclusive) and 1 (inclusive), but was: "+qualityValue+" for "+mediaTypeName);
      }
      BigDecimal qualityDecimal = new BigDecimal(qualityValue);
      if (qualityDecimal.scale() > 3) {
         qualityDecimal = qualityDecimal.setScale(3, RoundingMode.UP);
      }
      headerValue = mediaTypeName + ";q="+qualityDecimal.toString();
   }

   public NamedMediaType(String mediaTypeName) {
      this(mediaTypeName, 1.0d);
   }

   @Override
   public final AcceptType<T> getAcceptType() {
      return new AcceptType<T>() {
         @Override
         public void applyTo(HttpRequest request) {
            new CommaSeparatedHeaderValue(Header.ACCEPT, headerValue).applyTo(request);
         }

         @Override
         public boolean isHandling(HttpResponse response) {
            return response.hasHeader(Header.CONTENT_TYPE) && response.getHeader(Header.CONTENT_TYPE).equals(mediaTypeName);
         }

         @Override
         public T deserialize(HttpResponse response, Function<URI, ResourceReference> referenceProducer) {
            return NamedMediaType.this.deserialize(response, referenceProducer);
         }
      };
   }

   public final ContentType<T> getContentType() {
      return new ContentType<T>() {
         @Override
         public void applyTo(HttpRequest request) {
            new SingleHeaderValue(Header.CONTENT_TYPE, mediaTypeName).applyTo(request);
         }

         @Override
         public void serialize(T object, HttpRequest request) {
            NamedMediaType.this.serialize(object, request);
         }
      };
   }

   protected abstract T deserialize(HttpResponse response, Function<URI, ResourceReference> referenceProducer);

   protected abstract void serialize(T object, HttpRequest request);
}

