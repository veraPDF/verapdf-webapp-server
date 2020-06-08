package org.verapdf.webapp.jobservice.model.deserializer;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class StringToUUIDFailSaveConverter extends StdConverter<String, UUID> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringToUUIDFailSaveConverter.class);

    @Override
    public UUID convert(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            LOGGER.warn("Invalid UUID value: {}", value);
            return null;
        }
    }
}
