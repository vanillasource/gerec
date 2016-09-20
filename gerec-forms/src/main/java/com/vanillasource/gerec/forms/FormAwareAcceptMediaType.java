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

package com.vanillasource.gerec.forms;

import com.vanillasource.gerec.mediatype.DelegatingAcceptMediaType;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.DeserializationContext;

/**
 * Use this media-type to add form-functionality and definitions to your
 * media-type.
 */
public class FormAwareAcceptMediaType<T> extends DelegatingAcceptMediaType<T> {
   public FormAwareAcceptMediaType(AcceptMediaType<T> delegate) {
      super(delegate);
   }

   @Override
   public T deserialize(HttpResponse response, DeserializationContext context) {
      return super.deserialize(response, context.addPostProcessing(Form.class,
               form -> form.setReferenceResolver(context::resolve)
      ));
   }
}
