package org.jboss.jdf.plugins.providers;

import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;

public class JDFBOMProvider {

	private static final String GROUPID = "org.jboss.bom";
	
	@Inject
	private Project project;
	
	/**
	 * This method looks for Dependencies only by GroupId and ArtfactId.
	 * It doesn't care about the version because it's planned to be used 
	 * in a update feature in future releases
	 * 
	 */
	public boolean isDependencyManagementInstalled(String artfact){
		DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
		Dependency dependency = DependencyBuilder.create(GROUPID + ":" + artfact);
		return dependencyFacet.hasDirectManagedDependency(dependency);
	}
	
	public void installBom(String artfact, String version) {
		DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
		Dependency bom = DependencyBuilder.create(GROUPID + ":" + artfact + ":" + version + ":import:pom");
		dependencyFacet.addManagedDependency(bom);
	}
	
}
