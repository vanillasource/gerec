/**
 * Copyright (C) 2020 VanillaSource
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

import static com.vanillasource.gerec.http.CacheControl.*;
import com.vanillasource.gerec.reference.RecordingAsyncHttpClient.RecordedRequest;
import com.vanillasource.gerec.HttpRequest;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import com.vanillasource.aio.channel.ByteArrayWritableByteChannelSlave;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

@Test
public final class SuspendingAsyncHttpClientTests {
   private RecordingAsyncHttpClient recordingClient;
   private SuspendingAsyncHttpClient suspendingClient;

   public void testGetSuspendGetsExecutedCorrectly() throws Exception {
      suspendingClient.doGet(new URI("https://localhost:8086/api"),
            noCache().and(content("CONTENT")));

      suspendingClient = new SuspendingAsyncHttpClient(suspendingClient.suspend());
      suspendingClient.execute(recordingClient);

      assertEquals(recordingClient.lastRequest,
            new RecordedRequest("GET", "https://localhost:8086/api", header("Cache-Control","no-cache"), "CONTENT"));
   }

   public void testPostSuspendGetsExecutedCorrectly() throws Exception {
      suspendingClient.doPost(new URI("https://localhost:8086/api"),
            noCache().and(content("CONTENT")));

      suspendingClient = new SuspendingAsyncHttpClient(suspendingClient.suspend());
      suspendingClient.execute(recordingClient);

      assertEquals(recordingClient.lastRequest,
            new RecordedRequest("POST", "https://localhost:8086/api", header("Cache-Control","no-cache"), "CONTENT"));
   }

   public void testOptionsSuspendGetsExecutedCorrectly() throws Exception {
      suspendingClient.doOptions(new URI("https://localhost:8086/api"),
            noCache().and(content("CONTENT")));

      suspendingClient = new SuspendingAsyncHttpClient(suspendingClient.suspend());
      suspendingClient.execute(recordingClient);

      assertEquals(recordingClient.lastRequest,
            new RecordedRequest("OPTIONS", "https://localhost:8086/api", header("Cache-Control","no-cache"), "CONTENT"));
   }

   public void testDeleteSuspendGetsExecutedCorrectly() throws Exception {
      suspendingClient.doDelete(new URI("https://localhost:8086/api"),
            noCache().and(content("CONTENT")));

      suspendingClient = new SuspendingAsyncHttpClient(suspendingClient.suspend());
      suspendingClient.execute(recordingClient);

      assertEquals(recordingClient.lastRequest,
            new RecordedRequest("DELETE", "https://localhost:8086/api", header("Cache-Control","no-cache"), "CONTENT"));
   }

   public void testHeadSuspendGetsExecutedCorrectly() throws Exception {
      suspendingClient.doHead(new URI("https://localhost:8086/api"),
            noCache().and(content("CONTENT")));

      suspendingClient = new SuspendingAsyncHttpClient(suspendingClient.suspend());
      suspendingClient.execute(recordingClient);

      assertEquals(recordingClient.lastRequest,
            new RecordedRequest("HEAD", "https://localhost:8086/api", header("Cache-Control","no-cache"), "CONTENT"));
   }

   public void testPutSuspendGetsExecutedCorrectly() throws Exception {
      suspendingClient.doPut(new URI("https://localhost:8086/api"),
            noCache().and(content("CONTENT")));

      suspendingClient = new SuspendingAsyncHttpClient(suspendingClient.suspend());
      suspendingClient.execute(recordingClient);

      assertEquals(recordingClient.lastRequest,
            new RecordedRequest("PUT", "https://localhost:8086/api", header("Cache-Control","no-cache"), "CONTENT"));
   }

   private Map<String, List<String>> header(String key, String value) {
      Map<String, List<String>> headers = new HashMap<>();
      headers.put(key.toUpperCase(), asList(value));
      return headers;
   }

   private HttpRequest.HttpRequestChange content(String content) {
      byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
      return request ->
         request.setByteProducer(output -> new ByteArrayWritableByteChannelSlave(output, contentBytes), contentBytes.length);
   }

   @BeforeMethod
   protected void setUp() {
      recordingClient = new RecordingAsyncHttpClient();
      suspendingClient = new SuspendingAsyncHttpClient();
   }
}
