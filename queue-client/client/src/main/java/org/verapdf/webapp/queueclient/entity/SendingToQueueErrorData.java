package org.verapdf.webapp.queueclient.entity;

public class SendingToQueueErrorData {
	private String queueName;
	private String message;
	private QueueErrorEventType queueErrorEventType;
	private String causeMessage;
	private Exception causeException;

	public SendingToQueueErrorData(String queueName, String message,
	                               QueueErrorEventType queueErrorEventType,
	                               String causeMessage, Exception causeException) {
		this.queueName = queueName;
		this.message = message;
		this.queueErrorEventType = queueErrorEventType;
		this.causeMessage = causeMessage;
		this.causeException = causeException;
	}

	public String getQueueName() {
		return queueName;
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
