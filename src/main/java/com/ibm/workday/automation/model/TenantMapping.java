package com.ibm.workday.automation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="TENANTMAPPING")
public class TenantMapping {

	@Id
	@Column(name="mappingId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long mappingId;
	
	@Column(name = "pageIndex")
	private Integer pageIndex;
	
	@Column(name="pageName")
	private String pageName;
	
	@Column(name = "tenantName")
	private String tenantName;
	
	@Column(name = "client")
	private String client;

	public TenantMapping() {
		super();
	}

	public TenantMapping(Long mappingId, Integer pageIndex, String pageName, String tenantName, String client) {
		super();
		this.mappingId = mappingId;
		this.pageIndex = pageIndex;
		this.pageName = pageName;
		this.tenantName = tenantName;
		this.client = client;
	}

	public Long getMappingId() {
		return mappingId;
	}

	public void setMappingId(Long mappingId) {
		this.mappingId = mappingId;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@Override
	public String toString() {
		return "TenantMapping [mappingId=" + mappingId + ", pageIndex=" + pageIndex + ", pageName=" + pageName
				+ ", tenantName=" + tenantName + ", client=" + client + "]";
	}	

}
