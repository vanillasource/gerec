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

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import com.vanillasource.gerec.mediatype.MediaTypes;

@Test
public class SuspendTests extends HttpTestsBase {
   public void testSuspendDoesNotCall() {
      reference().prepareGet().suspend();
   }

   public void testSuspendedCallCanBeExecuted() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.person")
               .withBody("{\"name\":\"John\", \"age\": 35}")));
      byte[] suspendedCall = reference().prepareGet().suspend();

      Person person = reference().resume(suspendedCall, Person.MEDIA_TYPE).join();

      assertEquals(person, new Person("John", 35));
   }

   public void testPostFormCanBeSuspendedAndThenExecuted() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.searchpage")
               .withBody("{\"greetingMessage\":\"Hello!\", \"searchForm\": {\"target\":\"/\", \"method\":\"POST\"}}")));
      stubFor(post(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "text/plain")));
      SearchPage page = reference().get(SearchPage.MEDIA_TYPE).join();
      byte[] suspendedCall = page.search("nini").suspend();

      reference().resume(suspendedCall, MediaTypes.textPlain()).join();

      verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("q=nini")));
   }

   public void testRequestAfterExecuteIsRelativeToExecute() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.searchpage")
               .withBody("{\"greetingMessage\":\"Hello!\", \"searchForm\": {\"target\":\"/\", \"method\":\"POST\"}}")));
      stubFor(post(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "text/plain")));

      byte[] suspendedCall = reference().prepareGet().suspend();
      SearchPage page = reference("http://some.other.server:8888").resume(suspendedCall, SearchPage.MEDIA_TYPE).join();
      page.search("nini")
         .send(MediaTypes.textPlain())
         .join();

      verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("q=nini")));
   }
}
