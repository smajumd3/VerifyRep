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
@Table(name="POSTLOAD")
public class PostLoad {
	
	@Id
	@Column(name="postLoadId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long postLoadId;

	@Column(name = "loadCycle")
	private String loadCycle;
	
	@Column(name = "ruleName")
	private String ruleName;

	@Column(name = "wdCSVFileName")
	private String wdCSVFileName;
	
	@Lob
	@Column (name="wdCSVFileContent", length=10485760 )
	private byte[] wdCSVFileContent;
	
	@Column(name = "srcCSVFileName")
	private String srcCSVFileName;
	
	@Lob
	@Column (name="srcCSVFileContent", length=10485760 )
	private byte[] srcCSVFileContent;

	@Column(name = "srcCSVFileNameNew")
	private String srcCSVFileNameNew;
	
	@Lob
	@Column (name="srcCSVFileContentNew", length=10485760 )
	private byte[] srcCSVFileContentNew;
	
	@Column(name = "wdXMLFileName")
	private String wdXMLFileName;
	
	@Lob
	@Column (name="wdXMLFileContent", length=10485760 )
	private byte[] wdXMLFileContent;
	
	@Column(name = "srcXMLFileName")
	private String srcXMLFileName;
	
	@Lob
	@Column (name="srcXMLFileContent", length=10485760 )
	private byte[] srcXMLFileContent;
	
	@Column(name = "userId")
	private long userId;
	
	@Column(name = "client")
	private String client;

	public PostLoad() {
		super();
	}

	public PostLoad(Long postLoadId, String loadCycle, String ruleName, String wdCSVFileName, byte[] wdCSVFileContent, String srcCSVFileName, byte[] srcCSVFileContent, String srcCSVFileNameNew,
			byte[] srcCSVFileContentNew, String wdXMLFileName, byte[] wdXMLFileContent ,String srcXMLFileName, byte[] srcXMLFileContent, long userId) {
		super();
		this.postLoadId = postLoadId;
		this.loadCycle = loadCycle;
		this.ruleName = ruleName;
		this.wdCSVFileName = wdCSVFileName;
		this.wdCSVFileContent = wdCSVFileContent;
		this.srcCSVFileName = srcCSVFileName;
		this.srcCSVFileContent = srcCSVFileContent;
		this.srcCSVFileNameNew = srcCSVFileNameNew;
		this.srcCSVFileContentNew = srcCSVFileContentNew;
		this.wdXMLFileName = wdXMLFileName;
		this.wdXMLFileContent = wdXMLFileContent;
		this.srcXMLFileName = srcXMLFileName;
		this.srcXMLFileContent = srcXMLFileContent;
		this.userId = userId;
	}

	public Long getPostLoadId() {
		return postLoadId;
	}

	public void setPostLoadId(Long postLoadId) {
		this.postLoadId = postLoadId;
	}

	public String getLoadCycle() {
		return loadCycle;
	}

	public void setLoadCycle(String loadCycle) {
		this.loadCycle = loadCycle;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getWdCSVFileName() {
		return wdCSVFileName;
	}

	public void setWdCSVFileName(String wdCSVFileName) {
		this.wdCSVFileName = wdCSVFileName;
	}

	public byte[] getWdCSVFileContent() {
		return wdCSVFileContent;
	}

	public void setWdCSVFileContent(byte[] wdCSVFileContent) {
		this.wdCSVFileContent = wdCSVFileContent;
	}

	public String getSrcCSVFileName() {
		return srcCSVFileName;
	}

	public void setSrcCSVFileName(String srcCSVFileName) {
		this.srcCSVFileName = srcCSVFileName;
	}

	public byte[] getSrcCSVFileContent() {
		return srcCSVFileContent;
	}

	public void setSrcCSVFileContent(byte[] srcCSVFileContent) {
		this.srcCSVFileContent = srcCSVFileContent;
	}
	
	public String getSrcCSVFileNameNew() {
		return srcCSVFileNameNew;
	}

	public void setSrcCSVFileNameNew(String srcCSVFileNameNew) {
		this.srcCSVFileNameNew = srcCSVFileNameNew;
	}

	public byte[] getSrcCSVFileContentNew() {
		return srcCSVFileContentNew;
	}

	public void setSrcCSVFileContentNew(byte[] srcCSVFileContentNew) {
		this.srcCSVFileContentNew = srcCSVFileContentNew;
	}

	public String getWdXMLFileName() {
		return wdXMLFileName;
	}

	public void setWdXMLFileName(String wdXMLFileName) {
		this.wdXMLFileName = wdXMLFileName;
	}

	public byte[] getWdXMLFileContent() {
		return wdXMLFileContent;
	}

	public void setWdXMLFileContent(byte[] wdXMLFileContent) {
		this.wdXMLFileContent = wdXMLFileContent;
	}

	public String getSrcXMLFileName() {
		return srcXMLFileName;
	}

	public void setSrcXMLFileName(String srcXMLFileName) {
		this.srcXMLFileName = srcXMLFileName;
	}

	public byte[] getSrcXMLFileContent() {
		return srcXMLFileContent;
	}

	public void setSrcXMLFileContent(byte[] srcXMLFileContent) {
		this.srcXMLFileContent = srcXMLFileContent;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
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
		return "PostLoad [postLoadId=" + postLoadId + ", loadCycle=" + loadCycle + ", ruleName=" + ruleName
				+ ", wdCSVFileName=" + wdCSVFileName + ", wdCSVFileContent=" + Arrays.toString(wdCSVFileContent)
				+ ", srcCSVFileName=" + srcCSVFileName + ", srcCSVFileContent=" + Arrays.toString(srcCSVFileContent)
				+ ", srcCSVFileNameNew=" + srcCSVFileNameNew + ", srcCSVFileContentNew="
				+ Arrays.toString(srcCSVFileContentNew) + ", wdXMLFileName=" + wdXMLFileName + ", wdXMLFileContent="
				+ Arrays.toString(wdXMLFileContent) + ", srcXMLFileName=" + srcXMLFileName + ", srcXMLFileContent="
				+ Arrays.toString(srcXMLFileContent) + ", userId=" + userId + ", client=" + client + "]";
	}

	

}
