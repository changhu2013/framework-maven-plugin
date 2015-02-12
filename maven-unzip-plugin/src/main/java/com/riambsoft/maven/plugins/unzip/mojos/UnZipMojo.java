package com.riambsoft.maven.plugins.unzip.mojos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @goal unzip
 * @requiresDependencyResolution runtime
 */
public class UnZipMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project}"
	 * @required
	 */
	protected MavenProject project;

	/**
	 * @component
	 */
	protected ArtifactFactory artifactFactory;

	/**
	 * @component
	 */
	private ArtifactResolver artifactResolver;

	/**
	 * @parameter expression="${localRepository}"
	 */
	private ArtifactRepository localRepository;

	/**
	 * @component
	 */
	private ArtifactMetadataSource source;

	/**
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @required
	 */
	private List<ArtifactRepository> pomRemoteRepositories;

	/**
	 * @parameter expression="${targetDirectory}"
	 */
	private File targetDirectory;

	/**
	 * @parameter expression="${extracts}"
	 */
	private List<Extract> extracts;

	// 执行解压缩
	public void execute() throws MojoExecutionException {
		for (Extract extract : extracts) {

			String groupId = extract.getGroupId();
			String artifactId = extract.getArtifactId();
			String version = extract.getVersion();
			String type = extract.getType();
			String classifier = extract.getClassifier();

			Artifact artifact = convert(groupId, artifactId, version, type,
					classifier);

			String temp = extract.getTargetDirectory();

			File dir = (temp == null ? targetDirectory : new File(temp
					+ (temp.endsWith("//") ? "" : "//")));

			// 删除原有文件
			delOldFiles(dir, artifact.getArtifactId());

			// 解压文件到指定目录
			try {
				doUnZipFileToDir(dir, artifact.getFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 删除原有的文件
	public void delOldFiles(File directory, String fileName) {
		File temp = new File(directory, fileName);
		getLog().info("删原文件:" + temp.getAbsolutePath());
		deleteFile(temp);
	}

	private void deleteFile(File file) {
		if (file.exists()) {
			getLog().debug("删原文件:" + file.getAbsolutePath());
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				for (File temp : file.listFiles()) {
					deleteFile(temp);
				}
				file.delete();
			}
		}
	}

	// 解压文件到指定目录
	private void doUnZipFileToDir(File directory, File file) throws IOException {

		getLog().info("解压文件:" + file.getName() + " 至目录:" + directory);

		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		byte b[] = new byte[1024];
		int length;
		ZipEntry entry = null;
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			File temp = new File(directory, entry.getName());
			getLog().debug("添加文件:" + temp.getAbsolutePath());
			if (entry.isDirectory()) {
				temp.mkdirs();
			} else {
				if (!temp.getParentFile().exists()) {
					temp.getParentFile().mkdirs();
				}
				OutputStream os = new FileOutputStream(temp);
				InputStream is = zipFile.getInputStream(entry);
				while ((length = is.read(b)) > 0) {
					os.write(b, 0, length);
				}
				os.close();
				is.close();
			}
		}
	}

	// 根据groupId等信息找到artifact
	private Artifact convert(String groupId, String artifactId, String version,
			String type, String classifier) throws MojoExecutionException {

		Artifact artifact = classifier == null ? artifactFactory
				.createBuildArtifact(groupId, artifactId, version, type)
				: artifactFactory.createArtifactWithClassifier(groupId,
						artifactId, version, type, classifier);

		Artifact dummyOriginatingArtifact = artifactFactory
				.createBuildArtifact("org.apache.maven.plugins",
						"maven-downloader-plugin", "1.0", "jar");

		List<ArtifactRepository> repoList = new ArrayList<ArtifactRepository>();

		if (pomRemoteRepositories != null) {
			repoList.addAll(pomRemoteRepositories);
		}

		try {
			artifactResolver.resolveTransitively(
					Collections.singleton(artifact), dummyOriginatingArtifact,
					repoList, localRepository, source);

		} catch (AbstractArtifactResolutionException e) {
			throw new MojoExecutionException("Couldn't download artifact: "
					+ e.getMessage(), e);
		}

		return artifact;
	}

}
