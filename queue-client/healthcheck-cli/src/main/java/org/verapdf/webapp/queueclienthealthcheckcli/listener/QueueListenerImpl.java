package org.verapdf.webapp.queueclienthealthcheckcli.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.verapdf.webapp.queueclient.listener.QueueListener;

import java.util.HashSet;
import java.util.Set;

@Service
public class QueueListenerImpl extends QueueListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueueListenerImpl.class);

	private Set<String> successfullyReceivedMessages = new HashSet <>();

	@Override
	public void process(String message) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOGGER.info("Caught exception while waiting for processing jobId by worker.");
		}
		successfullyReceivedMessages.add(message);
	}

	public Set<String> getSuccessfullyReceivedMessages() {
		return successfullyReceivedMessages;
	}
}
