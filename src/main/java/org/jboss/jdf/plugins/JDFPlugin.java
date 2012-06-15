/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.jdf.plugins;

import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.jdf.plugins.cdi.JDFVersions;
import org.jboss.jdf.plugins.providers.JDFBOMProvider;
import org.jboss.jdf.plugins.providers.JDFStackProvider;
import org.jboss.jdf.plugins.shell.JDFVersionCompleter;


/**
 * The JDF Plugin itself
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 */
@Alias("jdf")
@RequiresProject
public class JDFPlugin implements Plugin {
	
	@Inject @JDFVersions
	private List<String> jdfVersions;
	
	@Inject
	private ShellPrompt prompt;
	
	@Inject
	private Project project;
	
	@Inject
	private BeanManager beanManager;

	@DefaultCommand
	public void defaultCommand(@Option(name="stack", required=true) JDFStackProvider stack, 
			@Option(name="version", required=true, completer=JDFVersionCompleter.class) String version, PipeOut out) {
		//validate input
		if (!jdfVersions.contains(version)){
			out.println(ShellColor.RED, "There is no available version [" + version + "]. Try one of those: " + jdfVersions);
			return;
		}
		JDFBOMProvider jdfbomProvider = stack.getProvider(beanManager);
		if (jdfbomProvider.isDependencyManagementInstalled(project)){
			out.println("Stack " + stack + " already installed");
		}else{
			jdfbomProvider.installBom(project, version);
			out.println("Stack " + stack + " installed!");
		}
	}

}
