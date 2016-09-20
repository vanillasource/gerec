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
import com.vanillasource.gerec.mediatype.MediaTypes;

@Test
public class FormTests extends HttpTestsBase {
   public void testFormCanDeserializeCorrectly() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody(
                  "{\"greetingMessage\":\"Hello!\", \"searchForm\": {\"target\":{\"href\":\"/\"}, \"method\":\"GET\"}}")));

      SearchPage page = reference().get(SearchPage.TYPE).getContent();

      assertEquals(page.getGreetingMessage(), "Hello!");
   }

   public void testGetFormSubmitsWithoutParameters() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody(
                  "{\"greetingMessage\":\"Hello!\", \"searchForm\": {\"target\":{\"href\":\"/\"}, \"method\":\"GET\"}}")));

      SearchPage page = reference().get(SearchPage.TYPE).getContent();
      page.getSearchForm().submit(MediaTypes.TEXT_PLAIN);

      verify(getRequestedFor(urlEqualTo("/")));
   }

   public void testGetFormSubmitsWithParameters() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody(
                  "{\"greetingMessage\":\"Hello!\", \"searchForm\": {\"target\":{\"href\":\"/\"}, \"method\":\"GET\", "+
                  "\"components\":[ {\"name\":\"q\", \"selectedValue\":\"default\" } ]"+
                  "}}")));
      stubFor(get(urlEqualTo("/?q=default")).willReturn(aResponse()));

      SearchPage page = reference().get(SearchPage.TYPE).getContent();
      page.getSearchForm().submit(MediaTypes.TEXT_PLAIN);

      verify(getRequestedFor(urlEqualTo("/?q=default")));
   }

   public void testGetFormSubmitsWithiModifiedParameterValues() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody(
                  "{\"greetingMessage\":\"Hello!\", \"searchForm\": {\"target\":{\"href\":\"/\"}, \"method\":\"GET\", "+
                  "\"components\":[ {\"name\":\"q\", \"selectedValue\":\"default\" } ]"+
                  "}}")));
      stubFor(get(urlEqualTo("/?q=nini")).willReturn(aResponse()));

      SearchPage page = reference().get(SearchPage.TYPE).getContent();
      page.getSearchForm().getComponent("q").setSelectedValue("nini");
      page.getSearchForm().submit(MediaTypes.TEXT_PLAIN);

      verify(getRequestedFor(urlEqualTo("/?q=nini")));
   }

   public void testPostFormPostsParameterValues() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody(
                  "{\"greetingMessage\":\"Hello!\", \"searchForm\": {\"target\":{\"href\":\"/\"}, \"method\":\"POST\", "+
                  "\"components\":[ {\"name\":\"q\", \"selectedValue\":\"default\" } ]"+
                  "}}")));
      stubFor(post(urlEqualTo("/")).willReturn(aResponse()));

      SearchPage page = reference().get(SearchPage.TYPE).getContent();
      page.getSearchForm().submit(MediaTypes.TEXT_PLAIN);

      verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo("q=default")));
   }
}

