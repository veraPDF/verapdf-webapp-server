package org.verapdf.webapp.jobservice.model.deserializer;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.UUID;

public class StringToUUIDConverter extends StdConverter<String, UUID>{
	@Override
	public UUID convert(String value) {
		try {
			return UUID.fromString(value);
		} catch(Exception e) {
			return null;
		}
	}
}
