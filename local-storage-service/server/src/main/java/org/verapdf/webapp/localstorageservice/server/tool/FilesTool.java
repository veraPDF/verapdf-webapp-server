package org.verapdf.webapp.localstorageservice.server.tool;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.verapdf.webapp.error.exception.VeraPDFBackendException;

import java.io.*;
import java.nio.file.Files;

public final class FilesTool {
	private static final Logger LOGGER = LoggerFactory.getLogger(FilesTool.class);

	private FilesTool() {
	}

	public static String evaluateMD5(File file) throws IOException {
		try (InputStream is = Files.newInputStream(file.toPath())) {
			return DigestUtils.md5DigestAsHex(is);
		}
	}

	public static void saveFileOnDiskAndCheck(InputStream fileStream, File fileToSave,
	                                          String expectedContentMD5) throws VeraPDFBackendException, IOException {
		try {
			try (OutputStream out = Files.newOutputStream(fileToSave.toPath())) {
				IOUtils.copyLarge(fileStream, out);
			}
			if (!expectedContentMD5.equals(evaluateMD5(fileToSave))) {
				throw new VeraPDFBackendException("Expected file checksum doesn't match obtained file checksum");
			}
		} catch (Throwable e) {
			deleteFile(fileToSave);
			throw e;
		}
	}

	public static void deleteFile(File toDelete) {
		if (toDelete != null && toDelete.exists()) {
			try {
				FileUtils.forceDelete(toDelete);
			} catch (IOException e) {
				LOGGER.error("Exception during file remove: " + toDelete.getAbsolutePath(), e);
			}
		}
	}
}
