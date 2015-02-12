package com.riambsoft.maven.plugins.zip.mojos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.riambsoft.maven.plugins.zip.utils.FileUtil;

/**
 * 合并带有文件清单的项目
 * 
 * @goal mergewithlist
 * @execute phase="clean"   并行执行一个clean的生命周期
 */
public class MergeMojoWithList extends AbstractMojo{

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
		
		//欲合并的清单文件放在src/main/merginglist文件夹下
		String listPath = baseDir.getAbsolutePath() + File.separator + "src"
				+ File.separator + "main" + File.separator + "merginglist";
		String targetPath = baseDir.getAbsolutePath() + File.separator + "src"
				+ File.separator + "main"+ File.separator + "webapp" + File.separator + "js";
		File listD = new File(listPath);
		File[] mergeLists = listD.listFiles();
		for (File list : mergeLists) {
			if (list.isFile()) {
				String listName = list.getName();
				
				//清单文件格式 ：文件夹名_..._文件夹名_文件类型_list.txt
				String prefixName = listName.substring(0, listName.indexOf(".txt"));
				String[] ds = prefixName.split("_");
				String targetD = "",targetFN = "";
				for(int i=0; i<ds.length-2; i++){
					targetD += File.separator + ds[i];
					targetFN += ds[i]+"-";
				}
				File targetFile = new File(targetPath + targetD + File.separator + targetFN + "debug.js");
				List<String> sourceFiles = FileUtil.readFile(list);
				BufferedWriter tBW;
				try {
					//tBW = new BufferedWriter(new FileWriter(targetFile));
					tBW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile,false),"UTF-8"));
				
					for (String fileName : sourceFiles) {
						writeFile(baseDir.getAbsolutePath() + File.separator + "src"
								+ File.separator + "main" + File.separator + "webapp" + File.separator + "js" + File.separator + fileName, tBW);
					}
					tBW.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				continue;
			}
		}
	}
	
	public void writeFile(String from, BufferedWriter bw){
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(from));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(from),"UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				bw.newLine();
				bw.write(line);
				bw.flush(); 
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
