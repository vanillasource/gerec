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

package com.vanillasource.gerec.forms;

import java.util.List;
import static java.util.Collections.emptyList;

public class FormComponent {
   private String name;
   private String selectedValue;
   private List<String> availableValues = emptyList();

   /**
    * For reflection-based frameworks.
    */
   protected FormComponent() {
   }

   public FormComponent(String name, List<String> availableValues) {
      this.name = name;
      this.availableValues = availableValues;
   }

   public FormComponent(String name) {
      this(name, emptyList());
   }

   public String getName() {
      return name;
   }

   public String getSelectedValue() {
      return selectedValue;
   }

   public void setSelectedValue(String selectedValue) {
      this.selectedValue = selectedValue;
   }

   /**
    * @return The available values for selection. If empty there is no
    * restriction on values.
    */
   public List<String> getAvailableValues() {
      return availableValues;
   }
}

