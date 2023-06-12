package com.ibm.workday.automation.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportElement {

	private String name;
	private String value;
	private Map<String,String> atributes;
	private List<ReportElement> children;
	private ReportElement parent;
	
	

	public ReportElement(String name)
	{
		this.name = name;
		this.value="";
		children=new ArrayList<ReportElement>();
		atributes=new HashMap<String,String>();
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean hasChildren()
	{
		boolean retVal=false;
		if(children.size()>=1)
		{
			retVal=true;
		}
		return retVal;
	}
	
	public String getAttribute(String attribute_name)
	{
		String ret_val=atributes.get(attribute_name);
		if(ret_val == null) ret_val="";
		return ret_val;
	}
	
	public Map<String,String> getAllAttributes()
	{
		return this.atributes;
	}
	
	public void addAttributes(String attr_name,String attr_val)
	{
		this.atributes.put(attr_name, attr_val);
	}
	
	public ReportElement getChild(String child_name)
	{
		ReportElement child=children.stream().filter(c-> c.getName().equals(child_name)).findFirst().orElse(null);
		return child;
	}
	
	public List<ReportElement> getChildren(String child_name)
	{
		List<ReportElement> ret_children=children.stream().filter(c-> c.getName().equals(child_name)).collect(Collectors.toList()); ;
		return ret_children;
	}

	
	public List<ReportElement> getAllChildren()
	{
		return this.children;
	}
	public ReportElement getParent() {
		return parent;
	}

	public void setParent(ReportElement parent) {
		this.parent = parent;
	}
	
	public void addChild(ReportElement child)
	{
		this.children.add(child);
	}
	public ReportElement getChildWithAttribute(String attribute_name,String attribute_val)
	{
		ReportElement retChild=null;
		
		for(ReportElement child:children)
		{
			String attr=child.getAttribute(attribute_name);
			if(attr !=null && attr.equals(attribute_val))
			{
				retChild=child; 
				break;
			}
		}
		
		return retChild;
	}
}
