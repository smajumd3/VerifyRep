package com.ibm.workday.automation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name="MAPFILE")
public class MapFile {
	
	@Id
	@Column(name="mapFileId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long mapFileId;
	
	@Column(name = "fileName")
	private String fileName;
	
	@Column(name = "fileLink")
	private String fileLink;

	@Column(name = "filePath")
	private String filePath;
	
	@Lob
	@Column (name="mapFileData", length=10485760 )
	private byte[] mapFileData;
	
	@Column(name = "operationId")
	private long operationId;

	public MapFile() {
		super();
	}

	public MapFile(Long mapFileId, String fileName, String fileLink, String filePath, byte[] mapFileData,
			long operationId) {
		super();
		this.mapFileId = mapFileId;
		this.fileName = fileName;
		this.fileLink = fileLink;
		this.filePath = filePath;
		this.mapFileData = mapFileData;
		this.operationId = operationId;
	}

	public Long getMapFileId() {
		return mapFileId;
	}

	public void setMapFileId(Long mapFileId) {
		this.mapFileId = mapFileId;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public byte[] getMapFileData() {
		return mapFileData;
	}

	public void setMapFileData(byte[] mapFileData) {
		this.mapFileData = mapFileData;
	}

	public long getOperationId() {
		return operationId;
	}

	public void setOperationId(long operationId) {
		this.operationId = operationId;
	}

	@Override
	public String toString() {
		return "MapFile [mapFileId=" + mapFileId + ", fileName=" + fileName + ", fileLink=" + fileLink + ", filePath="
				+ filePath + ", operationId=" + operationId + "]";
	}

}
