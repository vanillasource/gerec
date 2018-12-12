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

/**
 * A generic form that can be filled out with string
 * keys and values and submitted.
 * Note: forms are immutable, always work with returned Form.
 */
public interface Form {
   Form put(String key, String value);

   Form putInt(String key, int value);

   Form putLong(String key, long value);

   Form putBytes(String key, byte[] value);

   <T> ContentResponse<T> submit(AcceptMediaType<T> acceptType);

   AsyncForm async();
}

