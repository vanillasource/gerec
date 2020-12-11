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

import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.Request;
import com.vanillasource.gerec.mediatype.jackson.JacksonMediaType;
import com.vanillasource.gerec.form.Form;
import com.vanillasource.gerec.ResourceReference;
import java.util.List;
import java.util.ArrayList;
import static java.util.Collections.singletonList;
import static java.util.Collections.emptyList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Collectors.toList;
import java.util.Collections;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ResultsPage {
   public static final AcceptMediaType<ResultsPage> TYPE = new JacksonMediaType<>(ResultsPage.class, "application/vnd.test.resultspage");
   private ResourceReference hit1;
   private ResourceReference hit2;
   private ResourceReference nextPage;
   private ResourceReference prevPage;

   protected ResultsPage() {
   }

   public List<Request> iterate() {
      return forwardLinks().stream()
         .map(ResourceReference::prepareGet)
         .collect(toList());
   }
   
   private List<ResourceReference> forwardLinks() {
      List<ResourceReference> references = new ArrayList<>();
      if (hit1 != null) {
         references.add(hit1);
      }
      if (hit2 != null) {
         references.add(hit2);
      }
      if (nextPage != null) {
         references.add(nextPage);
      }
      return references;
   }
}

