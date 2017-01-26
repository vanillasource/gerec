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

import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.async.ExceptionTransparentCall;
import java.util.concurrent.CompletableFuture;

/**
 * A generic form that can be filled out with string
 * keys and values and submitted.
 */
public interface AsyncForm {
   void put(String key, String value);

   <T> CompletableFuture<ContentResponse<T>> submit(AcceptMediaType<T> acceptType);

   default Form sync() {
      return new Form() {
         @Override
         public void put(String key, String value) {
            AsyncForm.this.put(key, value);
         }

         @Override
         public <T> ContentResponse<T> submit(AcceptMediaType<T> acceptType) {
            return new ExceptionTransparentCall<>(AsyncForm.this.submit(acceptType)).get();
         }

         @Override
         public AsyncForm async() {
            return AsyncForm.this;
         }
      };
   }
}


