package org.verapdf.webapp.localstorageservice.server.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.verapdf.webapp.localstorageservice.server.service.LocalFileService;
import org.verapdf.webapp.tool.FilesTool;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@SpringBootTest
@ActiveProfiles("test")
public class LocalFileServiceClearFilesTests {
	@Value("${verapdf.cleaning.lifetime-delay-days}")
	private int fileLifetimeDays;

	@Autowired
	private LocalFileService localFileService;

	private File fileBaseDir;

	public LocalFileServiceClearFilesTests(@Value("${verapdf.files.base-dir}") String baseDirPath) {
		fileBaseDir = new File(baseDirPath);
	}

	@BeforeEach
	public void clearLocalFiles() {
		for (File dir : fileBaseDir.listFiles()) {
			FilesTool.deleteFile(dir);
		}
	}

	@Test
	public void nothingClearedInLocalFilesOnClearLocalFiles() throws IOException {
		File file1 = new File(fileBaseDir, LocalDate.now().toString());
		file1.mkdir();
		File innerFile1 = new File(file1, "filename1");
		innerFile1.createNewFile();
		File innerFile2 = new File(file1, "filename2");
		innerFile2.createNewFile();
		File file2 = new File(fileBaseDir, LocalDate.now()
				.minus(fileLifetimeDays / 2, ChronoUnit.DAYS).toString());
		file2.mkdir();
		File file3 = new File(fileBaseDir, LocalDate.now()
				.minus(fileLifetimeDays - 1, ChronoUnit.DAYS).toString());
		file3.mkdir();
		File innerFile3 = new File(file3, "filename3");
		innerFile3.createNewFile();
		File innerDirectory = new File(file3, "innerdir");
		innerDirectory.mkdir();
		File fileOfInnerDirectory = new File(innerDirectory, "innerfile");
		fileOfInnerDirectory.createNewFile();

		Assertions.assertEquals(3, fileBaseDir.listFiles().length);
		Assertions.assertEquals(2, file1.listFiles().length);
		Assertions.assertEquals(0, file2.listFiles().length);
		Assertions.assertEquals(2, file3.listFiles().length);
		Assertions.assertEquals(1, innerDirectory.listFiles().length);

		localFileService.clearLocalFiles();

		Assertions.assertEquals(3, fileBaseDir.listFiles().length);
		Assertions.assertEquals(2, file1.listFiles().length);
		Assertions.assertEquals(0, file2.listFiles().length);
		Assertions.assertEquals(2, file3.listFiles().length);
		Assertions.assertEquals(1, innerDirectory.listFiles().length);
	}

	@Test
	public void partlyClearedLocalFilesOnClearLocalFiles() throws IOException {
		File file1 = new File(fileBaseDir, LocalDate.now()
				.minus(fileLifetimeDays / 2, ChronoUnit.DAYS).toString());
		file1.mkdir();
		File innerFile1 = new File(file1, "filename1");
		innerFile1.createNewFile();
		File innerFile2 = new File(file1, "filename2");
		innerFile2.createNewFile();
		File file2 = new File(fileBaseDir, LocalDate.now()
				.minus(fileLifetimeDays, ChronoUnit.DAYS).toString());
		file2.mkdir();
		File file3 = new File(fileBaseDir, LocalDate.now()
				.minus(fileLifetimeDays + 1, ChronoUnit.DAYS).toString());
		file3.mkdir();
		File innerFile3 = new File(file3, "filename3");
		innerFile3.createNewFile();
		File innerDirectory = new File(file3, "innerdir");
		innerDirectory.mkdir();
		File fileOfInnerDirectory = new File(innerDirectory, "innerfile");
		fileOfInnerDirectory.createNewFile();

		Assertions.assertEquals(3, fileBaseDir.listFiles().length);
		Assertions.assertEquals(2, file1.listFiles().length);
		Assertions.assertEquals(0, file2.listFiles().length);
		Assertions.assertEquals(2, file3.listFiles().length);
		Assertions.assertEquals(1, innerDirectory.listFiles().length);

		localFileService.clearLocalFiles();

		Assertions.assertEquals(1, fileBaseDir.listFiles().length);
		Assertions.assertEquals(2, file1.listFiles().length);
		Assertions.assertFalse(file2.exists());
		Assertions.assertFalse(file3.exists());
		Assertions.assertFalse(fileOfInnerDirectory.exists());
	}

	@Test
	public void allClearedLocalFilesOnClearLocalFiles() throws IOException {
		File file1 = new File(fileBaseDir, LocalDate.now()
				.minus(fileLifetimeDays, ChronoUnit.DAYS).toString());
		file1.mkdir();
		File innerFile1 = new File(file1, "filename1");
		innerFile1.createNewFile();
		File innerFile2 = new File(file1, "filename2");
		innerFile2.createNewFile();
		File file2 = new File(fileBaseDir, LocalDate.now()
				.minus(fileLifetimeDays + 1, ChronoUnit.DAYS).toString());
		file2.mkdir();
		File file3 = new File(fileBaseDir, LocalDate.now()
				.minus(fileLifetimeDays + 5, ChronoUnit.DAYS).toString());
		file3.mkdir();
		File innerFile3 = new File(file3, "filename3");
		innerFile3.createNewFile();
		File innerDirectory = new File(file3, "innerdir");
		innerDirectory.mkdir();
		File fileOfInnerDirectory = new File(innerDirectory, "innerfile");
		fileOfInnerDirectory.createNewFile();

		Assertions.assertEquals(3, fileBaseDir.listFiles().length);
		Assertions.assertEquals(2, file1.listFiles().length);
		Assertions.assertEquals(0, file2.listFiles().length);
		Assertions.assertEquals(2, file3.listFiles().length);
		Assertions.assertEquals(1, innerDirectory.listFiles().length);

		localFileService.clearLocalFiles();

		Assertions.assertEquals(0, fileBaseDir.listFiles().length);
		Assertions.assertFalse(file1.exists());
		Assertions.assertFalse(file2.exists());
		Assertions.assertFalse(file3.exists());
		Assertions.assertFalse(fileOfInnerDirectory.exists());
	}

	@Test
	public void notDirectoryInRootOnClearLocalFiles() throws IOException {
		File file1 = new File(fileBaseDir, LocalDate.now().toString());
		file1.createNewFile();
		File file2 = new File(fileBaseDir, "some file");
		file2.createNewFile();

		Assertions.assertEquals(2, fileBaseDir.listFiles().length);

		localFileService.clearLocalFiles();

		Assertions.assertFalse(file1.exists());
		Assertions.assertFalse(file2.exists());
	}

	@Test
	public void incorrectDirectoryNameOnClearLocalFiles() {
		File file = new File(fileBaseDir, "incorrect name");
		file.mkdir();

		Assertions.assertEquals(1, fileBaseDir.listFiles().length);

		localFileService.clearLocalFiles();

		Assertions.assertFalse(file.exists());
	}
}
