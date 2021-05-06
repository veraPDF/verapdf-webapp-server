package org.verapdf.webapp.queueclient.util;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class QueueUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueueUtil.class);

	private static QueueUtil instance;

	private QueueUtil() {
	}

	public static synchronized QueueUtil getInstance() {
		if (instance == null) {
			instance = new QueueUtil();
		}
		return instance;
	}

	public void applyAndDiscardJob(Channel channel, long deliveryTag, UUID jobId, UUID taskId) {
		try {
			channel.basicAck(deliveryTag, false);
		} catch (IOException e) {
			LOGGER.error("Task, taskId: " + taskId + " from job, jobId: " + jobId
			             + " cannot be applied and discarded", e);
		}
	}

	public void rejectAndKeepJob(Channel channel, long deliveryTag, UUID jobId, UUID taskId) {
		try {
			channel.basicNack(deliveryTag, false, true);
		} catch (IOException e) {
			LOGGER.error("Task, taskId: " + taskId + " from job, jobId: " + jobId
			             + " cannot be rejected and kept", e);
		}
	}

	public void rejectAndDiscardJob(Channel channel, long deliveryTag, UUID jobId, UUID taskId) {
		try {
			channel.basicReject(deliveryTag, false);
		} catch (IOException e) {
			LOGGER.error("Task, taskId: " + taskId + " from job, jobId: " + jobId
			             + " cannot be rejected and discarded", e);
		}
	}
}

