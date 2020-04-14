package org.verapdf.webapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

@SpringBootTest
@ActiveProfiles("test")
public class JobServiceServerApplicationTest {

    @MockBean
    private QueueSender queueSender;

    @MockBean
    private QueueListener queueListener;

    @Test
    public void contextLoads() {
    }
}
