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
import com.vanillasource.gerec.Response;
import static com.vanillasource.gerec.http.CacheControl.*;
import com.vanillasource.gerec.mediatype.MediaTypes;

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

      verify(getRequestedFor(urlEqualTo("/")).withHeader("Accept", equalTo("application/vnd.test.person;q=1")));
   }

   public void testUsingMatchUsesETag() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("{\"name\":\"John\", \"age\": 35}").withHeader("ETag","ABCD")));

      Response<Person> personResponse = reference().get(Person.TYPE);
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

   public void testPostCanHaveDifferentMediaTypesForContentAndAccept() {
      stubFor(post(urlEqualTo("/")).willReturn(aResponse().withBody("OK")));

      String result = reference().post(Person.TYPE, new Person("Jack", 50), MediaTypes.TEXT_PLAIN).getContent();

      verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("{\"name\":\"Jack\",\"age\":50}")));
      assertEquals(result, "OK");
   }
}


