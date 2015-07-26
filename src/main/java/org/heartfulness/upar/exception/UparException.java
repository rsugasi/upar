package org.heartfulness.upar.exception;

public class UparException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer errorCode;
	private String message;
	
	
	public UparException(Integer errorCode, String message) {
		super();
		this.errorCode = errorCode;
		this.message = message;
	}
	
	public Integer getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
