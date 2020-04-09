package org.verapdf.webapp.localstorageservice.model.dto;

import java.util.Objects;
import java.util.UUID;

public class StoredFileDTO {
	private UUID id;
	private String contentMD5;
	private String contentType;
	private long contentSize;
	private String fileName;

	public StoredFileDTO() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getContentMD5() {
		return contentMD5;
	}

	public void setContentMD5(String contentMD5) {
		this.contentMD5 = contentMD5;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getContentSize() {
		return contentSize;
	}

	public void setContentSize(long contentSize) {
		this.contentSize = contentSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StoredFileDTO)) {
			return false;
		}
		StoredFileDTO that = (StoredFileDTO) o;
		return contentSize == that.contentSize &&
		       Objects.equals(id, that.id) &&
		       Objects.equals(contentMD5, that.contentMD5) &&
		       Objects.equals(contentType, that.contentType) &&
		       Objects.equals(fileName, that.fileName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, contentMD5, contentType, contentSize, fileName);
	}

	@Override
	public String toString() {
		return "StoredFileDTO{" +
		       "id=" + id +
		       ", contentMD5='" + contentMD5 + '\'' +
		       ", contentType='" + contentType + '\'' +
		       ", contentSize=" + contentSize +
		       ", fileName='" + fileName + '\'' +
		       '}';
	}
}

