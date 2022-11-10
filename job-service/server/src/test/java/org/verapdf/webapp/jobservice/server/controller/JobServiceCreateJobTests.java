package org.verapdf.webapp.jobservice.server.controller;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobServiceCreateJobTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@Test
	public void createAndGetJobWithoutFilesTest() throws Exception {
		String requestBody = "{\"profile\":\"TAGGED_PDF\"}";
		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andExpect(jsonPath("$.tasks").isEmpty())
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createAndGetJobWithFilesTest() throws Exception {
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"tasks\": [" +
		                     "    {" +
		                     "    \"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                     "    }," +
		                     "    {" +
		                     "    \"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\"" +
		                     "    }" +
		                     "]" +
		                     "}";
		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andExpect(jsonPath("$.tasks").isArray())
		                          .andExpect(jsonPath("$.tasks.length()").value(2))
		                          .andExpect(jsonPath("$.tasks[0].fileId")
				                                     .value("534bd16b-6bd5-404e-808e-5dc731c73963"))
		                          .andExpect(jsonPath("$.tasks[0].status").value("CREATED"))
		                          .andExpect(jsonPath("$.tasks[1].fileId")
				                                     .value("774bd16b-7ad5-354e-808e-5dc731c73963"))
		                          .andExpect(jsonPath("$.tasks[1].status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}," +
		                                 "{" +
		                                 "'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}" +
		                                 "]}", true));
	}

	@Test
	public void createJobWithNullProfileTest() throws Exception {
		String requestBody = "{\"profile\": null}";
		//Creating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(requestBody))
		                                     .andExpect(status().isBadRequest())
		                                     .andExpect(MockMvcResultMatchers.content()
		                                                                     .contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message").value("Invalid arguments"))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
		                                     .andExpect(jsonPath("$.invalidFields.size()").value(1))
		                                     .andExpect(jsonPath("$.invalidFields", Matchers.hasEntry("profile", null)))
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
		assertTrue(resolvedException.getMessage().startsWith("Validation failed for argument"));

	}

	@Test
	public void createJobWithInvalidProfileValueTest() throws Exception {
		String requestBody = "{\"profile\":\"Invalid profile\"}";
		//Creating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(requestBody))
		                                     .andExpect(status().isBadRequest())
		                                     .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message").isNotEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(HttpMessageNotReadableException.class, resolvedException.getClass());
		assertTrue(resolvedException.getMessage().startsWith("JSON parse error"));
	}

	@Test
	public void createJobWithNullTasksTest() throws Exception {
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"tasks\": null}";
		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andExpect(jsonPath("$.tasks").isEmpty())
		                          .andReturn();
		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createJobWithNullFileIdTaskTest() throws Exception {
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"tasks\": [" +
		                     "    {" +
		                     "    \"fileId\":null" +
		                     "    }" +
		                     "]" +
		                     "}";

		//Creating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(requestBody))
		                                     .andExpect(status().isBadRequest())
		                                     .andExpect(MockMvcResultMatchers.content()
		                                                                     .contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message").value("Invalid arguments"))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
		                                     .andExpect(jsonPath("$.invalidFields.size()").value(1))
		                                     .andExpect(jsonPath("$.invalidFields", Matchers.hasEntry("tasks[0].fileId", null)))
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
		assertTrue(resolvedException.getMessage().startsWith("Validation failed for argument"));
	}

	@Test
	public void createJobWithNullValueTasksTest() throws Exception {
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"tasks\": [null]" +
		                     "}";

		//Creating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(requestBody))
		                                     .andExpect(status().isBadRequest())
		                                     .andExpect(MockMvcResultMatchers.content()
		                                                                     .contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message").value("Invalid arguments"))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
		                                     .andExpect(jsonPath("$.invalidFields.size()").value(1))
		                                     .andExpect(jsonPath("$.invalidFields", Matchers.hasEntry("tasks[0]", null)))
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
		assertTrue(resolvedException.getMessage().startsWith("Validation failed for argument"));
	}

	@Test
	public void createJobWithIdTest() throws Exception {
		String id = "534bd16b-6bd5-404e-808e-5dc731c73963";
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"id\":\"" + id + "\"}";

		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));
		assertNotEquals(uploadedJobId, id);

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createJobWithInvalidIdTest() throws Exception {
		String id = "Incorrect data";
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"id\":\"" + id + "\"}";

		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));
		assertNotEquals(uploadedJobId, id);

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createJobWithInvalidStatusTest() throws Exception {
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"status\":\"invalid status\"}";
		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		Assertions.assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createJobWithStatusInBodyTest() throws Exception {
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"status\":\"PROCESSING\"}";
		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createJobWithInvalidTasksFields() throws Exception {
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                     "\"tasks\": [" +
		                     "    {" +
		                     "    \"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\"," +
		                     "    \"status\":\"Invalid status\"," +
		                     "    \"errorType\":\"Invalid type\"," +
		                     "    \"errorMessage\":null," +
		                     "    \"validationResultId\":\"Invalid id\"" +
		                     "    }" +
		                     "]" +
		                     "}";

		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andExpect(jsonPath("$.tasks").isArray())
		                          .andExpect(jsonPath("$.tasks.length()").value(1))
		                          .andExpect(jsonPath("$.tasks[0].fileId")
				                                     .value("534bd16b-6bd5-404e-808e-5dc731c73963"))
		                          .andExpect(jsonPath("$.tasks[0].status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}]}", true));
	}

	@Test
	public void createJobWithRedundantDTOFieldTest() throws Exception {
		String requestBody = "{\"profile\":\"TAGGED_PDF\"," +
		                     "\"redundantField\":\"redundant\"}";
		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isCreated())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'progress':null," +
		                                 "'queuePosition':null," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createJobWithInvalidProfileAndInvalidTasksTest() throws Exception {
		String requestBody = "{\"profile\": null," +
		                     "\"tasks\": [" +
		                     "    {" +
		                     "    \"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                     "    }," +
		                     "    null," +
		                     "    {" +
		                     "    \"fileId\":null" +
		                     "    }" +
		                     "]" +
		                     "}";

		//Creating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(requestBody))
		                                     .andExpect(status().isBadRequest())
		                                     .andExpect(MockMvcResultMatchers.content()
		                                                                     .contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message").value("Invalid arguments"))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
		                                     .andExpect(jsonPath("$.invalidFields.size()").value(3))
		                                     .andExpect(jsonPath("$.invalidFields", Matchers.hasEntry("tasks[1]", null)))
		                                     .andExpect(jsonPath("$.invalidFields", Matchers.hasEntry("tasks[2].fileId", null)))
		                                     .andExpect(jsonPath("$.invalidFields", Matchers.hasEntry("profile", null)))
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(MethodArgumentNotValidException.class, resolvedException.getClass());
		assertTrue(resolvedException.getMessage().startsWith("Validation failed for argument"));
	}
}
