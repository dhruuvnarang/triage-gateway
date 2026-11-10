package com.triage.gateway.api;

import com.triage.gateway.vendor.VendorCallException;
import com.triage.gateway.verification.UnsupportedVerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + " " + error.getDefaultMessage())
				.findFirst()
				.orElse("request is invalid");
		return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	ResponseEntity<ApiError> handleMissingHeader(MissingRequestHeaderException e) {
		return error(HttpStatus.BAD_REQUEST, "MISSING_HEADER", e.getHeaderName() + " header is required");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException e) {
		return error(HttpStatus.BAD_REQUEST, "MALFORMED_BODY", "request body is not valid JSON");
	}

	@ExceptionHandler(InvalidRequestException.class)
	ResponseEntity<ApiError> handleInvalid(InvalidRequestException e) {
		return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage());
	}

	@ExceptionHandler(UnsupportedVerificationException.class)
	ResponseEntity<ApiError> handleUnsupported(UnsupportedVerificationException e) {
		return error(HttpStatus.NOT_IMPLEMENTED, "NOT_IMPLEMENTED", e.getMessage());
	}

	@ExceptionHandler(VendorCallException.class)
	ResponseEntity<ApiError> handleVendorFailure(VendorCallException e) {
		return error(HttpStatus.BAD_GATEWAY, "VENDOR_FAILURE", e.getMessage());
	}

	private static ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status).body(new ApiError(code, message));
	}
}
