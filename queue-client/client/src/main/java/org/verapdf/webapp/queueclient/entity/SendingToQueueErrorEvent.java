package org.verapdf.webapp.queueclient.entity;

public class SendingToQueueErrorEvent {
	private String message;
	private QueueErrorEventType queueErrorEventType;
	private String causeMessage;
	private Exception causeException;

	public SendingToQueueErrorEvent(String message, QueueErrorEventType queueErrorEventType,
	                                String causeMessage, Exception causeException) {
		this.message = message;
		this.queueErrorEventType = queueErrorEventType;
		this.causeMessage = causeMessage;
		this.causeException = causeException;
	}

	public String getMessage() {
		return message;
	}

	public QueueErrorEventType getQueueErrorEventType() {
		return queueErrorEventType;
	}

	public String getCauseMessage() {
		return causeMessage;
	}

	public Exception getCauseException() {
		return causeException;
	}
}
