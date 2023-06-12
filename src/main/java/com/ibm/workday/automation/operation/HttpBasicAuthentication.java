package com.ibm.workday.automation.operation;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

//import Decoder.BASE64Encoder;

public class HttpBasicAuthentication {
	
	private static Logger logger = Logger.getLogger(HttpBasicAuthentication.class);

	public String getWithBasicAuthentication(String endUrl, String userName, String password) {
		try {
	        Client client = Client.create();
	        Base64 base64 = new Base64();
	        String authString = userName + ":" + password;
	        //String authStringEnc = new BASE64Encoder().encode(authString.getBytes());
	        String authStringEnc = base64.encodeToString(authString.getBytes());
	        WebResource webResource = client.resource(endUrl);
	        
	        ClientResponse resp = webResource.accept("application/json")
	                                         .header("Authorization", "Basic " + authStringEnc)
	                                         .get(ClientResponse.class);
	        if(resp.getStatus() != 200){
	            logger.error("Unable to connect to the server");
	        }
	        String output = resp.getEntity(String.class);
	        return output;
	      } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	      } 
	}
	
	/*
	 * Begin get call with query parameter
	 * This overloaded method is for getting json as response
	 */
	
	public String getJsonWithBasicAuthentication(String endUrl, String userName, String password) {
		try {
	        Client client = Client.create();
	        Base64 base64 = new Base64();
	        String authString = userName + ":" + password;
	        String authStringEnc = base64.encodeToString(authString.getBytes());
	        System.out.println(authStringEnc);
	        System.out.println(endUrl);
	        WebResource webResource = client.resource(endUrl)
	        							.queryParam("format", "json");
	        //webResource.queryParam("format", "json");
	        
	        ClientResponse resp = webResource.accept("application/json")
	                                         .header("Authorization", "Basic " + authStringEnc)
	                                         .get(ClientResponse.class);
	        if(resp.getStatus() != 200){
	            logger.error("Unable to connect to the server");
	        }
	        String output = resp.getEntity(String.class);
	        return output;
	      } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	      } 
	}
	/*
	 * End get call with query parameter
	 */
}
