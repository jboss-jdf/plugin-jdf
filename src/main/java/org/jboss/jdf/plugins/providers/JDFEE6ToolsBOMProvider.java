package org.jboss.jdf.plugins.providers;

import org.jboss.forge.maven.MavenPluginFacet;
import org.jboss.forge.maven.plugins.MavenPlugin;
import org.jboss.forge.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;


public class JDFEE6ToolsBOMProvider extends BaseJDFBomProvider {
	
	private static String[] plugins = new String[]{
		"org.apache.maven.plugins:maven-surefire-plugin:2.10",
		"org.jboss.as.plugins:jboss-as-maven-plugin:7.1.1.Final"
	};

	@Override
	public String getArtfactId() {
		return "jboss-javaee-6.0-with-tools";
	}
	
	@Override
	public void installBom(Project project, String version) {
		super.installBom(project, version);
		MavenPluginFacet mavenPluginFacet = project.getFacet(MavenPluginFacet.class);
		for (String plugin: plugins){
			Dependency pluginDependency = DependencyBuilder.create(plugin);
			MavenPlugin mavenPlugin = MavenPluginBuilder.create().setDependency(pluginDependency);
			mavenPluginFacet.addPlugin(mavenPlugin);
		}
	}

}
