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
import java.util.concurrent.ExecutionException;
import static java.util.Arrays.asList;

@Test
public class ConceptTests extends HttpTestsBase {
   public void testSimpleGetRequest() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      Person person = reference().get(Person.TYPE).get().getContent();

      assertEquals(person, new Person("John", 35));
   }

   public void testSimpleGetSubmitsTheAcceptMediaType() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      Person person = reference().get(Person.TYPE).get().getContent();

      verify(getRequestedFor(urlEqualTo("/")).withHeader("Accept", equalTo("application/vnd.test.person")));
   }

   public void testUsingMatchUsesETag() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}").withHeader("ETag","ABCD")));

      ContentResponse<Person> personResponse = reference().get(Person.TYPE).get();
      reference().get(Person.TYPE, personResponse.ifMatch());

      verify(getRequestedFor(urlEqualTo("/")).withHeader("If-Match", equalTo("ABCD")));
   }

   public void testUsingMultipleRequestChanges() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}").withHeader("ETag","ABCD")));

      reference().get(Person.TYPE, maxAge(10).and(maxStale(10))).get();

      verify(getRequestedFor(urlEqualTo("/")).withHeader("Cache-Control", equalTo("max-age=10, max-stale=10")));
   }

   public void testPostSubmitsCorrectJsonData() throws Exception {
      stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      reference().post(Person.TYPE, new Person("Jack", 50)).get();

      verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("{\"name\":\"Jack\",\"age\":50}")));
   }

   public void testPutSubmitsCorrectJsonData() throws Exception {
      stubFor(put(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}")));

      reference().put(Person.TYPE, new Person("Jack", 50)).get();

      verify(putRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("{\"name\":\"Jack\",\"age\":50}")));
   }

   public void testEmptyDeleteReturnsOk() throws Exception {
      stubFor(delete(urlEqualTo("/")).willReturn(aResponse()));

      reference().delete().get();

      verify(deleteRequestedFor(urlEqualTo("/")));
   }

   public void testPostCanHaveDifferentMediaTypesForContentAndAccept() throws Exception {
      stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody("OK")));

      String result = reference().post(Person.TYPE, new Person("Jack", 50), MediaTypes.textPlain()).get().getContent();

      verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("{\"name\":\"Jack\",\"age\":50}")));
      assertEquals(result, "OK");
   }

   public void testIndicatesAllowedIsPresentIfHeaderSupplied() throws Exception {
      stubFor(options(urlEqualTo("/")).willReturn(aResponse().withHeader("Allow", "GET, POST").withBody("OK")));

      Response response = reference().options().get();

      assertTrue(response.hasAllow());
   }

   public void testIndicatesGetMethodAllowedWhenSupplied() throws Exception {
      stubFor(options(urlEqualTo("/")).willReturn(aResponse().withHeader("Allow", "GET, POST").withBody("OK")));

      Response response = reference().options().get();

      assertTrue(response.isGetAllowed());
   }

   public void testExceptionIfAlternativeIsNotFound() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withHeader("Content-type", "text/html; charset=UTF-8").withBody("OK")));

      try {
         reference().get(
               new SameTypeAlternatives<>(asList(
                  new JacksonMediaType<>(Person.class, "application/vnd.test.person-v1"),
                  new JacksonMediaType<>(Person.class, "application/vnd.test.person-v2"))))
            .get();
         fail("should have thrown exception");
      } catch (ExecutionException e) {
         assertEquals(e.getCause().getClass(), IllegalStateException.class);
      }
   }

   public void testAlternativeContentTypeIsSelected() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withHeader("Content-type", "application/vnd.test.person-v1; charset=UTF-8").withBody("{\"name\":\"John\", \"age\": 35}")));

      Person person = reference().get(
            new SameTypeAlternatives<>(asList(
               new JacksonMediaType<>(Person.class, "application/vnd.test.person-v1"),
               new JacksonMediaType<>(Person.class, "application/vnd.test.person-v2")))).get().getContent();
   }

   public void testNotFoundIsThrownAsException() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(404)));

      try {
         reference().get(Person.TYPE).get();
      } catch (ExecutionException e) {
         assertEquals(e.getCause().getClass(), HttpErrorException.class);
      }
   }

   public void testErrorBodyCanBeParsed() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(404).withBody("Nelson: Haha!")));

      try {
         reference().get(Person.TYPE).get();
         fail("should have thrown exception");
      } catch (ExecutionException rawE) {
         HttpErrorException e = (HttpErrorException) rawE.getCause();
         assertEquals(e.getResponse().getBody(MediaTypes.textPlain()), "Nelson: Haha!");
      }
   }
}


