package com.riambsoft.maven.plugins.zip.mojos;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import com.riambsoft.maven.plugins.zip.data.ZipMeteData;
import com.riambsoft.maven.plugins.zip.utils.ZipUtil;

public class ZipMojo extends AbstractMojo {

	/**
	 * The Maven project.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * Maven ProjectHelper.
	 * 
	 * @component
	 */
	protected MavenProjectHelper projectHelper;

	/**
	 * @parameter expression = "${project.build.directory}"
	 * @required
	 */
	private File directory;

	/**
	 * 要包含的文件
	 * 
	 * @parameter
	 */
	private String[] includes;

	/**
	 * 不包含文件
	 * 
	 * @parameter
	 */
	private String[] excludes;

	/**
	 * @parameter expression = "${project.build.finalName}"
	 */
	private String finalName;

	public void execute() throws MojoExecutionException, MojoFailureException {

	}

	public File doAssemblyZip(String actionType) throws MojoExecutionException {

		File zip = null;

		String sourceDir = directory.getAbsolutePath() + File.separator
				+ project.getArtifactId();

		String zipFile = directory.getAbsolutePath(); // 目标目录

		String defaultFileName = finalName;

		try {
			ZipMeteData zipMeteData = new ZipMeteData();
			zipMeteData.setSourcePath(sourceDir); // ..../target/子系统
			zipMeteData.setTargetPath(zipFile);
			zipMeteData.setSystemName(project.getArtifactId());
			zipMeteData.setIncludes(includes);
			zipMeteData.setExcludes(excludes);
			zipMeteData.setActionType(actionType);
			zipMeteData.setLog(getLog());

			ZipUtil zipUtil = new ZipUtil();
			zipUtil.setMeteData(zipMeteData);

			zip = zipUtil.convertZip(defaultFileName);

			getLog().info(zip.getName() + "打包成功");

		} catch (Exception e) {
			throw new MojoExecutionException( zip.getName() + "打包不成功 : " + e.getMessage() , e );
		}
		return zip;
	}
}
