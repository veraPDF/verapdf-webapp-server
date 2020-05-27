package org.verapdf.webapp.jobservice.server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.verapdf.webapp.error.exception.BadRequestException;
import org.verapdf.webapp.error.exception.ConflictException;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobServiceExecuteJobTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JobRepository jobRepository;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@Test
	public void createAndStartJobWithProcessingStatusTest() throws Exception {
		Job job = new Job(Profile.TAGGED_PDF);
		job.setStatus(JobStatus.PROCESSING);
		job = jobRepository.saveAndFlush(job);

		//Starting job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/" + job.getId() + "/execution")
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
				.andExpect(jsonPath("$.message").value(
						"Cannot start already started job with specified id: " + job.getId()))
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
				.andReturn().getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(ConflictException.class, resolvedException.getClass());
		assertEquals("Cannot start already started job with specified id: " + job.getId(),
				resolvedException.getMessage());
	}

	@Test
	public void createAndStartJobWithFinishedStatusTest() throws Exception {
		Job job = new Job(Profile.TAGGED_PDF);
		job.setStatus(JobStatus.FINISHED);
		job = jobRepository.saveAndFlush(job);

		//Starting job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/" + job.getId() + "/execution")
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isConflict())
				.andExpect(MockMvcResultMatchers.content()
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.getReasonPhrase()))
				.andExpect(jsonPath("$.message").value(
						"Cannot start already started job with specified id: " + job.getId()))
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
				.andReturn().getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(ConflictException.class, resolvedException.getClass());
		assertEquals("Cannot start already started job with specified id: " + job.getId(),
				resolvedException.getMessage());
	}

	@Test
	public void createAndStartJobWithoutTasksTest() throws Exception {
		Job job = new Job(Profile.TAGGED_PDF);
		job = jobRepository.saveAndFlush(job);

		//Starting job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/" + job.getId() + "/execution")
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
				.andExpect(jsonPath("$.message").value(
						"Cannot start job: " + job.getId() + " without tasks"))
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
				.andReturn().getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(BadRequestException.class, resolvedException.getClass());
		assertEquals("Cannot start job: " + job.getId() + " without tasks",
				resolvedException.getMessage());
	}

	@Test
	public void startJobWithoutJobInDatabaseTest() throws Exception {
		String id = "893ce251-4754-4d92-a6bc-69f886ab1ac6";

		//Starting storedFile data
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/" + id + "/execution")
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$").doesNotExist())
				.andReturn().getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(NotFoundException.class, resolvedException.getClass());
		assertEquals("Job with specified id not found in DB: " + id, resolvedException.getMessage());
	}

	@Test
	public void startJobWithIncorrectJobIdTest() throws Exception {
		//Starting job
		Exception resolvedException = mockMvc.perform(MockMvcRequestBuilders.post("/jobs/incorrectId/execution")
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
				.andExpect(jsonPath("$.message").isEmpty())
				.andExpect(jsonPath("$.timestamp").isNotEmpty())
				.andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
				.andReturn().getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(MethodArgumentTypeMismatchException.class, resolvedException.getClass());
		assertEquals("Failed to convert value of type 'java.lang.String'" +
				" to required type 'java.util.UUID';" +
				" nested exception is java.lang.IllegalArgumentException:" +
				" Invalid UUID string: incorrectId", resolvedException.getMessage());
	}
}
