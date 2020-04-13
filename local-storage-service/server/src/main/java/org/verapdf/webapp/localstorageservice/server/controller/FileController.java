package org.verapdf.webapp.localstorageservice.server.controller;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.verapdf.webapp.localstorageservice.model.dto.StoredFileDTO;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.error.exception.VeraPDFBackendException;
import org.verapdf.webapp.localstorageservice.server.service.StoredFileService;

import java.io.FileNotFoundException;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

	private final StoredFileService storedFileService;

	public FileController(StoredFileService storedFileService) {
		this.storedFileService = storedFileService;
	}

	@PostMapping
	public StoredFileDTO uploadFile(@RequestPart("file") MultipartFile file,
	                                @RequestPart("contentMD5") String contentMD5) throws VeraPDFBackendException {
		return storedFileService.saveStoredFile(file, contentMD5);
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
