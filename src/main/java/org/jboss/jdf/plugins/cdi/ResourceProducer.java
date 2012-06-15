package org.jboss.jdf.plugins.cdi;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Produces;

public class ResourceProducer {

	public String[] jdfBomVersions = new String[]{"1.0.0.Final"};
	
	
	@Produces @JDFVersions
	public List<String> getJdfBomVersions() {
		return Arrays.asList(jdfBomVersions);
	}
}
