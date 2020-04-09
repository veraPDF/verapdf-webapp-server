package org.verapdf.webapp.localstorageservice.server.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.verapdf.webapp.error.AbstractGlobalExceptionHandler;

@ControllerAdvice
public class LocalStorageServiceExceptionHandler extends AbstractGlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageServiceExceptionHandler.class);

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
