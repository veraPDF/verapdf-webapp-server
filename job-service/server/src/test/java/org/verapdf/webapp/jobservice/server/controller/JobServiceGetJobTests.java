package org.verapdf.webapp.jobservice.server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskStatus;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.entity.JobTask;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobServiceGetJobTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JobRepository jobRepository;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

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
						"'progress':null," +
						"'queuePosition':null," +
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
						"'progress':null," +
						"'queuePosition':null," +
						"'tasks':[{" +
						"'fileId' : '893ce251-4754-4d92-a6bc-69f886ab1ac6'," +
						"'status':'FINISHED'," +
						"'validationResultId':'534bd16b-6bd5-404e-808e-5dc731c73963'" +
						"}]}", true));
	}
}
