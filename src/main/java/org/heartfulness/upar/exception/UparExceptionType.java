package org.heartfulness.upar.exception;

public enum UparExceptionType {
	Already_Requested_Sitting(new UparException(100, "A sitting request has already been raised. Ignoring current request.")),
	Not_Registered_With_GCM(new UparException(100, "Device is not registered with GCM yet!")),
	Invalid_Abhyasi_ID(new UparException(100, "Abhyasi details provided are invalid!")),
	Invalid_Sitting_Session(new UparException(100, "This session is already closed.")), 
	AIMS_Exception(new UparException(100, "Exception occured during registration. Please try again.")), 
	Abhyasi_Does_Not_Exist(new UparException(100, "Abhyasi does not exist in system!"));
	private UparException exception;

	private UparExceptionType(UparException exception){
		this.exception = exception;
	}
	public UparException getException() {
		return exception;
	}

	public void setException(UparException exception) {
		this.exception = exception;
	}
	
}
