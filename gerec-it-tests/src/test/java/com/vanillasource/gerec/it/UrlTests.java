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
public class UrlTests extends HttpTestsBase {
   public void testCanFollowAsciiLink() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.link")
               .withBody("{\"link\": { \"href\": \"/nini\" }}")));
      stubFor(get(urlEqualTo("/nini")).willReturn(aResponse()
               .withHeader("Content-Type", "text/plain")));

      Link link = reference().get(Link.TYPE).join();
      link.getLink().get(MediaTypes.textPlain()).get();

      verify(getRequestedFor(urlEqualTo("/nini")));
   }

   public void testCanFollowLinkWithSpaceEncoded() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.link")
               .withBody("{\"link\": { \"href\": \"/ni%20ni\" }}")));
      stubFor(get(urlEqualTo("/ni%20ni")).willReturn(aResponse()
               .withHeader("Content-Type", "text/plain")));

      Link link = reference().get(Link.TYPE).join();
      link.getLink().get(MediaTypes.textPlain()).get();

      verify(getRequestedFor(urlEqualTo("/ni%20ni")));
   }
}

