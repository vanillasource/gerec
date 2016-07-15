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

package com.vanillasource.gerec.resource;

import com.vanillasource.gerec.http.HttpStatusCode;
import com.vanillasource.gerec.http.HttpRequest;
import java.util.List;

/**
 * A server response after calling a HTTP Method.
 */
public interface Response<T> {
   HttpStatusCode getStatusCode();

   /**
    * @return True, if response contains an 'ETag', for using conditional requests based on identity.
    */
   boolean hasIdentity();

   /**
    * Request will only be attempted if the resource matches the identity in this response.
    * If response does not contain the identity, this method fails with an exception.
    */
   HttpRequest.HttpRequestChange ifMatch();

   /**
    * Request will only be attempted if the resource does not match the identity in this response.
    * If response does not contain the identity, this method fails with an exception.
    */
   HttpRequest.HttpRequestChange ifNotMatch();

   /**
    * Request will only be attempted if the resource has been modified since this response.
    */
   HttpRequest.HttpRequestChange ifModifiedSince();

   /**
    * Request will only be attempted if the resource has not been modified since this response.
    */
   HttpRequest.HttpRequestChange ifUnmodifiedSince();

   /**
    * @return True, if the 'Last-Modified' date was sent by the server.
    */
   boolean hasLastModified();

   /**
    * Request will only be attempted if the resource has been modified since the last modified date in this response.
    */
   HttpRequest.HttpRequestChange ifModifiedSinceLastModified();

   /**
    * Request will only be attempted if the resource has not been modified since the last modified date in this response.
    */
   HttpRequest.HttpRequestChange ifUnmodifiedSinceLastModified();

   boolean hasLocation();

   ResourceReference followLocation();

   T getContent();
}

