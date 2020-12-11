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

import com.vanillasource.gerec.MediaType;
import com.vanillasource.gerec.mediatype.jackson.JacksonMediaType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Person {
   public static final MediaType<Person> MEDIA_TYPE = new JacksonMediaType<>(Person.class, "application/vnd.test.person");
   private String name;
   private int age;

   protected Person() {
   }

   public Person(String name, int age) {
      this.name = name;
      this.age = age;
   }

   @JsonIgnore
   public boolean isAdult() {
      return age >= 18;
   }

   public boolean equals(Object that) {
      if ((that == null) || (!(that instanceof Person))) {
         return false;
      }
      Person p = (Person) that;
      return age == p.age && name.equals(p.name);
   }
}
