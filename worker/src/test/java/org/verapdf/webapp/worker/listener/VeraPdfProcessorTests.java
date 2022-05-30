package org.verapdf.webapp.worker.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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
import java.util.Objects;

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

	//TODO enable tests after the main work on ua profiles is done
	//@ParameterizedTest
	@EnumSource(value = Profile.class)
	public void testingValidationWithAllProfilesOnValidateTest(Profile profile) throws VeraPDFProcessingException,
	                                                                                   IOException, JSONException {
		Path fileToValidatePath
				= Files.createTempFile(tempDir.toPath(), "file_to_validate", "");
		FileUtils.copyToFile(
				Objects.requireNonNull(getClass().getResourceAsStream("/files/veraPDFTestSuite.pdf")),
				fileToValidatePath.toFile());
		String expectedReportAsString = new String(getClass().getResourceAsStream(
				"/files/report-" + profile.toString() + ".json").readAllBytes(), StandardCharsets.UTF_8);
		ValidationReport validationReport
				= veraPDFProcessor.validate(fileToValidatePath.toFile(), profile);
		String validationReportAsJson = writeValidationReportAsJson(validationReport);
		Assertions.assertTrue(JSONCompare.compareJSON(expectedReportAsString, validationReportAsJson,
		                                              JSONCompareMode.NON_EXTENSIBLE).passed());
	}

	private String writeValidationReportAsJson(ValidationReport validationReport) {
		try {
			return objectMapper.writeValueAsString(validationReport);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
