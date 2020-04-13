package org.verapdf.webapp.queueclient.handler;

import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorEvent;

public interface SendingToQueueErrorEventHandler {
	void handleEvent(SendingToQueueErrorEvent event);
}
