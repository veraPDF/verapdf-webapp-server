package org.verapdf.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.verapdf.webapp.queueclient.sender.QueueSender;
import org.verapdf.webapp.queueclienthealthcheckcli.handler.QueueSenderHandlerImpl;
import org.verapdf.webapp.queueclienthealthcheckcli.listener.QueueListenerHandlerImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SpringBootApplication
@EnableAsync
public class QueueClientHealthcheckCliApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueueClientHealthcheckCliApplication.class);

	private static ConfigurableApplicationContext context;

	private QueueSender queueSender;
	private QueueListenerHandlerImpl queueListener;
	private QueueSenderHandlerImpl handler;

	private Set<String> sentMessages = new HashSet<>();

	@Autowired
	public QueueClientHealthcheckCliApplication(QueueSender queueSender,
	                                            QueueListenerHandlerImpl queueListener,
	                                            QueueSenderHandlerImpl handler) {
		this.queueSender = queueSender;
		this.queueListener = queueListener;
		this.handler = handler;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Async
	public void createAndSendTasks() {
		for(int i = 0; i < 30; i++) {
			String jobId = UUID.randomUUID().toString();
			queueSender.sendMessage(jobId);
			sentMessages.add(jobId);
		}
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			LOGGER.info("Caught exception while waiting for sending all jobsId to sender.");
		}

		int exitCode = SpringApplication.exit(context,
				(ExitCodeGenerator) () -> checkCorrectSendingAndReceivingJobs(
						sentMessages,
						queueListener.getSuccessfullyReceivedMessages(),
						handler.getUnsuccessfullyReceivedMessages()));

		System.exit(exitCode);
	}

	private static int checkCorrectSendingAndReceivingJobs(Set<String> allSentMessages,
	                                                       Set<String> sentSuccessfulMessages,
	                                                       Set<String> sentUnsuccessfulMessages) {
		Set<String> receivedMessages = new HashSet <>(sentSuccessfulMessages);
		receivedMessages.addAll(sentUnsuccessfulMessages);

		return allSentMessages.equals(receivedMessages) ? 0 : 1;
	}

	public static void main(String[] args) {
		context = new SpringApplicationBuilder(
				QueueClientHealthcheckCliApplication.class).web(WebApplicationType.NONE).run();
	}
}
