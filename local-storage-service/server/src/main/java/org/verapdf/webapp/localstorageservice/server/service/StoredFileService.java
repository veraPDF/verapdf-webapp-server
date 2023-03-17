package org.verapdf.webapp.localstorageservice.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.verapdf.webapp.error.exception.BadRequestException;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.error.exception.VeraPDFBackendException;
import org.verapdf.webapp.localstorageservice.model.dto.StoredFileDTO;
import org.verapdf.webapp.localstorageservice.server.entity.StoredFile;
import org.verapdf.webapp.localstorageservice.server.mapper.StoredFileMapper;
import org.verapdf.webapp.localstorageservice.server.repository.StoredFileRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class StoredFileService {

	private final int storedFileLifetimeDays;
	private final StoredFileRepository storedFileRepository;
	private final LocalFileService localFileService;
	private final StoredFileMapper storedFileMapper;

	public StoredFileService(@Value("${verapdf.cleaning.lifetime-delay-days}") int storedFileLifetimeDays,
	                         StoredFileRepository storedFileRepository, LocalFileService localFileService,
	                         StoredFileMapper storedFileMapper) {
		this.storedFileLifetimeDays = storedFileLifetimeDays;
		this.storedFileRepository = storedFileRepository;
		this.localFileService = localFileService;
		this.storedFileMapper = storedFileMapper;
	}

	public Pair<MediaType, Resource> getFileResourceById(UUID fileId) throws NotFoundException, FileNotFoundException {
		StoredFile storedFile = findStoredFileById(fileId);
		Resource resource = localFileService.getFileOnDiskResource(storedFile.getLocalPath());
		MediaType contentType = MediaType.valueOf(storedFile.getContentType());
		return Pair.of(contentType, resource);
	}

	public StoredFileDTO getStoredFileById(UUID fileID) throws NotFoundException {
		StoredFile storedFile = findStoredFileById(fileID);
		return storedFileMapper.createDTOFromEntity(storedFile);
	}

	@Transactional
	public StoredFileDTO saveStoredFile(MultipartFile file, String expectedChecksum) throws VeraPDFBackendException {
		StoredFile storedFile = constructStoredFile(file);
		StoredFile savedStoredFile = storedFileRepository.saveAndFlush(storedFile);

		Pair<String, String> localPathAndFileContentMD5;
		try {
			try (InputStream is = file.getInputStream()) {
				localPathAndFileContentMD5 =
						localFileService.saveFileOnDisk(is, storedFile.getId().toString(), expectedChecksum);
			} catch (IOException e) {
				throw new VeraPDFBackendException("Error saving file on disk.", e);
			}
		} catch (Throwable e) {
			storedFileRepository.delete(savedStoredFile);
			throw e;
		}
		savedStoredFile.setLocalPath(localPathAndFileContentMD5.getFirst());
		savedStoredFile.setContentMD5(localPathAndFileContentMD5.getSecond());

		return storedFileMapper.createDTOFromEntity(savedStoredFile);
	}

	@Transactional
	public StoredFileDTO saveStoredFileFromUrl(String urlString) throws VeraPDFBackendException, IOException {
		URL url = new URL(urlString);
		StoredFile storedFile = constructStoredFileFromUrl(url);
		StoredFile savedStoredFile = storedFileRepository.saveAndFlush(storedFile);

		Pair<String, String> localPathAndFileContentMD5;
		try {
			try (InputStream is = url.openStream()) {
				localPathAndFileContentMD5 =
						localFileService.saveFileOnDisk(is, storedFile.getId().toString(), null);
			} catch (IOException e) {
				throw new VeraPDFBackendException("Error while extracting file from url and saving it on disk.", e);
			}
		} catch (Throwable e) {
			storedFileRepository.delete(savedStoredFile);
			throw e;
		}
		savedStoredFile.setLocalPath(localPathAndFileContentMD5.getFirst());
		savedStoredFile.setContentMD5(localPathAndFileContentMD5.getSecond());

 		return storedFileMapper.createDTOFromEntity(savedStoredFile);
	}

	@Transactional
	@Scheduled(cron = "{verapdf.cleaning.cron}")
	public void clearStoredFiles() {
		Instant expiredTime = Instant.now()
		                             .minus(storedFileLifetimeDays, ChronoUnit.DAYS)
		                             .truncatedTo(ChronoUnit.DAYS);
		storedFileRepository.deleteAllByCreatedAtLessThan(expiredTime);
	}

	private StoredFile constructStoredFile(MultipartFile file) throws BadRequestException {
		String contentType = file.getContentType();
		contentType = checkAndGetContentType(contentType);

		return new StoredFile(null, null, contentType, file.getSize(), file.getOriginalFilename());
	}

	private StoredFile constructStoredFileFromUrl(URL url) throws BadRequestException, IOException {
		URLConnection urlConnection = url.openConnection();
		String contentType = urlConnection.getContentType();
		contentType = checkAndGetContentType(contentType);

		String filename = Paths.get(url.getPath()).getFileName().toString();
		return new StoredFile(null, null, contentType, urlConnection.getContentLength(), filename);
	}

	private String checkAndGetContentType(String contentType) throws BadRequestException {
		if (contentType != null) {
			try {
				contentType = MediaType.valueOf(contentType).toString();
			} catch (InvalidMediaTypeException e) {
				throw new BadRequestException("Content type can not be parsed: " + contentType);
			}
		}

		return contentType;
	}

	private StoredFile findStoredFileById(UUID fileId) throws NotFoundException {
		return storedFileRepository.findById(fileId)
		                           .orElseThrow(() -> new NotFoundException("File with specified id not found in DB: " + fileId));
	}
}
