package org.verapdf.webapp.jobservice.client.service;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
		this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
		this.uriToJobService = uriToJobService;
	}

	/**
	 * Makes request to job service to get the job by id.
	 *
	 * @param jobId - id of the requested job
	 * @return requested job
	 * @throws {@code RestClientResponseException} throws in case when request failed.
	 *                Possible cases:
	 *                400 BAD_REQUEST - incorrect type of parameters for used path.
	 *                404 NOT_FOUND - job wasn't found
	 */
	public JobDTO getJobById(UUID jobId) {
		if (jobId == null) {
			return null;
		}

		return restTemplate.getForEntity(UriComponentsBuilder.fromUri(uriToJobService)
		                                                     .path("/jobs/{jobId}")
		                                                     .buildAndExpand(jobId.toString())
		                                                     .toUri(), JobDTO.class)
		                   .getBody();
	}

	public Integer increaseTaskProcessingCount(UUID jobId, UUID fileId) {
		if (jobId == null || fileId == null) {
			return null;
		}

		return restTemplate.patchForObject(UriComponentsBuilder.fromUri(uriToJobService)
		                                                       .path("/jobs/{jobId}/increaseTaskProcessingCount/file/{fileId}")
		                                                       .buildAndExpand(jobId.toString(), fileId.toString())
		                                                       .toUri(), null, Integer.class);
	}

	public Boolean updateJobStatusToProcessing(UUID jobId) {
		if (jobId != null) {
			return restTemplate.patchForObject(UriComponentsBuilder.fromUri(uriToJobService)
			                                                       .path("/jobs/{jobId}/processing")
			                                                       .buildAndExpand(jobId.toString())
			                                                       .toUri(), null, Boolean.class);
		}
		return false;
	}

	public Boolean updateJobProgress(UUID jobId, String progress) {
		if (jobId != null) {
			return restTemplate.patchForObject(UriComponentsBuilder.fromUri(uriToJobService)
			                                                       .path("/jobs/{jobId}/progress/{progress}")
			                                                       .buildAndExpand(jobId.toString(), progress)
			                                                       .toUri(), null, Boolean.class);
		}
		return false;
	}

	public Boolean clearJobProgress(UUID jobId) {
		if (jobId != null) {
			return restTemplate.patchForObject(UriComponentsBuilder.fromUri(uriToJobService)
			                                                       .path("/jobs/{jobId}/progress")
			                                                       .buildAndExpand(jobId.toString())
			                                                       .toUri(), null, Boolean.class);
		}
		return false;
	}
}
