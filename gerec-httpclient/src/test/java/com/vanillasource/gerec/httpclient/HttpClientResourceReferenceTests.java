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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.net.URI;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Arrays.asList;

@Test
public class HttpClientResourceReferenceTests {
   private HttpClientResourceReference reference;
   private CloseableHttpClient httpClient;
   private WireMockServer wireMock = new WireMockServer(wireMockConfig().port(8091));
   private HttpRequest.HttpRequestChange change;

   public void testGetResourceOkReturnsResponseOk() {
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = reference.doGet(change);

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testHeadResourceOkReturnsResponseOk() {
      stubFor(head(urlEqualTo("/nini")).willReturn(aResponse().withBody("")));

      HttpResponse response = reference.doHead(change);

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testPostResourceOkReturnsResponseOk() {
      stubFor(post(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = reference.doPost(change);

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testPutResourceOkReturnsResponseOk() {
      stubFor(put(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = reference.doPut(change);

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testDeleteResourceOkReturnsResponseOk() {
      stubFor(delete(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = reference.doDelete(change);

      assertTrue(response.getStatusCode() == HttpStatusCode.OK);
   }

   public void testChangeIsAppliedToRequest() {
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse().withBody("ABC")));

      HttpResponse response = reference.doGet(change);

      verify(change).applyTo(any(HttpRequest.class));
   }

   public void testLocationHeaderCanBeRead() {
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse().withHeader("Location", "nunu").withBody("ABC")));

      HttpResponse response = reference.doGet(change);

      assertEquals(response.getHeader(Headers.LOCATION), "nunu");
   }

   public void testLocationHeaderIsReadEvenIfNotCapitalized() {
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse().withHeader("location", "nunu").withBody("ABC")));

      HttpResponse response = reference.doGet(change);

      assertEquals(response.getHeader(Headers.LOCATION), "nunu");
   }

   public void testAllowsHeaderCanBeReadIfPresent() {
      stubFor(options(urlEqualTo("/nini")).willReturn(aResponse().withHeader("Allow", "GET, POST").withBody("ABC")));

      HttpResponse response = reference.doOptions(change);

      assertEquals(response.getHeader(Headers.ALLOW), asList("GET", "POST"));
   }

   @BeforeMethod
   protected void setUp() {
      change = mock(HttpRequest.HttpRequestChange.class);
      httpClient = HttpClientBuilder.create().build();
      reference = new HttpClientResourceReference(() -> httpClient, URI.create("http://localhost:8091/nini"));
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

