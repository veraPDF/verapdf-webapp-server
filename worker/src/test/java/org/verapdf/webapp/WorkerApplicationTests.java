package org.verapdf.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

@ActiveProfiles("test")
@SpringBootTest
class WorkerApplicationTests {

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@Test
	void contextLoads() {
	}

}
