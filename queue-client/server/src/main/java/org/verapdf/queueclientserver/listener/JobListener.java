package org.verapdf.queueclientserver.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.UUID;

public abstract class JobListener {
	@RabbitListener(queues = "${verapdf.rabbitmq.queue.name}")
	public void listen(UUID jobId) {
		process(jobId);
	}

	public abstract void process(UUID jobId);
}
