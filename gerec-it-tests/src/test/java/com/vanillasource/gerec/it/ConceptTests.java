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

package com.vanillasource.gerec.it;

import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.Response;
import com.vanillasource.gerec.HttpErrorException;
import static com.vanillasource.gerec.http.CacheControl.*;
import com.vanillasource.gerec.mediatype.MediaTypes;
import com.vanillasource.gerec.mediatype.SameTypeAlternatives;
import com.vanillasource.gerec.mediatype.jackson.JacksonMediaType;
import static java.util.Arrays.asList;

@Test
public class ConceptTests extends HttpTestsBase {
   public void testSimpleGetRequest() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      Person person = reference().get(Person.TYPE).getContent();

      assertEquals(person, new Person("John", 35));
   }

   public void testSimpleGetSubmitsTheAcceptMediaType() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      Person person = reference().get(Person.TYPE).getContent();

      verify(getRequestedFor(urlEqualTo("/")).withHeader("Accept", equalTo("application/vnd.test.person; q=1")));
   }

   public void testUsingMatchUsesETag() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}").withHeader("ETag","ABCD")));

      ContentResponse<Person> personResponse = reference().get(Person.TYPE);
      reference().get(Person.TYPE, personResponse.ifMatch());

      verify(getRequestedFor(urlEqualTo("/")).withHeader("If-Match", equalTo("ABCD")));
   }

   public void testUsingMultipleRequestChanges() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}").withHeader("ETag","ABCD")));

      reference().get(Person.TYPE, maxAge(10).and(maxStale(10)));

      verify(getRequestedFor(urlEqualTo("/")).withHeader("Cache-Control", equalTo("max-age=10, max-stale=10")));
   }

   public void testPostSubmitsCorrectJsonData() {
      stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      reference().post(Person.TYPE, new Person("Jack", 50));

      verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("{\"name\":\"Jack\",\"age\":50}")));
   }

   public void testPutSubmitsCorrectJsonData() {
      stubFor(put(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      reference().put(Person.TYPE, new Person("Jack", 50));

      verify(putRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("{\"name\":\"Jack\",\"age\":50}")));
   }

   public void testEmptyDeleteReturnsOk() {
      stubFor(delete(urlEqualTo("/")).willReturn(aResponse()));

      reference().delete();

      verify(deleteRequestedFor(urlEqualTo("/")));
   }

   public void testPostCanHaveDifferentMediaTypesForContentAndAccept() {
      stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody("OK")));

      String result = reference().post(Person.TYPE, new Person("Jack", 50), MediaTypes.TEXT_PLAIN).getContent();

      verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("{\"name\":\"Jack\",\"age\":50}")));
      assertEquals(result, "OK");
   }

   public void testIndicatesAllowedIsPresentIfHeaderSupplied() {
      stubFor(options(urlEqualTo("/")).willReturn(aResponse().withHeader("Allow", "GET, POST").withBody("OK")));

      Response response = reference().options();

      assertTrue(response.hasAllow());
   }

   public void testIndicatesGetMethodAllowedWhenSupplied() {
      stubFor(options(urlEqualTo("/")).willReturn(aResponse().withHeader("Allow", "GET, POST").withBody("OK")));

      Response response = reference().options();

      assertTrue(response.isGetAllowed());
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testExceptionIfAlternativeIsNotFound() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withHeader("Content-type", "text/html; charset=UTF-8").withBody("OK")));

      reference().get(
            new SameTypeAlternatives<>(asList(
               new JacksonMediaType<>(Person.class, "application/vnd.test.person-v1"),
               new JacksonMediaType<>(Person.class, "application/vnd.test.person-v2"))));
   }

   public void testAlternativeContentTypeIsSelected() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withHeader("Content-type", "application/vnd.test.person-v1; charset=UTF-8").withBody("{\"name\":\"John\", \"age\": 35}")));

      Person person = reference().get(
            new SameTypeAlternatives<>(asList(
               new JacksonMediaType<>(Person.class, "application/vnd.test.person-v1"),
               new JacksonMediaType<>(Person.class, "application/vnd.test.person-v2")))).getContent();
   }

   @Test(expectedExceptions = HttpErrorException.class)
   public void testNotFoundIsThrownAsException() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(404)));

      reference().get(Person.TYPE);
   }

   public void testErrorBodyCanBeParsed() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(404).withBody("Nelson: Haha!")));

      try {
         reference().get(Person.TYPE);
         fail("should have thrown exception");
      } catch (HttpErrorException e) {
         assertEquals(e.getResponse().getBody(MediaTypes.TEXT_PLAIN), "Nelson: Haha!");
      }
   }

   public void testConnectionIsConsumedAfterCall() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      reference().get(Person.TYPE).getContent();

      assertEquals(0, connectionManager().getTotalStats().getLeased());
   }

   public void testConnectionIsConsumedAfterErrorEvenIfExceptionContentIsNotRequested() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(404).withBody("ERROR")));

      try {
         reference().get(Person.TYPE).getContent();
         fail("should have thrown error");
      } catch (HttpErrorException e) {
         // Ok
      }

      assertEquals(0, connectionManager().getTotalStats().getLeased());
   }
}


