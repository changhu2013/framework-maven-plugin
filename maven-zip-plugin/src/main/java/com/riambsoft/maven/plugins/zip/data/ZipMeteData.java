package com.riambsoft.maven.plugins.zip.data;

import org.apache.maven.plugin.logging.Log;

public class ZipMeteData {

	private String sourcePath; // 源代码路径

	private String targetPath; // 目标路径

	private String systemName; // 子系统名称

	private String[] includes; // 需要单独包含的文件

	private String[] excludes; // 需要排除的文件

	private String actionType; // 打包类型 , 文件打包/项目打包

	private Log log;

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String[] getIncludes() {
		return includes;
	}

	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	public String[] getExcludes() {
		return excludes;
	}

	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}
}
