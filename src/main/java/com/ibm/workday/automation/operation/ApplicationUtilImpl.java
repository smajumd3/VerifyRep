package com.ibm.workday.automation.operation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.Application;
import com.ibm.workday.automation.properties.ApplicationProperties;

@Component
public class ApplicationUtilImpl implements ApplicationUtil, CommonConstants {
	
	@Autowired
	ApplicationProperties appProperties;

	@SuppressWarnings({ "unchecked" })
	@Override
	public List<String> getAvailableOperations(Application application) {
		List<String> availableOperations = new ArrayList<>();
		try {
			InputStream in = new ByteArrayInputStream(application.getWsdlFileData());
			InputSource is = new InputSource(in);

			WSDLFactory wsdl = WSDLFactory.newInstance();

			WSDLReader reader = wsdl.newWSDLReader();
			Definition wsdlInstance = reader.readWSDL(null, is);

			Map<?, ?> portTypes = wsdlInstance.getPortTypes();
			Iterator<?> portTypesMapIterator = portTypes.values().iterator();
			
			while (portTypesMapIterator.hasNext()) {
				PortType portType = (PortType) portTypesMapIterator.next();
				List<Operation> operations = portType.getOperations();

				for (int i = 0; i < operations.size(); i++) {
					Operation operation = operations.get(i);
					availableOperations.add(operation.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		availableOperations.sort(String.CASE_INSENSITIVE_ORDER);
		return availableOperations;
	}

	@Override
	public byte[] generateWsdlData(Application application) {
		byte[] bytes = null;
		String version = "v" + application.getVersion();
		String path = application.getApplicationName() + "/" + version + 
				      "/" + application.getApplicationName() + WSDL_SUFFIX;
		
		InputStream in = null;
		try {
			URL url = new URL(appProperties.getWorkdayCommunityBaseURL() + path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(
					connection.getInputStream());
			bytes = IOUtils.toByteArray(in);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return bytes;
	}

	@Override
	public byte[] generateXsdData(Application application) {
		byte[] bytes = null;
		String version = "v" + application.getVersion();
		String path = application.getApplicationName() + "/" + version + 
			      "/" + application.getApplicationName() + XSD_SUFFIX;
	
	    BufferedInputStream in = null;
	    try {
		    URL url = new URL(appProperties.getWorkdayCommunityBaseURL() + path);
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    in = new BufferedInputStream(
				connection.getInputStream());
		    bytes = IOUtils.toByteArray(in);
	    } catch (MalformedURLException e) {
		    e.printStackTrace();
	    } catch (IOException e) {
		    e.printStackTrace();
	    } finally {
		    if(in != null) {
			    try {
				    in.close();
			    } catch (IOException e) {
				    e.printStackTrace();
			    }
		    }
	    }
	    
	    return bytes;
	}

	@Override
	public Map<String, String> getKeyValueURLs() {
		// TODO Auto-generated method stub
		return null;
	}

}
