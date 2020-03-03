package org.verapdf.localstorageservice.server.controller;

import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.verapdf.localstorageservice.model.dto.StoredFileDTO;
import org.verapdf.error.exception.NotFoundException;
import org.verapdf.error.exception.VeraPDFBackendException;
import org.verapdf.localstorageservice.server.service.StoredFileService;

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

	@GetMapping(value = "/{fileId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public StoredFileDTO getFileData(@PathVariable UUID fileId) throws NotFoundException {
		return storedFileService.getStoredFileById(fileId);
	}
	@GetMapping(value = "/{fileId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) throws NotFoundException,
	                                                                               FileNotFoundException {
		Pair<MediaType, Resource> result = storedFileService.getFileResourceById(fileId);
		return ResponseEntity.ok()
		                     .contentType(result.getFirst())
		                     .body(result.getSecond());
	}
}
