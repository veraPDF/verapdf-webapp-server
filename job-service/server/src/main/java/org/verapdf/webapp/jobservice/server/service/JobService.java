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
import org.verapdf.webapp.jobservice.model.dto.ExecutableTaskResultDTO;
import org.verapdf.webapp.jobservice.model.dto.JobDTO;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskStatus;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.entity.JobTask;
import org.verapdf.webapp.jobservice.server.mapper.JobMapper;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;
import org.verapdf.webapp.jobservice.server.repository.JobTaskRepository;
import org.verapdf.webapp.jobservice.server.util.JobProgressProperties;
import org.verapdf.webapp.jobservice.server.util.JobQueueProperties;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;


@Service
public class JobService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

	private final int jobLifetimeDays;
	private final int processingLimit;

	private final JobRepository jobRepository;
	private final JobTaskRepository taskRepository;

	private final QueueSender queueSender;
	private final JobTaskResultHandler jobTaskResultHandler;

	private final JobMapper jobMapper;
	private final ObjectMapper objectMapper;

	private final JobProgressProperties jobProgressProperties;
	private final JobQueueProperties jobQueueProperties;

	public JobService(@Value("${verapdf.cleaning.lifetime-delay-days}") int jobLifetimeDays,
	                  @Value("${verapdf.task.processing-limit}") int processingLimit,
	                  JobRepository jobRepository, JobTaskRepository taskRepository,
	                  QueueSender queueSender, JobTaskResultHandler jobTaskResultHandler, JobMapper jobMapper,
	                  ObjectMapper objectMapper, JobProgressProperties jobProgressProperties,
	                  JobQueueProperties jobQueueProperties) {
		this.jobLifetimeDays = jobLifetimeDays;
		this.processingLimit = processingLimit;
		this.jobRepository = jobRepository;
		this.taskRepository = taskRepository;
		this.queueSender = queueSender;
		this.jobTaskResultHandler = jobTaskResultHandler;
		this.jobMapper = jobMapper;
		this.objectMapper = objectMapper;
		this.jobProgressProperties = jobProgressProperties;
		this.jobQueueProperties = jobQueueProperties;
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
		JobDTO jobDTO = jobMapper.createDTOFromEntity(job);
		checkAndAddAdditionalFieldsForJobDTO(jobDTO, job.getStatus(), jobId);

		return jobDTO;
	}

	@Transactional
	public int increaseTaskProcessingCount(UUID jobId, UUID fileId) throws NotFoundException, BadRequestException, ConflictException {
		Job job = findJobById(jobId);
		if (job.getStatus() != JobStatus.PROCESSING) {
			throw new ConflictException("Job with id: " + job.getId() + " not in PROCESSING state");
		}

		JobTask task = job.getJobTasks()
		                  .stream()
		                  .filter(jobTask -> jobTask.getFileId().equals(fileId))
		                  .findFirst()
		                  .orElse(null);

		if (task == null) {
			jobTaskResultHandler.updateJobStatus(job);
			throw new NotFoundException("Task in job, jobId: " + jobId + " not found in DB, taskId: " + fileId);
		}

		int processingCount = task.getProcessingCount();
		if (processingCount >= processingLimit) {
			handleProcessingLimit(job, jobId, fileId);
		}

		int increasedAttempts = processingCount + 1;
		task.setProcessingCount(increasedAttempts);
		return increasedAttempts;
	}

	@Transactional
	public boolean updateJobStatusToProcessing(UUID jobId) throws NotFoundException, ConflictException {
		Job job = findJobById(jobId);

		if (job.getStatus() == JobStatus.PROCESSING) {
			LOGGER.info("Job with specified id: {} is already processing, skipping" +
			            " job status updating to PROCESSING", jobId);
			return true;
		}
		if (job.getStatus() != JobStatus.WAITING) {
			throw new ConflictException("Can't change job status to PROCESSING, job with specified id: " + jobId +
			                            " is not in WAITING state");
		}
		updateJobAndTasksStatus(job, JobStatus.PROCESSING, TaskStatus.PROCESSING);

		jobQueueProperties.removeJobFromWaitingJobs(jobId);
		jobRepository.saveAndFlush(job);

		return true;
	}

	public void checkAndUpdateJobProgress(UUID jobId, String progress) {
		if (progress != null) {
			jobProgressProperties.updateProgressForJob(jobId.toString(), progress);
		} else {
			jobProgressProperties.clearJobProgressByJobId(jobId.toString());
		}
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
				String taskMessage = objectMapper.writeValueAsString(executableTaskDTO);
				jobQueueProperties.registerNewWaitingJob(jobId);
				queueSender.sendMessage(taskMessage);
				LOGGER.info("Message {} entered the queue", taskMessage);
				task.setStatus(TaskStatus.WAITING);
			} catch (JsonProcessingException e) {
				String errorMessage = "Cannot convert task to json. Exception message: " + e.getMessage();
				task.setErrorResult(TaskError.PROCESSING_INTERNAL_ERROR, errorMessage);
				LOGGER.error(errorMessage, e);
			}
			taskRepository.saveAndFlush(task);
		}
		job.setStatus(JobStatus.WAITING);
		job = jobRepository.saveAndFlush(job);

		JobDTO jobDTO = jobMapper.createDTOFromEntity(job);
		checkAndAddAdditionalFieldsForJobDTO(jobDTO, JobStatus.WAITING, jobId);

		return jobDTO;
	}

	@Transactional
	@Scheduled(cron = "{verapdf.cleaning.cron}")
	public void clearJobsAndTasks() {
		Instant expiredTime = Instant.now().minus(jobLifetimeDays, ChronoUnit.DAYS)
		                             .truncatedTo(ChronoUnit.DAYS);
		jobRepository.deleteAllByCreatedAtLessThan(expiredTime);
	}

	private void handleProcessingLimit(Job job, UUID jobId, UUID fileId) throws BadRequestException {
		String errorMessage = "The number of attempts to process the task exceeded the limit";
		ExecutableTaskDTO executableTaskDTO = new ExecutableTaskDTO(jobId, fileId);
		ExecutableTaskResultDTO executableTaskResultDTO
				= new ExecutableTaskResultDTO(executableTaskDTO, TaskError.TASK_PROCESSING_LIMIT_ERROR, errorMessage);
		jobTaskResultHandler.updateTaskStatus(executableTaskResultDTO, fileId, job);
		jobTaskResultHandler.updateJobStatus(job);
		throw new BadRequestException(errorMessage + ", jobId: " + jobId + ", fileId: " + fileId);
	}

	private Job findJobById(UUID jobId) throws NotFoundException {
		return jobRepository.findById(jobId)
		                    .orElseThrow(() -> new NotFoundException("Job with specified id not found in DB: " + jobId));
	}

	private void checkAndAddAdditionalFieldsForJobDTO(JobDTO jobDTO, JobStatus status, UUID jobId) {
		if (JobStatus.PROCESSING.equals(status) && jobProgressProperties.isPresent(jobId.toString())) {
			jobDTO.setProgress(jobProgressProperties.getProgressForJob(jobId.toString()));
		} else if (JobStatus.WAITING.equals(status)) {
			jobDTO.setQueuePosition(jobQueueProperties.getJobPositionInQueue(jobId));
		}
	}

	private void updateJobAndTasksStatus(Job job, JobStatus jobStatus, TaskStatus taskStatus) {
		job.setStatus(jobStatus);
		for (JobTask task : job.getJobTasks()) {
			task.setStatus(taskStatus);
		}
	}
}
