package org.verapdf.webapp.localstorageservice.server.controller;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.error.exception.VeraPDFBackendException;
import org.verapdf.webapp.localstorageservice.model.dto.StoredFileDTO;
import org.verapdf.webapp.localstorageservice.server.service.StoredFileService;

import javax.validation.constraints.Pattern;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@Validated
public class FileController {

	private final StoredFileService storedFileService;

	public FileController(StoredFileService storedFileService) {
		this.storedFileService = storedFileService;
	}

	@PostMapping
	public ResponseEntity<StoredFileDTO> uploadFile(@RequestPart("file") MultipartFile file,
	                                                @RequestPart(required = false) @Pattern(regexp = "^[\\da-fA-F]{32}$") String contentMD5)
			throws VeraPDFBackendException {
		StoredFileDTO storedFileDTO = storedFileService.saveStoredFile(file, contentMD5);
		URI uri = MvcUriComponentsBuilder
				.fromMethodName(FileController.class, "getFileData", storedFileDTO.getId())
				.build()
				.encode()
				.toUri();

		return ResponseEntity.created(uri).body(storedFileDTO);
	}

	@PostMapping(value = "/url")
	public ResponseEntity<StoredFileDTO> uploadFileAsUrl(@RequestParam String url) throws IOException,
	                                                                                      VeraPDFBackendException,
	                                                                                      URISyntaxException {
		StoredFileDTO storedFileDTO = storedFileService.saveStoredFileFromUrl(url);
		URI uri = MvcUriComponentsBuilder
				.fromMethodName(FileController.class, "getFileData", storedFileDTO.getId())
				.build()
				.encode()
				.toUri();

		return ResponseEntity.created(uri).body(storedFileDTO);
	}

	@GetMapping(value = "/{fileId}")
	@Order(1)
	public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) throws NotFoundException,
	                                                                               FileNotFoundException {
		Pair<MediaType, Resource> result = storedFileService.getFileResourceById(fileId);
		return ResponseEntity.ok()
		                     .contentType(result.getFirst())
		                     .body(result.getSecond());
	}

	@GetMapping(value = "/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Order(2)
	public StoredFileDTO getFileData(@PathVariable UUID fileId) throws NotFoundException {
		return storedFileService.getStoredFileById(fileId);
	}
}
