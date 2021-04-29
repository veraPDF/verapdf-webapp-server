package org.verapdf.webapp.queueclienthealthcheckcli.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.verapdf.webapp.queueclient.handler.QueueListenerHandler;
import org.verapdf.webapp.queueclient.util.QueueUtil;

import java.util.HashSet;
import java.util.Set;

@Service
public class QueueListenerHandlerImpl implements QueueListenerHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueueListenerHandlerImpl.class);

	private final QueueUtil queueUtil;
	private final Set<String> successfullyReceivedMessages = new HashSet<>();

	public QueueListenerHandlerImpl(QueueUtil queueUtil) {
		this.queueUtil = queueUtil;
	}

	@Override
	public void handleMessage(String message, Channel channel, long deliveryTag) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOGGER.info("Caught exception while waiting for processing jobId by worker.");
		}
		successfullyReceivedMessages.add(message);
		queueUtil.applyAndDiscardJob(channel, deliveryTag, null, null);
	}

	public Set<String> getSuccessfullyReceivedMessages() {
		return successfullyReceivedMessages;
	}
}
