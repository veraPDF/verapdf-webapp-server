package org.verapdf.webapp.queueclienthealthcheckcli.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.verapdf.webapp.queueclient.handler.QueueListenerHandler;
import org.verapdf.webapp.queueclient.handler.QueueSenderErrorEventHandler;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import java.util.List;

@Configuration
public class HealthcheckCLIConfig {
	@Bean
	public QueueSender queueSender(
			@Value("${verapdf.rabbitmq.queues.listening-queue.name}") String sendingQueueName,
			@Value("${verapdf.rabbitmq.queues.listening-queue.max-size}") DataSize sendingQueueSize,
			RabbitTemplate rabbitTemplate,
			AmqpAdmin amqpAdmin,
			List<QueueSenderErrorEventHandler> queueSenderErrorEventHandlers) {
		return new QueueSender(sendingQueueName, sendingQueueSize, rabbitTemplate,
				amqpAdmin, queueSenderErrorEventHandlers);
	}

	@Bean
	public QueueListener queueListener(
			AmqpAdmin amqpAdmin,List<QueueListenerHandler> queueListenerHandlers) {
		return new QueueListener(amqpAdmin, queueListenerHandlers);
	}
}
