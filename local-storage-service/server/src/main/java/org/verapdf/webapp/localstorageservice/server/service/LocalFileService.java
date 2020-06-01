package org.verapdf.webapp.localstorageservice.server.service;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.verapdf.webapp.localstorageservice.server.error.exception.LowDiskSpaceException;
import org.verapdf.webapp.error.exception.VeraPDFBackendException;
import org.verapdf.webapp.tool.FilesTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class LocalFileService {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileService.class);

	private final File fileBaseDir;
	private final int fileLifetimeDays;
	private final DataSize minSpaceThreshold;

	public LocalFileService(@Value("${verapdf.files.base-dir}") String baseDirPath,
	                        @Value("${verapdf.cleaning.lifetime-delay-days}") int fileLifetimeDays,
	                        @Value("${verapdf.files.min-space-threshold}") DataSize minSpaceThreshold) throws IOException {
		this.fileBaseDir = new File(baseDirPath);
		this.fileLifetimeDays = fileLifetimeDays;
		this.minSpaceThreshold = minSpaceThreshold;
		if (!this.fileBaseDir.isDirectory()) {
			LOGGER.warn("Missing file directory. Trying to create with path: {}", this.fileBaseDir.getAbsolutePath());
			FileUtils.forceMkdir(this.fileBaseDir);
		}
	}

	public Resource getFileOnDiskResource(String localPath) throws FileNotFoundException {
		File fileOnDisk = getFileOnDiskByPath(localPath);
		return new FileSystemResource(fileOnDisk);
	}

	public String saveFileOnDisk(InputStream inputStream, String fileName,
	                             String expectedContentMD5) throws IOException, VeraPDFBackendException {
		checkNewFileAvailability();
		return saveFile(inputStream, fileName, expectedContentMD5);
	}

	@Scheduled(cron = "{verapdf.cleaning.cron}")
	public void clearLocalFiles() {
		LocalDate now = LocalDate.now();
		for (File dir : fileBaseDir.listFiles()) {
			boolean shouldBeRemoved = true;
			if (dir.isDirectory()) {
				try {
					LocalDate dirNameDate = LocalDate.parse(dir.getName());
					long period = ChronoUnit.DAYS.between(dirNameDate, now);
					shouldBeRemoved = period >= fileLifetimeDays;
				} catch (DateTimeException e) {
					shouldBeRemoved = true;
				}
			}
			if (shouldBeRemoved) {
				FilesTool.deleteFile(dir);
			}
		}
	}

	private File getFileOnDiskByPath(String localPath) throws FileNotFoundException {
		File res = new File(this.fileBaseDir, localPath);
		if (!res.isFile()) {
			throw new FileNotFoundException("Cannot find file on disk");
		}
		return res;
	}

	private String saveFile(InputStream inputStream, String fileName,
	                        String expectedContentMD5) throws VeraPDFBackendException, IOException {
		File dirToSaveFile = getDirToSaveFile();
		File toSave = new File(dirToSaveFile, fileName);

		FilesTool.saveFileOnDiskAndCheck(inputStream, toSave, expectedContentMD5);
		return this.fileBaseDir.toPath().relativize(toSave.toPath()).toString();
	}

	private File getDirToSaveFile() {
		File res = new File(this.fileBaseDir, LocalDate.now().toString());
		if (!res.isDirectory() && !res.mkdir()) {
			throw new IllegalStateException("Cannot obtain the directory to save a file");
		}
		return res;
	}

	private void checkNewFileAvailability() throws LowDiskSpaceException {
		if (fileBaseDir.getUsableSpace() < minSpaceThreshold.toBytes()) {
			throw new LowDiskSpaceException("Low disk space");
		}
	}
}

