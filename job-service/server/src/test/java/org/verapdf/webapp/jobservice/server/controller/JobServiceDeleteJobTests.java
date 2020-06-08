package org.verapdf.webapp.jobservice.server.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskStatus;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.entity.JobTask;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;
import org.verapdf.webapp.jobservice.server.repository.JobTaskRepository;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobServiceDeleteJobTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobTaskRepository jobTaskRepository;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@BeforeEach
	public void clearJobsAndTasks() {
		jobRepository.deleteAll();
		jobTaskRepository.deleteAll();
	}

	@Test
	public void deleteJobWithoutJobInDatabaseTest() throws Exception {
		String jobId = "893ce251-4754-4d92-a6bc-69f886ab1ac6";

		Assertions.assertEquals(0, jobRepository.count());

		//Deleting storedFile data
		mockMvc.perform(MockMvcRequestBuilders.delete("/jobs/" + jobId))
				.andExpect(status().isNoContent());

		Assertions.assertEquals(0, jobRepository.count());
	}

	@Test
	public void deleteJobWithTasksTest() throws Exception {
		String taskId = "893ce251-4754-4d92-a6bc-69f886ab1ac6";
		String taskResultId = "397856eb-10fb-48c1-ad45-c90e418f070a";

		JobTask jobTask = new JobTask();
		jobTask.setFileId(UUID.fromString(taskId));
		jobTask.setSuccessfulResult(UUID.fromString(taskResultId));
		jobTask.setStatus(TaskStatus.FINISHED);

		Job job = new Job(Profile.TAGGED_PDF);
		job.addTask(jobTask);
		job.setStatus(JobStatus.FINISHED);
		job = jobRepository.saveAndFlush(job);

		Assertions.assertEquals(1, jobRepository.count());
		Assertions.assertEquals(1, jobTaskRepository.count());

		//Deleting job data
		mockMvc.perform(MockMvcRequestBuilders.delete("/jobs/" + job.getId())
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNoContent());

		Assertions.assertEquals(0, jobRepository.count());
		Assertions.assertEquals(0, jobTaskRepository.count());
	}

	@Test
	public void deleteJustOneJobWithTasksTest() throws Exception {
		String taskId = "893ce251-4754-4d92-a6bc-69f886ab1ac6";
		String taskResultId = "397856eb-10fb-48c1-ad45-c90e418f070a";

		JobTask jobTask = new JobTask();
		jobTask.setFileId(UUID.fromString(taskId));
		jobTask.setSuccessfulResult(UUID.fromString(taskResultId));
		jobTask.setStatus(TaskStatus.FINISHED);

		Job jobToDelete = new Job(Profile.TAGGED_PDF);
		jobToDelete.addTask(jobTask);
		jobToDelete.setStatus(JobStatus.FINISHED);
		jobRepository.saveAndFlush(jobToDelete);
		Job jobToSave = new Job(Profile.TAGGED_PDF);
		jobToSave.setStatus(JobStatus.FINISHED);
		jobRepository.saveAndFlush(jobToSave);

		Assertions.assertEquals(2, jobRepository.count());
		Assertions.assertEquals(1, jobTaskRepository.count());

		//Deleting job data
		mockMvc.perform(MockMvcRequestBuilders.delete("/jobs/" + jobToDelete.getId())
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNoContent());

		Assertions.assertEquals(1, jobRepository.count());
		Assertions.assertEquals(0, jobTaskRepository.count());
	}

	@Test
	public void deleteJobWithoutTasksTest() throws Exception {
		Job job = new Job(Profile.TAGGED_PDF);
		job.setStatus(JobStatus.FINISHED);
		job = jobRepository.saveAndFlush(job);

		Assertions.assertEquals(1, jobRepository.count());

		//Deleting job data
		mockMvc.perform(MockMvcRequestBuilders.delete("/jobs/" + job.getId())
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isNoContent());

		Assertions.assertEquals(0, jobRepository.count());
	}
}
