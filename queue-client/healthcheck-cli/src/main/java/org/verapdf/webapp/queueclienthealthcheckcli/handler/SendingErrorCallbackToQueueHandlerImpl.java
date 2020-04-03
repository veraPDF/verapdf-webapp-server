package org.verapdf.webapp.queueclienthealthcheckcli.handler;

import org.springframework.stereotype.Service;
import org.verapdf.webapp.queueclient.entity.QueueErrorEventType;
import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorEvent;
import org.verapdf.webapp.queueclient.handler.SendingToQueueErrorEventHandler;

import java.util.HashSet;
import java.util.Set;

@Service
public class SendingErrorCallbackToQueueHandlerImpl implements SendingToQueueErrorEventHandler {

	private final Set<String> unsuccessfullyReceivedMessages = new HashSet<>();

	@Override
	public void handleEvent(SendingToQueueErrorEvent event) {
		if (event.getQueueErrorEventType().equals(QueueErrorEventType.SENDING_ERROR_CALLBACK)) {
			unsuccessfullyReceivedMessages.add(event.getMessage());
		}
	}

	public Set<String> getUnsuccessfullyReceivedMessages() {
		return unsuccessfullyReceivedMessages;
	}
}
