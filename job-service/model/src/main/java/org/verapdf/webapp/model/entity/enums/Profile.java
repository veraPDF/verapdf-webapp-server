package org.verapdf.webapp.model.entity.enums;

public enum Profile {
	TAGGED_PDF("Tagged PDF"),
	PDFUA_1_MACHINE("PDF/UA-1 (Machine)"),
	PDFUA_1("PDF/UA-1"),
	WCAG_2_1("WCAG 2.1"),
	PDFA_1_A("PDF/A-1A"),
	PDFA_1_B("PDF/A-1B"),
	PDFA_2_A("PDF/A-2A"),
	PDFA_2_B("PDF/A-2B"),
	PDFA_2_U("PDF/A-2U"),
	PDFA_3_A("PDF/A-3A"),
	PDFA_3_B("PDF/A-3B"),
	PDFA_3_U("PDF/A-3U"),
	PDFA_AUTO("PDF/A Auto-detect");

	private String humanReadableName;

	Profile(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}

	public String getHumanReadableName() {
		return humanReadableName;
	}
}
