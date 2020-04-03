package org.verapdf.webapp.queueclient.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.verapdf.webapp.queueclient.entity.QueueErrorEventType;
import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorEvent;

@Configuration
@EnableRabbit
public class AmqpConfig {

	private final String defaultQueueName;
	private final DataSize defaultQueueSize;

	public AmqpConfig(@Value("${verapdf.rabbitmq.queue.name}") String defaultQueueName,
	                  @Value("${verapdf.rabbitmq.queue.max-size}") DataSize defaultQueueSize) {
		this.defaultQueueName = defaultQueueName;
		this.defaultQueueSize = defaultQueueSize;
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
	                                     ApplicationEventPublisher publisher) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate();
		rabbitTemplate.setConnectionFactory(connectionFactory);
		rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (!ack && correlationData != null && defaultQueueName.equals(correlationData.getId())) {
				Message returnedMessage = correlationData.getReturnedMessage();
				if (returnedMessage != null) {
					String message = new String(correlationData.getReturnedMessage().getBody());
					publisher.publishEvent(new SendingToQueueErrorEvent(
							message, QueueErrorEventType.SENDING_ERROR_CALLBACK, cause, null));
				}
			}
		});

		return rabbitTemplate;
	}

	@Bean("defaultQueue")
	public Queue defaultQueue() {
		return QueueBuilder
				.durable(defaultQueueName)
				.overflow(QueueBuilder.Overflow.rejectPublish)
				.maxLengthBytes((int) defaultQueueSize.toBytes())
				.build();
	}
}
