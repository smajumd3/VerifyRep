package com.ibm.workday.automation.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "PAGE")
public class Page {
	
	@Id
	@Column(name="pageId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long pageId;
	
	@Column(name = "index")
	private Integer index;	
	
	@Column(name="pageName")
	private String pageName;
	
	@Column(name = "userId")
	private Long userId;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "page", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Section> sections;
/*	
	@JsonBackReference
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name ="fileDataId")
	private FileData fileData;
*/
	public Page() {
		super();
	}

	protected Page(Long pageId, Integer index, String pageName, List<Section> sections, Long userId) {
		super();
		this.pageId = pageId;
		this.index = index;
		this.pageName = pageName;
		this.sections = sections;
		this.userId = userId;
//		this.fileData = fileData;
	}

	public Long getPageId() {
		return pageId;
	}

	public void setPageId(Long pageId) {
		this.pageId = pageId;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public List<Section> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}
/*
	public FileData getFileData() {
		return fileData;
	}

	public void setFileData(FileData fileData) {
		this.fileData = fileData;
	}
*/
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Page [pageId=" + pageId + ", index=" + index + ", pageName=" + pageName + ", userId=" + userId
				+ ", sections=" + sections + "]";
	}

}
