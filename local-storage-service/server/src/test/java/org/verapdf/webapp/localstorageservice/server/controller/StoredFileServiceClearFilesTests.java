package org.verapdf.webapp.localstorageservice.server.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.verapdf.webapp.localstorageservice.server.entity.StoredFile;
import org.verapdf.webapp.localstorageservice.server.repository.StoredFileRepository;
import org.verapdf.webapp.localstorageservice.server.service.StoredFileService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StoredFileServiceClearFilesTests {

	@Value("${verapdf.cleaning.lifetime-delay-days}")
	private int fileLifetimeDays;

	@Autowired
	private StoredFileService storedFileService;

	@Autowired
	private StoredFileRepository storedFileRepository;

	@BeforeEach
	public void clearStoredFiles() {
		storedFileRepository.deleteAll();
	}

	@Test
	public void nothingClearedOnClearStoredFilesTest() {
		StoredFile storedFile1 = new StoredFile(null,
				"893ce251-4754-4d92-a6bc-69f886ab1ac6",
				MediaType.APPLICATION_JSON_VALUE, 500, "originalName1");
		storedFile1.setCreatedAt(startOfToday());
		storedFileRepository.saveAndFlush(storedFile1);

		StoredFile storedFile2 = new StoredFile(null,
				"534bd16b-6bd5-404e-808e-5dc731c73963",
				MediaType.APPLICATION_JSON_VALUE, 10, "originalName2");
		storedFile2.setCreatedAt(
				startOfToday().minus(fileLifetimeDays / 2, ChronoUnit.DAYS)
						.minus(10, ChronoUnit.MINUTES));
		storedFileRepository.saveAndFlush(storedFile2);

		StoredFile storedFile3 = new StoredFile(null,
				"774bd16b-7ad5-354e-808e-5dc731c73963",
				MediaType.APPLICATION_JSON_VALUE, 10005, "originalName3");
		storedFile3.setCreatedAt(startOfToday().minus(fileLifetimeDays, ChronoUnit.DAYS)
				.plus(10, ChronoUnit.MINUTES));
		storedFileRepository.saveAndFlush(storedFile3);

		Assertions.assertEquals(3, storedFileRepository.count());

		storedFileService.clearStoredFiles();

		Assertions.assertEquals(3, storedFileRepository.count());
	}

	@Test
	public void partlyClearedOnClearStoredFilesTest() {
		StoredFile storedFile1 = new StoredFile(null,
				"893ce251-4754-4d92-a6bc-69f886ab1ac6",
				MediaType.APPLICATION_JSON_VALUE, 500, "originalName1");
		storedFile1.setCreatedAt(startOfToday().minus(fileLifetimeDays / 2, ChronoUnit.DAYS)
				.minus(10, ChronoUnit.MINUTES));
		storedFileRepository.saveAndFlush(storedFile1);

		StoredFile storedFile2 = new StoredFile(null,
				"534bd16b-6bd5-404e-808e-5dc731c73963",
				MediaType.APPLICATION_JSON_VALUE, 10, "originalName2");
		storedFile2.setCreatedAt(startOfToday().minus(fileLifetimeDays, ChronoUnit.DAYS).plus(1, ChronoUnit.SECONDS));
		storedFileRepository.saveAndFlush(storedFile2);

		StoredFile storedFile3 = new StoredFile(null,
				"774bd16b-7ad5-354e-808e-5dc731c73963",
				MediaType.APPLICATION_JSON_VALUE, 10005, "originalName3");
		storedFile3.setCreatedAt(startOfToday().minus(fileLifetimeDays, ChronoUnit.DAYS)
				.minus(1, ChronoUnit.SECONDS));
		storedFileRepository.saveAndFlush(storedFile3);

		Assertions.assertEquals(3, storedFileRepository.count());

		storedFileService.clearStoredFiles();

		Assertions.assertEquals(2, storedFileRepository.count());
	}


	@Test
	public void allClearedOnClearStoredFilesTest() {
		StoredFile storedFile1 = new StoredFile(null,
				"893ce251-4754-4d92-a6bc-69f886ab1ac6",
				MediaType.APPLICATION_JSON_VALUE, 500, "originalName1");
		storedFile1.setCreatedAt(startOfToday().minus(fileLifetimeDays, ChronoUnit.DAYS)
				.minus(1, ChronoUnit.SECONDS));
		storedFileRepository.saveAndFlush(storedFile1);

		StoredFile storedFile2 = new StoredFile(null,
				"534bd16b-6bd5-404e-808e-5dc731c73963",
				MediaType.APPLICATION_JSON_VALUE, 10, "originalName2");
		storedFile2.setCreatedAt(startOfToday().minus(
				fileLifetimeDays + 3, ChronoUnit.DAYS));
		storedFileRepository.saveAndFlush(storedFile2);

		StoredFile storedFile3 = new StoredFile(null,
				"774bd16b-7ad5-354e-808e-5dc731c73963",
				MediaType.APPLICATION_JSON_VALUE, 10005, "originalName3");
		storedFile3.setCreatedAt(startOfToday().minus(
				fileLifetimeDays + 5, ChronoUnit.DAYS));
		storedFileRepository.saveAndFlush(storedFile3);

		Assertions.assertEquals(3, storedFileRepository.count());

		storedFileService.clearStoredFiles();

		Assertions.assertEquals(0, storedFileRepository.count());
	}

	@Test
	public void emptyJobsAndTasksOnCleanJobsAndTasksTest() {
		Assertions.assertEquals(0, storedFileRepository.count());

		storedFileService.clearStoredFiles();

		Assertions.assertEquals(0, storedFileRepository.count());
	}

	private Instant startOfToday() {
		return Instant.now().truncatedTo(ChronoUnit.DAYS);
	}
}
