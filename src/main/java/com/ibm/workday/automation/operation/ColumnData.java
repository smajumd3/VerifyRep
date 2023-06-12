package com.ibm.workday.automation.operation;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.CellType;

public class ColumnData implements Serializable {

	private static final long serialVersionUID = 7141596766774635631L;
	private String name;
	private String value;
	private CellType type;

	public ColumnData(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public ColumnData(String name, String value, CellType type) {
		this(name, value);
		this.type = type;
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

	public CellType getType() {
		return type;
	}

	public void setType(CellType type) {
		this.type = type;
	}

}
