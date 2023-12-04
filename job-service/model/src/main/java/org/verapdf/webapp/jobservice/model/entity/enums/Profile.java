package org.verapdf.webapp.jobservice.model.entity.enums;

public enum Profile {
	TAGGED_PDF("Tagged PDF"),
	PDFUA_1_MACHINE("PDF/UA-1 (Machine)"),
	PDFUA_2_MACHINE("PDF/UA-2 (Machine)"),
	PDFUA_2_TAGGED_PDF("PDF/UA-2 + Tagged PDF"),
	PDFUA_1_HUMAN("PDF/UA-1 (Human)"),
	WCAG_2_1("WCAG 2.1 (Extra)"),
	WCAG_2_1_COMPLETE("WCAG 2.1 (All)"),
	WCAG_2_1_DEV("WCAG 2.1 (DEV)"),
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
