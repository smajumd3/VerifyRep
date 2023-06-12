package com.ibm.workday.automation.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PropertiesController {
    @Autowired
	private Environment env;
	 
	@RequestMapping(value = "/property/{key}", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<String> getPropertyValue(@PathVariable("key") String key) {
	    List<String> list = new ArrayList<>();
	    
	    String keyValue = env.getProperty(key);
	  
	    if( keyValue != null && !keyValue.isEmpty())
	    {
	    	list.add(keyValue);
	    }	    
	    return list;
	 }
}
