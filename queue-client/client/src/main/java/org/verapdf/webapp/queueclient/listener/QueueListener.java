package org.verapdf.webapp.queueclient.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

public abstract class QueueListener {

	@RabbitListener(queues = "${verapdf.rabbitmq.queue.name}")
	public final void listen(String message) {
		process(message);
	}

	public abstract void process(String message);
}
