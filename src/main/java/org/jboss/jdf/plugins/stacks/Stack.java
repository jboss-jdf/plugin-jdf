package org.jboss.jdf.plugins.stacks;

public class Stack {

	private String id;

	private String name;

	private String description;

	private String artifact;

	public Stack(String id, String name, String description, String artifact) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.artifact = artifact;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getArtifact() {
		return artifact;
	}

}
