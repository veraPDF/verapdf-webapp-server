package org.verapdf.webapp.queueclient.handler;

import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorData;

public interface QueueSenderErrorEventHandler {
	void handleEvent(SendingToQueueErrorData event);
}
