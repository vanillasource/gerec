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

package com.vanillasource.gerec.httpclient;

import com.vanillasource.gerec.*;
import com.vanillasource.gerec.http.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import java.net.URI;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Arrays.asList;

@Test
public class AsyncApacheHttpClientITTests {
   private AsyncApacheHttpClient client;
   private URI requestURI = URI.create("http://localhost:8091/nini");
   private CloseableHttpAsyncClient httpClient;
   private WireMockServer wireMock = new WireMockServer(wireMockConfig().port(8091));
   private HttpRequest.HttpRequestChange change;

   public void testGetResourceOkReturnsResponseOk() throws Exception {
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = client.doGet(requestURI, change).get();

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testHeadResourceOkReturnsResponseOk() throws Exception {
      stubFor(head(urlEqualTo("/nini")).willReturn(aResponse().withBody("")));

      HttpResponse response = client.doHead(requestURI, change).get();

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testPostResourceOkReturnsResponseOk() throws Exception {
      stubFor(post(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = client.doPost(requestURI, change).get();

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testPutResourceOkReturnsResponseOk() throws Exception {
      stubFor(put(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = client.doPut(requestURI, change).get();

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testDeleteResourceOkReturnsResponseOk() throws Exception {
      stubFor(delete(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = client.doDelete(requestURI, change).get();

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testChangeIsAppliedToRequest() throws Exception {
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = client.doGet(requestURI, change).get();

      verify(change).applyTo(any(HttpRequest.class));
   }

   public void testLocationHeaderCanBeRead() throws Exception {
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse().withHeader("Location", "nunu").withBody("ABC")));

      HttpResponse response = client.doGet(requestURI, change).get();

      assertEquals(response.getHeader(Headers.LOCATION), "nunu");
   }

   public void testLocationHeaderIsReadEvenIfNotCapitalized() throws Exception {
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse().withHeader("location", "nunu").withBody("ABC")));

      HttpResponse response = client.doGet(requestURI, change).get();

      assertEquals(response.getHeader(Headers.LOCATION), "nunu");
   }

   public void testAllowsHeaderCanBeReadIfPresent() throws Exception {
      stubFor(options(urlEqualTo("/nini")).willReturn(aResponse().withHeader("Allow", "GET, POST").withBody("ABC")));

      HttpResponse response = client.doOptions(requestURI, change).get();

      assertEquals(response.getHeader(Headers.ALLOW), asList("GET", "POST"));
   }

   @BeforeMethod
   protected void setUp() {
      change = mock(HttpRequest.HttpRequestChange.class);
      httpClient = HttpAsyncClients.createDefault();
      httpClient.start();
      client = new AsyncApacheHttpClient(httpClient);
      WireMock.reset();
   }

   @AfterMethod
   protected void tearDown() throws Exception {
      httpClient.close();
   }

   @BeforeClass
   protected void startTests() {
      wireMock.start();
      WireMock.configureFor("localhost", 8091);
   }

   @AfterClass
   protected void stopTests() {
      wireMock.stop();
   }
}

