package com.riambsoft.maven.plugins.zip.mojos;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import com.riambsoft.maven.plugins.zip.utils.FileUtil;

/**
 * 打包文件
 * 
 * @goal zipwithlist
 */
public class ZipMojoWithList extends AbstractMojo {

	/**
	 * The Maven project.
	 * 
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * @parameter expression = "${project.build.directory}"
	 * @required
	 */
	private File directory;

	/**
	 * @parameter expression = "${project.build.finalName}"
	 */
	private String finalName;

	/**
	 * @parameter expression = "${project.basedir}"
	 * @required
	 * @readonly
	 */
	private File baseDir;
	
	/**
	 * @parameter expression = "${project.version}"
	 */
	private String version;

	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if(!directory.exists()){
			directory.mkdirs();
		}
		String targetD = directory.getAbsolutePath();
		
		
		
		String defaultFileName = targetD + File.separator + finalName + "-" + version + ".zip";
		String listPath = baseDir.getAbsolutePath() + File.separator + "src"
				+ File.separator + "main" + File.separator + "zippinglist"
				+ File.separator + "zipping_list.txt";

		OutputStream os = null;
		File zip = new File(defaultFileName);
		File listF = new File(listPath);
		List<String> sourceFiles = FileUtil.readFile(listF);
		try {
			os = new FileOutputStream(zip);// 打开一个写ZIP文件的输出流
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedOutputStream bos = new BufferedOutputStream(os);// 接上一个缓冲流、不用频繁读取文件
		JarOutputStream zos = null;
		try {
			zos = new JarOutputStream(bos);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		for (String filePath : sourceFiles) {
			zipFile(filePath, zos);
		}
		try {
			zos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param file
	 * @param zos
	 * @throws Exception
	 */
	private void zipFile(String filePath, ZipOutputStream zos){
		String targetPrefix = baseDir.getAbsolutePath() + File.separator
				+ "src" + File.separator + "main" + File.separator + "webapp"
				+ File.separator + "js";
		int bytesRead;
		byte[] buffer = new byte[1024];
		File file = new File(targetPrefix + File.separator + filePath);
		if (file.isDirectory()) {
			File[] subFiles = file.listFiles();
		    try {
				zos.putNextEntry(new ZipEntry(finalName + "/" + filePath + "/"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		    for (int i = 0; i < subFiles.length; i++) {
		    	zipFile(filePath + "/" + subFiles[i].getName(), zos);
		    }
		} else{
			BufferedInputStream bis;
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				ZipEntry entry = new ZipEntry(finalName + "/" + filePath);
				zos.putNextEntry(entry);
				while ((bytesRead = bis.read(buffer)) != -1) {
					zos.write(buffer, 0, bytesRead);
				}
				bis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
