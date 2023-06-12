package com.ibm.workday.automation.operation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import com.ibm.wd.conversion.tool.dao.transfer.WSResponseDO;
//import com.ibm.wd.conversion.tool.dao.transfer.WSXPathExpressionDO;
import com.ibm.workday.automation.common.CommonConstants;

public class SendWWSRequest implements Runnable, CommonConstants {
	
	ResponseDataUtil responseData;
	private SOAPConnection soapConnection;
	private SOAPMessage soapRequest;
	private String endPointURL;
	private WSXPathExpression expression;
	private WSResponse wsResponse;
	private String name;
	private Transformer transformer;
	private Date responseDateTime;
	private static final long THREAD_SLEEP = 100;

	private NamespaceContext namespace = new SimpleNamespaceContext();

	public SendWWSRequest(String name, SOAPConnection soapConnection,
			SOAPMessage soapRequest, ResponseDataUtil responseData, String endPointURL,
			WSXPathExpression expression, final WSResponse wsResponse) {
		super();
		this.name = name;
		this.soapConnection = soapConnection;
		this.soapRequest = soapRequest;
		this.responseData = responseData;
		this.endPointURL = endPointURL;
		this.expression = expression;
		this.wsResponse = wsResponse;

		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			System.out.println("Sending request : " + soapRequest);
			getSoapMessage(soapRequest);
			SOAPMessage soapResponse = soapConnection.call(soapRequest, endPointURL);

			if (soapResponse != null) {
				processResponse(soapResponse);
			}
			Thread.sleep(THREAD_SLEEP);

		} catch (Exception exception) {
			// WSResponseDO responseData = new WSResponseDO();
			responseDateTime = new Date(System.currentTimeMillis());
			wsResponse.setResult(STATUS_FAILUE);
			wsResponse.setMessage("Error occurred while sending request to Workday");
			wsResponse.setResponseDateTime(responseDateTime);
			wsResponse.setTotalTime(System.currentTimeMillis()
					- wsResponse.getRequestDateTime().getTime());
			wsResponse.setFaultMessage(exception.getMessage());
			wsResponse.setComplete(true);
			wsResponse.setStatus(STATUS_TIMEOUT);
			wsResponse.setRunning(false);
			
			responseData.incrementFailureCount();
			responseData.decrementSoapExecutionPendingCount();
			System.err.println("Timeout occurred !");
		}

	}
	
	private void getSoapMessage(SOAPMessage soapRequest2) {
	    SOAPPart soapPart = soapRequest2.getSOAPPart();
	    SOAPEnvelope soapEnvelope;
		try {
			soapEnvelope = soapPart.getEnvelope();
		

	    SOAPHeader soapHeader = soapEnvelope.getHeader();
	    SOAPHeaderElement headerElement = soapHeader.addHeaderElement(soapEnvelope.createName(
	        "Signature", "SOAP-SEC", "http://schemas.xmlsoap.org/soap/security/2000-12"));

	    SOAPBody soapBody = soapEnvelope.getBody();
	    soapBody.addAttribute(soapEnvelope.createName("id", "SOAP-SEC",
	        "http://schemas.xmlsoap.org/soap/security/2000-12"), "Body");
	    Name bodyName = soapEnvelope.createName("FooBar", "z", "http://example.com");
	    SOAPBodyElement gltp = soapBody.addBodyElement(bodyName);

	    Source source = soapPart.getContent();

	    Node root = null;
	    if (source instanceof DOMSource) {
	      root = ((DOMSource) source).getNode();
	    } else if (source instanceof SAXSource) {
	      InputSource inSource = ((SAXSource) source).getInputSource();
	      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	      dbf.setNamespaceAware(true);
	      DocumentBuilder db = null;

	      db = dbf.newDocumentBuilder();

	      Document doc = db.parse(inSource);
	      root = (Node) doc.getDocumentElement();
	    }
	    
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.transform(new DOMSource(root), new StreamResult(System.out));   
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	private void processResponse(SOAPMessage soapResponse) {
		try {

			Document doc = null;
			try {
				doc = soapResponse.getSOAPBody().extractContentAsDocument();
			} catch (Exception e) {
				System.out.println("Not receieved any response from WebService, hence treating as success");
				wsResponse.setResult(STATUS_SUCCESS);
				wsResponse.setMessage("Loaded into Workday. Please verify it");
				wsResponse.setResponseDateTime(new Date(System
						.currentTimeMillis()));
				wsResponse.setTotalTime(System.currentTimeMillis()
						- wsResponse.getRequestDateTime().getTime());
				wsResponse.setResultXML("No response data");
				wsResponse.setRunning(false);
				wsResponse.setComplete(true);
				wsResponse.setStatus(STATUS_SUCCESS);
				
				responseData.incrementSuccessCount();
				responseData.decrementSoapExecutionPendingCount();
			}

			if (doc != null) {
				Source sourceContent = new DOMSource(doc);

				System.out.print("\nResponse SOAP Message = ");
				StringWriter stringWriter = new StringWriter();
				StreamResult result = new StreamResult(stringWriter);
				transformer.transform(sourceContent, result);
				System.out.println(stringWriter.toString());

				XPath xPath = XPathFactory.newInstance().newXPath();
				xPath.setNamespaceContext(namespace);

				System.out.println("Path Expression : "
						+ expression.getResponseExpression());
				String value = null;
				if (expression.getResponseExpression() != null
						&& !expression.getResponseExpression().isEmpty()) {
					value = xPath.compile(expression.getResponseExpression())
							.evaluate(doc);
					System.out.println("Value got : " + value);
				}

				String message = "";
				String res = null;
				if (value != null && !value.trim().isEmpty()) {
					res = "Success";
					message = "Loaded into Workday";
					wsResponse.setStatus(STATUS_SUCCESS);
					
					responseData.incrementSuccessCount();				
				} else {
					value = xPath.compile(expression.getFaultExpression())
							.evaluate(doc);
					wsResponse.setFaultMessage(value);
					processFaultMessage(doc.getChildNodes(),
							"wd:Detail_Message");
					res = STATUS_FAILUE;
					message = "Could not load into Workday";
					wsResponse.setStatus(STATUS_FAILUE);
					
					responseData.incrementFailureCount();
				}

				responseDateTime = new Date(System.currentTimeMillis());
				wsResponse.setResult(res);
				wsResponse.setMessage(message);
				wsResponse.setResponseDateTime(responseDateTime);
				wsResponse.setTotalTime(System.currentTimeMillis()
						- wsResponse.getRequestDateTime().getTime());
				wsResponse.setResultXML(stringWriter.toString());
				wsResponse.setRunning(false);
				wsResponse.setComplete(true);
				responseData.decrementSoapExecutionPendingCount();
			}

		} catch (Exception e) {
			e.printStackTrace();
			wsResponse.setStatus(STATUS_FAILUE);
			wsResponse.setComplete(true);
			wsResponse.setRunning(false);
			wsResponse.setResultXML("No response data");
			wsResponse
					.setResponseDateTime(new Date(System.currentTimeMillis()));
			wsResponse.setTotalTime(System.currentTimeMillis()
					- wsResponse.getRequestDateTime().getTime());
			
			responseData.incrementFailureCount();
			responseData.decrementSoapExecutionPendingCount();
		}
	}

	public void setSoapConnection(SOAPConnection soapConnection) {
		this.soapConnection = soapConnection;
	}

	public void setSoapRequest(SOAPMessage soapRequest) {
		this.soapRequest = soapRequest;
	}

	public SOAPMessage getSoapRequest() {
		return soapRequest;
	}

	public void setEndPointURL(String endPointURL) {
		this.endPointURL = endPointURL;
	}

	public WSXPathExpression getExpression() {
		return expression;
	}

	public void setExpression(WSXPathExpression expression) {
		this.expression = expression;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private void processFaultMessage(final NodeList validations,
			String messageNode) {
		for (int i = 0; i < validations.getLength(); i++) {
			Node aNode = validations.item(i);
			if (aNode.getNodeType() == Node.ELEMENT_NODE) {
				if (aNode.getNodeName().equalsIgnoreCase(messageNode)) {
					NodeList childNodes = aNode.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node node = childNodes.item(j);
						if (node.getNodeType() == Node.TEXT_NODE) {
							wsResponse.addFaultMessage(node.getNodeValue());
						}
					}
				} else {
					processFaultMessage(aNode.getChildNodes(), messageNode);
				}
			}
		}
	}

	private class SimpleNamespaceContext implements NamespaceContext {

		private final Map<String, String> namespaceMap = new HashMap<>();

		public SimpleNamespaceContext() {
			namespaceMap.put(NAMESPACE_PREFIX, NAMESPACE_URL);
			namespaceMap.put(SOAP_ENV_NAMESPACE_PREFIX, SOAP_ENV_NAMESPACE_URL);
		}

		public String getNamespaceURI(String prefix) {
			return namespaceMap.get(prefix);
		}

		public String getPrefix(String uri) {
			throw new UnsupportedOperationException();
		}

		public Iterator<RuntimeException> getPrefixes(String uri) {
			throw new UnsupportedOperationException();
		}
	}

	public WSResponse getWsResponse() {
		return wsResponse;
	}

}
