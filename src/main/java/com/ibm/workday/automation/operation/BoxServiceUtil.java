package com.ibm.workday.automation.operation;

import java.io.InputStream;

import javax.servlet.http.HttpSession;

public interface BoxServiceUtil {
	String uploadFile(String boxPath, String fileName,  byte[] fileBytes, HttpSession session);
	InputStream readFileFromBox(String fileName, String boxPath);
}
