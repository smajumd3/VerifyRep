package com.ibm.workday.automation.model;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name="FILE")
public class File {

	@Id
	@Column(name="fileId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long fileId;
	
	@Column(name = "fileName")
	private String fileName;
	
	@Column(name = "fileLink")
	private String fileLink;
	
	@Column(name = "userId")
	private Long userId;
	
	@Column(name = "client")
	private String client;
	
	@Lob
	@Column (name="fileData", length=10485760 )
	private byte[] fileData;

	public File() {
		super();
	}

	protected File(Long fileId, String fileName, String fileLink, Long userId, String client, byte[] fileData) {
		super();
		this.fileId = fileId;
		this.fileName = fileName;
		this.fileLink = fileLink;
		this.userId = userId;
		this.client = client;
		this.fileData = fileData;
	}

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@Override
	public String toString() {
		return "File [fileId=" + fileId + ", fileName=" + fileName + ", fileLink=" + fileLink + ", userId=" + userId
				+ ", client=" + client + ", fileData=" + Arrays.toString(fileData) + "]";
	}
	
}
