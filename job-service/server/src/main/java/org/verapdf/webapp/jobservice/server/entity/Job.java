package org.verapdf.webapp.jobservice.server.entity;

import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class Job {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private UUID id;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private JobStatus status;
	@Enumerated(EnumType.STRING)
	@Column(name = "profile", nullable = false)
	private Profile profile;
	@OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<JobTask> jobTasks = new ArrayList<>();

	public Job() {
		this(null);
	}

	public Job(Profile profile) {
		this.profile = profile;
		this.status = JobStatus.CREATED;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public List<JobTask> getJobTasks() {
		return jobTasks;
	}

	public void clearJobTasks() {
		this.jobTasks.clear();
	}

	public void addTask(JobTask jobTask) {
		assert jobTask.getSourceFor() == null;

		jobTask.setSourceFor(this);
		this.jobTasks.add(jobTask);
	}
}
