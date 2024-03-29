package org.verapdf.webapp.worker.entity;

import org.springframework.stereotype.Component;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.validation.profiles.ProfileDirectory;
import org.verapdf.pdfa.validation.profiles.Profiles;
import org.verapdf.pdfa.validation.profiles.ValidationProfile;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;

import javax.xml.bind.JAXBException;
import java.util.EnumMap;
import java.util.Objects;

@Component
public class ProfileMapper {

	private final EnumMap<Profile, ValidationProfile> profilesMap;

	public ProfileMapper() throws JAXBException {
		this.profilesMap = new EnumMap<>(Profile.class);

		ProfileDirectory veraProfileDirectory = Profiles.getVeraProfileDirectory();
		// PDF/A profilesMap
		this.profilesMap.put(Profile.PDFA_1_A, veraProfileDirectory
				.getValidationProfileByFlavour(PDFAFlavour.PDFA_1_A));
		this.profilesMap.put(Profile.PDFA_1_B, veraProfileDirectory
				.getValidationProfileByFlavour(PDFAFlavour.PDFA_1_B));
		this.profilesMap.put(Profile.PDFA_2_A, veraProfileDirectory
				.getValidationProfileByFlavour(PDFAFlavour.PDFA_2_A));
		this.profilesMap.put(Profile.PDFA_2_B, veraProfileDirectory
				.getValidationProfileByFlavour(PDFAFlavour.PDFA_2_B));
		this.profilesMap.put(Profile.PDFA_2_U, veraProfileDirectory
				.getValidationProfileByFlavour(PDFAFlavour.PDFA_2_U));
		this.profilesMap.put(Profile.PDFA_3_A, veraProfileDirectory
				.getValidationProfileByFlavour(PDFAFlavour.PDFA_3_A));
		this.profilesMap.put(Profile.PDFA_3_B, veraProfileDirectory
				.getValidationProfileByFlavour(PDFAFlavour.PDFA_3_B));
		this.profilesMap.put(Profile.PDFA_3_U, veraProfileDirectory
				.getValidationProfileByFlavour(PDFAFlavour.PDFA_3_U));

		// PDF/UA related profilesMap
		this.profilesMap.put(Profile.WCAG_2_2_COMPLETE,
		                     Profiles.profileFromXml(Objects.requireNonNull(getClass().getResourceAsStream(
				                     "/profiles/veraPDF-validation-profiles/PDF_UA/WCAG-2-2-Complete.xml"))));
		this.profilesMap.put(Profile.WCAG_2_2_MACHINE,
				Profiles.profileFromXml(Objects.requireNonNull(getClass().getResourceAsStream(
						"/profiles/veraPDF-validation-profiles/PDF_UA/WCAG-2-2-Machine.xml"))));
		this.profilesMap.put(Profile.PDFUA_1,
		                     Profiles.profileFromXml(Objects.requireNonNull(getClass().getResourceAsStream(
				                     "/profiles/veraPDF-validation-profiles/PDF_UA/PDFUA-1.xml"))));
		this.profilesMap.put(Profile.PDFUA_2,
				Profiles.profileFromXml(Objects.requireNonNull(getClass().getResourceAsStream(
						"/profiles/veraPDF-validation-profiles/PDF_UA/PDFUA-2.xml"))));
		this.profilesMap.put(Profile.PDFUA_2_TAGGED_PDF,
				Profiles.profileFromXml(Objects.requireNonNull(getClass().getResourceAsStream(
						"/profiles/veraPDF-validation-profiles/PDF_UA/PDFUA-2-ISO32005.xml"))));
		this.profilesMap.put(Profile.TAGGED_PDF,
		                     Profiles.profileFromXml(Objects.requireNonNull(getClass().getResourceAsStream(
				                     "/profiles/veraPDF-validation-profiles/PDF_UA/ISO-32005-Tagged.xml"))));
		this.profilesMap.put(Profile.WCAG_2_2_HUMAN,
		                     Profiles.profileFromXml(Objects.requireNonNull(getClass().getResourceAsStream(
				                     "/profiles/veraPDF-validation-profiles/PDF_UA/WCAG-2-2.xml"))));
		this.profilesMap.put(Profile.WCAG_2_2_DEV,
		                     Profiles.profileFromXml(Objects.requireNonNull(getClass().getResourceAsStream(
				                     "/profiles/veraPDF-validation-profiles/PDF_UA/WCAG-2-2-Dev.xml"))));
	}

	public ValidationProfile getValidationProfile(Profile profile) {
		return profilesMap.get(profile);
	}
}
