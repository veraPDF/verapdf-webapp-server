package org.verapdf.webapp.jobservice.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.verapdf.webapp.error.ErrorDTO;
import org.verapdf.webapp.error.exception.ConflictException;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.error.exception.VeraPDFBackendException;
import org.verapdf.webapp.jobservice.model.dto.JobDTO;
import org.verapdf.webapp.jobservice.server.service.JobService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/jobs")
public class JobController {
	private final JobService jobService;

	public JobController(JobService jobService) {
		this.jobService = jobService;
	}

	@Operation(summary = "Create job")
	@ApiResponses(value = {
			//TODO: const for response codes
			@ApiResponse(responseCode = "200", description = "Successfully created job",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = JobDTO.class))})
	})
	@PostMapping
	public JobDTO createJob(@RequestBody @Valid JobDTO jobDTO) {
		return jobService.createJob(jobDTO);
	}

	@Operation(summary = "Get job data")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found job",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = JobDTO.class))}),
			@ApiResponse(responseCode = "404", description = "Job was not found", content = @Content)
	})
	@GetMapping("/{jobId}")
	public JobDTO getJob(@PathVariable UUID jobId) throws NotFoundException {
		return jobService.getJobById(jobId);
	}

	@Operation(summary = "Update job")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully updated job",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = JobDTO.class))}),
			@ApiResponse(responseCode = "404", description = "Job was not found", content = @Content),
			@ApiResponse(responseCode = "409", description = "Conflict while updating already started job",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = ErrorDTO.class))})
	})
	@PutMapping("/{jobId}")
	public JobDTO updateJob(@PathVariable UUID jobId, @RequestBody @Valid JobDTO jobDTO) throws NotFoundException,
	                                                                                            ConflictException {
		return jobService.updateJob(jobId, jobDTO);
	}

	@Operation(summary = "Start job")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully started job execution",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = JobDTO.class))}),
			@ApiResponse(responseCode = "500", description = "Exception while starting job",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = ErrorDTO.class))})
	})
	@PostMapping("/{jobId}/execution")
	//TODO: why we dont throw not found exception on execution request?
	public JobDTO startJobExecution(@PathVariable UUID jobId) throws VeraPDFBackendException {
		return jobService.startJobExecution(jobId);
	}
}
