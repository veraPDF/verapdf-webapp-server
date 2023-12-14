package org.verapdf.webapp.jobservice.model.entity.enums;

public enum Profile {
	TAGGED_PDF("ISO 32005"),
	PDFUA_1("PDF/UA-1"),
	PDFUA_2("PDF/UA-2"),
	PDFUA_2_TAGGED_PDF("PDF/UA-2 & ISO 32005"),
	WCAG_2_2_HUMAN("WCAG 2.2 (Human)"),
	WCAG_2_2_MACHINE("WCAG 2.2 (Machine)"),
	WCAG_2_2_COMPLETE("WCAG 2.2 Machine & Human (experimental)"),
	WCAG_2_2_DEV("WCAG 2.2 (DEV)"),
	PDFA_1_A("PDF/A-1A"),
	PDFA_1_B("PDF/A-1B"),
	PDFA_2_A("PDF/A-2A"),
	PDFA_2_U("PDF/A-2U"),
	PDFA_2_B("PDF/A-2B"),
	PDFA_3_A("PDF/A-3A"),
	PDFA_3_U("PDF/A-3U"),
	PDFA_3_B("PDF/A-3B"),
	PDFA_AUTO("PDF/A Auto-detect");

	private String humanReadableName;

	Profile(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}

	public String getHumanReadableName() {
		return humanReadableName;
	}

}
