package org.verapdf.webapp.jobservice.model.entity.enums;

public enum TaskError {
	SENDING_TO_QUEUE_ERROR,
	INVALID_TASK_DATA_ERROR,
	INVALID_CONFIGURATION_DATA_ERROR,
	JOB_OBTAINING_TO_PROCESS_ERROR,
	FILE_OBTAINING_TO_PROCESS_ERROR,
	SAVE_RESULT_FILE_ERROR,
	PROCESSING_INTERNAL_ERROR
}
