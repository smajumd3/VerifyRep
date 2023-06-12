package com.ibm.workday.automation.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="RESULT")
public class Result {
	
	@Id
	@Column(name="resultId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long resultId;
	
	@Column(name="totalRecords")
	private Integer totalRecords;
	
	@Column(name="totalSuccess")
	private Integer totalSuccess;
	
	@Column(name="totalFailures")
	private Integer totalFailures;
	
	@Column(name="loadDate")
	private Date loadDate;
	
	@Lob
	@Column (name="wsResponseData", length=10485760 )
	private byte[] wsResponseData;
	
	@JsonBackReference
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name ="sectionId")
	private Section section;

	public Result() {
		super();
	}

	protected Result(Long resultId, Integer totalRecords, Integer totalSuccess, 
			         Integer totalFailures, Date loadDate, Section section, byte[] wsResponseData) {
		super();
		this.resultId = resultId;
		this.totalRecords = totalRecords;
		this.totalSuccess = totalSuccess;
		this.totalFailures = totalFailures;
		this.loadDate = loadDate;
		this.section = section;
		this.wsResponseData = wsResponseData;
	}

	public Long getResultId() {
		return resultId;
	}

	public void setResultId(Long resultId) {
		this.resultId = resultId;
	}

	public Integer getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	public Integer getTotalSuccess() {
		return totalSuccess;
	}

	public void setTotalSuccess(Integer totalSuccess) {
		this.totalSuccess = totalSuccess;
	}

	public Integer getTotalFailures() {
		return totalFailures;
	}

	public void setTotalFailures(Integer totalFailures) {
		this.totalFailures = totalFailures;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public Date getLoadDate() {
		return loadDate;
	}

	public void setLoadDate(Date loadDate) {
		this.loadDate = loadDate;
	}
	
	public byte[] getWsResponseData() {
		return wsResponseData;
	}

	public void setWsResponseData(byte[] wsResponseData) {
		this.wsResponseData = wsResponseData;
	}

	@Override
	public String toString() {
		return "Result [resultId=" + resultId + ", totalRecords=" + totalRecords + ", totalSuccess=" + totalSuccess
				+ ", totalFailures=" + totalFailures + ", loadDate=" + loadDate + ", section=" + section + "]";
	}

}
