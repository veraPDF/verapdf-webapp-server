package org.verapdf.webapp.worker.listener;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClientResponseException;
import org.verapdf.webapp.localstorageservice.client.service.LocalStorageServiceClient;
import org.verapdf.webapp.localstorageservice.model.dto.StoredFileDTO;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest
public class ExecutableTaskWorkerTests {

	@Autowired
	private ExecutableTaskWorker executableTaskWorker;

	@Autowired
	private File workingDir;

	@MockBean
	private LocalStorageServiceClient localStorageServiceClient;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@TempDir
	public static File tempDir;

	@AfterEach
	public void checkWorkingDirectory() {
		assertTrue(workingDir.listFiles().length == 0);
		Mockito.verifyNoMoreInteractions(localStorageServiceClient, queueSender);
		Mockito.validateMockitoUsage();
	}

	@Test
	public void processTest() throws IOException {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doReturn(createDescriptor(fileId))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doReturn(createResource())
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);
		String expectedReportAsString = new String(getClass().getResourceAsStream(
				"/files/report-PDFA_1_A.json").readAllBytes(), StandardCharsets.UTF_8);
		Mockito.doReturn(createReportFileDescriptor(fileId))
				.when(localStorageServiceClient)
				.saveFile(argThat(new FileAsJsonMatcher(expectedReportAsString)),
						eq("35a7d1deffc1ad490bcc5eb3c557a399"));

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"profile\":\"PDFA_1_A\"}");
		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(localStorageServiceClient).saveFile(
				any(File.class),eq("35a7d1deffc1ad490bcc5eb3c557a399"));
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void notParseableUUIDOnProcessTest() {
		executableTaskWorker.handleMessage(
				"{\"jobId\":\"notParseableMessage\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");
		//just write log
	}

	@Test
	public void notParseableProfileOnProcessTest() {
		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"notExistedProfile\"}");
		//just write log
	}

	@Test
	public void nullArgumentPassedOnProcessTest() {
		executableTaskWorker.handleMessage(null);
		//just write log
	}

	@Test
	public void nullableFileIdOnPassedExecutableTaskDTOOnProcessTest() {
		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":null,"
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\"," +
				"\"errorType\":\"INVALID_TASK_DATA_ERROR\"," +
				"\"errorMessage\":\"Invalid task request: " +
				"{\\\"jobId\\\":\\\"780e3129-42a7-4154-8ce9-436fc1a6dc35\\\"," +
				"\\\"fileId\\\":null,\\\"profile\\\":\\\"PDFA_1_A\\\"}\"}";

		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void nullableProfileOnPassedExecutableTaskDTOOnProcessTest() {
		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":null}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\"" +
				",\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\"" +
				",\"errorType\":\"INVALID_TASK_DATA_ERROR\"," +
				"\"errorMessage\":\"Invalid task request: " +
				"{\\\"jobId\\\":\\\"780e3129-42a7-4154-8ce9-436fc1a6dc35\\\"," +
				"\\\"fileId\\\":\\\"07b17d5d-1010-4980-b280-94129cb13838\\\"," +
				"\\\"profile\\\":null}\"}";

		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void notFoundFileDescriptorOnProcessTest() {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doThrow(new RestClientResponseException("404 : [no body]",
				HttpStatus.NOT_FOUND.value(), "", null, "no body".getBytes(), null))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);

		executableTaskWorker.handleMessage(
					"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
					+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
					+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
				+ "\"errorMessage\":\"no body\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void internalServerExceptionOnGetFileDescriptorOnProcessTest() {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doThrow(new RestClientResponseException(
				"500 : [internal exception during getting file descriptor]",
				HttpStatus.INTERNAL_SERVER_ERROR.value(), "", null,
				"internal exception during getting file descriptor".getBytes(), null))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
				+ "\"errorMessage\":\"internal exception during getting file descriptor\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void badRequestExceptionOnGetFileDescriptorOnProcessTest() {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doThrow(new RestClientResponseException("400 : [incorrect type of parameters]",
				HttpStatus.BAD_REQUEST.value(), "", null,
				"incorrect type of parameters".getBytes(), null))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
				+ "\"errorMessage\":\"incorrect type of parameters\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void requestTimeoutExceptionOnGetFileDescriptorOnProcessTest() {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doThrow(new RestClientResponseException("408 : [request timeout]",
				HttpStatus.REQUEST_TIMEOUT.value(), "", null, "request timeout".getBytes(), null))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
				+ "\"errorMessage\":\"request timeout\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void notFoundResourceOnProcessTest() {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doReturn(createDescriptor(fileId))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doThrow(new RestClientResponseException("404 : [no body]",
				HttpStatus.NOT_FOUND.value(), "", null, "no body".getBytes(), null))
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
				+ "\"errorMessage\":\"no body\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void internalServerExceptionOnGetResourceOnProcessTest() {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doReturn(createDescriptor(fileId))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doThrow(new RestClientResponseException(
				"500 : [internal exception during getting file resource]",
				HttpStatus.INTERNAL_SERVER_ERROR.value(), "", null,
				"internal exception during getting file resource".getBytes(), null))
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
				+ "\"errorMessage\":\"internal exception during getting file resource\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void badRequestExceptionOnGetResourceOnProcessTest() {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doReturn(createDescriptor(fileId))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doThrow(new RestClientResponseException("400 : [incorrect type of parameters]",
				HttpStatus.BAD_REQUEST.value(), "", null,
				"incorrect type of parameters".getBytes(), null))
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
				+ "\"errorMessage\":\"incorrect type of parameters\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void requestTimeoutExceptionOnGetResourceOnProcessTest() {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doReturn(createDescriptor(fileId))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doThrow(new RestClientResponseException("408 : [incorrect type of parameters]",
				HttpStatus.REQUEST_TIMEOUT.value(), "", null,
				"incorrect type of parameters".getBytes(), null))
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
				+ "\"errorMessage\":\"incorrect type of parameters\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void incorrectChecksumOnSaveFileOnDiskOnProcessTest() throws IOException {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		StoredFileDTO storedFileDTO = createDescriptor(fileId);
		storedFileDTO.setContentMD5("Incorrect checksum");
		Mockito.doReturn(storedFileDTO)
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doReturn(createResource())
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"PROCESSING_INTERNAL_ERROR\","
				+ "\"errorMessage\":\"Expected file checksum doesn't match"
				+ " obtained file checksum. Expected: 4043dbacc119258a99820d62552e6a93,"
				+ " actual: Incorrect checksum\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void internalServerExceptionOnSaveReportFileOnProcessTest() throws IOException {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doReturn(createDescriptor(fileId))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doReturn(createResource())
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);
		String expectedReportAsString = new String(getClass().getResourceAsStream(
				"/files/report-PDFA_1_A.json").readAllBytes(), StandardCharsets.UTF_8);
		Mockito.doThrow(new RestClientResponseException(
				"500 : [internal exception during saving file report]",
				HttpStatus.INTERNAL_SERVER_ERROR.value(), "", null,
				"internal exception during saving file report".getBytes(), null))
				.when(localStorageServiceClient)
				.saveFile(argThat(new FileAsJsonMatcher(expectedReportAsString)),
						eq("35a7d1deffc1ad490bcc5eb3c557a399"));

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"SAVE_RESULT_FILE_ERROR\","
				+ "\"errorMessage\":\"internal exception during saving file report\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(localStorageServiceClient).saveFile(any(File.class),
				eq("35a7d1deffc1ad490bcc5eb3c557a399"));
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void badRequestExceptionOnSaveReportFileOnProcessTest() throws IOException {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doReturn(createDescriptor(fileId))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doReturn(createResource())
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);
		String expectedReportAsString = new String(getClass().getResourceAsStream(
				"/files/report-PDFA_1_A.json").readAllBytes(), StandardCharsets.UTF_8);
		Mockito.doThrow(new RestClientResponseException("400 : [incorrect parameters]",
				HttpStatus.BAD_REQUEST.value(), "", null,
				"incorrect parameters".getBytes(), null))
				.when(localStorageServiceClient)
				.saveFile(argThat(new FileAsJsonMatcher(expectedReportAsString)),
						eq("35a7d1deffc1ad490bcc5eb3c557a399"));

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"SAVE_RESULT_FILE_ERROR\","
				+ "\"errorMessage\":\"incorrect parameters\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(localStorageServiceClient).saveFile(any(File.class),
				eq("35a7d1deffc1ad490bcc5eb3c557a399"));
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	@Test
	public void requestTimeoutExceptionOnSaveReportFileOnProcessTest() throws IOException {
		UUID fileId = UUID.fromString("07b17d5d-1010-4980-b280-94129cb13838");

		Mockito.doReturn(createDescriptor(fileId))
				.when(localStorageServiceClient)
				.getFileDescriptorById(fileId);
		Mockito.doReturn(createResource())
				.when(localStorageServiceClient)
				.getFileResourceById(fileId);
		String expectedReportAsString = new String(getClass().getResourceAsStream(
				"/files/report-PDFA_1_A.json").readAllBytes(), StandardCharsets.UTF_8);
		Mockito.doThrow(new RestClientResponseException("408 : [request timeout]",
				HttpStatus.REQUEST_TIMEOUT.value(), "", null, "request timeout".getBytes(), null))
				.when(localStorageServiceClient)
				.saveFile(argThat(new FileAsJsonMatcher(expectedReportAsString)),
						eq("35a7d1deffc1ad490bcc5eb3c557a399"));

		executableTaskWorker.handleMessage(
				"{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
						+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
						+ "\"profile\":\"PDFA_1_A\"}");

		String expectedMessage
				= "{\"jobId\":\"780e3129-42a7-4154-8ce9-436fc1a6dc35\","
				+ "\"fileId\":\"07b17d5d-1010-4980-b280-94129cb13838\","
				+ "\"errorType\":\"SAVE_RESULT_FILE_ERROR\","
				+ "\"errorMessage\":\"request timeout\"}";

		Mockito.verify(localStorageServiceClient).getFileDescriptorById(fileId);
		Mockito.verify(localStorageServiceClient).getFileResourceById(fileId);
		Mockito.verify(localStorageServiceClient).saveFile(any(File.class),
				eq("35a7d1deffc1ad490bcc5eb3c557a399"));
		Mockito.verify(queueSender).sendMessage(expectedMessage);
	}

	private Resource createResource() throws IOException {
		Path fileToValidatePath
				= Files.createTempFile(tempDir.toPath(), "file_to_validate", "");
		FileUtils.copyToFile(getClass().getResourceAsStream("/files/veraPDFTestSuite.pdf"),
				fileToValidatePath.toFile());
		return new FileSystemResource(fileToValidatePath);
	}

	private StoredFileDTO createDescriptor(UUID fileId) {
		StoredFileDTO storedFileDTO = new StoredFileDTO();
		storedFileDTO.setId(fileId);
		storedFileDTO.setFileName("veraPDFTestSuite.pdf");
		storedFileDTO.setContentMD5("4043dbacc119258a99820d62552e6a93");
		storedFileDTO.setContentType("application/pdf");
		storedFileDTO.setContentSize(3387);
		return storedFileDTO;
	}

	private StoredFileDTO createReportFileDescriptor(UUID fileId) {
		StoredFileDTO storedFileDTO = new StoredFileDTO();
		storedFileDTO.setId(UUID.fromString("d39da7b6-3665-4374-84e2-70c1e24e7029"));
		storedFileDTO.setFileName("report-" + fileId);
		storedFileDTO.setContentMD5("35a7d1deffc1ad490bcc5eb3c557a399");
		storedFileDTO.setContentType("application/octet-stream");
		storedFileDTO.setContentSize(617);
		return storedFileDTO;
	}

	private static class FileAsJsonMatcher implements ArgumentMatcher<File>{

		private String expectedReportAsString;

		public FileAsJsonMatcher(String expectedReportAsString) {
			this.expectedReportAsString = expectedReportAsString;
		}

		@Override
		public boolean matches(File file) {
			try {
				String actualReportAsString = new String(FileUtils.readFileToByteArray(file),
						StandardCharsets.UTF_8);
				return JSONCompare.compareJSON(expectedReportAsString,
						actualReportAsString, JSONCompareMode.NON_EXTENSIBLE).passed();
			} catch (JSONException | IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
