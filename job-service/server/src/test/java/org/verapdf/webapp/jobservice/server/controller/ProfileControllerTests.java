package org.verapdf.webapp.jobservice.server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProfileControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@Test
	public void getProfilesTest() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/profiles"))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(content().json("[" +
				       "{\"profileName\":\"WCAG_2_1_COMPLETE\"," +
				       "\"humanReadableName\":\"WCAG 2.1 (All)\"," +
				       "\"enabled\":true}," +
		                                 "{\"profileName\":\"WCAG_2_1\"," +
		                                 "\"humanReadableName\":\"WCAG 2.1 (Extra)\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"PDFUA_1_MACHINE\"," +
		                                 "\"humanReadableName\":\"PDF/UA-1 (Machine)\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"PDFUA_1_HUMAN\"," +
		                                 "\"humanReadableName\":\"PDF/UA-1 (Human)\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"TAGGED_PDF\"," +
		                                 "\"humanReadableName\":\"Tagged PDF\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"PDFA_1_A\"," +
		                                 "\"humanReadableName\":\"PDF/A-1A\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFA_1_B\"," +
		                                 "\"humanReadableName\":\"PDF/A-1B\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFA_2_A\"," +
		                                 "\"humanReadableName\":\"PDF/A-2A\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFA_2_U\"," +
		                                 "\"humanReadableName\":\"PDF/A-2U\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFA_2_B\"," +
		                                 "\"humanReadableName\":\"PDF/A-2B\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFA_3_A\"," +
		                                 "\"humanReadableName\":\"PDF/A-3A\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFA_3_U\"," +
		                                 "\"humanReadableName\":\"PDF/A-3U\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFA_3_B\"," +
		                                 "\"humanReadableName\":\"PDF/A-3B\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFA_AUTO\"," +
		                                 "\"humanReadableName\":\"PDF/A Auto-detect\"," +
		                                 "\"enabled\":false}]", true));
	}
}
