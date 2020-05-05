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

	private List<ProfileDTO> profiles = new ArrayList<>();

	public ProfileController() {
		this.profiles.add(new ProfileDTO(Profile.TAGGED_PDF, true, true));
		this.profiles.add(new ProfileDTO(Profile.PDFUA_1_MACHINE, false, true));
		this.profiles.add(new ProfileDTO(Profile.PDFUA_1, true, true));
		this.profiles.add(new ProfileDTO(Profile.WCAG_2_1, true, true));
		this.profiles.add(new ProfileDTO(Profile.PDFA_1_A, true, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_1_B, true, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_2_A, true, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_2_U, true, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_2_B, true, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_3_A, true, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_3_U, true, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_3_B, true, false));
		this.profiles.add(new ProfileDTO(Profile.PDFA_AUTO, true, false));

	}

	@GetMapping
	public List<ProfileDTO> getProfiles() {
		return profiles;
	}

}
