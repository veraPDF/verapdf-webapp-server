package org.verapdf.webapp.jobservice.server.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.verapdf.webapp.error.AbstractGlobalExceptionHandler;

@ControllerAdvice
public class JobServiceExceptionHandler extends AbstractGlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobServiceExceptionHandler.class);

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
}
