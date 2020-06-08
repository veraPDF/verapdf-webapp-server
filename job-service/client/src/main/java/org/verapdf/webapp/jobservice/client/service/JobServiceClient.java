package org.verapdf.webapp.jobservice.client.service;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.verapdf.webapp.jobservice.model.dto.JobDTO;

import java.net.URI;
import java.util.UUID;

public class JobServiceClient {
	private final RestTemplate restTemplate;
	private final URI uriToJobService;

	public JobServiceClient(URI uriToJobService) {
		this.restTemplate = new RestTemplate();
		this.uriToJobService = uriToJobService;
	}

	/**
	 * Makes request to job service to get the job by id.
	 *
	 * @param jobId - id of the requested job
	 * @return requested job
	 * @throws {@code RestClientResponseException} throws in case when request failed.
	 * Possible cases:
	 *  400 BAD_REQUEST - incorrect type of parameters for used path.
	 *  404 NOT_FOUND - job wasn't found
	 */
	public JobDTO getJobById(UUID jobId) {
		if (jobId == null) {
			return null;
		}

		return restTemplate
				.getForEntity(UriComponentsBuilder
								.fromUri(uriToJobService)
								.path("/jobs/{jobId}")
								.buildAndExpand(jobId.toString())
								.toUri(),
						JobDTO.class)
				.getBody();
	}
}
