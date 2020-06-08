package org.verapdf.webapp.localstorageservice.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.verapdf.webapp.error.ErrorDTO;
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

	@Operation(summary = "Upload file")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully uploaded file",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = StoredFileDTO.class))}),
			@ApiResponse(responseCode = "500", description = "Error saving file on disk",
			             content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
			                                 schema = @Schema(implementation = ErrorDTO.class))})
	})
	@PostMapping
	public StoredFileDTO uploadFile(@RequestPart("file") MultipartFile file,
	                                @RequestPart("contentMD5") String contentMD5) throws VeraPDFBackendException {
		return storedFileService.saveStoredFile(file, contentMD5);
	}

	//TODO: Swagger don't support same url and HTTP-method methods
	@Operation(summary = "Download file with the specified id or get its data")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Found the file for being downloaded or to get its data"),
			@ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File was not found in db or on disk", content = @Content)
	})
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
