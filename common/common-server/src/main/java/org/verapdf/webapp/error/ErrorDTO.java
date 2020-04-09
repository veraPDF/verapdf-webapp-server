package org.verapdf.webapp.error;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public class ErrorDTO {
	private String error;
	private String message;
	private Instant timestamp;
	private int status;

	public ErrorDTO(HttpStatus status) {
		this(status, null);
	}

	public ErrorDTO(HttpStatus status, String message) {
		this.timestamp = Instant.now();
		this.error = status.getReasonPhrase();
		this.message = message;
		this.status = status.value();
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public int getStatus() {
		return status;
	}
}
