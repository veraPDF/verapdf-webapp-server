package org.verapdf.webapp.jobservice.server.controller;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.verapdf.webapp.error.exception.ConflictException;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskStatus;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.entity.JobTask;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobServiceTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JobRepository jobRepository;

	@Test
	public void createAndGetJobWithoutFilesTest() throws Exception {
		String requestBody = "{\"profile\":\"TAGGED_PDF\"}";
		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                         .contentType(MediaType.APPLICATION_JSON)
		                                                         .content(requestBody))
		                          .andExpect(status().isOk())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andExpect(jsonPath("$.tasks").isEmpty())
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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
		                          .andExpect(status().isOk())
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

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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
		                                     .andExpect(jsonPath("$.message")
				                                                .value("Argument parsing failed"))
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
		                          .andExpect(status().isOk())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andExpect(jsonPath("$.tasks").isEmpty())
		                          .andReturn();
		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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
		                          .andExpect(status().isOk())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertNotEquals(uploadedJobId, id);

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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
		                          .andExpect(status().isOk())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertNotEquals(uploadedJobId, id);

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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
		                          .andExpect(status().isOk())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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
		                          .andExpect(status().isOk())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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
		                          .andExpect(status().isOk())
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

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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
		                          .andExpect(status().isOk())
		                          .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		                          .andExpect(jsonPath("$.id").isNotEmpty())
		                          .andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
		                          .andExpect(jsonPath("$.status").value("CREATED"))
		                          .andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
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

	@Test
	public void getJobWithoutJobInDatabaseTest() throws Exception {
		String id = "893ce251-4754-4d92-a6bc-69f886ab1ac6";
		//Retrieving storedFile data
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + id)
		                                                                    .accept(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(status().isNotFound())
		                                     .andExpect(jsonPath("$").doesNotExist())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(NotFoundException.class, resolvedException.getClass());
		assertEquals("Job with specified id not found in DB: " + id, resolvedException.getMessage());
	}

	@Test
	public void getErrorJobTest() throws Exception {
		String taskId = "893ce251-4754-4d92-a6bc-69f886ab1ac6";

		JobTask jobTask = new JobTask();
		jobTask.setFileId(UUID.fromString(taskId));
		jobTask.setStatus(TaskStatus.ERROR);
		jobTask.setErrorType(TaskError.SENDING_TO_QUEUE_ERROR);
		jobTask.setErrorMessage("Error");

		Job job = new Job(Profile.TAGGED_PDF);
		job.addTask(jobTask);
		job.setStatus(JobStatus.FINISHED);
		job = jobRepository.saveAndFlush(job);

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + job.getId())
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + job.getId() + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'FINISHED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '893ce251-4754-4d92-a6bc-69f886ab1ac6'," +
		                                 "'status':'ERROR'," +
		                                 "'errorType':'SENDING_TO_QUEUE_ERROR'," +
		                                 "'errorMessage':'Error'" +
		                                 "}]}", true));
	}

	@Test
	public void getResultJobTest() throws Exception {
		String taskId = "893ce251-4754-4d92-a6bc-69f886ab1ac6";
		String resultFileId = "534bd16b-6bd5-404e-808e-5dc731c73963";

		JobTask jobTask = new JobTask();
		jobTask.setFileId(UUID.fromString(taskId));
		jobTask.setStatus(TaskStatus.FINISHED);
		jobTask.setResultFileId(UUID.fromString(resultFileId));

		Job job = new Job(Profile.TAGGED_PDF);
		job.addTask(jobTask);
		job.setStatus(JobStatus.FINISHED);
		job = jobRepository.saveAndFlush(job);

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + job.getId())
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + job.getId() + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'FINISHED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '893ce251-4754-4d92-a6bc-69f886ab1ac6'," +
		                                 "'status':'FINISHED'," +
		                                 "'validationResultId':'534bd16b-6bd5-404e-808e-5dc731c73963'" +
		                                 "}]}", true));
	}

	@Test
	public void createUpdateGetJobWithTasksTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		String updateRequestBody = "{\"profile\": \"WCAG_2_1\"," +
		                           "\"tasks\": [" +
		                           "    {" +
		                           "    \"fileId\":\"777bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }," +
		                           "    {" +
		                           "    \"fileId\":\"888bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }," +
		                           "    {" +
		                           "    \"fileId\":\"999bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }" +
		                           "]" +
		                           "}";

		//Updating job
		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'WCAG_2_1'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '888bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}," +
		                                 "{" +
		                                 "'fileId' : '999bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}," +
		                                 "{" +
		                                 "'fileId' : '777bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}" +
		                                 "]}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'WCAG_2_1'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '888bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}," +
		                                 "{" +
		                                 "'fileId' : '999bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}," +
		                                 "{" +
		                                 "'fileId' : '777bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}" +
		                                 "]}", true));
	}

	@Test
	public void createUpdateGetJobWithSameTasksTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		String updateRequestBody = "{\"profile\": \"WCAG_2_1\"," +
		                           "\"tasks\": [" +
		                           "    {" +
		                           "    \"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\"" +
		                           "    }," +
		                           "    {" +
		                           "    \"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }" +
		                           "]" +
		                           "}";

		//Updating job
		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'WCAG_2_1'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}," +
		                                 "{" +
		                                 "'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}" +
		                                 "]}", true));


		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'WCAG_2_1'," +
		                                 "'status':'CREATED'," +
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
	public void createUpdateGetJobWithPartialTaskUpdateTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		String updateRequestBody = "{\"profile\": \"WCAG_2_1\"," +
		                           "\"tasks\": [" +
		                           "    {" +
		                           "    \"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\"" +
		                           "    }," +
		                           "    {" +
		                           "    \"fileId\":\"414bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }" +
		                           "]" +
		                           "}";

		//Updating job
		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'WCAG_2_1'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '414bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}," +
		                                 "{" +
		                                 "'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}" +
		                                 "]}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'WCAG_2_1'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '414bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}," +
		                                 "{" +
		                                 "'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}" +
		                                 "]}", true));
	}

	@Test
	public void createUpdateGetJobWithoutTasksTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\": \"WCAG_2_1\"}";

		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'WCAG_2_1'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'WCAG_2_1'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createUpdateJobWithProcessingStatusTest() throws Exception {
		Job job = new Job(Profile.TAGGED_PDF);
		job.setStatus(JobStatus.PROCESSING);
		job = jobRepository.saveAndFlush(job);

		String updateRequestBody = "{\"profile\": \"WCAG_2_1\"," +
		                           "\"tasks\": [" +
		                           "    {" +
		                           "    \"fileId\":\"777bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }," +
		                           "    {" +
		                           "    \"fileId\":\"888bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }," +
		                           "    {" +
		                           "    \"fileId\":\"999bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }" +
		                           "]" +
		                           "}";

		//Updating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + job.getId())
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
		                                     .andExpect(status().isConflict())
		                                     .andExpect(MockMvcResultMatchers.content()
		                                                                     .contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message")
				                                                .value("Cannot update already started job with specified id: " + job.getId()))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
		                                     .andReturn().getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(ConflictException.class, resolvedException.getClass());
		assertEquals("Cannot update already started job with specified id: " + job.getId(), resolvedException.getMessage());
	}

	@Test
	public void createUpdateJobWithErrorStatusTest() throws Exception {
		Job job = new Job(Profile.TAGGED_PDF);
		job.setStatus(JobStatus.FINISHED);
		job = jobRepository.saveAndFlush(job);

		String updateRequestBody = "{\"profile\": \"WCAG_2_1\"," +
		                           "\"tasks\": [" +
		                           "    {" +
		                           "    \"fileId\":\"777bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }" +
		                           "]" +
		                           "}";

		//Updating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + job.getId())
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
		                                     .andExpect(status().isConflict())
		                                     .andExpect(MockMvcResultMatchers.content()
		                                                                     .contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message")
				                                                .value("Cannot update already started job with specified id: " + job.getId()))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
		                                     .andReturn().getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(ConflictException.class, resolvedException.getClass());
		assertEquals("Cannot update already started job with specified id: " + job.getId(), resolvedException.getMessage());
	}

	@Test
	public void createUpdateJobWithFinishedStatusTest() throws Exception {
		Job job = new Job(Profile.TAGGED_PDF);
		job.setStatus(JobStatus.FINISHED);
		job = jobRepository.saveAndFlush(job);

		String updateRequestBody = "{\"profile\": \"WCAG_2_1\"," +
		                           "\"tasks\": [" +
		                           "    {" +
		                           "    \"fileId\":\"777bd16b-6bd5-404e-808e-5dc731c73963\"" +
		                           "    }" +
		                           "]" +
		                           "}";

		//Updating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + job.getId())
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
		                                     .andExpect(status().isConflict())
		                                     .andExpect(MockMvcResultMatchers.content()
		                                                                     .contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message")
				                                                .value("Cannot update already started job with specified id: " + job.getId()))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
		                                     .andReturn().getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(ConflictException.class, resolvedException.getClass());
		assertEquals("Cannot update already started job with specified id: " + job.getId(), resolvedException.getMessage());
	}

	@Test
	public void updateJobWithoutJobInDatabaseTest() throws Exception {
		String id = "893ce251-4754-4d92-a6bc-69f775ab1ac6";
		String updateRequestBody = "{\"profile\": \"WCAG_2_1\"}";

		//Updating job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + id)
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
		                                     .andExpect(status().isNotFound())
		                                     .andExpect(jsonPath("$").doesNotExist())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(NotFoundException.class, resolvedException.getClass());
		assertEquals("Job with specified id not found in DB: " + id, resolvedException.getMessage());
	}

	@Test
	public void createAndUpdateJobWithNullProfileTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\":null}";

		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
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
	public void createAndUpdateJobWithInvalidProfileValueTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\":\"Invalid profile\"}";

		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
		                                     .andExpect(status().isBadRequest())
		                                     .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		                                     .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
		                                     .andExpect(jsonPath("$.message")
				                                                .value("Argument parsing failed"))
		                                     .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
		                                     .andExpect(jsonPath("$.timestamp").isNotEmpty())
		                                     .andReturn().getResolvedException();
		assertNotNull(resolvedException);
		assertEquals(HttpMessageNotReadableException.class, resolvedException.getClass());
		assertTrue(resolvedException.getMessage().startsWith("JSON parse error"));
	}

	@Test
	public void createAndUpdateJobWithIdInBodyTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String id = "534bd16b-6bd5-404e-808e-5dc731c73963";
		String updateRequestBody = "{\"profile\":\"TAGGED_PDF\"," +
		                           "\"id\":\"" + id + "\"}";

		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createAndUpdateJobWithInvalidIdInBodyTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String id = "Incorrect data";
		String updateRequestBody = "{\"profile\":\"TAGGED_PDF\"," +
		                           "\"id\":\"" + id + "\"}";

		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createUpdateGetJobWithStatusInBodyTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                           "\"status\":\"PROCESSING\"}";

		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createUpdateGetJobWithInvalidStatusInBodyTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                           "\"status\":\"Incorrect data\"}";

		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createUpdateGetJobWithNullTasksTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                           "\"tasks\": null}";

		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createAndUpdateJobWithNullFileIdTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                           "\"tasks\": [" +
		                           "    {" +
		                           "    \"fileId\":null" +
		                           "    }" +
		                           "]" +
		                           "}";

		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
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
	public void createAndUpdateJobWithNullValueTasksTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
		                           "\"tasks\": [null]" +
		                           "}";

		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
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
	public void createUpdateGetJobWithInvalidTaskFieldsTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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

		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}]}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':[{" +
		                                 "'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
		                                 "'status':'CREATED'" +
		                                 "}]}", true));
	}

	@Test
	public void createUpdateGetJobWithRedundantDTOFieldTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\":\"TAGGED_PDF\"," +
		                           "\"redundantField\":\"redundant\"}";

		mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                      .contentType(MediaType.APPLICATION_JSON)
		                                      .content(updateRequestBody))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));

		//Retrieving job data
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
		                                      .accept(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
		       .andExpect(content().json("{'id':'" + uploadedJobId + "'," +
		                                 "'profile':'TAGGED_PDF'," +
		                                 "'status':'CREATED'," +
		                                 "'tasks':null}", true));
	}

	@Test
	public void createUpdateJobWithInvalidProfileAndInvalidTasksTest() throws Exception {
		String createRequestBody = "{\"profile\": \"TAGGED_PDF\"," +
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
		MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
		                                                               .contentType(MediaType.APPLICATION_JSON)
		                                                               .content(createRequestBody))
		                                .andExpect(status().isOk())
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

		String jsonResponse = createResult.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		//Updating job
		String updateRequestBody = "{\"profile\": null," +
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

		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.put("/jobs/" + uploadedJobId)
		                                                                    .contentType(MediaType.APPLICATION_JSON)
		                                                                    .content(updateRequestBody))
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
