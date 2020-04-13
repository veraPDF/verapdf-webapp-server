package org.verapdf.webapp.model.dto;

import org.verapdf.webapp.model.entity.enums.Profile;

import java.util.Objects;
import java.util.UUID;

public class ExecutableTaskDTO {
	UUID jobId;
	UUID fileId;
	Profile profile;

	public ExecutableTaskDTO() {
	}

	public ExecutableTaskDTO(UUID jobId, UUID fileId, Profile profile) {
		this.jobId = jobId;
		this.fileId = fileId;
		this.profile = profile;
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

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
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
		return Objects.equals(getJobId(), that.getJobId())
				&& Objects.equals(getFileId(), that.getFileId())
				&& getProfile() == that.getProfile();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getJobId(), getFileId(), getProfile());
	}
}
