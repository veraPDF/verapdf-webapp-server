package org.verapdf.webapp.queueclient.sender;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.unit.DataSize;
import org.verapdf.webapp.queueclient.entity.QueueErrorEventType;
import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorData;
import org.verapdf.webapp.queueclient.handler.QueueSenderErrorEventHandler;

import java.util.ArrayList;
import java.util.List;

public class QueueSender {

	private final String sendingQueueName;
	private final RabbitTemplate rabbitTemplate;
	private final List<QueueSenderErrorEventHandler> sendingErrorEventHandlers = new ArrayList<>();

	public QueueSender(String sendingQueueName,
	                   DataSize sendingQueueSize,
	                   RabbitTemplate rabbitTemplate,
	                   AmqpAdmin amqpAdmin,
	                   List<QueueSenderErrorEventHandler> queueSenderErrorEventHandlers) {
		setUpRabbit(sendingQueueName, sendingQueueSize, rabbitTemplate, amqpAdmin);
		this.sendingQueueName = sendingQueueName;
		this.rabbitTemplate = rabbitTemplate;
		if (queueSenderErrorEventHandlers != null) {
			this.sendingErrorEventHandlers.addAll(queueSenderErrorEventHandlers);
		}
	}

	public void sendMessage(String message) {
		try {
			CorrelationData correlationData = new CorrelationData(sendingQueueName);
			rabbitTemplate.convertAndSend(sendingQueueName, message,
					rabbitMessage -> {
						rabbitMessage.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
						correlationData.setReturnedMessage(rabbitMessage);
						return rabbitMessage;
					}, correlationData);
		} catch (Exception e) {
			handleSendToQueueError(new SendingToQueueErrorData(sendingQueueName,
					message, QueueErrorEventType.SENDING_EXCEPTION, e.getMessage(), e));
		}
	}

	private void handleSendToQueueError(SendingToQueueErrorData errorData) {
		for (QueueSenderErrorEventHandler handler : sendingErrorEventHandlers) {
			handler.handleEvent(errorData);
		}
	}

	private void setUpRabbit(String sendingQueueName, DataSize sendingQueueSize,
	                         RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
		rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (!ack && correlationData != null) {
				Message returnedMessage = correlationData.getReturnedMessage();
				if (returnedMessage != null) {
					String message = new String(correlationData.getReturnedMessage().getBody());
					handleSendToQueueError(new SendingToQueueErrorData(
							correlationData.getId(), message,
							QueueErrorEventType.SENDING_ERROR_CALLBACK,
							cause, null));
				}
			}
		});

		amqpAdmin.declareQueue(QueueBuilder
				.durable(sendingQueueName)
				.overflow(QueueBuilder.Overflow.rejectPublish)
				.maxLengthBytes((int) sendingQueueSize.toBytes())
				.build());
	}
}
