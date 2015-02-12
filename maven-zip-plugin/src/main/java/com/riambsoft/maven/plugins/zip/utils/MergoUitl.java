package com.riambsoft.maven.plugins.zip.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.riambsoft.maven.plugins.zip.data.MergoMeteData;
import com.yahoo.platform.yui.compressor.YUICompressor;

/**
 * 
 * @author YHB
 * 
 */
public class MergoUitl {

	private MergoMeteData data;

	private String moduleName;

	public void doMergoSource() throws Exception {

		File targetfile = new File(data.getTargetPath() + File.separator
				+ data.getSystemName());
		if (targetfile.exists()) {
			targetfile.delete();
		}

		File[] files = getAllModule(data.getSourcePath()); // 获得所有模块
		File rJsfile = null;
		List<String> filesPath;
		for (File f : files) {
			if (f.isFile()) {
				copySystemJspFile(f);
				continue;
			}
			rJsfile = getModuleRJs(f); // 根据模块获得所有R.js文件 f表示模块
			if (rJsfile == null) {
				if ("WEB-INF".equals(f.getName())) {
					continue;
				}
				if ("pub".equals(f.getName())) {
					continue;
				}
				if ("rs".equals(f.getName())) {
					continue;
				}
				data.getLog().info("【" + f.getName() + "】 模块下没有发现R.js文件");
			} else {
				filesPath = getFilePathByRJs(rJsfile); // 根据R.js文件获得该模块声明的JS文件
				if (filesPath.size() == 0) {
					throw new RuntimeException("【" + f.getName()
							+ "】 模块下R.js文件没有声明js文件");
				}
				moduleName = f.getName();
				mergoFile(filesPath); // 压缩文件
				modifyRJs("mergo"); // 修改R.js文件
				copyModuleResources();
			}
		}
		copyRsToTarget(data.getSourcePath());
	}

	public void doCompressionFile() throws Exception {
		File targetfile = new File(data.getTargetPath() + File.separator
				+ data.getSystemName());
		if (targetfile.exists()) {
			targetfile.delete();
		}
		File[] files = getAllModule(data.getSourcePath()); // 获得所有模块
		File rJsfile = null;
		List<String> filesPath;
		for (File f : files) {
			if (f.isFile()) {
				copySystemJspFile(f);
				continue;
			}

			rJsfile = getModuleRJs(f); // 根据模块获得所有R.js文件 f表示模块
			if (rJsfile == null) { // 如果是web-inf pub rs文件则跳过 不做压缩
				if ("WEB-INF".equals(f.getName())) {
					continue;
				}
				if ("pub".equals(f.getName())) {
					continue;
				}/*
				 * if ("resources".equals(f.getName())) { continue; }
				 */
				if ("rs".equals(f.getName())) {
					continue;
				}
				data.getLog().info(
						"【" + f.getName() + "】 模块下没有发现R.js文件,如果:" + f.getName()
								+ "不是模块,请忽略.");
			} else {
				filesPath = getFilePathByRJs(rJsfile); // 根据R.js文件获得该模块声明的JS文件
				if (filesPath.size() == 0) {
					throw new RuntimeException("【" + f.getName()
							+ "】 模块下R.js文件没有声明js文件");
				}
				moduleName = f.getName();
				mergoFile(filesPath); // 合并文件
				compressionFile();// 压缩
				modifyRJs("compression"); // 修改R.js文件
				copyModuleResources();
			}
		}
		copyRsToTarget(data.getSourcePath());
	}

	/**
	 * 获得所有模块
	 * 
	 * @param file
	 * @return
	 */
	public File[] getAllModule(String filepath) {
		File file = new File(filepath);
		return file.listFiles();
	}

	/**
	 * 根据模块获得该模块下的R.js文件
	 * 
	 * @param file
	 * @return
	 */
	public File getModuleRJs(File file) {

		if ("WEB-INF".equals(file.getName())) { // web-inf 不需要合并
			return null;
		}

		if ("pub".equals(file.getName())) { // web-inf 不需要合并
			return null;
		}

		File[] files = file.listFiles();
		for (File R : files) {
			if ("R.js".equals(R.getName())) {
				return R;
			}
		}
		return null;
	}

	/**
	 * 将子系统下的pub公共代码和资源文件复制到target/子系统下
	 * 
	 * @param file
	 *            []
	 * @return
	 * @throws IOException
	 */
	public void copyRsToTarget(String filePath) throws IOException {
		String targetDir = data.getTargetPath() + File.separator
				+ data.getSystemName();
		List<File> pubResources = getCopyRs(new File(filePath));
		for (File f : pubResources) {
			if (f.exists()) { // 存在
				FileUtil.copyDirectiory(f.getAbsolutePath(), targetDir
						+ File.separator + f.getName());
			}
		}
	}

	/**
	 * 将子系统下的pub公共代码和资源文件复制到target/子系统下
	 * 
	 * @param file
	 *            []
	 * @return
	 */
	public List<File> getCopyRs(File file) {
		List<File> pubResources = new ArrayList<File>();
		File[] files = file.listFiles();
		for (File f : files) {
			if ("rs".equals(f.getName())) {
				pubResources.add(f);
			}
		}
		return pubResources;
	}

	/**
	 * 根据R.js获得该模块下的所有声明的js文件
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public List<String> getFilePathByRJs(File file) throws IOException {
		List<String> list = new ArrayList<String>();
		FileInputStream in = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		boolean iscludeFile = false;
		while ((line = reader.readLine()) != null) {
			if (iscludeFile && line.trim().startsWith("]")) {
				iscludeFile = false;
				break;
			}
			if (iscludeFile) {
				list.add(line.trim().replaceAll("'|,", ""));
			}
			if (line.trim().matches("js\\s*:\\s*\\[")) { // 如果行的值是js:[
				iscludeFile = true;
			}
		}
		in.close();
		return list;
	}

	/**
	 * 复制模块下的资源文件
	 * 
	 * @throws Exception
	 */
	public void copyModuleResources() throws Exception {
		String dir = data.getSourcePath() + File.separator + this.moduleName
				+ File.separator + "resource";

		if (!new File(dir).exists()) {
			return;
		}

		String path = data.getTargetPath() + File.separator
				+ data.getSystemName() + File.separator + this.moduleName
				+ File.separator + "resource";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		FileUtil.copyDirectiory(dir, file.getAbsolutePath());
	}

	/**
	 * 复制子系统下的jsp请求文件
	 * @param file
	 * @throws Exception
	 */
	public void copySystemJspFile(File file) throws Exception {
		String targetPath = data.getTargetPath() + File.separator + "WEB-INF"
				+ File.separator + data.getSystemName() + File.separator
				+ file.getName();
		if (file.getName().endsWith(".jsp")) {
			File targetFile = new File(targetPath);
			targetFile.delete();
			targetFile.getParentFile().mkdirs();
			targetFile.createNewFile();
			FileInputStream in = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(targetFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.replaceAll("/" + data.getSystemName() + "/pub/", "/framework/pub/") ;
				writer.write(line + "\r\n") ;
			}
			writer.flush();
			writer.close();
			in.close();
		}
	}

	/**
	 * 将所有的JS文件合并为一个文件 文件名字: 模块名+debug.js
	 * 
	 * @param filepath
	 *            JS文件路径
	 * @param targetPath
	 *            目标路径
	 * @param systemName
	 *            子系统名字
	 * @param moduleName
	 *            模块名字
	 * @return
	 * @throws Exception
	 */
	public File mergoFile(List<String> filepath) throws Exception {
		// ... target/inv/warehousedefinde/warehousedefinde-.debug
		String path = data.getTargetPath() + File.separator
				+ data.getSystemName() + File.separator + this.moduleName
				+ File.separator + this.moduleName + "-debug.js";

		File file = new File(path);
		file.delete();
		file.getParentFile().mkdirs();
		file.createNewFile();

		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		String dir = data.getSourcePath() + File.separator + this.moduleName;

		for (String fileName : filepath) {
			data.getLog().info("正在读取【" + this.moduleName + "】模块文件：" + fileName);
			FileUtil.writeFile(dir + File.separator + fileName, bw);
		}

		bw.flush();
		bw.close();
		return file;
	}

	/**
	 * 压缩混淆合并的文件 文件名字: 模块名+mini.js
	 * 
	 * @param file
	 */
	public void compressionFile() {

		String baseDir = data.getTargetPath() + File.separator
				+ data.getSystemName() + File.separator + this.moduleName
				+ File.separator + this.moduleName;

		String debugFilepath = baseDir + "-debug.js";

		String miniFilepath = baseDir + "-mini.js";

		String[] rsjs = new String[] { "--type", "js", "--charset", "utf-8",
				debugFilepath, "-o", miniFilepath };
		YUICompressor.main(rsjs);
	}

	/**
	 * 根据操作类型将R.js文件的修改为 声明的文件为相应的文件 合并: 则声明 js:[xxx-debug.js] 压缩混淆: 则声明
	 * js:[xxx-mini.js]
	 * 
	 * @param actionType
	 *            mergo/compression
	 * @throws Exception
	 */
	public void modifyRJs(String actionType) throws Exception {

		String targetfile = data.getTargetPath() + File.separator
				+ data.getSystemName() + File.separator + this.moduleName
				+ File.separator + "R.js";

		String sourcefile = data.getSourcePath() + File.separator
				+ this.moduleName + File.separator + "R.js";

		FileInputStream in = new FileInputStream(sourcefile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(targetfile)));
		String line;
		boolean iscludeFile = false;
		while ((line = reader.readLine()) != null) {
			if (iscludeFile && line.trim().startsWith("]")) {
				if ("mergo".equals(actionType)) { // 合并
					writer.write("'" + this.moduleName + "-debug.js'");
				} else if ("compression".equals(actionType)) { // 压缩混淆
					writer.write("'" + this.moduleName + "-mini.js'");
				}
				writer.write(line.trim() + "\r\n");
				iscludeFile = false;
				continue;
			}
			if (iscludeFile) {
				continue;
			}
			if (line.trim().matches("js\\s*:\\s*\\[")) { // 如果行的值是js:[
				writer.write(line);
				iscludeFile = true;
				continue;
			}
			writer.write(line + "\r\n");
		}
		writer.flush();
		writer.close();
		in.close();

	}

	public MergoMeteData getData() {
		return data;
	}

	public void setData(MergoMeteData data) {
		this.data = data;
	}
}
