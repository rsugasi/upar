package org.heartfulness.upar.input;

public class UparInput {
	private String submit;
	
	private String message;

	private Integer count;
	
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
	

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}


	public enum SubmitType { start, end, sharePair, chat, none, error, close, success };
	
	public enum GenericMessageType {
		noAbhyasiAvailable("No abhyasi waiting at this time!"),
		alreadyInASitting("Please complete or cancel the current sitting to proceed"),
		sessionClose("Session Closed!"),
		abhyasiJoined("Abhyasi joined for sitting!");
		private String value;
		private GenericMessageType(String value) {
			this.value = value;
		}
		public String value(){
			return value;
		}
	}
}
