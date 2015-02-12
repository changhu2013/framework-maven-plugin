package com.riambsoft.maven.plugins.unzip.mojos;

public class Extract {

	private String groupId;
	private String artifactId;
	private String version;

	private String type;

	private String classifier;

	private String targetDirectory;

	public Extract() {
		super();
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type == null ? "zip" : type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassifier() {
		return classifier == null ? "" : classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public String toString() {
		return "groupId:" + getGroupId() + " artifactId:" + getArtifactId()
				+ " version:" + getVersion() + " type:" + getType()
				+ " classifier:" + getClassifier() + " targetDirectory:"
				+ getTargetDirectory();
	}

}
