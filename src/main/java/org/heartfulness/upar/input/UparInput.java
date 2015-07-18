package org.heartfulness.upar.input;

public class UparInput {
	private String submit;
	
	private String message;

	public UparInput() {
	    setSubmit(SubmitType.none);
	}
	
	public String getSubmit() {
		return submit;
	}

	public void setSubmit(SubmitType type) {
		this.submit = type.name();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setMessage(GenericMessageType type) {
		this.message = type.value();
	}
	
	public enum SubmitType { start, end, sharePair, chat, none, error };
	
	public enum GenericMessageType {
		noAbhyasiAvailable("No abhyasi waiting at this time!"),
		alreadyInASitting("Please complete or cancel the current sitting to proceed"),
		sessionClose("Session Closed!");
		private String value;
		private GenericMessageType(String value) {
			this.value = value;
		}
		public String value(){
			return value;
		}
	}
}
