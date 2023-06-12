package com.ibm.workday.automation.operation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

//import com.ibm.wd.conversion.tool.dao.transfer.WSResponseDO;
//import com.ibm.wd.conversion.tool.dao.transfer.WSXPathExpressionDO;
import com.ibm.workday.automation.common.CommonConstants;

public class ProcessBatchRequest implements Runnable, CommonConstants {

	ResponseDataUtil responseData;
	private volatile boolean exit;
	private Map<String, SOAPMessage> soapMessages;
	private WSXPathExpression expression;
	private String endPoint;
	private int batchRequestSize;
	private List<WSResponse> responseMessages = new ArrayList<>();

	private Map<String, List<SendWWSRequest>> requestBatch;
	private Map<String, List<WSResponse>> statusMessages;
	private int batchCount;
	private List<SOAPConnection> connections;
	private long delay;
	private SOAPConnectionFactory soapConnectionFactory = null;

	public ProcessBatchRequest(Map<String, SOAPMessage> soapMessages,
			                   ResponseDataUtil responseData,
			                   WSXPathExpression expression, String endPoint,
			                   int batchRequestSize, long delay) {
		super();
		this.exit = false;
		this.soapMessages = soapMessages;
		this.responseData = responseData;
		this.expression = expression;
		this.endPoint = endPoint;
		this.batchRequestSize = batchRequestSize;
		this.delay = delay;
		requestBatch = new HashMap<>();
		statusMessages = new HashMap<>();
		
		try {
			soapConnectionFactory = SOAPConnectionFactory.newInstance();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		initialize();
	}

	@Override
	public void run() {
		try {
			runBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initialize() {
		long startTime = System.currentTimeMillis();

		batchCount = getBatchCount(soapMessages.size(), batchRequestSize);
		responseData.setBatchCount(batchCount);
		responseData.setTotalSoapExecutionPending(soapMessages.size());
		
		int totalConnections = batchRequestSize > soapMessages.size() ? 
				               soapMessages.size() : batchRequestSize;
		connections = getSOAPConnections(totalConnections);// Do not create too many connections

		final List<WSResponse> wsResponse = new ArrayList<>();

		List<String> messageKeys = new ArrayList<>(soapMessages.keySet());	// need index level access
		int previousIndex = 0;
		for (int currentBatch = 1; currentBatch <= batchCount; currentBatch++) {	// Form batches
			String batchName = BATCH_NAME_PREFIX + currentBatch;
			List<SendWWSRequest> batch = new ArrayList<>();
			requestBatch.put(batchName, batch);

			List<WSResponse> statuses = new ArrayList<>();
			statusMessages.put(batchName, statuses);

			for (int i = previousIndex, connectionIndex = 0; i < (currentBatch * batchRequestSize)
					&& i < messageKeys.size(); i++, connectionIndex++) {
				String messageID = messageKeys.get(i);
				WSResponse wsStatus = new WSResponse();
				wsStatus.setName(messageID);
				wsStatus.setResult("Initialized");
				wsStatus.setMessage("Waiting for submission to Workday!..");
				wsStatus.setRequestDateTime(new Date(System.currentTimeMillis()));
				wsStatus.setComplete(false);
				wsStatus.setBatchNo(currentBatch);
				wsResponse.add(wsStatus);
				SOAPMessage aSOAPMessage = soapMessages.get(messageID);
				SOAPConnection aSOAPConnection = connections.get(connectionIndex);
				SendWWSRequest threadRequest = new SendWWSRequest(messageID, aSOAPConnection, aSOAPMessage,
						                                          responseData, endPoint, expression, wsStatus);
				batch.add(threadRequest);

				statuses.add(wsStatus);
			}
			responseMessages.addAll(statuses);
			previousIndex = currentBatch * batchRequestSize;
		}
		System.out.println("Total time to initialize : "
				+ (System.currentTimeMillis() - startTime));
	}

	private void runBatch() {

		for (int batchNo = 1; batchNo <= batchCount; batchNo++) {
			if(exit) {
				responseData.setTotalSoapExecutionPending(0);
				return;
			}
			String currentBatch = BATCH_NAME_PREFIX + batchNo;
			if (batchNo > 1) {// check previous batch completed
				String batchName = BATCH_NAME_PREFIX + (batchNo - 1);
				List<WSResponse> responses = statusMessages.get(batchName);
				while (!isBatchComplete(responses)) { // wait indefinitely until all previous one completes, Do nothing
				}
				List<SendWWSRequest> timeouts = getTimeOutRequests(requestBatch
						.get(batchName));
				if (timeouts.size() > 0) {
					System.out.println("Found timeouts and re-sending again");
					// submitRequest(timeouts);
				}
				// Check any timeout errors in previous batch and submit those only.
			}
			submitRequest(requestBatch.get(currentBatch), currentBatch);
		}
	}

	private boolean isBatchComplete(List<WSResponse> responses) {
		for (WSResponse response : responses) {
			if (!response.isComplete())
				return false;
		}
		return true;
	}

	private int getBatchCount(int messagesSize, int batchRequestSize) {
		int batchCount = 1;// Default - Single thread to handle all the requests
		if (batchRequestSize > 0) {
			if (messagesSize <= batchRequestSize) {
				batchCount = 1;
			} else {
				batchCount = messagesSize / batchRequestSize;
				if ((messagesSize % batchRequestSize) > 0) {
					batchCount += 1;
				}
			}
		} else {
			batchCount = messagesSize;
		}
		return batchCount;
	}

	private List<SOAPConnection> getSOAPConnections(int size) {
		List<SOAPConnection> connections = new ArrayList<>();
		try {
			for (int i = 0; i < size; i++) {
				System.out.println("Creating SOAP connection");
				SOAPConnection soapConnection = soapConnectionFactory
						.createConnection();
				connections.add(soapConnection);
			}
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (SOAPException e) {
			e.printStackTrace();
		}

		return connections;
	}

	public List<WSResponse> getResponseMessages() {
		return responseMessages;
	}

	private void submitRequest(final List<SendWWSRequest> requests,
			String batchName) {
		ThreadGroup batchGroup = new ThreadGroup(batchName);

		for (SendWWSRequest request : requests) {
			if(exit) {
				responseData.setTotalSoapExecutionPending(0);
				return;
			}
			WSResponse responseStatus = request.getWsResponse();
			responseStatus.setResult("Submitted");
			responseStatus.setMessage("Waiting for response from Workday!..");
			responseStatus.setRunning(true);
			responseStatus.setComplete(false);
			responseData.setRunningbatchNumber(responseStatus.getBatchNo());
			new Thread(batchGroup, request).start();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {

			}
		}
	}

	private List<SendWWSRequest> getTimeOutRequests(
			final List<SendWWSRequest> requests) {
		List<SendWWSRequest> timeouts = new ArrayList<>();
		for (SendWWSRequest request : requests) {
			if (STATUS_TIMEOUT.equalsIgnoreCase(request.getWsResponse()
					.getStatus())) {
				timeouts.add(request);
			}
		}
		return timeouts;
	}
	
    public void stopRequest() {
        exit = true;
    }

	@Override
	protected void finalize() throws Throwable {
		for (SOAPConnection soapCon : connections) {
			try {
				// soapCon.close();
				System.out.println("Connection closed successfully!");
			} catch (Exception e) {
				System.err.println("Error while closing SOAP Connection");
			}
		}
	}

}
