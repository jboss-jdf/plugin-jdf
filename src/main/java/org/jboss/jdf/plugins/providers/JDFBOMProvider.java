package org.jboss.jdf.plugins.providers;

import org.jboss.forge.project.Project;


/**
 * This is the contract interface for various JDF available stacks
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 *
 */
public interface JDFBOMProvider {

	public void installBom(Project project, String version);

	public boolean isDependencyManagementInstalled(Project project);
	
}
