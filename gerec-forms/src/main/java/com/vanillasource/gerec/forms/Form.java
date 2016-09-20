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

import com.vanillasource.gerec.AcceptMediaType;
import com.vanillasource.gerec.ContentMediaType;
import com.vanillasource.gerec.ContentResponse;
import com.vanillasource.gerec.ResourceReference;
import com.vanillasource.gerec.mediatype.MediaTypes;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;
import java.util.function.Function;
import static java.util.Collections.emptyList;

/**
 * Represents a form which can be submitted. To use this Form, just include it
 * in your protocol object, or deserialize it in your <code>AcceptMediaType</code>.
 */
public final class Form {
   private ResourceReference target;
   private Method method;
   private List<FormComponent> components = emptyList();
   private transient Function<URI, ResourceReference> referenceResolver;

   /**
    * For reflection based frameworks.
    */
   protected Form() {
   }

   Form(ResourceReference target, Method method, List<FormComponent> components) {
      this.target = target;
      this.method = method;
      this.components = components;
   }

   void setReferenceResolver(Function<URI, ResourceReference> referenceResolver) {
      this.referenceResolver = referenceResolver;
   }

   public <T> ContentResponse<T> submit(AcceptMediaType<T> acceptType) {
      return method.applyTo(this, acceptType);
   }

   private Map<String, String> collectFormData() {
      Map<String, String> data = new HashMap<>();
      for (FormComponent component: components) {
         if (component.getSelectedValue() != null) {
            data.put(component.getName(), component.getSelectedValue());
         }
      }
      return data;
   }

   public ResourceReference getTarget() {
      return target;
   }

   public Method getMethod() {
      return method;
   }

   public List<FormComponent> getComponents() {
      return components;
   }

   public FormComponent getComponent(String name) {
      for (FormComponent component: components) {
         if (component.getName().equals(name)) {
            return component;
         }
      }
      throw new IllegalArgumentException("no such component: "+name);
   }

   enum Method {
      GET {
         @Override
         public <T> ContentResponse<T> applyTo(Form form, AcceptMediaType<T> acceptType) {
            String queryString = parameterString(form.collectFormData());
            URI targetURI = form.target.toURI();
            String parameterConcatenator = "?";
            if (targetURI.getQuery() != null) {
               parameterConcatenator = "&";
            }
            return form.referenceResolver.apply(URI.create(targetURI.toString()+parameterConcatenator+queryString)).get(acceptType);
         }
      },
      POST {
         @Override
         public <T> ContentResponse<T> applyTo(Form form, AcceptMediaType<T> acceptType) {
            String postString = parameterString(form.collectFormData());
            return form.target.post(MediaTypes.FORM_URLENCODED, postString, acceptType);
         }
      };

      private static String parameterString(Map<String, String> formData) {
         StringBuilder builder = new StringBuilder();
         for (Map.Entry<String, String> parameter : formData.entrySet()) {
            if (builder.length() > 0) {
               builder.append("&");
            }
            builder.append(parameter.getKey());
            builder.append("=");
            builder.append(parameter.getValue());
         }
         return builder.toString();
      }

      public abstract <T> ContentResponse<T> applyTo(Form form, AcceptMediaType<T> acceptType);
   }
}

