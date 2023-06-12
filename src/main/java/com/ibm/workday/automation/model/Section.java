package com.ibm.workday.automation.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name="SECTION")
public class Section {

	@Id
	@Column(name="sectionId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long sectionId;
	
	@Column(name = "index")
	private Integer index;
	
	@Column(name="areaName")
	private String areaName;
	
	@Column(name="taskName")
	private String taskName;
	
	@Column(name="operationName")
	private String operationName;
	
	@Column(name="execute")
	private Boolean execute;
	
	@Column(name="isDownload")
	private Boolean isDownload;	
	
	@Column(name="status")
	private Integer status;
	
	@Column(name="assignedTo")
	private String assignedTo;
	
	@Column(name="validateDate")
	private String validateDate;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "section", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Result> results;
	
	@JsonBackReference
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name ="pageId")
	private Page page;

	public Section() {
		super();
	}

	protected Section(Long sectionId, Integer index, String areaName, String taskName, String operationName,
			Boolean execute, Boolean isDownload, Integer status, String assignedTo, String validateDate, List<Result> results, Page page) {
		super();
		this.sectionId = sectionId;
		this.index = index;
		this.areaName = areaName;
		this.taskName = taskName;
		this.operationName = operationName;
		this.execute = execute;
		this.isDownload = isDownload;
		this.status = status;
		this.assignedTo = assignedTo;
		this.validateDate = validateDate;
		this.results = results;
		this.page = page;
	}

	public Long getSectionId() {
		return sectionId;
	}

	public void setSectionId(Long sectionId) {
		this.sectionId = sectionId;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public Boolean getExecute() {
		return execute;
	}

	public Boolean getIsDownload() {
		return isDownload;
	}

	public void setIsDownload(Boolean isDownload) {
		this.isDownload = isDownload;
	}

	public void setExecute(Boolean execute) {
		this.execute = execute;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	public String getValidateDate() {
		return validateDate;
	}

	public void setValidateDate(String validateDate) {
		this.validateDate = validateDate;
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	@Override
	public String toString() {
		return "Section [sectionId=" + sectionId + ", index=" + index + ", areaName=" + areaName + ", taskName="
				+ taskName + ", operationName=" + operationName + ", execute=" + execute + ", isDownload=" + isDownload
				+ ", status=" + status + ", assignedTo=" + assignedTo + ", validateDate=" + validateDate + ", results="
				+ results + ", page=" + page + "]";
	}

}
