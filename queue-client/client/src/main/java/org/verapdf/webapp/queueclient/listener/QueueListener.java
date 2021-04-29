package org.verapdf.webapp.queueclient.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.util.unit.DataSize;
import org.verapdf.webapp.queueclient.handler.QueueListenerHandler;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

public class QueueListener {

	@Value("${verapdf.rabbitmq.queues.listening-queue.name}")
	private String listeningQueueName;
	@Value("${verapdf.rabbitmq.queues.listening-queue.max-size}")
	private DataSize listeningQueueSize;
	private final AmqpAdmin amqpAdmin;

	private final List<QueueListenerHandler> handlers = new ArrayList<>();

	// TODO: drop @RabbitListener and @PostConstruct, modify
	//  all listening variables names after dropping
	public QueueListener(AmqpAdmin amqpAdmin, List<QueueListenerHandler> handlers) {
		this.amqpAdmin = amqpAdmin;

		if (handlers != null) {
			this.handlers.addAll(handlers);
		}
	}

	@PostConstruct
	public void initialize() {
		amqpAdmin.declareQueue(QueueBuilder.durable(listeningQueueName)
		                                   .overflow(QueueBuilder.Overflow.rejectPublish)
		                                   .maxLengthBytes((int) listeningQueueSize.toBytes())
		                                   .build());
	}

	@RabbitListener(queues = "${verapdf.rabbitmq.queues.listening-queue.name}")
	public final void listen(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
		for (QueueListenerHandler handler : this.handlers) {
			handler.handleMessage(message, channel, deliveryTag);
		}
	}
}
