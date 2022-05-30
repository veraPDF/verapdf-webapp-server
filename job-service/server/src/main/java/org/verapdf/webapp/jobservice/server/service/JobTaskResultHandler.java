package org.verapdf.webapp.jobservice.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.verapdf.webapp.jobservice.model.dto.ExecutableTaskDTO;
import org.verapdf.webapp.jobservice.model.dto.ExecutableTaskResultDTO;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskStatus;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.entity.JobTask;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;
import org.verapdf.webapp.jobservice.server.repository.JobTaskRepository;
import org.verapdf.webapp.queueclient.entity.QueueErrorEventType;
import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorData;
import org.verapdf.webapp.queueclient.handler.QueueListenerHandler;
import org.verapdf.webapp.queueclient.handler.QueueSenderErrorEventHandler;
import org.verapdf.webapp.queueclient.util.QueueUtil;

import java.util.UUID;

@Service
public class JobTaskResultHandler implements QueueListenerHandler, QueueSenderErrorEventHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobTaskResultHandler.class);

	private final JobRepository jobRepository;
	private final JobTaskRepository taskRepository;
	private final ObjectMapper objectMapper;
	private final QueueUtil queueUtil;

	public JobTaskResultHandler(JobRepository jobRepository, JobTaskRepository taskRepository,
	                            ObjectMapper objectMapper, QueueUtil queueUtil) {
		this.jobRepository = jobRepository;
		this.taskRepository = taskRepository;
		this.objectMapper = objectMapper;
		this.queueUtil = queueUtil;
	}

	@Override
	@Transactional
	public void handleMessage(String message, Channel channel, long deliveryTag) {
		ExecutableTaskResultDTO taskResult = null;
		try {
			taskResult = objectMapper.readValue(message, ExecutableTaskResultDTO.class);
			saveJobTaskResult(taskResult);
			queueUtil.applyAndDiscardJob(channel, deliveryTag, taskResult.getJobId(), taskResult.getFileId());
		} catch (JsonProcessingException e) {
			LOGGER.error("Cannot parse message to task result. Message: '" + message + '\'', e);
			queueUtil.rejectAndDiscardJob(channel, deliveryTag, null, null);
		} catch (Exception e) {
			LOGGER.error("Unexpected internal exception during result message processing. Message: "
			             + message, e);
			if (taskResult != null) {
				queueUtil.rejectAndDiscardJob(channel, deliveryTag, taskResult.getJobId(), taskResult.getFileId());
			} else {
				queueUtil.rejectAndDiscardJob(channel, deliveryTag, null, null);
			}
		}
	}

	@Override
	@Transactional
	public void handleEvent(SendingToQueueErrorData event) {
		String eventMessage = event.getMessage();
		try {
			ExecutableTaskDTO executableTask
					= objectMapper.readValue(eventMessage, ExecutableTaskDTO.class);
			ExecutableTaskResultDTO taskResult
					= createExecutableTaskResultDTO(executableTask, event);

			saveJobTaskResult(taskResult);
		} catch (JsonProcessingException e) {
			LOGGER.error("Cannot parse to ExecutableTaskDTO: " + eventMessage, e);
		} catch (Exception e) {
			LOGGER.error("Unexpected internal exception during processing event. Event message: "
			             + eventMessage, e);
		}
	}

	@Transactional
	public void saveJobTaskResult(ExecutableTaskResultDTO taskResult) {
		UUID jobId = taskResult.getJobId();
		UUID fileId = taskResult.getFileId();
		if (jobId == null || fileId == null) {
			return;
		}

		Job job = findJobById(jobId);
		if (job == null) {
			return;
		}

		updateTaskStatus(taskResult, fileId, job);
		updateJobStatus(job);
	}

	public void updateTaskStatus(ExecutableTaskResultDTO taskResult, UUID fileId, Job job) {
		JobTask task = job.getJobTasks()
		                  .stream()
		                  .filter(jobTask -> jobTask.getFileId().equals(fileId))
		                  .findFirst()
		                  .orElse(null);

		if (task == null) {
			return;
		}

		UUID validationResultId = taskResult.getValidationResultId();
		if (validationResultId != null) {
			task.setSuccessfulResult(validationResultId);
		} else {
			task.setErrorResult(taskResult.getErrorType(), taskResult.getErrorMessage());
		}
		taskRepository.saveAndFlush(task);
	}

	public void updateJobStatus(Job job) {
		for (JobTask task : job.getJobTasks()) {
			if (task.getStatus() == TaskStatus.CREATED || task.getStatus() == TaskStatus.PROCESSING) {
				return;
			}
		}

		job.setStatus(JobStatus.FINISHED);
		jobRepository.saveAndFlush(job);
	}

	private ExecutableTaskResultDTO createExecutableTaskResultDTO(ExecutableTaskDTO executableTask,
	                                                              SendingToQueueErrorData event) {
		ExecutableTaskResultDTO taskResult = new ExecutableTaskResultDTO();
		taskResult.setJobId(executableTask.getJobId());
		taskResult.setFileId(executableTask.getFileId());
		String errorMessage;
		if (QueueErrorEventType.SENDING_ERROR_CALLBACK == event.getQueueErrorEventType()) {
			errorMessage = "Message: " + event.getMessage()
			               + " cannot be send into the queue '" + event.getQueueName()
			               + "', cause: " + event.getCauseMessage();
		} else {
			errorMessage = "Message: " + event.getMessage()
			               + " cannot be send into the queue '" + event.getQueueName()
			               + "', internal error, cause: " + event.getCauseMessage();
		}
		taskResult.setErrorMessage(errorMessage);
		taskResult.setErrorType(TaskError.SENDING_TO_QUEUE_ERROR);
		return taskResult;
	}

	private Job findJobById(UUID jobId) {
		return jobRepository.findById(jobId).orElse(null);
	}
}
