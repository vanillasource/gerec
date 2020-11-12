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
import java.util.function.Consumer;
import com.vanillasource.gerec.mediatype.MediaTypeSpecification;
import com.vanillasource.gerec.mediatype.MediaTypes;
import com.vanillasource.gerec.mediatype.LineBasedCollectionAcceptType;
import java.util.concurrent.CompletionException;

@Test
public class LongPollingTests extends HttpTestsBase {
   @SuppressWarnings("unchecked")
   public void testSimpleGetRequest() throws Exception {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.strings")
               .withBody("a\nb\n")));
      Consumer<String> consumer = mock(Consumer.class);

      reference().get(new LineBasedCollectionAcceptType<>(MediaTypeSpecification.mediaType("application/vnd.test.strings"),
               MediaTypes.textPlain(), consumer)).join();

      verify(consumer).accept("a");
      verify(consumer).accept("b");
   }

   @SuppressWarnings("unchecked")
   public void testItemsAfterAShortDelayStillGetProcessed() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.strings")
               .withFixedDelay(1000)
               .withBody("a\nb\n")));
      Consumer<String> consumer = mock(Consumer.class);

      reference().get(new LineBasedCollectionAcceptType<>(MediaTypeSpecification.mediaType("application/vnd.test.strings"),
               MediaTypes.textPlain(), consumer)).join();

      verify(consumer).accept("a");
      verify(consumer).accept("b");
   }

   @SuppressWarnings("unchecked")
   @Test(expectedExceptions = CompletionException.class)
   public void testPollTimeoutResultsInException() {
      stubFor(get(urlEqualTo("/")).willReturn(aResponse()
               .withHeader("Content-Type", "application/vnd.test.strings")
               .withChunkedDribbleDelay(2, 10000)
               .withBody("a\nb\n")));
      Consumer<String> consumer = mock(Consumer.class);

      reference().get(new LineBasedCollectionAcceptType<>(MediaTypeSpecification.mediaType("application/vnd.test.strings"),
               MediaTypes.textPlain(), consumer, 1000L)).join();

      verifyNoMoreInteractions(consumer);
   }
}

