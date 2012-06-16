package org.jboss.jdf.plugins.providers;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.DependencyFacet;

/**
 * Base class that installs a BOM to a Project pom.xml
 * and also can verifies if a BOM is installed
 *  
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 *
 */
public abstract class BaseJDFBomProvider implements JDFBOMProvider {
	
	private static final String GROUPID = "org.jboss.bom";
	
	/**
	 * This method looks for Dependencies only by GroupId and ArtfactId.
	 * It doesn't care about the version because it's planned to be used 
	 * in a update feature in future releases
	 * 
	 */
	@Override
	public boolean isDependencyManagementInstalled(Project project){
		DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
		Dependency dependency = DependencyBuilder.create(GROUPID + ":" + getArtfactId());
		return dependencyFacet.hasDirectManagedDependency(dependency);
	}

	@Override
	public void installBom(Project project, String version) {
		DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
		Dependency bom = DependencyBuilder.create(GROUPID + ":" + getArtfactId() + ":" + version + ":import:pom");
		dependencyFacet.addManagedDependency(bom);
	}
	
	public abstract String getArtfactId();	

}
