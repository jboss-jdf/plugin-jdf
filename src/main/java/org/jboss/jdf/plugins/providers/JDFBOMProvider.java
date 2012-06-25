package org.jboss.jdf.plugins.providers;

import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;

public class JDFBOMProvider
{

   private static final String GROUPID = "org.jboss.bom";

   @Inject
   private Project project;

   /**
    * This method looks for Dependencies only by GroupId and ArtfactId. It doesn't care about the version because it's
    * planned to be used in a update feature in future releases
    * 
    */
   public boolean isDependencyManagementInstalled(String artifact)
   {
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      Dependency dependency = DependencyBuilder.create(GROUPID + ":" + artifact);
      return dependencyFacet.hasDirectManagedDependency(dependency) || dependencyFacet.hasEffectiveDependency(dependency);
   }

   public void installBom(String artifact, String version)
   {
      String parsedVersion = version.replaceAll("[*]", ""); //Removes the * (recommend version) tag
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      Dependency bom = DependencyBuilder.create(GROUPID + ":" + artifact + ":" + parsedVersion + ":import:pom");
      dependencyFacet.addManagedDependency(bom);
   }
   
   public String getInstalledVersionStack(String artifact){
      DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      Dependency dependency = DependencyBuilder.create(GROUPID + ":" + artifact);
      Dependency effeDependency = dependencyFacet.getManagedDependency(dependency);
      return effeDependency.getVersion();
   }

}
