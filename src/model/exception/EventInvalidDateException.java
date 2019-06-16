package model.exception;

@SuppressWarnings("serial")
public class EventInvalidDateException extends Exception {

	public EventInvalidDateException() {
	}

	public EventInvalidDateException(String message) {
		super(message);
	}

	public EventInvalidDateException(Throwable cause) {
		super(cause);
	}

	public EventInvalidDateException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventInvalidDateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
