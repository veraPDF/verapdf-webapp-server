package org.verapdf.webapp.worker.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.verapdf.processor.reports.ValidationReport;
import org.verapdf.webapp.error.exception.BadRequestException;
import org.verapdf.webapp.jobservice.model.dto.ExecutableTaskResultDTO;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.queueclient.handler.QueueListenerHandler;
import org.verapdf.webapp.jobservice.model.dto.ExecutableTaskDTO;
import org.verapdf.webapp.localstorageservice.client.service.LocalStorageServiceClient;
import org.verapdf.webapp.localstorageservice.model.dto.StoredFileDTO;
import org.verapdf.webapp.queueclient.sender.QueueSender;
import org.verapdf.webapp.tool.FilesTool;
import org.verapdf.webapp.worker.error.exception.VeraPDFWorkerException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class ExecutableTaskWorker implements QueueListenerHandler {

	private static final Logger LOGGER
			= LoggerFactory.getLogger(ExecutableTaskWorker.class);

	private final QueueSender queueSender;

	private final ObjectMapper objectMapper;
	private final LocalStorageServiceClient localStorageServiceClient;
	private final VeraPdfProcessor veraPdfProcessor;

	private final File workDir;

	@Autowired
	public ExecutableTaskWorker(QueueSender queueSender,
	                            LocalStorageServiceClient localStorageServiceClient,
	                            VeraPdfProcessor veraPdfProcessor,
	                            ObjectMapper objectMapper,
	                            @Qualifier("workingDir") File workDir) {
		this.queueSender = queueSender;
		this.localStorageServiceClient = localStorageServiceClient;
		this.veraPdfProcessor = veraPdfProcessor;
		this.objectMapper = objectMapper;
		this.workDir = workDir;
	}

	@Override
	public void handleMessage(String message) {
		ExecutableTaskDTO taskDTO;

		try {
			taskDTO = objectMapper.readValue(message, ExecutableTaskDTO.class);
		} catch (Exception e) {
			LOGGER.error("Could not parse to ExecutableTaskDTO data: " + message);
			return;
		}

		ExecutableTaskResultDTO taskResult;
		File fileToProcess = null;
		try {
			checkParsedExecutableTaskDTO(taskDTO, message);

			fileToProcess = getFileToProcess(taskDTO.getFileId());

			ValidationReport validationReport
					= veraPdfProcessor.validate(fileToProcess, taskDTO.getProfile());

			StoredFileDTO reportFile = saveReportFile(validationReport, taskDTO.getFileId());

			taskResult = new ExecutableTaskResultDTO(taskDTO, reportFile.getId());
		} catch (VeraPDFWorkerException e) {
			LOGGER.error(e.getMessage());
			taskResult = new ExecutableTaskResultDTO(taskDTO, e.getTaskError(), e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			taskResult = new ExecutableTaskResultDTO(taskDTO,
					TaskError.PROCESSING_INTERNAL_ERROR, e.getMessage());
		} finally {
			FilesTool.deleteFile(fileToProcess);
		}

		try {
			queueSender.sendMessage(objectMapper.writeValueAsString(taskResult));
		} catch (JsonProcessingException e) {
			LOGGER.error("Could not serialise task result to string");
		}
	}

	private void checkParsedExecutableTaskDTO(ExecutableTaskDTO taskDTO, String originalDTOData) throws VeraPDFWorkerException {
		if (taskDTO.getFileId() == null || taskDTO.getProfile() == null) {
			throw new VeraPDFWorkerException(TaskError.INVALID_TASK_DATA_ERROR,
					"Invalid task request: " + originalDTOData);
		}
	}

	private File getFileToProcess(UUID fileId) throws VeraPDFWorkerException {
		StoredFileDTO fileDTO;
		Resource resource;
		try {
			fileDTO = localStorageServiceClient.getFileDescriptorById(fileId);
			resource = localStorageServiceClient.getFileResourceById(fileDTO.getId());
		} catch (RestClientResponseException e) {
			throw new VeraPDFWorkerException(
					TaskError.FILE_OBTAINING_TO_PROCESS_ERROR,
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
			throw new VeraPDFWorkerException(
					TaskError.SAVE_RESULT_FILE_ERROR,
					e.getResponseBodyAsString());
		} catch (IOException e) {
			throw new VeraPDFWorkerException( TaskError.SAVE_RESULT_FILE_ERROR,
					"Could not write validation report into report file: "
							+ reportFile.getName());
		} finally {
			FilesTool.deleteFile(reportFile);
		}
	}
}
