package org.verapdf.error.exception;

public class VeraPDFBackendException extends Exception {

	public VeraPDFBackendException() {
	}

	public VeraPDFBackendException(String message) {
		super(message);
	}

	public VeraPDFBackendException(String message, Throwable cause) {
		super(message, cause);
	}

	public VeraPDFBackendException(Throwable cause) {
		super(cause);
	}

	public VeraPDFBackendException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
