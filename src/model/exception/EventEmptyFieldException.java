package model.exception;

@SuppressWarnings("serial")
public class EventEmptyFieldException extends Exception {

	public EventEmptyFieldException() {
		super();
	}

	public EventEmptyFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EventEmptyFieldException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventEmptyFieldException(String message) {
		super(message);
	}

	public EventEmptyFieldException(Throwable cause) {
		super(cause);
	}
}
