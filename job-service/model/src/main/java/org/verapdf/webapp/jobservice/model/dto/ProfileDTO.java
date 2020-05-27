package org.verapdf.webapp.jobservice.model.dto;

import org.verapdf.webapp.jobservice.model.entity.enums.Profile;

import java.util.Objects;

public class ProfileDTO {

	private Profile profileName;
	private String humanReadableName;
	private boolean enabled;

	public ProfileDTO(Profile profile, boolean enabled) {
		this.profileName = profile;
		this.humanReadableName = profile.getHumanReadableName();
		this.enabled = enabled;
	}

	public Profile getProfileName() {
		return profileName;
	}

	public String getHumanReadableName() {
		return humanReadableName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ProfileDTO)) return false;
		ProfileDTO that = (ProfileDTO) o;
		return enabled == that.enabled &&
				profileName == that.profileName &&
				Objects.equals(humanReadableName, that.humanReadableName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(profileName, humanReadableName, enabled);
	}

	@Override
	public String toString() {
		return "ProfileDTO{" +
		       "profileName=" + profileName +
		       ", humanReadableName='" + humanReadableName + '\'' +
		       ", enabled=" + enabled +
		       '}';
	}
}
