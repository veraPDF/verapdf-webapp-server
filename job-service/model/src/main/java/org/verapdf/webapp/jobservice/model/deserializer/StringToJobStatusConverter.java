package org.verapdf.webapp.jobservice.model.deserializer;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;

public class StringToJobStatusConverter extends StdConverter<String, JobStatus> {
	@Override
	public JobStatus convert(String value) {
		try {
			return JobStatus.valueOf(value);
		} catch(Exception e) {
			return null;
		}
	}
}
