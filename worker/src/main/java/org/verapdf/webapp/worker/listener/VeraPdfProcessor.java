package org.verapdf.webapp.worker.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.reports.Reports;
import org.verapdf.processor.reports.ValidationDetails;
import org.verapdf.processor.reports.ValidationReport;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.worker.entity.ProfileMapper;
import org.verapdf.webapp.worker.error.exception.VeraPDFProcessingException;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@Service
public class VeraPdfProcessor {

	private static final String STATEMENT_PREFIX = "PDF file is ";
	private static final String NOT_INSERT = "not ";
	private static final String STATEMENT_SUFFIX = "compliant with Validation Profile requirements.";
	private static final String COMPLIANT_STATEMENT = STATEMENT_PREFIX
			+ STATEMENT_SUFFIX;
	private static final String NONCOMPLIANT_STATEMENT = STATEMENT_PREFIX
			+ NOT_INSERT + STATEMENT_SUFFIX;

	private final ProfileMapper profileMapper;

	@Autowired
	public VeraPdfProcessor(ProfileMapper profileMapper) {
		this.profileMapper = profileMapper;
		VeraGreenfieldFoundryProvider.initialise();
	}

	private static String getStatement(boolean status) {
		return status ? COMPLIANT_STATEMENT : NONCOMPLIANT_STATEMENT;
	}

	public ValidationReport validate(File source, Profile profile) throws VeraPDFProcessingException {
		try (InputStream is = Files.newInputStream(source.toPath())) {
			ValidationResult validationResult;
			if (profile == Profile.PDFA_AUTO) {
				try (PDFAParser parser = Foundries.defaultInstance().createParser(is);
				     PDFAValidator validator
						     = Foundries.defaultInstance().createValidator(parser.getFlavour(), false)) {
					validationResult = validator.validate(parser);
				}
			} else {
				ValidationProfile validationProfile
						= profileMapper.getValidationProfile(profile);
				if (validationProfile == null) {
					throw new VeraPDFProcessingException(
							"Missing validation profile for " + profile.name());
				}
				try (PDFAParser parser = Foundries.defaultInstance().createParser(is, validationProfile.getPDFAFlavour());
				     PDFAValidator validator = ValidatorFactory.createValidator(validationProfile, false)) {
					validationResult = validator.validate(parser);
				}
			}
			ValidationDetails details
					= Reports.fromValues(validationResult, false, 100);
			return Reports.createValidationReport(details,
					validationResult.getProfileDetails().getName(),
					getStatement(validationResult.isCompliant()),
					validationResult.isCompliant());
		} catch(VeraPDFProcessingException e) {
			throw e;
		} catch (Exception e) {
			throw new VeraPDFProcessingException("Exception during job processing.", e);
		}
	}
}
