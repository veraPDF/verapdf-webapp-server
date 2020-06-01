package org.verapdf.webapp.jobservice.server.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.entity.JobTask;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;
import org.verapdf.webapp.jobservice.server.repository.JobTaskRepository;
import org.verapdf.webapp.jobservice.server.service.JobService;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobServiceClearJobTests {

	@Value("${verapdf.cleaning.lifetime-delay-days}")
	private int jobLifetimeDays;

	@Autowired
	private JobService jobService;

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
	public void nothingClearedOnCleanJobsAndTasksTest() {
		Job job1 = new Job();
		job1.setProfile(Profile.TAGGED_PDF);
		job1.setCreatedAt(startOfToday());
		JobTask jobTask1 = new JobTask();
		jobTask1.setFileId(UUID.fromString("893ce251-4754-4d92-a6bc-69f886ab1ac6"));
		job1.addTask(jobTask1);
		JobTask jobTask2 = new JobTask();
		jobTask2.setFileId(UUID.fromString("534bd16b-6bd5-404e-808e-5dc731c73963"));
		job1.addTask(jobTask2);
		jobRepository.saveAndFlush(job1);

		Job job2 = new Job();
		job2.setProfile(Profile.TAGGED_PDF);
		job2.setCreatedAt(startOfToday().minus(jobLifetimeDays / 2, ChronoUnit.DAYS)
				.minus(10, ChronoUnit.MINUTES));
		jobRepository.saveAndFlush(job2);

		Job job3 = new Job();
		job3.setProfile(Profile.TAGGED_PDF);
		job3.setCreatedAt(startOfToday().minus(jobLifetimeDays, ChronoUnit.DAYS)
				.plus(10, ChronoUnit.MINUTES));
		JobTask jobTask3 = new JobTask();
		jobTask3.setFileId(UUID.fromString("774bd16b-7ad5-354e-808e-5dc731c73963"));
		job3.addTask(jobTask3);
		jobRepository.saveAndFlush(job3);

		Assertions.assertEquals(3, jobRepository.count());
		Assertions.assertEquals(3, jobTaskRepository.count());

		jobService.clearJobsAndTasks();

		Assertions.assertEquals(3, jobRepository.count());
		Assertions.assertEquals(3, jobTaskRepository.count());
	}

	@Test
	public void partlyClearedOnCleanJobsAndTasksTest() {
		Job job1 = new Job();
		job1.setProfile(Profile.TAGGED_PDF);
		job1.setCreatedAt(startOfToday().minus(jobLifetimeDays / 2, ChronoUnit.DAYS)
				.minus(10, ChronoUnit.MINUTES));
		JobTask jobTask1 = new JobTask();
		jobTask1.setFileId(UUID.fromString("893ce251-4754-4d92-a6bc-69f886ab1ac6"));
		job1.addTask(jobTask1);
		JobTask jobTask2 = new JobTask();
		jobTask2.setFileId(UUID.fromString("534bd16b-6bd5-404e-808e-5dc731c73963"));
		job1.addTask(jobTask2);
		jobRepository.saveAndFlush(job1);

		Job job2 = new Job();
		job2.setProfile(Profile.TAGGED_PDF);
		job2.setCreatedAt(startOfToday().minus(jobLifetimeDays, ChronoUnit.DAYS));
		jobRepository.saveAndFlush(job2);

		Job job3 = new Job();
		job3.setProfile(Profile.TAGGED_PDF);
		job3.setCreatedAt(startOfToday().minus(jobLifetimeDays, ChronoUnit.DAYS)
				.minus(1, ChronoUnit.SECONDS));
		JobTask jobTask3 = new JobTask();
		jobTask3.setFileId(UUID.fromString("774bd16b-7ad5-354e-808e-5dc731c73963"));
		job3.addTask(jobTask3);
		jobRepository.saveAndFlush(job3);

		Assertions.assertEquals(3, jobRepository.count());
		Assertions.assertEquals(3, jobTaskRepository.count());

		jobService.clearJobsAndTasks();

		Assertions.assertEquals(2, jobRepository.count());
		Assertions.assertEquals(2, jobTaskRepository.count());
	}

	@Test
	public void allClearedOnCleanJobsAndTasksTest() {
		Job job1 = new Job();
		job1.setProfile(Profile.TAGGED_PDF);
		job1.setCreatedAt(startOfToday().minus(jobLifetimeDays, ChronoUnit.DAYS)
				.minus(1, ChronoUnit.SECONDS));
		JobTask jobTask1 = new JobTask();
		jobTask1.setFileId(UUID.fromString("893ce251-4754-4d92-a6bc-69f886ab1ac6"));
		job1.addTask(jobTask1);
		JobTask jobTask2 = new JobTask();
		jobTask2.setFileId(UUID.fromString("534bd16b-6bd5-404e-808e-5dc731c73963"));
		job1.addTask(jobTask2);
		jobRepository.saveAndFlush(job1);

		Job job2 = new Job();
		job2.setProfile(Profile.TAGGED_PDF);
		job2.setCreatedAt(startOfToday().minus(
				jobLifetimeDays + 3, ChronoUnit.DAYS));
		jobRepository.saveAndFlush(job2);

		Job job3 = new Job();
		job3.setProfile(Profile.TAGGED_PDF);
		job3.setCreatedAt(startOfToday().minus(
				jobLifetimeDays + 5, ChronoUnit.DAYS));
		JobTask jobTask3 = new JobTask();
		jobTask3.setFileId(UUID.fromString("774bd16b-7ad5-354e-808e-5dc731c73963"));
		job3.addTask(jobTask3);
		jobRepository.saveAndFlush(job3);

		Assertions.assertEquals(3, jobRepository.count());
		Assertions.assertEquals(3, jobTaskRepository.count());

		jobService.clearJobsAndTasks();

		Assertions.assertEquals(0, jobRepository.count());
		Assertions.assertEquals(0, jobTaskRepository.count());
	}

	@Test
	public void emptyJobsAndTasksOnCleanJobsAndTasksTest() {
		Assertions.assertEquals(0, jobRepository.count());
		Assertions.assertEquals(0, jobTaskRepository.count());

		jobService.clearJobsAndTasks();

		Assertions.assertEquals(0, jobRepository.count());
		Assertions.assertEquals(0, jobTaskRepository.count());
	}

	private Instant startOfToday() {
		return Instant.now().truncatedTo(ChronoUnit.DAYS);
	}
}
