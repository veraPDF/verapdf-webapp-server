package org.verapdf.webapp.jobservice.server.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobProgressProperties {

	private final ConcurrentHashMap<String, String> jobIdProgressMap = new ConcurrentHashMap<>();

	public void updateProgressForJob(String jobId, String progress) {
		jobIdProgressMap.put(jobId, progress);
	}

	public boolean isPresent(String jobId) {
		return jobIdProgressMap.containsKey(jobId);
	}

	public String getProgressForJob(String jobId) {
		return jobIdProgressMap.get(jobId);
	}

	public void clearJobProgressByJobId(String jobId) {
		jobIdProgressMap.remove(jobId);
	}
}
