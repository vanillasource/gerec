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

import com.vanillasource.gerec.AsyncResourceReference;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.mediatype.MediaTypes;
import java.util.concurrent.CompletableFuture;

/**
 * A form that posts all parameters as form encoded, just like a HTML Form with POST method.
 */
public final class PostAsyncForm implements AsyncForm {
   private final AsyncResourceReference target;
   private final FormParameters parameters = new FormParameters();

   public PostAsyncForm(AsyncResourceReference target) {
      this.target = target;
   }

   @Override
   public void put(String key, String value) {
      parameters.put(key, value);
   }

   @Override
   public <T> CompletableFuture<ContentResponse<T>> submit(AcceptMediaType<T> acceptType) {
      return target.post(MediaTypes.FORM_URLENCODED, parameters.aggregate(), acceptType);
   }
}


