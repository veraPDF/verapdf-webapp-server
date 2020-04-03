package org.verapdf.queueclientserver.config;

/*import com.duallab.verapdf.server.entity.enums.JobError;
import com.duallab.verapdf.server.service.JobService;*/
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.util.UUID;

@Configuration
@EnableRabbit
public class AmqpConfig {

	private final String jobsQueueName;
	private final DataSize jobsQueueSize;

	public AmqpConfig(@Value("${verapdf.rabbitmq.queue.name}") String jobsQueueName,
	                  @Value("${verapdf.rabbitmq.queue.max-size}") DataSize jobsQueueSize) {
		this.jobsQueueName = jobsQueueName;
		this.jobsQueueSize = jobsQueueSize;
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory/*,
	                                     JobService jobService*/) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate();
		rabbitTemplate.setConnectionFactory(connectionFactory);
		rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (correlationData != null && !ack) {
				Message returnedMessage = correlationData.getReturnedMessage();
				if (returnedMessage != null && jobsQueueName.equals(correlationData.getId())) {
					UUID jobId = UUID.nameUUIDFromBytes(returnedMessage.getBody());
					/*jobService.jobError(jobId, JobError.SENDING_TO_QUEUE_ERROR,
							"Job with id: " + jobId + " couldn't be added to the jobQueue.")*/;
				}
			}
		});
		return rabbitTemplate;
	}

	@Bean("jobsQueue")
	public Queue jobsQueue() {
		return QueueBuilder
				.durable(jobsQueueName)
				.overflow(QueueBuilder.Overflow.rejectPublish)
				.maxLengthBytes((int) jobsQueueSize.toBytes())
				.build();
	}
}
