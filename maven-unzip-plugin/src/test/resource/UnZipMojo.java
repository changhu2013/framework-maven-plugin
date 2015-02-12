package com.riambsoft.maven.plugins.unzip.mojos;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @goal unzip
 * @requiresDependencyResolution runtime
 */
public class UnZipMojo extends AbstractMojo {

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
	 * The overlays to apply. 解压缩配置
	 * 
	 * @parameter
	 */
	private List<Extract> extracts;

	public void execute() throws MojoExecutionException {

		String DEFAULT_PATH = baseDir.getAbsolutePath() + File.separator
				+ "src" + File.separator + "main" + File.separator + "webapp";
		try {

			List<Artifact> artifacts = getZip();
			for (Artifact artifact : artifacts) {
				for (Extract extrac : extracts) {
					String artifactId = extrac.getArtifactId();
					String groupId = extrac.getGroupId();
					if (artifact.getArtifactId().equals(artifactId)
							&& artifact.getGroupId().equals(groupId)) {
						if (extrac.getWorkDirectory() == null) {
							getLog().info(
									"正在解压:" + artifact.getFile().getName());
							unZip(artifact.getFile(), DEFAULT_PATH);
						} else {
							getLog().info(
									"正在解压:" + artifact.getFile().getName());
							unZip(artifact.getFile(),
									baseDir.getAbsolutePath() + File.separator
											+ extrac.getWorkDirectory());
						}
					}
				}
			}
		} catch (Exception e) {
			throw new MojoExecutionException("解压出现异常: " + e.getMessage(), e);
		}
	}

	public List<Artifact> getZip() {
		List<Artifact> result = new ArrayList<Artifact>();
		Set<Artifact> artifacts = project.getArtifacts();
		Iterator it = artifacts.iterator();
		while (it.hasNext()) {
			Artifact artifact = (Artifact) it.next();
			if ("zip".equals(artifact.getType())) {
				result.add(artifact);
			}
		}
		return result;
	}

	/**
	 * 
	 * @param @param zipFile
	 * @param @param descDir
	 * @return void
	 * @throws IOException
	 */
	private void unZip(File zipfile, String descDir) throws IOException {
		// 验证目录正确性
		descDir = descDir.endsWith("//") ? descDir : descDir + "//";
		byte b[] = new byte[1024];
		int length;
		ZipFile zipFile;

		try {
			// 打开ZIP压缩文件
			zipFile = new ZipFile(zipfile);
			// 返回文件目录枚举
			Enumeration enumeration = zipFile.entries();
			// 创建Zip目录
			ZipEntry zipEntry = null;
			// 遍历目录枚举
			while (enumeration.hasMoreElements()) {
				// 获得压缩目录
				zipEntry = (ZipEntry) enumeration.nextElement();
				// 创建压缩目录文件
				File loadFile = new File(descDir + zipEntry.getName());

				if (zipEntry.isDirectory()) {
					loadFile.mkdirs();
				} else {
					if (!loadFile.getParentFile().exists()) {
						loadFile.getParentFile().mkdirs();
					}
					OutputStream os = new FileOutputStream(loadFile);
					InputStream zis = zipFile.getInputStream(zipEntry);
					while ((length = zis.read(b)) > 0) {
						os.write(b, 0, length);
					}
					os.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

	}

}
