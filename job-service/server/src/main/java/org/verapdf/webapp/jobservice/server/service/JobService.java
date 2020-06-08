package org.verapdf.webapp.jobservice.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.verapdf.webapp.error.exception.BadRequestException;
import org.verapdf.webapp.error.exception.ConflictException;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.error.exception.VeraPDFBackendException;
import org.verapdf.webapp.jobservice.model.dto.ExecutableTaskDTO;
import org.verapdf.webapp.jobservice.model.dto.JobDTO;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskStatus;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.entity.JobTask;
import org.verapdf.webapp.jobservice.server.mapper.JobMapper;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;
import org.verapdf.webapp.jobservice.server.repository.JobTaskRepository;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Service
public class JobService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

	private final int jobLifetimeDays;
	private final JobRepository jobRepository;
	private final JobTaskRepository taskRepository;
	private final QueueSender queueSender;
	private final JobMapper jobMapper;
	private final ObjectMapper objectMapper;

	public JobService(@Value("${verapdf.cleaning.lifetime-delay-days}") int jobLifetimeDays,
	                  JobRepository jobRepository, JobTaskRepository taskRepository,
	                  QueueSender queueSender, JobMapper jobMapper,
	                  ObjectMapper objectMapper) {
		this.jobLifetimeDays = jobLifetimeDays;
		this.jobRepository = jobRepository;
		this.taskRepository = taskRepository;
		this.queueSender = queueSender;
		this.jobMapper = jobMapper;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public JobDTO createJob(JobDTO jobDTO) {
		Job job = jobMapper.createEntityFromDTO(jobDTO);
		job = jobRepository.saveAndFlush(job);
		return jobMapper.createDTOFromEntity(job);
	}

	@Transactional
	public JobDTO getJobById(UUID jobId) throws NotFoundException {
		Job job = findJobById(jobId);
		return jobMapper.createDTOFromEntity(job);
	}

	@Transactional
	public JobDTO updateJob(UUID jobId, JobDTO jobDTO) throws NotFoundException, ConflictException {
		Job job = findJobById(jobId);

		if (job.getStatus() != JobStatus.CREATED) {
			throw new ConflictException("Cannot update already started job with specified id: " + jobId);
		}

		jobMapper.updateEntityFromDTO(job, jobDTO);

		job = jobRepository.save(job);
		return jobMapper.createDTOFromEntity(job);
	}

	@Transactional
	public void deleteJobById(UUID jobId) {
		if (jobRepository.existsById(jobId)) {
			jobRepository.deleteById(jobId);
		}
	}

	@Transactional
	public JobDTO startJobExecution(UUID jobId) throws VeraPDFBackendException {

		Job job = findJobById(jobId);

		if (job.getStatus() != JobStatus.CREATED) {
			throw new ConflictException("Cannot start already started job with specified id: " + jobId);
		}

		if (job.getJobTasks() == null || job.getJobTasks().isEmpty()) {
			throw new BadRequestException("Cannot start job: " + jobId + " without tasks");
		}

		for (JobTask task : job.getJobTasks()) {

			if (task.getStatus() != TaskStatus.CREATED) {
				continue;
			}

			ExecutableTaskDTO executableTaskDTO = new ExecutableTaskDTO(jobId, task.getFileId());
			try {
				queueSender.sendMessage(objectMapper.writeValueAsString(executableTaskDTO));
				task.setStatus(TaskStatus.PROCESSING);
			} catch (JsonProcessingException e) {
				String errorMessage = "Cannot convert task to json. Exception message: " + e.getMessage();
				task.setErrorResult(TaskError.PROCESSING_INTERNAL_ERROR, errorMessage);
				LOGGER.error(errorMessage, e);
			}
			taskRepository.saveAndFlush(task);
		}
		job.setStatus(JobStatus.PROCESSING);
		job = jobRepository.saveAndFlush(job);

		return jobMapper.createDTOFromEntity(job);
	}

	@Transactional
	@Scheduled(cron = "{verapdf.cleaning.cron}")
	public void clearJobsAndTasks() {
		Instant expiredTime = Instant.now().minus(jobLifetimeDays, ChronoUnit.DAYS)
				.truncatedTo(ChronoUnit.DAYS);
		jobRepository.deleteAllByCreatedAtLessThan(expiredTime);
	}

	private Job findJobById(UUID jobId) throws NotFoundException {
		return jobRepository.findById(jobId)
				.orElseThrow(() -> new NotFoundException("Job with specified id not found in DB: " + jobId));
	}
}
