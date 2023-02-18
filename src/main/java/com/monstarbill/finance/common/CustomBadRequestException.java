package com.monstarbill.finance.common;

/**
 * Useful to throw exception if validation fails
 * @author prashant
 * 17-07-2022
 */
public class CustomBadRequestException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1081401164838163662L;

	public CustomBadRequestException(String message) {
		super(message);
	}
}
