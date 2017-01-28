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
import java.util.Map;
import java.util.HashMap;
import static com.vanillasource.gerec.http.ValueWithParameter.FORMAT;

@Test
public class ValueWithParameterTests {

   public void testMatchesValueWithoutParameters() {
      ValueWithParameter value = FORMAT.deserialize("text/plain");

      assertTrue(value.matchesValue(new ValueWithParameter("text/plain")));
   }

   public void testMatchesValueWithoutSpaces() {
      ValueWithParameter value = FORMAT.deserialize(" text/plain ");

      assertTrue(value.matchesValue(new ValueWithParameter("text/plain")));
   }

   public void testMatchesValueCaseInsensitively() {
      ValueWithParameter value = FORMAT.deserialize("text/PLAIN");

      assertTrue(value.matchesValue(new ValueWithParameter("text/plain")));
   }

   public void testMatchesValueWithoutQuotes() {
      ValueWithParameter value = FORMAT.deserialize("\"text/PLAIN\"");

      assertTrue(value.matchesValue(new ValueWithParameter("text/plain")));
   }

   public void testHasParameterKeyWithoutValue() {
      ValueWithParameter value = FORMAT.deserialize("text/plain;notreally");

      assertTrue(value.hasParameter("notreally"));
   }

   public void testParsesParameterKeyRemovesSpaces() {
      ValueWithParameter value = FORMAT.deserialize("text/plain; notreally ");

      assertTrue(value.hasParameter("notreally"));
   }

   public void testMakesParameterKeysLowerCase() {
      ValueWithParameter value = FORMAT.deserialize("text/plain;NOTREALLY");

      assertTrue(value.hasParameter("notreally"));
   }

   public void testParsesValuesToParameters() {
      ValueWithParameter value = FORMAT.deserialize("text/plain; q=1.0");

      assertEquals(value.getParameterValue("q", null), "1.0");
   }

   public void testParsesValuesToParametersRemovesQuotes() {
      ValueWithParameter value = FORMAT.deserialize("text/plain; q=\"1.0\"");

      assertEquals(value.getParameterValue("q", null), "1.0");
   }

   public void testParsedParameterValueHasCase() {
      ValueWithParameter value = FORMAT.deserialize("text/plain; q=AbC");

      assertEquals(value.getParameterValue("q", null), "AbC");
   }

   public void testSerializesValuesAndParameters() {
      Map<String, String> parameters = new HashMap<>();
      parameters.put("q","1.0");
      parameters.put("test", "TEST");
      ValueWithParameter value = new ValueWithParameter("Value", parameters);

      String headerValue = FORMAT.serialize(value);

      assertEquals(headerValue, "Value; test=TEST; q=1.0");
   }

   public void testParametersWithoutValuesWillOnlyHaveKeys() {
      Map<String, String> parameters = new HashMap<>();
      parameters.put("test", null);
      ValueWithParameter value = new ValueWithParameter("Value", parameters);

      String headerValue = FORMAT.serialize(value);

      assertEquals(headerValue, "Value; test");
   }

   public void testAddingParametersPreservesOriginal() {
      ValueWithParameter value = new ValueWithParameter("Value", "key", "value");
      ValueWithParameter value2 = value.addParameter("key2", "value2");

      assertFalse(value.hasParameter("key2"));
      assertTrue(value2.hasParameter("key2"));
   }
}

