package org.verapdf.webapp.jobservice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;

import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutableTaskResultDTO {
	private UUID jobId;
	private UUID fileId;
	private UUID validationResultId;
	private TaskError errorType;
	private String errorMessage;

	public ExecutableTaskResultDTO() {

	}

	public ExecutableTaskResultDTO(ExecutableTaskDTO taskDTO, UUID validationResultId) {
		this.jobId = taskDTO.getJobId();
		this.fileId = taskDTO.getFileId();
		this.validationResultId = validationResultId;
	}

	public ExecutableTaskResultDTO(ExecutableTaskDTO taskDTO,
	                               TaskError errorType, String errorMessage) {
		this.jobId = taskDTO.getJobId();
		this.fileId = taskDTO.getFileId();
		this.errorType = errorType;
		this.errorMessage = errorMessage;
	}

	public UUID getJobId() {
		return jobId;
	}

	public void setJobId(UUID jobId) {
		this.jobId = jobId;
	}

	public UUID getFileId() {
		return fileId;
	}

	public void setFileId(UUID fileId) {
		this.fileId = fileId;
	}

	public UUID getValidationResultId() {
		return validationResultId;
	}

	public void setValidationResultId(UUID validationResultId) {
		this.validationResultId = validationResultId;
	}

	public TaskError getErrorType() {
		return errorType;
	}

	public void setErrorType(TaskError errorType) {
		this.errorType = errorType;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ExecutableTaskResultDTO)) {
			return false;
		}
		ExecutableTaskResultDTO that = (ExecutableTaskResultDTO) o;
		return Objects.equals(jobId, that.jobId) &&
				Objects.equals(fileId, that.fileId) &&
				Objects.equals(validationResultId, that.validationResultId) &&
				errorType == that.errorType &&
				Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jobId, fileId, validationResultId, errorType, errorMessage);
	}

	@Override
	public String toString() {
		return "ExecutableTaskResultDTO{" +
				"jobId=" + jobId +
				", fileId=" + fileId +
				", validationResultId=" + validationResultId +
				", errorType=" + errorType +
				", errorMessage='" + errorMessage + '\'' +
				'}';
	}
}
