package com.ibm.workday.automation.operation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class XmlParserManager {
	
	public static ReportElement parseXml(String xml) throws Exception
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		  try {
			  factory.setFeature("http://xml.org/sax/features/validation", false);
			  factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			  factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			  factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			  factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		  }
		  catch (Exception e1) {
		    e1.printStackTrace();
		  }
        SAXParser saxParser = factory.newSAXParser();
        //UserHandler userhandler = new UserHandler();
        SaxParserHandler handler = new SaxParserHandler();
        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        saxParser.parse(is, handler);
        return handler.getReportRootElement();
	}
}
