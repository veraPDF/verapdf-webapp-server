package org.verapdf.webapp.jobservice.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
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
		this.profiles.add(new ProfileDTO(Profile.WCAG_2_1_COMPLETE, true));
		this.profiles.add(new ProfileDTO(Profile.WCAG_2_1, true));
		this.profiles.add(new ProfileDTO(Profile.PDFUA_1_MACHINE, true));
		this.profiles.add(new ProfileDTO(Profile.PDFUA_1_HUMAN, true));
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

	@Operation(summary = "Get list of all validation profiles")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved list of profiles",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = ProfileDTO.class))})
	})
	@GetMapping
	public List<ProfileDTO> getProfiles() {
		return profiles;
	}

}
