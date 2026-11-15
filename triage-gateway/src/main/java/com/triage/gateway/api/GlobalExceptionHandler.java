package com.triage.gateway.api;

import com.triage.gateway.error.GatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(GatewayException.class)
	public ResponseEntity<ErrorResponse> gateway(GatewayException e) {
		return ResponseEntity.status(e.status()).body(new ErrorResponse(e.code(), e.getMessage()));
	}

	@ExceptionHandler({MissingRequestHeaderException.class, HttpMessageNotReadableException.class,
			IllegalArgumentException.class})
	public ResponseEntity<ErrorResponse> badRequest(Exception e) {
		return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> notFound(NoResourceFoundException e) {
		return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "no such endpoint");
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ErrorResponse> store(DataAccessException e) {
		log.error("idempotency store unavailable", e);
		return error(HttpStatus.SERVICE_UNAVAILABLE, "STORE_UNAVAILABLE", "idempotency store unavailable");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> unexpected(Exception e) {
		log.error("unexpected error", e);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "unexpected error");
	}

	private static ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status).body(new ErrorResponse(code, message));
	}

}
