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
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpStatusCode;
import com.vanillasource.gerec.mediatype.MediaTypes;
import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.HttpErrorException;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.gerec.http.ValueWithParameter;
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

   public void testSyncHeadInvokesHead() throws Exception {
      when(client.doHead(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(new byte[] {'T', 'E', 'S', 'T'}));

      reference.sync().headResponse();

      verify(client).doHead(any(), any());
   }

   public void testSyncOptionsInvokesOptions() throws Exception {
      when(client.doOptions(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.sync().optionsResponse();

      verify(client).doOptions(any(), any());
   }

   public void testSyncGetInvokesGet() throws Exception {
      when(client.doGet(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.sync().get(MediaType.NONE);

      verify(client).doGet(any(), any());
   }

   public void testSyncPostInvokesPost() throws Exception {
      when(client.doPost(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.sync().post(MediaType.NONE, null);

      verify(client).doPost(any(), any());
   }

   public void testSyncPutInvokesPut() throws Exception {
      when(client.doPut(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.sync().put(MediaType.NONE, null);

      verify(client).doPut(any(), any());
   }

   public void testSyncDeleteInvokesDelete() throws Exception {
      when(client.doDelete(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.sync().delete();

      verify(client).doDelete(any(), any());
   }

   @Test(expectedExceptions = HttpErrorException.class)
   public void testSyncOperationsAreTransparentForHttpErrorException() throws Exception {
      when(client.doGet(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.NOT_FOUND);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.sync().get(MediaType.NONE);
   }

   public void testHeadInvokesHead() throws Exception {
      when(client.doHead(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(new byte[] {'T', 'E', 'S', 'T'}));

      reference.headResponse().get();

      verify(client).doHead(any(), any());
   }

   public void testOptionsInvokesOptions() throws Exception {
      when(client.doOptions(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.optionsResponse().get();

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
      when(response.hasHeader(Headers.CONTENT_TYPE)).thenReturn(true);
      when(response.getHeader(Headers.CONTENT_TYPE)).thenReturn(new ValueWithParameter("text/plain"));
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(new byte[] {'T', 'E', 'S', 'T'}));

      String retrievedContent = reference.get(MediaTypes.textPlain()).join();

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

   public void testHeadConsumesContent() throws Exception {
      when(client.doHead(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(new byte[] {'T', 'E', 'S', 'T'}));

      reference.headResponse().join();

      verify(response).consumeContent(any());
   }

   public void testIfMediaTypeDoesNotMatchExceptionIsThrown() throws Exception {
      when(client.doGet(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference
         .get(MediaTypes.textPlain())
         .handle((content, exception) -> {
            assertNull(content);
            assertTrue(((CompletionException) exception).getCause() instanceof HttpErrorException);
            return null;
         })
         .get();
   }

   @SuppressWarnings("unchecked")
   public void testEvenIfMediaTypeThrowsExceptionResponseIsProcessed() throws Exception {
      AcceptMediaType<String> throwingType = mock(AcceptMediaType.class);
      when(throwingType.isHandling(any())).thenReturn(true);
      when(throwingType.deserialize(any(), any())).thenThrow(new IllegalStateException("test"));
      when(client.doGet(any(), any())).thenReturn(CompletableFuture.completedFuture(response));
      when(response.getStatusCode()).thenReturn(HttpStatusCode.OK);
      when(response.consumeContent(any())).thenReturn(CompletableFuture.completedFuture(null));

      reference.get(throwingType);

      verify(response).consumeContent(any());
   }

   @BeforeMethod
   protected void setUp() {
      response = mock(HttpResponse.class);
      client = mock(AsyncHttpClient.class);
      reference = new AsyncHttpClientResourceReference(client, URI.create("/root"));
   }
}

