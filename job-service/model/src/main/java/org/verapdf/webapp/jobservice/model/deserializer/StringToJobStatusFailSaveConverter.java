package org.verapdf.webapp.jobservice.model.deserializer;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;

public class StringToJobStatusFailSaveConverter extends StdConverter<String, JobStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringToJobStatusFailSaveConverter.class);

    @Override
    public JobStatus convert(String value) {
        try {
            return JobStatus.valueOf(value);
        } catch (Exception e) {
            LOGGER.warn("Invalid job status value: {}", value);
            return null;
        }
    }
}
