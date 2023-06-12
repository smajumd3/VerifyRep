package com.ibm.workday.automation.operation;

import java.util.List;
import java.util.Map;

public class FileData {
	private String identifier;
	private String uniqueIdColumn;
	private String name;
	private List<ColumnData[]> data;
	private Map<Integer, List<ColumnData[]>> contents;

	public FileData() {
		super();
	}

	public FileData(String identifier, String uniqueIdColumn, String name,
			List<ColumnData[]> data) {
		super();
		this.identifier = identifier;
		this.uniqueIdColumn = uniqueIdColumn;
		this.name = name;
		this.data = data;
	}

	public FileData(String identifier, String uniqueIdColumn, String name,
			Map<Integer, List<ColumnData[]>> contents) {
		super();
		this.identifier = identifier;
		this.uniqueIdColumn = uniqueIdColumn;
		this.name = name;
		this.contents = contents;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getUniqueIdColumn() {
		return uniqueIdColumn;
	}

	public void setUniqueIdColumn(String uniqueIdColumn) {
		this.uniqueIdColumn = uniqueIdColumn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ColumnData[]> getData() {
		return data;
	}

	public void setData(List<ColumnData[]> data) {
		this.data = data;
	}

	public Map<Integer, List<ColumnData[]>> getContents() {
		return contents;
	}

	public void setContents(Map<Integer, List<ColumnData[]>> contents) {
		this.contents = contents;
	}

}
