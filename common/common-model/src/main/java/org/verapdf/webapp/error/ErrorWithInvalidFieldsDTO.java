package org.verapdf.webapp.error;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ErrorWithInvalidFieldsDTO extends ErrorDTO {
	private Map<String, Object> invalidFields;

	public ErrorWithInvalidFieldsDTO(HttpStatus status, String errorMessage, Map<String, Object> invalidFields) {
		super(status, errorMessage);
		this.invalidFields = invalidFields;
	}

	public Map<String, Object> getInvalidFields() {
		return invalidFields;
	}
}
