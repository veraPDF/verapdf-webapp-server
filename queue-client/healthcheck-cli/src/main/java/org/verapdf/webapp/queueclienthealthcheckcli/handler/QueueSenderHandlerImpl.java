package org.verapdf.webapp.queueclienthealthcheckcli.handler;

import org.springframework.stereotype.Service;
import org.verapdf.webapp.queueclient.entity.QueueErrorEventType;
import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorData;
import org.verapdf.webapp.queueclient.handler.QueueSenderErrorEventHandler;

import java.util.HashSet;
import java.util.Set;

@Service
public class QueueSenderHandlerImpl implements QueueSenderErrorEventHandler {

	private final Set<String> unsuccessfullyReceivedMessages = new HashSet<>();

	@Override
	public void handleEvent(SendingToQueueErrorData event) {
		if (QueueErrorEventType.SENDING_ERROR_CALLBACK == event.getQueueErrorEventType()) {
			unsuccessfullyReceivedMessages.add(event.getMessage());
		}
	}

	public Set<String> getUnsuccessfullyReceivedMessages() {
		return unsuccessfullyReceivedMessages;
	}
}
