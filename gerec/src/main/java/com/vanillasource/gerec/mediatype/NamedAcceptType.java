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
import com.vanillasource.gerec.AcceptMediaType;
import java.util.function.Function;
import java.net.URI;
import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class NamedAcceptType<T> implements AcceptMediaType<T> {
   private final String mediaTypeName;
   private final double qualityValue;
   private final String headerValue;

   public NamedAcceptType(String mediaTypeName, double qualityValue) {
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

   public NamedAcceptType(String mediaTypeName) {
      this(mediaTypeName, 1.0d);
   }

   protected String getMediaTypeName() {
      return mediaTypeName;
   }

   @Override
   public void applyAsOption(HttpRequest request) {
      new CommaSeparatedHeaderValue(Header.ACCEPT, headerValue).applyTo(request);
   }

   @Override
   public boolean isHandling(HttpResponse response) {
      return response.hasHeader(Header.CONTENT_TYPE) && response.getHeader(Header.CONTENT_TYPE).equals(mediaTypeName);
   }

   @Override
   public String toString() {
      return headerValue;
   }
}

