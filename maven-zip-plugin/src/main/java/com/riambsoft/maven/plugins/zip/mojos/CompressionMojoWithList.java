package com.riambsoft.maven.plugins.zip.mojos;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.riambsoft.maven.plugins.zip.utils.FileUtil;
import com.yahoo.platform.yui.compressor.YUICompressor;

/**
 * 根据清单压缩混淆文件
 * 
 * @goal compresswithlist
 * @execute phase="clean"   并行执行一个clean的生命周期
 */
public class CompressionMojoWithList extends AbstractMojo{

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
		
		String listPath = baseDir.getAbsolutePath() + File.separator + "src"
				+ File.separator + "main" + File.separator + "packinglist" 
				+ File.separator + "packing_list.txt";

		String targetPrefix = baseDir.getAbsolutePath() + File.separator + "src"
				+ File.separator + "main"+ File.separator + "webapp" + File.separator + "js";
		
		File listF = new File(listPath);
		List<String> sourceFiles = FileUtil.readFile(listF);
		for(String file : sourceFiles){
			String sourcePath = targetPrefix + File.separator + file;
			String targetPath = "";
			String[] rsjs;
			if(file.indexOf(".css")>0){
				targetPath = sourcePath.replace("-all.css","-mini.css");
				rsjs = new String[] { "--type", "css", "--charset", "utf-8",
					sourcePath, "-o", targetPath};
			} else{
				targetPath = sourcePath.replace("-debug.js","-mini.js");
				rsjs = new String[] { "--type", "js", "--charset", "utf-8",
						sourcePath, "-o", targetPath};
			}
			
			YUICompressor.main(rsjs);
		}
		
	}

}
