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

package com.vanillasource.gerec;

import java.util.List;

/**
 * A server response after interacting with a resource. The response contains methods to be able to make
 * conditional requests. All the <code>if..()</code> methods produce request changers that can be supplied,
 * potentially combined, to subsequent requests. All preconditions are checked on the server side,
 * and will be answered with the HTTP error code <code>412</code> if not fulfilled.
 */
public interface Response {
   HttpStatusCode getStatusCode();

   /**
    * @return True, if response contains an 'ETag', for using conditional requests based on identity.
    */
   boolean hasIdentity();

   /**
    * Request processing will only be attempted if the resource matches the identity in this response.
    * If response does not contain the identity, this method fails with an exception.
    */
   HttpRequest.HttpRequestChange ifMatch();

   /**
    * Request processing will only be attempted if the resource does <strong>not</strong> match the identity in this response.
    * If response does not contain the identity, this method fails with an exception.
    */
   HttpRequest.HttpRequestChange ifNotMatch();

   /**
    * Request processing will only be attempted if the resource has been modified since this response.
    */
   HttpRequest.HttpRequestChange ifModifiedSince();

   /**
    * Request processing will only be attempted if the resource has <strong>not</strong> been modified since this response.
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

   /**
    * @return True, if the response contains a 'Location' header. This header indicates the content's URI.
    */
   boolean hasLocation();

   /**
    * Follow the 'Location' header received from the server.
    */
   ResourceReference followLocation();
}

