package org.verapdf.webapp.worker.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.verapdf.core.ValidationException;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.reports.Reports;
import org.verapdf.processor.reports.ValidationDetails;
import org.verapdf.processor.reports.ValidationReport;
import org.verapdf.processor.reports.enums.JobEndStatus;
import org.verapdf.wcag.algorithms.semanticalgorithms.containers.StaticContainers;
import org.verapdf.wcag.algorithms.semanticalgorithms.utils.WCAGValidationInfo;
import org.verapdf.webapp.jobservice.client.service.JobServiceClient;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.worker.entity.ProfileMapper;
import org.verapdf.webapp.worker.error.exception.VeraPDFProcessingException;
import org.verapdf.webapp.worker.error.exception.VeraPDFWorkerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class VeraPdfProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(VeraPdfProcessor.class);

	private static final String STATEMENT_PREFIX = "PDF file is ";
	private static final String NOT_INSERT = "not ";
	private static final String STATEMENT_SUFFIX = "compliant with Validation Profile requirements.";
	private static final String COMPLIANT_STATEMENT = STATEMENT_PREFIX
			+ STATEMENT_SUFFIX;
	private static final String NONCOMPLIANT_STATEMENT = STATEMENT_PREFIX
			+ NOT_INSERT + STATEMENT_SUFFIX;

	private final int processingTimeout;
	private final ProfileMapper profileMapper;
	private final JobServiceClient jobServiceClient;

	@Autowired
	public VeraPdfProcessor(@Value("${verapdf.task.processing-timeout-in-min}") int processingTimeout,
	                        ProfileMapper profileMapper, JobServiceClient jobServiceClient) {
		this.processingTimeout = processingTimeout;
		this.profileMapper = profileMapper;
		this.jobServiceClient = jobServiceClient;
		VeraGreenfieldFoundryProvider.initialise();
	}

	private static String getStatement(boolean status) {
		return status ? COMPLIANT_STATEMENT : NONCOMPLIANT_STATEMENT;
	}

	public ValidationReport validate(File source, Profile profile, UUID jobId) throws VeraPDFProcessingException {
		try (InputStream is = new FileInputStream(source)) {
			ValidationResult validationResult;
			if (profile == Profile.PDFA_AUTO) {
				try (PDFAParser parser = Foundries.defaultInstance().createParser(is);
				     PDFAValidator validator
						     = Foundries.defaultInstance().createValidator(parser.getFlavour(), false)) {
					validationResult = startValidation(validator, parser, jobId);
				}
			} else {
				ValidationProfile validationProfile
						= profileMapper.getValidationProfile(profile);
				if (validationProfile == null) {
					throw new IllegalArgumentException(
							"Missing validation profile for " + profile.name());
				}
				try (PDFAParser parser = Foundries.defaultInstance().createParser(source, validationProfile.getPDFAFlavour());
					PDFAValidator validator = ValidatorFactory.createValidator(validationProfile, 100, false, true, false)) {
					validationResult = startValidation(validator, parser, jobId);
				}
			}
			ValidationDetails details
					= Reports.fromValues(validationResult, false);
			return Reports.createValidationReport(details,
					validationResult.getProfileDetails().getName(),
					getStatement(validationResult.isCompliant()),
					validationResult.isCompliant(), validationResult.getJobEndStatus().getValue());
		} catch (Exception e) {
			throw new VeraPDFProcessingException(e.getMessage(), e);
		}
	}

	private ValidationResult startValidation(PDFAValidator validator, PDFAParser parser,
	                                         UUID jobId) throws ValidationException {
		WCAGValidationInfo wcagValidationInfo = new WCAGValidationInfo();
		StaticContainers.setWCAGValidationInfo(wcagValidationInfo);
		Runnable updateProgress = () -> {
			try {
				String progress = wcagValidationInfo.getWCAGProcessStatusWithPercent();
				if (progress == null) {
					progress = validator.getValidationProgressString();
				}
				boolean cancelJob = updateJobProgress(jobId, progress);
				if (cancelJob) {
					validator.cancelValidation(JobEndStatus.CANCELLED);
					wcagValidationInfo.setAbortProcessing(true);
				}
			} catch (VeraPDFWorkerException e) {
				e.printStackTrace();
			}
		};
		Runnable stopJob = () -> {
			validator.cancelValidation(JobEndStatus.TIMEOUT);
			wcagValidationInfo.setAbortProcessing(true);
		};

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
		executor.schedule(stopJob, processingTimeout, TimeUnit.MINUTES);
		executor.scheduleAtFixedRate(updateProgress, 0, 1, TimeUnit.SECONDS);

		ValidationResult validationResult = validator.validate(parser);

		executor.shutdown();
		return validationResult;
	}

	private Boolean updateJobProgress(UUID jobId, String progress) throws VeraPDFWorkerException {
		try {
			return jobServiceClient.updateProgressAndCheckCancellationOfJob(jobId, progress);
		} catch (RestClientResponseException e) {
			int statusCode = e.getRawStatusCode();
			String responseBody = e.getResponseBodyAsString();
			if (HttpStatus.NOT_FOUND.value() == statusCode || HttpStatus.BAD_REQUEST.value() == statusCode
			    || HttpStatus.CONFLICT.value() == statusCode) {
				LOGGER.error(responseBody);
				return false;
			} else {
				throw new VeraPDFWorkerException(TaskError.UPDATE_JOB_PROGRESS_ERROR, responseBody);
			}
		}
	}
}
