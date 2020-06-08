package org.verapdf.webapp.worker.config;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.verapdf.webapp.jobservice.client.service.JobServiceClient;
import org.verapdf.webapp.localstorageservice.client.service.LocalStorageServiceClient;
import org.verapdf.webapp.queueclient.handler.QueueListenerHandler;
import org.verapdf.webapp.queueclient.handler.QueueSenderErrorEventHandler;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Configuration
public class WorkerConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkerConfig.class);

	@Bean("workingDir")
	public File workingDir(
			@Value("${verapdf.files.worker-dir}") String baseDirPath) throws IOException {
		File res = new File(baseDirPath);
		if (!res.isDirectory()) {
			LOGGER.warn("Missing working directory. Trying to create with path: " + res.getAbsolutePath());
			FileUtils.forceMkdir(res);
		}
		FileUtils.cleanDirectory(res);
		return res;
	}

	@Bean
	public JobServiceClient jobServiceClient(
			@Value("${verapdf.job-service.uri}") String uriToJobService) throws URISyntaxException {
		return new JobServiceClient(new URI(uriToJobService));
	}

	@Bean
	public LocalStorageServiceClient localStorageServiceClient(
			@Value("${verapdf.local-storage-service.uri}") String uriToStorageFileService) throws URISyntaxException {
		return new LocalStorageServiceClient(new URI(uriToStorageFileService));
	}

	@Bean
	public QueueListener queueListener(AmqpAdmin amqpAdmin,
	                                   List<QueueListenerHandler> queueListenerHandlers) {
		return new QueueListener(amqpAdmin, queueListenerHandlers);
	}

	@Bean
	public QueueSender queueSender(@Value("${verapdf.rabbitmq.queues.result-queue.name}") String sendingQueueName,
	                               @Value("${verapdf.rabbitmq.queues.result-queue.max-size}") DataSize sendingQueueSize,
	                               RabbitTemplate rabbitTemplate,
	                               AmqpAdmin amqpAdmin,
	                               List<QueueSenderErrorEventHandler> queueSenderErrorEventHandler) {
		return new QueueSender(sendingQueueName, sendingQueueSize, rabbitTemplate,
				amqpAdmin, queueSenderErrorEventHandler);
	}
}
