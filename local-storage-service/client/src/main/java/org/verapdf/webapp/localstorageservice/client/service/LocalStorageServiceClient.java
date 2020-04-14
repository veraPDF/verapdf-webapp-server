package org.verapdf.webapp.localstorageservice.client.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.verapdf.webapp.localstorageservice.model.dto.StoredFileDTO;

import java.io.File;
import java.net.URI;

import java.util.*;

public class LocalStorageServiceClient {
	private final RestTemplate restTemplate;
	private final URI uriToStorageFileService;

	public LocalStorageServiceClient(URI uriToStorageFileService) {
		this.restTemplate = new RestTemplate();
		this.uriToStorageFileService = uriToStorageFileService;
	}

	/**
	 * Makes request to local storage service server to get a resource of the file by id.
	 *
	 * @param fileId - id of the requested file
	 * @return resource of the requested file
	 * @throws {@code RestClientResponseException} throws in case when request failed.
	 * Possible cases:
	 *  400 BAD_REQUEST - incorrect type of parameters for used path.
	 *  404 NOT_FOUND - file wasn't found.
	 *  500 INTERNAL_SERVER_ERROR - internal server error.
	 */
	public Resource getFileResourceById(UUID fileId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.emptyList());

		HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

		return restTemplate
				.exchange(
						UriComponentsBuilder
								.fromUri(uriToStorageFileService)
								.path("/files/{fileId}")
								.buildAndExpand(fileId.toString())
								.toUri(),
						HttpMethod.GET, httpEntity, Resource.class)
				.getBody();
	}

	/**
	 * Makes request to local storage service server to get the file descriptor by id.
	 *
	 * @param fileId - id of requested file
	 * @return requested file descriptor
	 * @throws {@code RestClientResponseException} throws in case when request failed.
	 * Possible cases:
	 *  400 BAD_REQUEST - incorrect type of parameters for used path.
	 *  404 NOT_FOUND - file wasn't found.
	 *  500 INTERNAL_SERVER_ERROR - internal server error.
	 */
	public StoredFileDTO getFileDescriptorById(UUID fileId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

		return restTemplate
				.exchange(
						UriComponentsBuilder
								.fromUri(uriToStorageFileService)
								.path("/files/{fileId}")
								.buildAndExpand(fileId.toString())
								.toUri(),
						HttpMethod.GET, httpEntity, StoredFileDTO.class)
				.getBody();
	}

	/**
	 * Makes request to local storage service server to save the file.
	 *
	 * @param fileToSave - file which should be saved
	 * @param fileChecksum - checksum of file which should be saved
	 * @return saved file descriptor
	 * @throws {@code RestClientResponseException} throws in case when request failed.
	 * Possible cases:
	 *  400 BAD_REQUEST - expected file fileChecksum doesn't match obtained file fileChecksum
	 * 	or incorrect type of parameters for used path.
	 *  500 INTERNAL_SERVER_ERROR - internal server error.
	 */
	public StoredFileDTO saveFile(File fileToSave, String fileChecksum) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

		//For content type "multipart/form-data" the MultiValueMap is required
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", new FileSystemResource(fileToSave));
		body.add("contentMD5", fileChecksum);

		HttpEntity<MultiValueMap<String, Object>> requestEntity
				= new HttpEntity<>(body, requestHeaders);

		return restTemplate
				.exchange(
						UriComponentsBuilder
								.fromUri(uriToStorageFileService)
								.path("/files")
								.build()
								.toUri(),
						HttpMethod.POST,
						requestEntity, StoredFileDTO.class)
				.getBody();
	}
}
