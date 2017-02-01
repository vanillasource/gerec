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

package com.vanillasource.gerec.reference;

import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.HttpStatusCode;
import com.vanillasource.gerec.mediatype.MediaTypes;
import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.HttpErrorException;
import org.testng.annotations.*;
import java.util.concurrent.CompletionException;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Test
public class AsyncHttpClientResourceReferenceTests {
   private HttpResponse response;
   private AsyncHttpClient client;
   private AsyncHttpClientResourceReference reference;

   public void testHeadInvokesHead() throws Exception {
      when(client.doHead(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);

      reference.head().get();

      verify(client).doHead(any(), any());
   }

   public void testOptionsInvokesOptions() throws Exception {
      when(client.doOptions(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.options().get();

      verify(client).doOptions(any(), any());
   }

   public void testGetInvokesGet() throws Exception {
      when(client.doGet(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.get(MediaType.NONE).get();

      verify(client).doGet(any(), any());
   }

   public void testPostInvokesPost() throws Exception {
      when(client.doPost(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.post(MediaType.NONE, null).get();

      verify(client).doPost(any(), any());
   }

   public void testPutInvokesPut() throws Exception {
      when(client.doPut(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.put(MediaType.NONE, null).get();

      verify(client).doPut(any(), any());
   }

   public void testDeleteInvokesDelete() throws Exception {
      when(client.doDelete(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.delete().get();

      verify(client).doDelete(any(), any());
   }

   public void testSuccessfulOperationRetrievesContentWithMediaType() throws Exception {
      when(client.doGet(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(new byte[] {'T', 'E', 'S', 'T'}));

      String retrievedContent = reference.get(MediaTypes.textPlain()).get().getContent();

      assertEquals(retrievedContent, "TEST");
   }

   public void testUnsuccessfulOperationReturnsBufferedContentInException() throws Exception {
      when(client.doGet(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.NOT_FOUND);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(new byte[] {'T', 'E', 'S', 'T'}));

      String retrievedContent = reference
         .get(MediaTypes.textPlain())
         .handle((content, exception) -> {
            assertNull(content);
            HttpErrorException cause = (HttpErrorException)((CompletionException) exception).getCause();
            return cause.getResponse().getBody(MediaTypes.textPlain());
         })
         .get();

      assertEquals(retrievedContent, "TEST");
   }

   @BeforeMethod
   protected void setUp() {
      response = mock(HttpResponse.class);
      client = mock(AsyncHttpClient.class);
      reference = new AsyncHttpClientResourceReference(client, URI.create("/root"));
   }
}

