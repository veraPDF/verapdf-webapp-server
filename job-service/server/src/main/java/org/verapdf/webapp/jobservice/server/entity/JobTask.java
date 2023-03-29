package org.verapdf.webapp.jobservice.server.entity;

import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "job_tasks")
@IdClass(JobTask.JobEntryId.class)
public class JobTask {
	@Id
	@Column(name = "file_id")
	private UUID fileId;
	@Column(name = "processing_count")
	private int processingCount;
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TaskStatus status;
	@Enumerated(EnumType.STRING)
	@Column(name = "error_type")
	private TaskError errorType;
	@Column(name = "error_message")
	private String errorMessage;
	@Column(name = "result_file")
	private UUID resultFileId;
	@Id
	@ManyToOne
	@JoinColumn(name = "job_id", referencedColumnName = "id")
	private Job job;

	public JobTask(){
		this.status = TaskStatus.CREATED;
	}

	public UUID getFileId() {
		return fileId;
	}

	public void setFileId(UUID fileId) {
		this.fileId = fileId;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public int getProcessingCount() {
		return processingCount;
	}

	public void setProcessingCount(int processingCount) {
		this.processingCount = processingCount;
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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public UUID getResultFileId() {
		return resultFileId;
	}

	public void setResultFileId(UUID resultFileId) {
		this.resultFileId = resultFileId;
	}

	public Job getSourceFor() {
		return job;
	}

	public void setSourceFor(Job job) {
		this.job = job;
	}

	public void setSuccessfulResult(UUID resultFileId) {
		if (TaskStatus.CANCELLED != this.status) {
			this.status = TaskStatus.FINISHED;
		}
		this.errorType = null;
		this.errorMessage = null;
		this.resultFileId = resultFileId;
	}

	public void setErrorResult(TaskError taskError, String errorMessage) {
		this.status = TaskStatus.ERROR;
		this.errorType = taskError;
		this.errorMessage = errorMessage;
		this.resultFileId = null;
	}

	@Embeddable
	public static class JobEntryId implements Serializable {
		private UUID fileId;
		private UUID job;

		public JobEntryId() {
		}

		public JobEntryId(UUID fileId, Job job) {
			this.fileId = fileId;
			this.job = job.getId();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof JobEntryId)) {
				return false;
			}
			JobEntryId that = (JobEntryId) o;
			return fileId.equals(that.fileId) &&
			       job.equals(that.job);
		}

		@Override
		public int hashCode() {
			return Objects.hash(fileId, job);
		}
	}
}
