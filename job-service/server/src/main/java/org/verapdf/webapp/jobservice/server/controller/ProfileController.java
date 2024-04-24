package org.verapdf.webapp.jobservice.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.verapdf.webapp.jobservice.model.dto.ProfileDTO;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

	private final List<ProfileDTO> profiles;

	public ProfileController() {
		this.profiles = new ArrayList<>();
		this.profiles.add(new ProfileDTO(Profile.WCAG_2_2_MACHINE, true));
		this.profiles.add(new ProfileDTO(Profile.WCAG_2_2_COMPLETE, true));
		this.profiles.add(new ProfileDTO(Profile.WCAG_2_2_HUMAN, false));
		this.profiles.add(new ProfileDTO(Profile.WCAG_2_2_DEV, false));
		this.profiles.add(new ProfileDTO(Profile.PDFUA_1, true));
		this.profiles.add(new ProfileDTO(Profile.PDFUA_2, true));
		this.profiles.add(new ProfileDTO(Profile.PDFUA_2_TAGGED_PDF, true));
		this.profiles.add(new ProfileDTO(Profile.TAGGED_PDF, true));
		this.profiles.add(new ProfileDTO(Profile.PDFA_1_A, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_1_B, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_2_A, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_2_U, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_2_B, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_3_A, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_3_U, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_3_B, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_AUTO, false));
	}

	@GetMapping
	public List<ProfileDTO> getProfiles() {
		return profiles;
	}

}
