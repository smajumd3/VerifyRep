package com.ibm.workday.automation.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app")
public class ApplicationProperties {
	
	private String names;
	private String workdayCommunityBaseURL;

	public String getNames() {
		return names;
	}

	public void setNames(String names) {
		this.names = names;
	}

	public String getWorkdayCommunityBaseURL() {
		return workdayCommunityBaseURL;
	}

	public void setWorkdayCommunityBaseURL(String workdayCommunityBaseURL) {
		this.workdayCommunityBaseURL = workdayCommunityBaseURL;
	}

}
