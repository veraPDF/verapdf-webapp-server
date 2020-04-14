package org.verapdf.webapp.worker.listener;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;
import org.verapdf.webapp.worker.entity.ProfileMapper;

@ActiveProfiles("test")
@SpringBootTest
public class ProfileMapperTests {

	@Autowired
	private ProfileMapper profileMapper;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@ParameterizedTest
	@EnumSource(value = Profile.class, names = {"PDFA_AUTO", "PDFUA_1_MACHINE"}, mode = EnumSource.Mode.EXCLUDE)
	public void checkExistenceOfAllProfiles(Profile profile) {
		Assertions.assertNotNull(profileMapper.getValidationProfile(profile));
	}

	@ParameterizedTest
	@EnumSource(value = Profile.class, names = {"PDFA_AUTO", "PDFUA_1_MACHINE"})
	public void checkMissingProfiles(Profile profile) {
		Assertions.assertNull(profileMapper.getValidationProfile(profile));
	}

	@Test
	public void checkPassingNullableProfileToProfileMapper() {
		Assertions.assertNull(profileMapper.getValidationProfile(null));
	}
}
