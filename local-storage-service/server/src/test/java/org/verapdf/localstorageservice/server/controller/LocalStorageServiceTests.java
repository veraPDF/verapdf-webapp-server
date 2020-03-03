package org.verapdf.localstorageservice.server.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.verapdf.error.exception.BadRequestException;
import org.verapdf.error.exception.NotFoundException;
import org.verapdf.error.exception.VeraPDFBackendException;
import org.verapdf.localstorageservice.server.entity.StoredFile;
import org.verapdf.localstorageservice.server.repository.StoredFileRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LocalStorageServiceTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StoredFileRepository storedFileRepository;

	@Value("${verapdf.files.base-dir}")
	private String baseDir;

	private static final byte[] FILE = {0x73, 0x72, 0x2e, 0x20, 0x11, 0x6a, 0x14, 0x16, 0x58, 0x5d};

	@Test
	public void uploadGetDownloadGeneralTest() throws Exception {
		MockMultipartFile mockFile = new MockMultipartFile("file", "testFileName.pdf", "application/pdf", FILE);
		MockMultipartFile mockChecksumPart = new MockMultipartFile(
				"contentMD5",
				"",
				"text/plain",
				"a8fc9834e7fef8d2b996020825133a55".getBytes(StandardCharsets.UTF_8));

		//Uploading file
		MvcResult uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
		                                                               .file(mockFile)
		                                                               .file(mockChecksumPart))
		                                .andExpect(status().isOk())
		                                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                .andExpect(jsonPath("$.id").isNotEmpty())
		                                .andExpect(jsonPath("$.fileName").value("testFileName.pdf"))
		                                .andExpect(jsonPath("$.contentType").value("application/pdf"))
		                                .andExpect(jsonPath("$.contentSize").value(10))
		                                .andExpect(jsonPath("$.contentMD5").value("a8fc9834e7fef8d2b996020825133a55"))
		                                .andReturn();
		//Retrieving fileID
		String jsonResponse = uploadResult.getResponse().getContentAsString();
		String uploadedFileId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving storedFile data
		mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                      .contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		       .andExpect(content().json("{'id':'" + uploadedFileId + "'," +
		                                 "'contentMD5':'a8fc9834e7fef8d2b996020825133a55'," +
		                                 "'contentType':'application/pdf'," +
		                                 "'contentSize':10," +
		                                 "'fileName':'testFileName.pdf'}", true));

		//Downloading file
		MvcResult downloadResult = mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                                                 .contentType(MediaType.APPLICATION_OCTET_STREAM))
		                                  .andExpect(status().isOk())
		                                  .andExpect(MockMvcResultMatchers.content().contentType("application/pdf"))
		                                  .andReturn();

		byte[] resultedFile = downloadResult.getResponse().getContentAsByteArray();
		assertArrayEquals(FILE, resultedFile);
	}

	@Test
	public void uploadGetDownloadWithoutFileNameTest() throws Exception {
		MockMultipartFile mockFile = new MockMultipartFile("file", null, "application/pdf", FILE);
		MockMultipartFile mockChecksumPart = new MockMultipartFile(
				"contentMD5",
				"",
				"text/plain",
				"a8fc9834e7fef8d2b996020825133a55".getBytes(StandardCharsets.UTF_8));
		//Uploading file
		MvcResult uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
		                                                               .file(mockFile)
		                                                               .file(mockChecksumPart))
		                                .andExpect(status().isOk())
		                                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                .andExpect(jsonPath("$.id").isNotEmpty())
		                                .andExpect(jsonPath("$.fileName").isEmpty())
		                                .andExpect(jsonPath("$.contentType").value("application/pdf"))
		                                .andExpect(jsonPath("$.contentSize").value(10))
		                                .andExpect(jsonPath("$.contentMD5").value("a8fc9834e7fef8d2b996020825133a55"))
		                                .andReturn();
		//Retrieving fileID
		String jsonResponse = uploadResult.getResponse().getContentAsString();
		String uploadedFileId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving storedFile data
		mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                      .contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		       .andExpect(content().json("{'id':'" + uploadedFileId + "'," +
		                                 "'contentMD5':'a8fc9834e7fef8d2b996020825133a55'," +
		                                 "'contentType':'application/pdf'," +
		                                 "'contentSize':10," +
		                                 "'fileName':''}", true));

		//Downloading file
		MvcResult downloadResult = mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                                                 .contentType(MediaType.APPLICATION_OCTET_STREAM))
		                                  .andExpect(status().isOk())
		                                  .andExpect(MockMvcResultMatchers.content().contentType("application/pdf"))
		                                  .andReturn();

		byte[] resultedFile = downloadResult.getResponse().getContentAsByteArray();
		assertArrayEquals(FILE, resultedFile);
	}

	@Test
	public void invalidChecksumOnUploadTest() throws Exception {
		MockMultipartFile mockFile = new MockMultipartFile("file", "testFileName.pdf", "application/pdf", FILE);
		MockMultipartFile mockChecksumPart = new MockMultipartFile(
				"contentMD5",
				"",
				"text/plain",
				"00009834e7fef8d2b996020825133a55".getBytes(StandardCharsets.UTF_8));

		//Uploading file
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
		                                                                    .file(mockFile)
		                                                                    .file(mockChecksumPart))
		                                     .andExpect(status().isInternalServerError())
		                                     .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message").isEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(VeraPDFBackendException.class, resolvedException.getClass());
		assertEquals(resolvedException.getMessage(), "Expected file checksum doesn't match obtained file checksum");
	}

	@Test
	public void uploadGetDownloadWithFileExtensionNotEqualsToContentType() throws Exception {

		MockMultipartFile mockFile = new MockMultipartFile("file", "testFileName.txt", "application/pdf", FILE);
		MockMultipartFile mockChecksumPart = new MockMultipartFile(
				"contentMD5",
				"",
				"text/plain",
				"a8fc9834e7fef8d2b996020825133a55".getBytes(StandardCharsets.UTF_8));

		//Uploading file
		MvcResult uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
		                                                               .file(mockFile)
		                                                               .file(mockChecksumPart))
		                                .andExpect(status().isOk())
		                                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                .andExpect(jsonPath("$.id").isNotEmpty())
		                                .andExpect(jsonPath("$.fileName").value("testFileName.txt"))
		                                .andExpect(jsonPath("$.contentType").value("application/pdf"))
		                                .andExpect(jsonPath("$.contentSize").value(10))
		                                .andExpect(jsonPath("$.contentMD5").value("a8fc9834e7fef8d2b996020825133a55"))
		                                .andReturn();
		//Retrieving fileID
		String jsonResponse = uploadResult.getResponse().getContentAsString();
		String uploadedFileId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving storedFile data
		mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                      .contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		       .andExpect(content().json("{'id':'" + uploadedFileId + "'," +
		                                 "'contentMD5':'a8fc9834e7fef8d2b996020825133a55'," +
		                                 "'contentType':'application/pdf'," +
		                                 "'contentSize':10," +
		                                 "'fileName':'testFileName.txt'}", true));

		//Downloading file
		MvcResult downloadResult = mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                                                 .contentType(MediaType.APPLICATION_OCTET_STREAM))
		                                  .andExpect(status().isOk())
		                                  .andExpect(MockMvcResultMatchers.content().contentType("application/pdf"))
		                                  .andReturn();

		byte[] resultedFile = downloadResult.getResponse().getContentAsByteArray();
		assertArrayEquals(FILE, resultedFile);
	}

	@Test
	public void uploadGetDownloadWithJsonContentTypeTest() throws Exception {
		MockMultipartFile mockFile = new MockMultipartFile("file", "testFileName.json", "application/json", FILE);
		MockMultipartFile mockChecksumPart = new MockMultipartFile(
				"contentMD5",
				"",
				"text/plain",
				"a8fc9834e7fef8d2b996020825133a55".getBytes(StandardCharsets.UTF_8));

		//Uploading file
		MvcResult uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
		                                                               .file(mockFile)
		                                                               .file(mockChecksumPart))
		                                .andExpect(status().isOk())
		                                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                .andExpect(jsonPath("$.id").isNotEmpty())
		                                .andExpect(jsonPath("$.fileName").value("testFileName.json"))
		                                .andExpect(jsonPath("$.contentType").value("application/json"))
		                                .andExpect(jsonPath("$.contentSize").value(10))
		                                .andExpect(jsonPath("$.contentMD5").value("a8fc9834e7fef8d2b996020825133a55"))
		                                .andReturn();
		//Retrieving fileID
		String jsonResponse = uploadResult.getResponse().getContentAsString();
		String uploadedFileId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving storedFile data
		mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                      .contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		       .andExpect(jsonPath("$.id").value(uploadedFileId))
		       .andExpect(jsonPath("$.fileName").value("testFileName.json"))
		       .andExpect(jsonPath("$.contentType").value("application/json"))
		       .andExpect(jsonPath("$.contentSize").value(10))
		       .andExpect(jsonPath("$.contentMD5").value("a8fc9834e7fef8d2b996020825133a55"));

		//Downloading file
		MvcResult downloadResult = mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                                                 .contentType(MediaType.APPLICATION_OCTET_STREAM))
		                                  .andExpect(status().isOk())
		                                  .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                  .andReturn();

		byte[] resultedFile = downloadResult.getResponse().getContentAsByteArray();
		assertArrayEquals(FILE, resultedFile);
	}

	@Test
	public void uploadWithUnparseableContentTypeTest() throws Exception {
		MockMultipartFile mockFile =
				new MockMultipartFile("file", "testFileName.pdf", "unparseable content type", FILE);
		MockMultipartFile mockChecksumPart = new MockMultipartFile(
				"contentMD5",
				"",
				"text/plain",
				"a8fc9834e7fef8d2b996020825133a55".getBytes(StandardCharsets.UTF_8));

		//Uploading file
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
		                                                                    .file(mockFile)
		                                                                    .file(mockChecksumPart))
		                                     .andExpect(status().isBadRequest())
		                                     .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message")
				                                                .value("Content type can not be parsed: unparseable content type"))
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(BadRequestException.class, resolvedException.getClass());
		assertEquals("Content type can not be parsed: unparseable content type", resolvedException.getMessage());
	}

	@Test
	public void getFileDataWithoutFileInDatabaseTest() throws Exception {

		String id = "893ce251-4754-4d92-a6bc-69f886ab1ac6";
		//Retrieving storedFile data
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.get("/files/" + id)
		                                                                    .contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(status().isNotFound())
		                                     .andExpect(jsonPath("$").doesNotExist())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(NotFoundException.class, resolvedException.getClass());
		assertEquals("File with specified id not found in DB: " + id, resolvedException.getMessage());
	}

	@Test
	public void downloadFileWithoutFileInDatabaseTest() throws Exception {

		String id = "893ce251-4754-4d92-a6bc-69f886ab1ac6";
		//Downloading file
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.get("/files/" + id)
		                                                                    .contentType(MediaType.APPLICATION_OCTET_STREAM))
		                                     .andExpect(status().isNotFound())
		                                     .andExpect(jsonPath("$").doesNotExist())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(NotFoundException.class, resolvedException.getClass());
		assertEquals("File with specified id not found in DB: " + id, resolvedException.getMessage());
	}

	@Test
	public void downloadFileExistedInDBAndMissingOnDiskTest() throws Exception {
		MockMultipartFile mockFile = new MockMultipartFile("file", "testFileName.pdf", "application/pdf", FILE);
		MockMultipartFile mockChecksumPart = new MockMultipartFile(
				"contentMD5",
				"",
				"text/plain",
				"a8fc9834e7fef8d2b996020825133a55".getBytes(StandardCharsets.UTF_8));

		//Uploading file
		MvcResult uploadResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/files")
		                                                               .file(mockFile)
		                                                               .file(mockChecksumPart))
		                                .andExpect(status().isOk())
		                                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                .andExpect(jsonPath("$.id").isNotEmpty())
		                                .andExpect(jsonPath("$.fileName").value("testFileName.pdf"))
		                                .andExpect(jsonPath("$.contentType").value("application/pdf"))
		                                .andExpect(jsonPath("$.contentSize").value(10))
		                                .andExpect(jsonPath("$.contentMD5").value("a8fc9834e7fef8d2b996020825133a55"))
		                                .andReturn();

		//Retrieving fileID
		String jsonResponse = uploadResult.getResponse().getContentAsString();
		String uploadedFileId = JsonPath.read(jsonResponse, "$.id");

		//Check file exists in db
		Optional<StoredFile> storedFile = storedFileRepository.findById(UUID.fromString(uploadedFileId));
		assertTrue(storedFile.isPresent());

		String localPath = storedFile.get().getLocalPath();

		//Check file exists on disk
		File file = new File(baseDir, localPath);
		assertTrue(file.exists());

		//Check file removed from file system
		assertTrue(file.delete());
		assertFalse(file.exists());

		//Retrieving storedFile data
		mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                      .contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		       .andExpect(content().json("{'id':'" + uploadedFileId + "'," +
		                                 "'contentMD5':'a8fc9834e7fef8d2b996020825133a55'," +
		                                 "'contentType':'application/pdf'," +
		                                 "'contentSize':10," +
		                                 "'fileName':'testFileName.pdf'}", true));

		//Check file download fails
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.get("/files/" + uploadedFileId)
		                                                                    .contentType(MediaType.APPLICATION_OCTET_STREAM))
		                                     .andExpect(status().isInternalServerError())
		                                     .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message").isEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(FileNotFoundException.class, resolvedException.getClass());
		assertEquals("Cannot find file on disk", resolvedException.getMessage());

	}
}
