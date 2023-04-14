package org.verapdf.webapp.jobservice.server.util;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class JobQueueProperties {

	private final CopyOnWriteArrayList<UUID> jobIdsWaitingInQueue = new CopyOnWriteArrayList<>();

	public void registerNewWaitingJob(UUID jobId) {
		jobIdsWaitingInQueue.add(jobId);
	}

	public boolean isJobWaiting(UUID jobId) {
		return jobIdsWaitingInQueue.contains(jobId);
	}

	public Integer getJobPositionInQueue(UUID jobId) {
		return jobIdsWaitingInQueue.indexOf(jobId);
	}

	public void removeJobFromWaitingJobs(UUID jobId) {
		jobIdsWaitingInQueue.remove(jobId);
	}

	public void clear() {
		jobIdsWaitingInQueue.clear();
	}
}
