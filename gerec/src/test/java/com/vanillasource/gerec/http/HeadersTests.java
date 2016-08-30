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

package com.vanillasource.gerec.http;

import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import static com.vanillasource.gerec.http.Headers.*;
import java.util.List;
import static java.util.Arrays.asList;

@Test
public class HeadersTests {
   public void testSingleStringValueSerializesIntoSingletonList() {
      List<String> values = singleStringHeader("Test").serialize("Value");

      assertEquals(values, asList("Value"));
   }

   public void testSingleStringValueDeserializesASingleValue() {
      String value = singleStringHeader("Test").deserialize(asList("Value"));

      assertEquals(value, "Value");
   }

   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testSingleStringValueDeserializeThrowsExceptionIfEmpty() {
      singleStringHeader("Test").deserialize(asList());
   }

   public void testCsvValuesSerializesIntoASingleCsvValue() {
      List<String> values = csvStringHeader("Test").serialize(asList("A", "B", "C"));

      assertEquals(values, asList("A, B, C"));
   }

   public void testCsvValuesDeserializeOneCsvValue() {
      List<String> values = csvStringHeader("Test").deserialize(asList("A, B, C"));

      assertEquals(values, asList("A", "B", "C"));
   }

   public void testCsvValuesDeserializeMultipleCsvValuesIntoSingleList() {
      List<String> values = csvStringHeader("Test").deserialize(asList("A, B", "C, D, E", "F"));

      assertEquals(values, asList("A", "B", "C", "D", "E", "F"));
   }
}

