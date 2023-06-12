package com.ibm.workday.automation.operation;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxParserHandler extends DefaultHandler
{
	private ReportElement reportRootElement=null;
	private ReportElement parentElement=null;
	private ReportElement workingElement=null;
	private String operationType="";
	
	private StringBuffer buffer =new StringBuffer();
	
	
	
	 public ReportElement getReportRootElement() {
		return reportRootElement;
	}


	@Override
	   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		  //System.out.println("Start Element :" + qName);
		  
		  if(operationType.equals("S"))
		  {
			  this.parentElement=this.workingElement; 
		  }
		  this.workingElement=new ReportElement(qName);
		  
		  for(int i=0;i<attributes.getLength();i++)
		  {
			  String attr_name=attributes.getQName(i);
			  String attr_val=attributes.getValue(attr_name);
			  this.workingElement.addAttributes(attr_name, attr_val);
		  }
		  
		 if(this.parentElement!=null)
		 {
			 this.parentElement.addChild(this.workingElement);
			 this.workingElement.setParent(parentElement);
		 }
		 
		 operationType="S";
		  
	 }
	 
	 @Override
	   public void endElement(String uri, 
	      String localName, String qName) throws SAXException {
		 //System.out.println("End Element :" + qName);
		 
		 if(this.workingElement!=null)
		 {
			 //Setting the value
			 this.workingElement.setValue(buffer.toString());
			 //reseting the buffer
			 this.buffer =new StringBuffer();
			 //Resetting the working element
			 this.workingElement=null; 
		 }
		 else
		 {
			 if(this.parentElement==null)
			 {
				 this.reportRootElement=this.workingElement;
			 }
			 else
			 {
				 if(this.parentElement.getParent()!=null)
				 {
					 this.parentElement=this.parentElement.getParent();
				 }
				 else
				 {
					 this.reportRootElement=this.parentElement;
				 }
			 }
		 }
		 operationType="E";

	   }
	 
	 @Override
	   public void characters(char ch[], int start, int length) throws SAXException {
		 
		 String val=new String(ch, start, length);
		 //System.out.println("-----");

		 //System.out.println(val);
		 if(val.trim().length()<=0)
		 {
//			 if(operationType.equals("S"))
//			 {
//				 if(this.parentElement!=null)
//				 {
//					 this.parentElement.addChild(this.workingElement);
//					 this.workingElement.setParent(parentElement);
//				 }
//				 this.parentElement=this.workingElement;
//			 }
//			 else
//			 {
//				 if(this.workingElement!=null)
//				 {
//					 this.workingElement=null; 
//				 }
//				 else
//				 {
//					 if(this.parentElement.getParent()!=null)
//					 {
//						 this.parentElement=this.parentElement.getParent();
//					 }
//					 else
//					 {
//						 this.reportRootElement=this.parentElement;
//					 }
//				 }
//				 
//			 }
		 }
		 else
		 {
			 /*
			 this.workingElement.setValue(val);
			 */
			 
			 buffer.append(val);
			 
			 
//			 if(this.parentElement!=null)
//			 {
//				 this.parentElement.addChild(this.workingElement);
//				 this.workingElement.setParent(parentElement);
//			 }
//			 else
//			 {
//				 this.parentElement=this.workingElement;
//			 }
			 
		 }
		 	      
	   }
}
