package com.riambsoft.maven.plugins.zip.mojos;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.riambsoft.maven.plugins.zip.data.MergoMeteData;
import com.riambsoft.maven.plugins.zip.utils.MergoUitl;

/**
 * 压缩混淆文件
 * 
 * @author YHB
 * @goal compression
 * @execute phase="clean"   并行执行一个clean的生命周期
 */
public class CompressionMojo extends AbstractMojo {

	/**
	 * The Maven project.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * @parameter expression = "${project.basedir}"
	 * @required
	 * @readonly
	 */
	private File baseDir;

	/**
	 * @parameter expression = "${project.build.directory}"
	 * @required
	 */
	private File directory;

	public void execute() throws MojoExecutionException, MojoFailureException {

		String sourcePath = baseDir.getAbsolutePath() + File.separator + "src"
				+ File.separator + "main" + File.separator + "webapp";

		String targetPath = directory.getAbsolutePath();
		MergoUitl mergoUitl = new MergoUitl();
		MergoMeteData data = new MergoMeteData();
		data.setSourcePath(sourcePath);
		data.setTargetPath(targetPath);
		data.setSystemName(project.getArtifactId());
		data.setLog(getLog()) ;
		mergoUitl.setData(data);
		try {
			getLog().info("开始合并、压缩、混淆");
			mergoUitl.doCompressionFile();
			getLog().info("合并、压缩、混淆完成");
		} catch (Exception e) {
			throw new MojoExecutionException("合并、压缩、混淆出错  " + e.getMessage() , e );
		}
	}

}
