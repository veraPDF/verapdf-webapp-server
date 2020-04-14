package org.verapdf.webapp.worker.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.JsonExpectationsHelper;
import org.verapdf.processor.reports.ValidationReport;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;
import org.verapdf.webapp.worker.error.exception.VeraPDFProcessingException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@ActiveProfiles("test")
@SpringBootTest
public class VeraPdfProcessorTests {

	@Autowired
	private VeraPdfProcessor veraPDFProcessor;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@TempDir
	public static File tempDir;

	@ParameterizedTest
	@EnumSource(value = Profile.class, names = {"PDFUA_1_MACHINE"}, mode = EnumSource.Mode.EXCLUDE)
	public void testingValidationWithAllProfilesOnValidateTest(Profile profile) throws VeraPDFProcessingException, IOException, JSONException {
		Path fileToValidatePath
				= Files.createTempFile(tempDir.toPath(), "file_to_validate", "");
		FileUtils.copyToFile(
				getClass().getResourceAsStream("/files/veraPDFTestSuite.pdf"),
				fileToValidatePath.toFile());
		String expectedReportAsString = new String(getClass().getResourceAsStream(
				"/files/report-" + profile.toString() + ".json").readAllBytes(), StandardCharsets.UTF_8);
		ValidationReport validationReport
				= veraPDFProcessor.validate(fileToValidatePath.toFile(), profile);
		String validationReportAsJson = writeValidationReportAsJson(validationReport);
		JSONAssert.assertEquals(expectedReportAsString, validationReportAsJson, false);
	}

	@ParameterizedTest
	@EnumSource(value = Profile.class, names = {"PDFUA_1_MACHINE"})
	public void testingValidationWithMissingProfilesOnValidateTest(Profile profile) throws IOException {
		Path fileToValidatePath
				= Files.createTempFile(tempDir.toPath(), "file_to_validate", "");
		FileUtils.copyToFile(
				getClass().getResourceAsStream("/files/veraPDFTestSuite.pdf"),
				fileToValidatePath.toFile());
		VeraPDFProcessingException exception
				= Assertions.assertThrows(VeraPDFProcessingException.class,
				() -> veraPDFProcessor.validate(fileToValidatePath.toFile(), profile));
		Assertions.assertEquals("Missing validation profile for " + profile,
				exception.getMessage());
	}

	private String writeValidationReportAsJson(ValidationReport validationReport) {
		try {
			return objectMapper.writeValueAsString(validationReport);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
