/**
 * Copyright (C) 2019 VanillaSource
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

package com.vanillasource.gerec.reference;

import org.testng.annotations.*;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import java.util.function.Predicate;
import java.net.URI;
import java.net.URISyntaxException;

@Test
public final class UriPredicatesTests {
   public void testSecondLevelMatchesSecondLevel() throws URISyntaxException {
      Predicate<URI> p = UriPredicates.sameSecondLevelDomain(new URI("http://same.second.level:8080/path"));

      assertTrue(p.test(new URI("http://other.second.level:8080/path")));
   }

   public void testSecondLevelDoesNotMatchOtherSecondLevel() throws URISyntaxException {
      Predicate<URI> p = UriPredicates.sameSecondLevelDomain(new URI("http://same.second.level:8080/path"));

      assertFalse(p.test(new URI("http://other.different-second.level:8080/path")));
   }

   public void testSecondLevelDoesNotMatchOtherTopLevel() throws URISyntaxException {
      Predicate<URI> p = UriPredicates.sameSecondLevelDomain(new URI("http://same.second.level:8080/path"));

      assertFalse(p.test(new URI("http://other.second.other-level:8080/path")));
   }
}
