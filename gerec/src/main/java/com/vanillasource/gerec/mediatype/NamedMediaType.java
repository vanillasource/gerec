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
import com.vanillasource.gerec.http.SingleHeaderValueSet;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.MediaType;
import java.util.function.Function;
import java.net.URI;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A media type that has a static name and quality value and applies that
 * to requests and responses.
 */
public abstract class NamedMediaType<T> extends NamedAcceptType<T> implements MediaType<T> {
   public NamedMediaType(String mediaTypeName, double qualityValue) {
      super(mediaTypeName, qualityValue);
   }

   public NamedMediaType(String mediaTypeName) {
      super(mediaTypeName);
   }

   @Override
   public void applyAsContent(HttpRequest request) {
      new SingleHeaderValueSet(Headers.CONTENT_TYPE, getTypeValue()).applyTo(request);
   }
}

