package org.verapdf.webapp.queueclient.handler;

import com.rabbitmq.client.Channel;

public interface QueueListenerHandler {

	void handleMessage(String message, Channel channel, long deliveryTag);
}
