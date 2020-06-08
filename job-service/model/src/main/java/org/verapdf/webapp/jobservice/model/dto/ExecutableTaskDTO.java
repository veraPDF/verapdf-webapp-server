package org.verapdf.webapp.jobservice.model.dto;

import java.util.Objects;
import java.util.UUID;

public class ExecutableTaskDTO {
	private UUID jobId;
	private UUID fileId;

	public ExecutableTaskDTO() {
	}

	public ExecutableTaskDTO(UUID jobId, UUID fileId) {
		this.jobId = jobId;
		this.fileId = fileId;
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
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ExecutableTaskDTO)) {
			return false;
		}
		ExecutableTaskDTO that = (ExecutableTaskDTO) o;
		return Objects.equals(jobId, that.jobId) &&
				Objects.equals(fileId, that.fileId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jobId, fileId);
	}

	@Override
	public String toString() {
		return "ExecutableTaskDTO{" +
				"jobId=" + jobId +
				", fileId=" + fileId +
				'}';
	}
}
