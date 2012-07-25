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
package org.jboss.jdf.plugins.providers;

import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;

public class JDFBOMProvider {

    @Inject
    private Project project;

    /**
     * This method looks for Dependencies only by GroupId and ArtfactId. It doesn't care about the version because it's planned
     * to be used in a update feature in future releases
     * 
     */
    public boolean isDependencyManagementInstalled(String groupId, String artifactId) {
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        Dependency dependency = DependencyBuilder.create(groupId + ":" + artifactId);
        return dependencyFacet.hasDirectManagedDependency(dependency) || dependencyFacet.hasEffectiveDependency(dependency);
    }

    public void installBom(String groupId, String artifactId, String version) {
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        Dependency bom = DependencyBuilder.create(groupId + ":" + artifactId + ":" + version + ":import:pom");
        dependencyFacet.addManagedDependency(bom);
    }

    public String getInstalledVersionStack(String groupId, String artifactId) {
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        Dependency dependency = DependencyBuilder.create(groupId + ":" + artifactId);
        Dependency effeDependency = dependencyFacet.getManagedDependency(dependency);
        return effeDependency.getVersion();
    }

    public void removeBom(String groupId, String artifactId, String version) {
        DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
        Dependency dependency = DependencyBuilder.create(groupId + ":" + artifactId + ":" + version + ":import:pom");
        dependencyFacet.removeManagedDependency(dependency);
    }

}
