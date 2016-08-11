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

import com.vanillasource.gerec.HttpResponse;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.HttpStatusCode;
import com.vanillasource.gerec.Header;
import java.net.URI;
import java.util.function.Function;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.UncheckedIOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LineBasedCollectionType<T> extends NamedAcceptType<Processor<T>> {
   private AcceptMediaType<T> acceptType;

   public LineBasedCollectionType(String mediaTypeName, double qualityValue, AcceptMediaType<T> acceptType) {
      super(mediaTypeName, qualityValue);
      this.acceptType = acceptType;
   }

   public LineBasedCollectionType(String mediaTypeName, AcceptMediaType<T> acceptType) {
      super(mediaTypeName);
      this.acceptType = acceptType;
   }

   @Override
   public Processor<T> deserialize(HttpResponse response, Function<URI, ResourceReference> referenceProducer) {
      return consumer -> {
         response.processContent(inputStream -> {
            try {
               try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
                  String line;
                  while ((line = reader.readLine()) != null) {
                     String thisLine = line;
                     if (line.length() > 0) {
                        consumer.accept(acceptType.deserialize(new HttpResponse() {
                           @Override
                           public HttpStatusCode getStatusCode() {
                              return response.getStatusCode();
                           }

                           @Override
                           public boolean hasHeader(Header header) {
                              return response.hasHeader(header);
                           }

                           @Override
                           public String getHeader(Header header) {
                              return response.getHeader(header);
                           }

                           @Override
                           public <T> T processContent(Function<InputStream, T> contentProcessor) {
                              try {
                                 return contentProcessor.apply(new ByteArrayInputStream(thisLine.getBytes("UTF-8")));
                              } catch (UnsupportedEncodingException e) {
                                 throw new UncheckedIOException(e);
                              }
                           }
                        }, referenceProducer));
                     }
                  }
               }
            } catch (IOException e) {
               throw new UncheckedIOException(e);
            }
         });
      };
   }
}
