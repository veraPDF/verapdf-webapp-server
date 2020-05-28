package org.verapdf.webapp.localstorageservice.server.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
public class StoredFile {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private UUID id;
	@Column(name = "local_path")
	private String localPath;
	@Column(name = "content_md5")
	private String contentMD5;
	@Column(name = "content_type", nullable = false)
	private String contentType;
	@Column(name = "content_size", nullable = false)
	private long contentSize;
	@Column(name = "file_name")
	private String fileName;
	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public StoredFile() {
		this.createdAt = Instant.now();
	}

	public StoredFile(String localPath, String contentMD5, String contentType, long contentSize) {
		this(localPath, contentMD5, contentType, contentSize, null);
	}

	public StoredFile(String localPath, String contentMD5, String contentType, long contentSize, String fileName) {
		this();
		this.localPath = localPath;
		this.contentMD5 = contentMD5;
		this.contentType = contentType;
		this.contentSize = contentSize;
		this.fileName = fileName;
	}

	public UUID getId() {
		return id;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
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

	public long getContentSize() {
		return contentSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
