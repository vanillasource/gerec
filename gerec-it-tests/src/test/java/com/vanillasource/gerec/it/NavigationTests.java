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

package com.vanillasource.gerec.it;

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.vanillasource.gerec.mediatype.MediaTypes;
import com.vanillasource.gerec.navigation.Navigation;
import java.util.Optional;

@Test
public class NavigationTests extends HttpTestsBase {
   public void testSearchForFirstAdult() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.searchpage")
               .withBody("{\"greetingMessage\":\"Hello!\", \"searchForm\": {\"href\":\"/1\"}}")));
      stubFor(get(urlEqualTo("/1?q=all+persons")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.resultspage")
               .withBody("{\"hit1\": {\"href\":\"/person1\"}, \"hit2\": {\"href\":\"/person2\"}, \"nextPage\":{\"href\":\"/2?q=all+persons\"}}")));
      stubFor(get(urlEqualTo("/2?q=all+persons")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.resultspage")
               .withBody("{\"hit1\": {\"href\":\"/person3\"}, \"hit2\": {\"href\":\"/person4\"}}")));
      stubFor(get(urlEqualTo("/person1")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.person")
               .withBody("{\"name\":\"Jack\", \"age\": 15}")));
      stubFor(get(urlEqualTo("/person2")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.person")
               .withBody("{\"name\":\"Jill\", \"age\": 16}")));
      stubFor(get(urlEqualTo("/person3")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.person")
               .withBody("{\"name\":\"John\", \"age\": 49}")));
      stubFor(get(urlEqualTo("/person4")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.person")
               .withBody("{\"name\":\"Jane\", \"age\": 3}")));

      Optional<Person> personMaybe = new Navigation<Person>(reference("/"))
         .navigate(SearchPage.TYPE, (searchPage, context) -> context.follow(searchPage.search("all persons")))
         .navigate(ResultsPage.TYPE, (resultsPage, context) -> context.first(resultsPage.iterate()))
         .navigate(Person.TYPE, (person, context) -> {
            if (person.isAdult()) {
               return context.finish(person);
            } else {
               return context.back();
            }
         })
         .execute()
         .join();

      assertTrue(personMaybe.isPresent());
      assertEquals(personMaybe.get(), new Person("John", 49));
   }
}

