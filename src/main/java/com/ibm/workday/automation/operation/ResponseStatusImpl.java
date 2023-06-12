package com.ibm.workday.automation.operation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ibm.workday.automation.common.CommonConstants;

@Component
public class ResponseStatusImpl implements ResponseStatus, CommonConstants {
	
	@Override
	public int getStatus(List<WSResponse> wsResponses, String type) {
		int total = 0;
		for (WSResponse response : wsResponses) {
			if (response.getResult().equalsIgnoreCase(type)) {
				total++;
			}
		}
		return total;
	}

	@Override
	public Set<String> getErrorIdentifiers(List<WSResponse> wsResponses) {
		Set<String> identifiers = new LinkedHashSet<String>();
		for (WSResponse response : wsResponses) {
			if (STATUS_FAILUE.equalsIgnoreCase(response.getResult())) {
				identifiers.add(response.getName());
			}
		}
		return identifiers;
	}

}
