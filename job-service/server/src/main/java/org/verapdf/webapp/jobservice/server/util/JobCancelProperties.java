package org.verapdf.webapp.jobservice.server.util;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobCancelProperties {

	private final Set<String> jobIdsToCancel = ConcurrentHashMap.newKeySet();

	public void cancelJob(String jobId) {
		jobIdsToCancel.add(jobId);
	}

	public boolean isPresent(String jobId) {
		return jobIdsToCancel.contains(jobId);
	}

	public void removeJobFromCancelled(String jobId) {
		jobIdsToCancel.remove(jobId);
	}
}
