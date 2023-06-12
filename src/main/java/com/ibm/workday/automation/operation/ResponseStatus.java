package com.ibm.workday.automation.operation;

import java.util.List;
import java.util.Set;

public interface ResponseStatus {
	int getStatus(List<WSResponse> wsResponses, String type);
	Set<String> getErrorIdentifiers(List<WSResponse> wsResponses);
}
