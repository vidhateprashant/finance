package com.monstarbill.finance.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandling {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		
		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
		Map<String, String> errors = new HashMap<>();
		
		ex.getConstraintViolations().forEach(cv -> {
			errors.put("message", cv.getMessage());
			errors.put("path", (cv.getPropertyPath()).toString());
		});	

		return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
//	@ExceptionHandler(value= {Exception.class})
//	public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest web) {
//		return new ResponseEntity<>(ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
//	}
	
	@ExceptionHandler(value= {Exception.class})
	public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest web) {
		log.info("Exception :: " + ex.toString());
		
		String errorExceptionMessage = ex.getLocalizedMessage();
		if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
		
		ErrorMessage errorMessage = new ErrorMessage(500, new Date(), errorExceptionMessage);
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(value= {NullPointerException.class})
	public ResponseEntity<Object> handleNullPointerException(NullPointerException ex, WebRequest web) {
		
		log.info("Exception :: " + ex.toString());
		
		String errorExceptionMessage = ex.getLocalizedMessage();
		if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
		
		ErrorMessage errorMessage = new ErrorMessage(500, new Date(), errorExceptionMessage);
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * use this if record is not found or custom message exception with success code
	 * @param ex
	 * @param web
	 * @return
	 */
	@ExceptionHandler(value= {CustomMessageException.class})
	public ResponseEntity<Object> handleCustomMessageException(CustomMessageException ex, WebRequest web) {
		String errorExceptionMessage = ex.getLocalizedMessage();
		if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
		
		ErrorMessage errorMessage = new ErrorMessage(200, new Date(), errorExceptionMessage);
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.OK);
	}
	
	/**
	 * Use this for Internal server error kind of exceptions
	 * @param ex
	 * @param web
	 * @return
	 */
	@ExceptionHandler(value= {CustomException.class})
	public ResponseEntity<Object> handleCustomException(CustomException ex, WebRequest web) {
		String errorExceptionMessage = ex.getLocalizedMessage();
		if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
		
		ErrorMessage errorMessage = new ErrorMessage(500, new Date(), errorExceptionMessage);
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Use this if Vallidation is failed
	 * @param ex
	 * @param web
	 * @return
	 */
	@ExceptionHandler(value= {CustomBadRequestException.class})
	public ResponseEntity<Object> handleCustomException(CustomBadRequestException ex, WebRequest web) {
		
		String errorExceptionMessage = ex.getLocalizedMessage();
		if (errorExceptionMessage == null) errorExceptionMessage = ex.toString();
		
		log.error("Bad Request Exception is :: " + errorExceptionMessage);
		
		ErrorMessage errorMessage = new ErrorMessage(400, new Date(), errorExceptionMessage);
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.BAD_REQUEST);
		
	}
}
