package org.jboss.jdf.plugins.stacks;

import javax.inject.Inject;

import org.jboss.forge.shell.Shell;


/**
 * This is a Utility class that handle the JDF BOMs
 * 
 * @author <a href="mailto:benevides@redhat.com">Rafael Benevides</a>
 *
 */
public class StacksUtil {
	
	@Inject
	private Shell shell;

	public void retrieveAvailableStacks() {
		 String defaultRepo = shell.getEnvironment().getProperties().toString();
		 shell.println(defaultRepo);
		
	}

}
