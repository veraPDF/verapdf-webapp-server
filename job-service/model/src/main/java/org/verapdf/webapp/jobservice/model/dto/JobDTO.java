package org.verapdf.webapp.jobservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class JobDTO {
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private UUID id;
	@NotNull
	private Profile profile;
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private JobStatus status;
	private List<@NotNull @Valid JobTaskDTO> tasks;

	public JobDTO(){

	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public List<JobTaskDTO> getTasks() {
		return tasks;
	}

	public void setTasks(List<JobTaskDTO> tasks) {
		this.tasks = tasks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JobDTO)) {
			return false;
		}
		JobDTO jobDTO = (JobDTO) o;
		return id.equals(jobDTO.id) &&
		       profile == jobDTO.profile &&
		       status == jobDTO.status &&
		       Objects.equals(tasks, jobDTO.tasks);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, profile, status, tasks);
	}

	@Override
	public String toString() {
		return "JobDTO{" +
		       "id=" + id +
		       ", profile=" + profile +
		       ", status=" + status +
		       ", tasks=" + tasks +
		       '}';
	}
}
