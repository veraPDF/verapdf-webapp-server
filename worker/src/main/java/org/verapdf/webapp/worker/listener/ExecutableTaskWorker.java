package org.verapdf.webapp.worker.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.verapdf.processor.reports.ValidationReport;
import org.verapdf.webapp.error.exception.BadRequestException;
import org.verapdf.webapp.jobservice.client.service.JobServiceClient;
import org.verapdf.webapp.jobservice.model.dto.ExecutableTaskDTO;
import org.verapdf.webapp.jobservice.model.dto.ExecutableTaskResultDTO;
import org.verapdf.webapp.jobservice.model.dto.JobDTO;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.localstorageservice.client.service.LocalStorageServiceClient;
import org.verapdf.webapp.localstorageservice.model.dto.StoredFileDTO;
import org.verapdf.webapp.queueclient.handler.QueueListenerHandler;
import org.verapdf.webapp.queueclient.sender.QueueSender;
import org.verapdf.webapp.queueclient.util.QueueUtil;
import org.verapdf.webapp.tool.FilesTool;
import org.verapdf.webapp.worker.error.exception.VeraPDFWorkerException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class ExecutableTaskWorker implements QueueListenerHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableTaskWorker.class);

	private final int processingLimit;
	private final QueueSender queueSender;
	private final QueueUtil queueUtil;

	private final ObjectMapper objectMapper;
	private final JobServiceClient jobServiceClient;
	private final LocalStorageServiceClient localStorageServiceClient;
	private final VeraPdfProcessor veraPdfProcessor;

	private final File workDir;

	@Autowired
	public ExecutableTaskWorker(@Value("${verapdf.task.processing-limit}") int processingLimit,
	                            QueueSender queueSender, QueueUtil queueUtil, ObjectMapper objectMapper,
	                            JobServiceClient jobServiceClient, LocalStorageServiceClient localStorageServiceClient,
	                            VeraPdfProcessor veraPdfProcessor, @Qualifier("workingDir") File workDir) {
		this.processingLimit = processingLimit;
		this.queueSender = queueSender;
		this.queueUtil = queueUtil;

		this.objectMapper = objectMapper;
		this.jobServiceClient = jobServiceClient;
		this.localStorageServiceClient = localStorageServiceClient;
		this.veraPdfProcessor = veraPdfProcessor;

		this.workDir = workDir;
	}

	@Override
	public void handleMessage(String message, Channel channel, long deliveryTag) {
		ExecutableTaskDTO taskDTO;
		LOGGER.info("Message {} left the queue", message);

		try {
			taskDTO = objectMapper.readValue(message, ExecutableTaskDTO.class);
		} catch (Exception e) {
			LOGGER.error("Could not parse to ExecutableTaskDTO data: " + message);
			queueUtil.rejectAndDiscardJob(channel, deliveryTag, null, null);
			return;
		}

		boolean recoverable = false;
		ExecutableTaskResultDTO taskResult;
		File fileToProcess = null;
		Integer jobProcessingCount = null;
		UUID jobId = null;
		UUID fileId = null;
		try {
			jobId = taskDTO.getJobId();
			fileId = taskDTO.getFileId();
			checkParsedExecutableTaskDTO(jobId, fileId, message);

			jobProcessingCount = increaseTaskProcessingCount(jobId, fileId);
			if (jobProcessingCount == null) {
				queueUtil.rejectAndDiscardJob(channel, deliveryTag, jobId, fileId);
				return;
			}

			JobDTO jobDTO = getJob(jobId);
			if (jobDTO == null || JobStatus.PROCESSING != jobDTO.getStatus()) {
				// no job to be process or job is already finished
				queueUtil.rejectAndDiscardJob(channel, deliveryTag, jobId, fileId);
				return;
			}

			LOGGER.info("Validation start: {}", jobId);

			checkProcessingConfiguration(jobDTO);

			fileToProcess = getFileToProcess(fileId);

			ValidationReport validationReport
					= veraPdfProcessor.validate(fileToProcess, jobDTO.getProfile());

			LOGGER.info("Validation end: {}", jobId);

			StoredFileDTO reportFile = saveReportFile(validationReport, fileId);

			taskResult = new ExecutableTaskResultDTO(taskDTO, reportFile.getId());
		} catch (VeraPDFWorkerException e) {
			LOGGER.error(e.getMessage());
			taskResult = new ExecutableTaskResultDTO(taskDTO, e.getTaskError(), e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			recoverable = true;
			taskResult = new ExecutableTaskResultDTO(taskDTO, TaskError.PROCESSING_INTERNAL_ERROR,
			                                         e.getMessage());
		} finally {
			FilesTool.deleteFile(fileToProcess);
		}

		if (recoverable) {
			if (jobProcessingCount != null && processingLimit <= jobProcessingCount) {
				LOGGER.warn("The number of attempts to process the task exceeded the limit,"
				            + " jobId: {}, fileId: {}", jobId, fileId);
			} else {
				queueUtil.rejectAndKeepJob(channel, deliveryTag, jobId, fileId);
				return;
			}
		}

		try {
			String resultMessage = objectMapper.writeValueAsString(taskResult);
			LOGGER.info("Message {} entered the queue", resultMessage);
			queueSender.sendMessage(resultMessage);
			queueUtil.applyAndDiscardJob(channel, deliveryTag, jobId, fileId);
			return;
		} catch (JsonProcessingException e) {
			LOGGER.error("Could not serialize task result to string");
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}

		queueUtil.rejectAndDiscardJob(channel, deliveryTag, jobId, fileId);
	}

	private void checkProcessingConfiguration(JobDTO jobDTO) throws VeraPDFWorkerException {
		if (jobDTO.getProfile() == null) {
			throw new VeraPDFWorkerException(TaskError.INVALID_CONFIGURATION_DATA_ERROR,
			                                 "Missing profile for validation");
		}
	}

	private void checkParsedExecutableTaskDTO(UUID jobId, UUID fileId, String originalDTOData) throws VeraPDFWorkerException {
		if (jobId == null || fileId == null) {
			throw new VeraPDFWorkerException(TaskError.INVALID_TASK_DATA_ERROR,
			                                 "Invalid task request: " + originalDTOData);
		}
	}


	private JobDTO getJob(UUID jobId) throws VeraPDFWorkerException {
		try {
			return jobServiceClient.getJobById(jobId);
		} catch (RestClientResponseException e) {
			if (HttpStatus.NOT_FOUND.value() == e.getRawStatusCode()) {
				return null;
			} else {
				throw new VeraPDFWorkerException(TaskError.JOB_OBTAINING_TO_PROCESS_ERROR,
				                                 e.getResponseBodyAsString());
			}
		}
	}

	private Integer increaseTaskProcessingCount(UUID jobId, UUID fileId) throws VeraPDFWorkerException {
		try {
			return jobServiceClient.increaseTaskProcessingCount(jobId, fileId);
		} catch (RestClientResponseException e) {
			int statusCode = e.getRawStatusCode();
			String responseBody = e.getResponseBodyAsString();
			if (HttpStatus.NOT_FOUND.value() == statusCode || HttpStatus.BAD_REQUEST.value() == statusCode
			    || HttpStatus.CONFLICT.value() == statusCode) {
				LOGGER.error(responseBody);
				return null;
			} else {
				throw new VeraPDFWorkerException(TaskError.INCREASE_TASK_PROCESSING_COUNT_ERROR, responseBody);
			}
		}
	}

	private File getFileToProcess(UUID fileId) throws VeraPDFWorkerException {
		StoredFileDTO fileDTO;
		Resource resource;
		try {
			fileDTO = localStorageServiceClient.getFileDescriptorById(fileId);
			resource = localStorageServiceClient.getFileResourceById(fileDTO.getId());
		} catch (RestClientResponseException e) {
			throw new VeraPDFWorkerException(TaskError.FILE_OBTAINING_TO_PROCESS_ERROR,
			                                 e.getResponseBodyAsString());
		}

		try (InputStream inputStream = resource.getInputStream()) {
			File fileToSave = new File(workDir, fileDTO.getId().toString());
			FilesTool.saveFileOnDiskAndCheck(inputStream, fileToSave, fileDTO.getContentMD5());
			return fileToSave;
		} catch (BadRequestException e) {
			throw new VeraPDFWorkerException(TaskError.PROCESSING_INTERNAL_ERROR, e.getMessage());
		} catch (IOException e) {
			throw new VeraPDFWorkerException(TaskError.PROCESSING_INTERNAL_ERROR,
			                                 "Could not obtain inputStream of file with id " + fileDTO.getId());
		}
	}

	private StoredFileDTO saveReportFile(ValidationReport validationReport, UUID fileId)
			throws VeraPDFWorkerException {
		File reportFile = new File(workDir, "report-" + fileId);
		try {
			objectMapper.writeValue(reportFile, validationReport);

			return localStorageServiceClient
					.saveFile(reportFile, FilesTool.evaluateMD5(reportFile), MediaType.APPLICATION_JSON);
		} catch (RestClientResponseException e) {
			throw new VeraPDFWorkerException(TaskError.SAVE_RESULT_FILE_ERROR, e.getResponseBodyAsString());
		} catch (IOException e) {
			throw new VeraPDFWorkerException(TaskError.SAVE_RESULT_FILE_ERROR,
			                                 "Could not write validation report into report file: "
			                                 + reportFile.getName());
		} finally {
			FilesTool.deleteFile(reportFile);
		}
	}
}
