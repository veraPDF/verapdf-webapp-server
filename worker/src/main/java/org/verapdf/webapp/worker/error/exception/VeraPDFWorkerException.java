package org.verapdf.webapp.worker.error.exception;

import org.verapdf.webapp.error.exception.VeraPDFBackendException;
import org.verapdf.webapp.jobservice.model.entity.enums.TaskError;

public class VeraPDFWorkerException extends VeraPDFBackendException {
	private TaskError taskError;

	public VeraPDFWorkerException() {
		super();
	}

	public VeraPDFWorkerException(TaskError taskError, String errorMessage) {
		super(errorMessage);
		this.taskError = taskError;
	}

	public VeraPDFWorkerException(TaskError taskError, String errorMessage, Throwable cause) {
		super(errorMessage, cause);
		this.taskError = taskError;
	}

	public VeraPDFWorkerException(TaskError taskError, String errorMessage,
	                              Throwable cause, boolean enableSuppression,
	                              boolean writableStackTrace) {
		super(errorMessage, cause, enableSuppression, writableStackTrace);
		this.taskError = taskError;
	}

	public TaskError getTaskError() {
		return taskError;
	}
}
