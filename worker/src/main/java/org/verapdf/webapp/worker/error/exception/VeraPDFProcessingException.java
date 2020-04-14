package org.verapdf.webapp.worker.error.exception;

import org.verapdf.webapp.error.exception.VeraPDFBackendException;

public class VeraPDFProcessingException extends VeraPDFBackendException {
	public VeraPDFProcessingException() {
		super();
	}

	public VeraPDFProcessingException(String message) {
		super(message);
	}

	public VeraPDFProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public VeraPDFProcessingException(Throwable cause) {
		super(cause);
	}

	protected VeraPDFProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
