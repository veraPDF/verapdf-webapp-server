package org.verapdf.webapp.queueclient.sender;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.verapdf.webapp.queueclient.entity.QueueErrorEventType;
import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorEvent;
import org.verapdf.webapp.queueclient.handler.SendingToQueueErrorEventHandler;

import java.util.List;
import java.util.UUID;

@Service
public class QueueSender {

	private final RabbitTemplate rabbitTemplate;
	private final String defaultQueueName;
	private final List<SendingToQueueErrorEventHandler> sendingToQueueErrorEventHandlers;
	private final ApplicationEventPublisher publisher;

	public QueueSender(RabbitTemplate rabbitTemplate,
	                   @Qualifier("defaultQueue") Queue queue,
	                   ApplicationEventPublisher publisher,
	                   List<SendingToQueueErrorEventHandler> sendingToQueueErrorEventHandlers) {
		this.rabbitTemplate = rabbitTemplate;
		this.defaultQueueName = queue.getName();
		this.publisher = publisher;
		this.sendingToQueueErrorEventHandlers = sendingToQueueErrorEventHandlers;
	}

	public void sendMessage(String message) {
		try {
			CorrelationData correlationData = new CorrelationData(defaultQueueName);
			rabbitTemplate.convertAndSend(defaultQueueName, message,
					rabbitMessage -> {
						rabbitMessage.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
						correlationData.setReturnedMessage(rabbitMessage);
						return rabbitMessage;
					}, correlationData);
		} catch (Exception e) {
			publisher.publishEvent(new SendingToQueueErrorEvent(
					message, QueueErrorEventType.SENDING_EXCEPTION, e.getMessage(), e));
		}
	}

	@EventListener
	public final void handleSendToQueueError(SendingToQueueErrorEvent event) {
		for (SendingToQueueErrorEventHandler handler : sendingToQueueErrorEventHandlers) {
			handler.handleEvent(event);
		}
	}
}
