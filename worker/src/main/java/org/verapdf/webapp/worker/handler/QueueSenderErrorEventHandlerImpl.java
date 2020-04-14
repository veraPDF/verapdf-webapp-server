package org.verapdf.webapp.worker.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.verapdf.webapp.queueclient.entity.QueueErrorEventType;
import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorData;
import org.verapdf.webapp.queueclient.handler.QueueSenderErrorEventHandler;

@Service
public class QueueSenderErrorEventHandlerImpl implements QueueSenderErrorEventHandler {

	private static final Logger LOGGER
			= LoggerFactory.getLogger(QueueSenderErrorEventHandlerImpl.class);

	@Override
	public void handleEvent(SendingToQueueErrorData event) {
		if (QueueErrorEventType.SENDING_ERROR_CALLBACK == event.getQueueErrorEventType()) {
			LOGGER.error("Message: {} cannot be send into the queue '{}', cause: {}",
					event.getMessage(), event.getQueueName(), event.getCauseMessage());
		} else {
			LOGGER.error("Message: {} cannot be send into the queue '{}', internal error, cause: {}",
					event.getMessage(), event.getQueueName(), event.getCauseMessage());
		}
	}
}
