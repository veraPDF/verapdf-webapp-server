package org.verapdf.webapp.jobservice.server.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

	@PostMapping
	public JobDTO createJob(@RequestBody @Valid JobDTO jobDTO) {
		return jobService.createJob(jobDTO);
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

	@PostMapping("/{jobId}/execution")
	public JobDTO startJobExecution(@PathVariable UUID jobId) throws VeraPDFBackendException {
		return jobService.startJobExecution(jobId);
	}
}
