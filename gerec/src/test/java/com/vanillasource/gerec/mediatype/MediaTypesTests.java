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

package com.vanillasource.gerec.mediatype;

import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.HttpRequest;
import java.util.function.Function;
import static com.vanillasource.gerec.mediatype.MediaTypes.*;
import com.vanillasource.gerec.http.Headers;
import com.vanillasource.aio.channel.UncontrollableByteArrayReadableByteChannelLeader;
import com.vanillasource.aio.channel.UncontrollableByteArrayWritableByteChannelLeader;
import com.vanillasource.aio.channel.WritableByteChannelLeader;
import com.vanillasource.aio.AioFollower;
import com.vanillasource.aio.channel.ReadableByteChannelLeader;
import com.vanillasource.gerec.http.ValueWithParameter;
import java.util.concurrent.CompletableFuture;

@Test
public class MediaTypesTests {
   private HttpResponse response;
   private HttpRequest request;
   private String requestContent;

   public void testAsciiStringIsDeserializedWithoutContentType() throws Exception {
      responseContent("abc", "UTF-8");

      String result = textPlain().deserialize(response, null).get();

      assertEquals(result, "abc");
   }

   public void testSerializingStringWritesUTFBytes() throws Exception {
      when(request.getHeader(Headers.CONTENT_TYPE)).thenReturn(new ValueWithParameter("text/plain"));

      textPlain().serialize("abc", request);

      assertEquals(requestContent, "abc");
   }

   public void testUtfStringIsDeserializedWithContentTypeWithoutCharset() throws Exception {
      responseContent("αβγ", "UTF-8");
      when(response.hasHeader(Headers.CONTENT_TYPE)).thenReturn(true);
      when(response.getHeader(Headers.CONTENT_TYPE)).thenReturn(new ValueWithParameter("text/plain", "charset", "UTF-8"));

      String result = textPlain().deserialize(response, null).get();

      assertEquals(result, "αβγ");
   }

   public void testLatin1StringIsDeserialized() throws Exception {
      responseContent("éáü", "ISO-8859-1");
      when(response.hasHeader(Headers.CONTENT_TYPE)).thenReturn(true);
      when(response.getHeader(Headers.CONTENT_TYPE)).thenReturn(new ValueWithParameter("text/plain", "charset", "ISO-8859-1"));

      String result = textPlain().deserialize(response, null).get();

      assertEquals(result, "éáü");
   }

   public void testUtfCharsetIsSetInRequest() {
      textPlain().applyAsContent(request);

      verify(request).setHeader(Headers.CONTENT_TYPE, new ValueWithParameter("text/plain", "charset", "UTF-8"));
   }

   @SuppressWarnings("unchecked")
   private void responseContent(String content, String encoding) {
      doAnswer(invocation -> {
         AioFollower<String> follower = ((Function<ReadableByteChannelLeader, AioFollower<String>>) invocation.getArguments()[0])
            .apply(new UncontrollableByteArrayReadableByteChannelLeader(content.getBytes(encoding)));
         follower.onReady();
         return CompletableFuture.completedFuture(follower.onCompleted());
      }).when(response).consumeContent(any());
   }

   @BeforeMethod
   @SuppressWarnings("unchecked")
   protected void setUp() {
      response = mock(HttpResponse.class);
      request = mock(HttpRequest.class);
      doAnswer(invocation -> {
         UncontrollableByteArrayWritableByteChannelLeader byteArrayLeader = new UncontrollableByteArrayWritableByteChannelLeader();
         AioFollower<Void> follower = ((Function<WritableByteChannelLeader, AioFollower<Void>>) invocation.getArguments()[0])
            .apply(byteArrayLeader);
         follower.onReady();
         follower.onCompleted();
         requestContent = new String(byteArrayLeader.toByteArray());
         return null;
      }).when(request).setByteProducer(any(), anyInt());
   }
}
