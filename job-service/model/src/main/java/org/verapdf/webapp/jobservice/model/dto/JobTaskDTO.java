package org.verapdf.webapp.jobservice.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskStatus;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobTaskDTO {
	@NotNull
	private UUID fileId;
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private TaskStatus status;
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private TaskError errorType;
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private String errorMessage;
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private UUID validationResultId;

	public JobTaskDTO() {
	}

	public JobTaskDTO(@NotNull UUID fileId, TaskStatus status, UUID validationResultId) {
		this.fileId = fileId;
		this.status = status;
		this.validationResultId = validationResultId;
	}

	public JobTaskDTO(@NotNull UUID fileId, TaskStatus status, TaskError errorType, String errorMessage) {
		this.fileId = fileId;
		this.status = status;
		this.errorType = errorType;
		this.errorMessage = errorMessage;
	}

	public UUID getFileId() {
		return fileId;
	}

	public void setFileId(UUID fileId) {
		this.fileId = fileId;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public TaskError getErrorType() {
		return errorType;
	}

	public void setErrorType(TaskError errorType) {
		this.errorType = errorType;
	}

	public UUID getValidationResultId() {
		return validationResultId;
	}

	public void setValidationResultId(UUID validationResultId) {
		this.validationResultId = validationResultId;
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
		if (!(o instanceof JobTaskDTO)) {
			return false;
		}
		JobTaskDTO jobTaskDTO = (JobTaskDTO) o;
		return fileId.equals(jobTaskDTO.fileId) &&
		       status == jobTaskDTO.status &&
		       errorType == jobTaskDTO.errorType &&
		       Objects.equals(errorMessage, jobTaskDTO.errorMessage) &&
		       Objects.equals(validationResultId, jobTaskDTO.validationResultId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileId, status, errorType, errorMessage, validationResultId);
	}

	@Override
	public String toString() {
		return "TaskDTO{" +
		       "fileId=" + fileId +
		       ", status=" + status +
		       ", errorType=" + errorType +
		       ", errorMessage='" + errorMessage + '\'' +
		       ", validationResultId=" + validationResultId +
		       '}';
	}
}
