package com.riambsoft.maven.plugins.zip.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.logging.Log;

import com.riambsoft.maven.plugins.zip.data.ZipMeteData;

/**
 * 
 * 功能： 1 、实现把指定文件夹下的所有文件压缩为指定文件夹下指定 zip 文件 2 、实现把指定文件夹下的 zip 文件解压到指定目录下
 * 
 */
public class ZipUtil {

	private ZipMeteData zipMeteData;

	private boolean isIncludes = false; // false 表示现在打包的目录是基本目录,是需要排除的 true
										// 表示打包的是单独需要包含的模块,不需要过滤

	/**
	 * 
	 * @param sourceDir
	 *            //指定要压缩的文件夹路径
	 * @param outZipFile
	 *            //指定压缩后文件夹输出路径
	 * @return void
	 * @throws Exception
	 */
	public File convertZip(String zipFileName) throws Exception {
		Log log = zipMeteData.getLog();
		String[] includes = zipMeteData.getIncludes();
		String sourceDir = zipMeteData.getSourcePath();
		String zipTargetFile = zipMeteData.getTargetPath();

		zipFileName = zipTargetFile + File.separator + zipFileName + ".zip";

		// 创建一个输出流
		OutputStream os = null;
		try {
			File zip = new File(zipFileName);
			os = new FileOutputStream(zip);// 打开一个写ZIP文件的输出流
			BufferedOutputStream bos = new BufferedOutputStream(os);// 接上一个缓冲流、不用频繁读取文件
			ZipOutputStream zos = new ZipOutputStream(bos);// 读取ZIP文件当然要用到ZIP的文件打印流
			File file = new File(sourceDir);// 打开要压缩的文件目录
			String basePath = null;// 基本路径
			if (file.isDirectory()) {// 如果有子目录就获得基本路径没有就获得上一节点路径
				basePath = file.getPath();
			} else {
				basePath = file.getParent();
			}
			log.info("进行" + zipMeteData.getSystemName() + "文件夹打包");
			createZip(file, basePath, zos);// 调用创建ZIP文件方法
			log.info(zipMeteData.getSystemName() + "文件夹打包完成");

			log.info("进行WEB-INF文件打包");
			String webInfPath = zipTargetFile + File.separator + "WEB-INF";
			File webInfFile = new File(webInfPath);
			if (webInfFile.exists()) {
				writerZipFile(webInfFile, zos);
			}
			log.info("WEB-INF文件夹打包完成");

			isIncludes = true;

			if (includes != null
					&& "filezip".equals(zipMeteData.getActionType())) {
				log.info("进行includes文件打包");
				for (String path : includes) {
					if (path.endsWith(".jsp")) {
						path = zipTargetFile + File.separator + "WEB-INF"
								+ File.separator + zipMeteData.getSystemName()
								+ File.separator + path;
					} else {
						path = zipTargetFile + File.separator
								+ zipMeteData.getSystemName() + File.separator
								+ path;
					}
					File f = new File(path);
					writerZipFile(f, zos);
				}
				log.info("includes文件打包完成");
			}
			zos.closeEntry();// 关掉当前ZIP写入流
			zos.close();// 关掉ZIP流和过滤流
			return zip;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param @param source
	 * @param @param basePath
	 * @param @param zos
	 * @return void
	 * @throws Exception
	 */
	private void createZip(File source, String basePath, ZipOutputStream zos)
			throws Exception {
		String[] excludes = zipMeteData.getExcludes();
		File[] files = new File[0];// 创建文件对象用于装载要压缩文件
		if (source.isDirectory()) {// 检测是否有子目录
			files = source.listFiles();// 获取目录下所有文档
		} else {
			files = new File[1];
			files[0] = source;
		}
		try {
			boolean isExclude = "filezip".equals(zipMeteData.getActionType());
			if (isExclude && excludes != null) {
				for (String exclude : excludes) {
					if (!isIncludes && "*".equals(exclude)) {
						return;
					}
				}
			}
			for (File file : files) {
				if (isExclude && !isIncludes && excludes != null
						&& doExclude(file)) { // 如果该文件在不打包的文件内,也就是该文件不打包,则跳过
					continue;
				}
				writerZipFile(file, zos);
			}
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * 将不包含的文件排除
	 * 
	 * @param @param file 要验证的文件
	 * @return boolean
	 */
	private boolean doExclude(File file) {
		for (String urlMatche : zipMeteData.getExcludes()) {
			boolean matcheSuccess = validateUrlMatche(file.getAbsolutePath(),
					urlMatche);
			if (matcheSuccess) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据规则验证不包含的文件
	 * 
	 * @param @param filePath 文件路径
	 * @param @param urlMatche 规则
	 * @return boolean
	 */
	private boolean validateUrlMatche(String filePath, String urlMatche) {
		String newUrlMatche = null;
		newUrlMatche = zipMeteData.getTargetPath() + File.separator
				+ zipMeteData.getSystemName() + File.separator + urlMatche;
		newUrlMatche = newUrlMatche.replaceAll("\\\\", "/");
		filePath = filePath.replaceAll("\\\\", "/");

		if (filePath.equals(newUrlMatche)) {
			return true;
		}
		newUrlMatche += File.separator;
		newUrlMatche = newUrlMatche.replaceAll("\\\\", "/");
		boolean result = filePath.startsWith(newUrlMatche);
		return result;
	}

	/**
	 * 
	 * @param file
	 * @param zos
	 * @throws Exception
	 */
	private void writerZipFile(File file, ZipOutputStream zos) throws Exception {
		String targetPath = zipMeteData.getTargetPath();
		String pathName;
		int length = 0;
		byte[] buf = new byte[1024];
		if (file.isDirectory()) {
			pathName = file.getPath().substring(targetPath.length() + 1) + "/";// 获得文件路径+“/”表示还有子目录
			zos.putNextEntry(new ZipEntry(pathName));// 创建一个Zip文件目录进行传递给输出流
			createZip(file, targetPath, zos);// 递归压缩到没有子目录
		} else {
			pathName = file.getPath().substring(targetPath.length() + 1);
			InputStream is = new FileInputStream(file);// 打开文件输入通道
			BufferedInputStream bis = new BufferedInputStream(is);// 使用缓冲流对接压缩流
			zipMeteData.getLog().debug(file.getAbsolutePath() + "写入压缩文件中");
			zos.putNextEntry(new ZipEntry(pathName));// 创建压缩目录
			while ((length = bis.read(buf)) > 0) {// 循环写入压缩流
				zos.write(buf, 0, length);
			}
			is.close();// 关闭文件流
		}
	}

	public ZipMeteData getMeteData() {
		return zipMeteData;
	}

	public void setMeteData(ZipMeteData zipMeteData) {
		this.zipMeteData = zipMeteData;
	}
}
