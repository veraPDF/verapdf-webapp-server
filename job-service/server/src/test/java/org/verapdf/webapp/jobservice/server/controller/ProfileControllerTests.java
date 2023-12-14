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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@Test
	void getProfilesTest() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/profiles"))
		       .andExpect(status().isOk())
		       .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
		       .andExpect(content().json("[" +
		                                 "{\"profileName\":\"WCAG_2_2_COMPLETE\"," +
		                                 "\"humanReadableName\":\"WCAG 2.2 Machine & Human (experimental)\"," +
		                                 "\"enabled\":true}," + 
		                                 "{\"profileName\":\"WCAG_2_2_MACHINE\"," +
		                                 "\"humanReadableName\":\"WCAG 2.2 (Machine)\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"WCAG_2_2_HUMAN\"," +
		                                 "\"humanReadableName\":\"WCAG 2.2 (Human)\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"WCAG_2_2_DEV\"," +
		                                 "\"humanReadableName\":\"WCAG 2.2 (DEV)\"," +
		                                 "\"enabled\":false}," +
		                                 "{\"profileName\":\"PDFUA_1\"," +
		                                 "\"humanReadableName\":\"PDF/UA-1\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"PDFUA_2\"," +
		                                 "\"humanReadableName\":\"PDF/UA-2\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"PDFUA_2_TAGGED_PDF\"," +
		                                 "\"humanReadableName\":\"PDF/UA-2 & ISO 32005\"," +
		                                 "\"enabled\":true}," +
		                                 "{\"profileName\":\"TAGGED_PDF\"," +
		                                 "\"humanReadableName\":\"ISO 32005\"," +
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
