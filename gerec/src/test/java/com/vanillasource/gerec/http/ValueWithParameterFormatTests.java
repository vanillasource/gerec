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

@Test
public class ValueWithParameterFormatTests {
   private ValueWithParameterFormat format = new ValueWithParameterFormat();

   public void testParsesValueWithoutParameters() {
      ValueWithParameter value = format.deserialize("text/plain");

      assertEquals(value.getValue(), "text/plain");
   }

   public void testParsesValueWithoutSpaces() {
      ValueWithParameter value = format.deserialize(" text/plain ");

      assertEquals(value.getValue(), "text/plain");
   }

   public void testMakesValueLowerCase() {
      ValueWithParameter value = format.deserialize("text/PLAIN");

      assertEquals(value.getValue(), "text/plain");
   }

   public void testRemovesQuotesFromValue() {
      ValueWithParameter value = format.deserialize("\"text/PLAIN\"");

      assertEquals(value.getValue(), "text/plain");
   }

   public void testParsesParameterKeyWithoutValue() {
      ValueWithParameter value = format.deserialize("text/plain;notreally");

      assertTrue(value.hasParameter("notreally"));
   }

   public void testParsesParameterKeyRemovesSpaces() {
      ValueWithParameter value = format.deserialize("text/plain; notreally ");

      assertTrue(value.hasParameter("notreally"));
   }

   public void testMakesParameterKeysLowerCase() {
      ValueWithParameter value = format.deserialize("text/plain;NOTREALLY");

      assertTrue(value.hasParameter("notreally"));
   }

   public void testParsesValuesToParameters() {
      ValueWithParameter value = format.deserialize("text/plain; q=1.0");

      assertEquals(value.getParameterValue("q"), "1.0");
   }

   public void testParsesValuesToParametersRemovesQuotes() {
      ValueWithParameter value = format.deserialize("text/plain; q=\"1.0\"");

      assertEquals(value.getParameterValue("q"), "1.0");
   }

   public void testParsedParameterValueHasCase() {
      ValueWithParameter value = format.deserialize("text/plain; q=AbC");

      assertEquals(value.getParameterValue("q"), "AbC");
   }

   public void testSerializesValuesAndParameters() {
      Map<String, String> parameters = new HashMap<>();
      parameters.put("q","1.0");
      parameters.put("test", "TEST");
      ValueWithParameter value = new ValueWithParameter("Value", parameters);

      String headerValue = format.serialize(value);

      assertEquals(headerValue, "Value; q=1.0; test=TEST");
   }

   public void testParametersWithoutValuesWillOnlyHaveKeys() {
      Map<String, String> parameters = new HashMap<>();
      parameters.put("test", null);
      ValueWithParameter value = new ValueWithParameter("Value", parameters);

      String headerValue = format.serialize(value);

      assertEquals(headerValue, "Value; test");
   }
}

