package com.ibm.workday.automation.operation;

import java.io.Serializable;

public class DataAttribute implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String prefix;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	private String name;
	private String value;
	private String type;

	public DataAttribute() {
		super();
	}

	public DataAttribute(String name, String value, String prefix) {
		super();
		this.name = name;
		this.value = value;
		this.prefix = prefix;
	}

	public DataAttribute(String name, String value, String type, String prefix) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
		this.prefix = prefix;
	}

	public DataAttribute(String name, String value) {
		super();
		this.name = name;
		this.value = value;
		this.prefix = "wd";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
