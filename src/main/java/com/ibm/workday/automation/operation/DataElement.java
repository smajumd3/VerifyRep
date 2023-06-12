package com.ibm.workday.automation.operation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private String fieldName;
	private String value;
	private DataElement parent;
	private List<DataElement> children = new ArrayList<>();
	private List<DataAttribute> attributes = new ArrayList<>();
	private DataRule rule = new DataRule();
	private String identifierValue;
	private boolean nonEmptyChild;

	public DataRule getRule() {
		return rule;
	}

	public void setRule(DataRule rule) {
		this.rule = rule;
	}

	public DataElement() {
		super();
	}

	public DataElement(DataElement parent, String name, String value) {
		super();
		this.parent = parent;
		this.name = name;
		this.value = value;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public DataElement getParent() {
		return parent;
	}

	public void setParent(DataElement parent) {
		this.parent = parent;
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

	public List<DataElement> getChildren() {
		return children;
	}

	public List<DataAttribute> getAttributes() {
		return attributes;
	}

	public boolean addChild(DataElement dataElement) {
		return children.add(dataElement);
	}

	public boolean addAttribute(DataAttribute dataAttribute) {
		return attributes.add(dataAttribute);
	}
	
	public void removeAttribute(List<DataAttribute> dataAttribute) {
		
		int size = dataAttribute.size();
		for(int i = 0; i < size; i++)
		{
			attributes.remove(i);
			size--;
			i--;
		}
	}

	public boolean hasParent() {
		return parent != null;
	}

	public boolean hasReplaceValue() {
		return value != null && !value.trim().equalsIgnoreCase("")
				&& value.trim().startsWith("$");
	}

	public String getIdentifierValue() {
		return identifierValue;
	}

	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}

	public boolean isNonEmptyChild() {
		return nonEmptyChild;
	}

	public void setNonEmptyChild(boolean nonEmptyChild) {
		this.nonEmptyChild = nonEmptyChild;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
}
