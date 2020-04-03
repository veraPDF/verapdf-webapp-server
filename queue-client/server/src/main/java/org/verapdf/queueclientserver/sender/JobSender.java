package org.verapdf.queueclientserver.sender;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

abstract public class JobSender {

	private final RabbitTemplate rabbitTemplate;
	private final String queueName;

	public JobSender(RabbitTemplate rabbitTemplate,
	                 @Qualifier("jobsQueue") Queue queue) {
		this.rabbitTemplate = rabbitTemplate;
		this.queueName = queue.getName();
	}

	public void sendTask(UUID jobId) {
		try {
			CorrelationData correlationData = new CorrelationData(queueName);
			rabbitTemplate.convertAndSend(queueName, jobId,
					message -> {
						message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
						correlationData.setReturnedMessage(message);
						return message;
					}, correlationData);
		} catch (Throwable e) {
			/*jobService.deleteJobById(jobDTO.getId());*/
			throw e;
		}
	}

	public abstract void handleSendToQueueError();
}
