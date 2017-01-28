/**
 * Copyright (C) 2017 VanillaSource
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

import com.vanillasource.gerec.http.ValueWithParameter;
import com.vanillasource.gerec.http.MultiValueHeaderAdd;
import com.vanillasource.gerec.http.SingleHeaderValueSet;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A specification of a MediaType for accept or content purposes.
 */
public final class MediaTypeSpecification {
   private final ValueWithParameter specification;

   private MediaTypeSpecification(ValueWithParameter specification) {
      this.specification = specification;
   }

   public MediaTypeSpecification withParameter(String key, String value) {
      return new MediaTypeSpecification(specification.addParameter(key, value));
   }

   public void addAsAcceptedTo(HttpRequest request) {
      new MultiValueHeaderAdd(Headers.ACCEPT, specification).applyTo(request);
   }

   public boolean isIn(HttpResponse response) {
      return response.hasHeader(Headers.CONTENT_TYPE) && response.getHeader(Headers.CONTENT_TYPE).matchesValue(specification);
   }

   public void addAsContentTo(HttpRequest request) {
      new SingleHeaderValueSet(Headers.CONTENT_TYPE, specification).applyTo(request);
   }

   @Override
   public String toString() {
      return specification.toString();
   }

   @Override
   public int hashCode() {
      return specification.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if ((o == null) || (!(o instanceof MediaTypeSpecification))) {
         return false;
      }
      return specification.equals(((MediaTypeSpecification) o).specification);
   }

   public static MediaTypeSpecification mediaType(String name) {
      return new MediaTypeSpecification(new ValueWithParameter(name));
   }

   public static MediaTypeSpecification mediaType(String name, double qualityValue) {
      if (qualityValue <= 0 || qualityValue > 1) {
         throw new IllegalArgumentException("quality value most be between 0 (exclusive) and 1 (inclusive), but was: "+qualityValue+" for "+name);
      }
      BigDecimal qualityDecimal = new BigDecimal(qualityValue);
      if (qualityDecimal.scale() > 3) {
         qualityDecimal = qualityDecimal.setScale(3, RoundingMode.UP);
      }
      return new MediaTypeSpecification(new ValueWithParameter(name).addParameter("q", qualityDecimal.toString()));
   }
}
