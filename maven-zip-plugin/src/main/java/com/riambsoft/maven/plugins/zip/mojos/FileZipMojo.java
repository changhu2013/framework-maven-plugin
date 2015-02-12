package com.riambsoft.maven.plugins.zip.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal filezip
 * @execute goal="compression"
 */
public class FileZipMojo extends ZipMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("开始进行【filezip】 打包");
		doAssemblyZip("filezip");
	}
}
