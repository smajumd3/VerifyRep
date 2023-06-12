package com.ibm.workday.automation.operation;

import java.util.HashSet;
import java.util.Set;

public class Condition {

	private String checkFrom;
	private String operator;
	private Set<String> values = new HashSet<String>();

	public Condition(String checkFrom, String operator, Set<String> values) {
		super();
		this.checkFrom = checkFrom;
		this.operator = operator;
		this.values = values;
	}

	public Condition() {
		super();
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Set<String> getValues() {
		return values;
	}

	public void setValues(Set<String> values) {
		this.values = values;
	}

	public String getCheckFrom() {
		return checkFrom;
	}

	public void setCheckFrom(String checkFrom) {
		this.checkFrom = checkFrom;
	}

}
