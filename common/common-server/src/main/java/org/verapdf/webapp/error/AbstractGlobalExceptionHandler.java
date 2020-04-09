package org.verapdf.webapp.error;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.verapdf.webapp.error.exception.BadRequestException;
import org.verapdf.webapp.error.exception.NotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

public abstract class AbstractGlobalExceptionHandler {

	@ExceptionHandler
	public ResponseEntity<ErrorDTO> handleException(Throwable e) {
		getLogger().error("Caught by " + this.getClass().getName(), e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		                     .body(new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@ExceptionHandler({ConstraintViolationException.class})
	public ResponseEntity<ErrorDTO> handleException(ConstraintViolationException e) {
		getLogger().info("Request parameter value is invalid: {}", e.getConstraintViolations()
		                                                            .stream()
		                                                            .map(c -> c.getPropertyPath().toString())
		                                                            .collect(Collectors.joining(", ")));
		return ResponseEntity.badRequest()
		                     .body(new ErrorDTO(HttpStatus.BAD_REQUEST));
	}

	@ExceptionHandler({MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ErrorDTO> handleException(MethodArgumentTypeMismatchException e) {
		getLogger().info("Request parameter value is invalid: {}", e.getName());
		return ResponseEntity.badRequest()
		                     .body(new ErrorDTO(HttpStatus.BAD_REQUEST));
	}

	@ExceptionHandler({MethodArgumentNotValidException.class})
	public ResponseEntity<ErrorDTO> handleException(MethodArgumentNotValidException e) {
		getLogger().info("Request parameter value is invalid: {}", e.getBindingResult());
		return ResponseEntity.badRequest()
		                     .body(new ErrorDTO(HttpStatus.BAD_REQUEST));
	}

	@ExceptionHandler({MissingServletRequestParameterException.class})
	public ResponseEntity<ErrorDTO> handleException(MissingServletRequestParameterException e) {
		getLogger().info("Required request parameter missing: {}", e.getParameterName());
		return ResponseEntity.badRequest()
		                     .body(new ErrorDTO(HttpStatus.BAD_REQUEST));
	}

	@ExceptionHandler({MissingServletRequestPartException.class})
	public ResponseEntity<ErrorDTO> handleException(MissingServletRequestPartException e) {
		getLogger().info("Required request part missing: {}", e.getRequestPartName());
		return ResponseEntity.badRequest()
		                     .body(new ErrorDTO(HttpStatus.BAD_REQUEST));
	}

	@ExceptionHandler({BadRequestException.class})
	public ResponseEntity<ErrorDTO> handleException(BadRequestException e) {
		getLogger().info("Bad request: {}", e.getMessage());
		return ResponseEntity.badRequest()
		                     .body(new ErrorDTO(HttpStatus.BAD_REQUEST, e.getMessage()));
	}

	@ExceptionHandler({NotFoundException.class})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleException(NotFoundException e) {
		getLogger().info("Not found: {}", e.getMessage());
	}

	protected abstract Logger getLogger();
}
