/*
 * JBoss, Home of Professional Open Source
 * Copyright <Year>, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.jdf.plugins.stacks;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Stack
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 * 
 */
public class Stack
{

   private String id;

   private String name;

   private String description;

   private String artifact;

   private String recommendedVersion;

   private List<String> availableVersions = new ArrayList<String>();

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getArtifact()
   {
      return artifact;
   }

   public void setArtifact(String artfact)
   {
      this.artifact = artfact;
   }

   public String getRecommendedVersion()
   {
      return recommendedVersion;
   }

   public void setRecommendedVersion(String recommendedVersion)
   {
      this.recommendedVersion = recommendedVersion;
   }

   public List<String> getAvailableVersions()
   {
      return availableVersions;
   }

   public void setAvailableVersions(List<String> availableVersions)
   {
      this.availableVersions = availableVersions;
   }

   @Override
   public String toString()
   {
      return this.getId();
   }

}
