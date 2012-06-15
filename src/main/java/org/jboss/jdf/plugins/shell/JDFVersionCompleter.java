package org.jboss.jdf.plugins.shell;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;
import org.jboss.jdf.plugins.cdi.JDFVersions;

public class JDFVersionCompleter extends SimpleTokenCompleter {
	
	@Inject @JDFVersions
	private List<String> jdfVersions;
	
	@Override
	public List<String> getCompletionTokens() {
		return jdfVersions;
	}

}
