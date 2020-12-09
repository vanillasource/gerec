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

package com.vanillasource.gerec.reference;

import com.vanillasource.gerec.HttpRequest;
import com.vanillasource.gerec.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * A generic HTTP Client for the <code>HttpClientResourceReference</code>.
 */
public interface HttpClient {
   CompletableFuture<HttpResponse> doHead(URI uri, HttpRequest.HttpRequestChange change);

   CompletableFuture<HttpResponse> doOptions(URI uri, HttpRequest.HttpRequestChange change);

   CompletableFuture<HttpResponse> doGet(URI uri, HttpRequest.HttpRequestChange change);

   CompletableFuture<HttpResponse> doPost(URI uri, HttpRequest.HttpRequestChange change);

   CompletableFuture<HttpResponse> doPut(URI uri, HttpRequest.HttpRequestChange change);

   CompletableFuture<HttpResponse> doDelete(URI uri, HttpRequest.HttpRequestChange change);
}
