package org.verapdf.webapp.localstorageservice.server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.verapdf.webapp.localstorageservice.server.error.exception.LowDiskSpaceException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"verapdf.files.min-space-threshold=8388607TB"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LowDiskSpaceTests {

	//Global test parameters
	private static final byte[] FILE = {0x73, 0x72, 0x2e, 0x20, 0x11, 0x6a, 0x14, 0x16, 0x58, 0x5d};

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void lowDiskSpaceOnUploadTest() throws Exception {
		MockMultipartFile mockFile = new MockMultipartFile("file", "testFileName.pdf",
		                                                   "application/pdf", FILE);
		MockMultipartFile mockChecksumPart = new MockMultipartFile(
				"contentMD5",
				"",
				"text/plain",
				"a8fc9834e7fef8d2b996020825133a55".getBytes(StandardCharsets.UTF_8));

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
		assertEquals(LowDiskSpaceException.class, resolvedException.getClass());
		assertEquals("Low disk space", resolvedException.getMessage());
	}
}
