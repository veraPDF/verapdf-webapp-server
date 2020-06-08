package org.verapdf.webapp.jobservice.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.verapdf.webapp.error.exception.ConflictException;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.error.exception.VeraPDFBackendException;
import org.verapdf.webapp.jobservice.model.dto.JobDTO;
import org.verapdf.webapp.jobservice.server.service.JobService;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/jobs")
public class JobController {
	private final JobService jobService;

	public JobController(JobService jobService) {
		this.jobService = jobService;
	}

	@PostMapping
	public ResponseEntity<JobDTO> createJob(@RequestBody @Valid JobDTO jobDTO) {
		JobDTO createdJobDTO = jobService.createJob(jobDTO);
		URI uri = MvcUriComponentsBuilder
				.fromMethodName(JobController.class,
				"getJob", createdJobDTO.getId())
				.build()
				.encode()
				.toUri();
		return ResponseEntity.created(uri).body(createdJobDTO);
	}

	@GetMapping("/{jobId}")
	public JobDTO getJob(@PathVariable UUID jobId) throws NotFoundException {
		return jobService.getJobById(jobId);
	}

	@PutMapping("/{jobId}")
	public JobDTO updateJob(@PathVariable UUID jobId, @RequestBody @Valid JobDTO jobDTO) throws NotFoundException,
	                                                                                            ConflictException {
		return jobService.updateJob(jobId, jobDTO);
	}

	@DeleteMapping("/{jobId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteJob(@PathVariable UUID jobId) {
		jobService.deleteJobById(jobId);
	}

	@PostMapping("/{jobId}/execution")
	public JobDTO startJobExecution(@PathVariable UUID jobId) throws VeraPDFBackendException {
		return jobService.startJobExecution(jobId);
	}
}
