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

package com.vanillasource.gerec.form;

public final class FormParameters {
   private final String aggregate;

   public FormParameters() {
      this("");
   }

   private FormParameters(String aggregate) {
      this.aggregate = aggregate;
   }

   public FormParameters put(String key, String value) {
      return new FormParameters((aggregate.isEmpty()?"":(aggregate+"&")) + key + "=" + value);
   }

   public String aggregate() {
      return aggregate;
   }
}
