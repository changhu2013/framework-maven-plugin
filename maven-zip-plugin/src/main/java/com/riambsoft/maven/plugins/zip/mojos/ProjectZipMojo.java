package com.riambsoft.maven.plugins.zip.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal projectzip
 * @execute goal="compression"
 */
public class ProjectZipMojo extends ZipMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("开始进行【projectzip】 打包");
		projectHelper.attachArtifact(project, "zip", null,
				doAssemblyZip("projectzip"));
	}
}
