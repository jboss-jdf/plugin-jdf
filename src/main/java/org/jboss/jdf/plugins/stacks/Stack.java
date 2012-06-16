package org.jboss.jdf.plugins.stacks;

import java.util.ArrayList;
import java.util.List;

public class Stack {

	private String id;

	private String name;

	private String description;

	private String artfact;

	private List<String> versions = new ArrayList<String>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public String getArtfact() {
		return artfact;
	}
	
	public void setArtfact(String artfact) {
		this.artfact = artfact;
	}
	
	public List<String> getVersions() {
		return versions;
	}

	public void setVersions(List<String> versions) {
		this.versions = versions;
	}
	
	@Override
	public String toString() {
		return this.getId();
	}
	
}
