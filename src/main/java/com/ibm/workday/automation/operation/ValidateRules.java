package com.ibm.workday.automation.operation;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.model.Tenant;

public class ValidateRules implements CommonConstants, Runnable {

	private static final String EMAIL_REGULAR_EXPRESSION = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
	private static final String PHONE_NUMBER_REGULAR_EXPRESSION = "(\\d-)?(\\d{3}-)?\\d{3}-\\d{4}";

	private static final String FIELD_EMPTY = "Value is required but empty in the file";
	private static final String FIELD_LENGTH = "Field length cannot exceed $1 characters or digits";
	private static final String FIELD_NUMERIC = "Value - $1 should be in numeric";
	private static final String FIELD_DATE_FORMAT = "Invalid date format - $1 should be in format - $2";
	private static final String FIELD_EMAIL_FORMAT = "Incorrect e-mail address format";
	private static final String FIELD_PHONE_NUMBER = "Incorrect phone number format";
	private static final String NO_VALIDATION_MESSAGE = "No validation issue";
	private static final String FIELD_REFERENCE_ID = "Reference id $1 is not valid";
//	private static final String VALDATION_SCHEMA_BASE_URL = "application.validation.schema.baseURL";
	private static final String FIELD_FILE_EXISTENCE = "File $1 does not exist";

	private static final String SUBSTITUE_INDEX_ONE = "$1";
	private static final String SUBSTITUE_INDEX_TWO = "$2";

	private static final String VALIDATION_ERROR = "Validation Error";
	private static final String ERROR = "Error";

	private DataElement ruleRoot;
	private Map<String, FileData> data;
	private String type;
	private Tenant tenant;
	private Set<String> exclusionIdTypes;
	private Map<String, Set<String>> refIdTypes;
	private boolean complete;
	private String xmlFileStr;

	private List<Validate> validationMessageList;
	
	private LoadDataRules loadDataRules;

	public ValidateRules(LoadDataRules loadDataRules2, Operation operation, Tenant tenant2) {
		super();
	}

	public ValidateRules(LoadDataRules loadDataRules, Operation operation, Tenant tenant, String excRefStr, String xmlFileStr) {
		this.loadDataRules = loadDataRules;
		this.ruleRoot = loadDataRules.getDataRules(operation, false);
		this.data = loadDataRules.getFileDataMap(operation, ruleRoot.getRule());
        this.type  = ruleRoot.getRule().getFileType();
        this.tenant = tenant;
        this.xmlFileStr = xmlFileStr;
        
		this.exclusionIdTypes = getDelimitedProperties(excRefStr, DELIMITER_SEMI_COLON);//EXCLUSION_REFERENCE_ID_PROP
		validationMessageList = new ArrayList<>();
}

	private void validateRulesData() {

		ProcessRules processedData = new ProcessRules(data, type, true, true);
		if (tenant != null) {
			LoadReferenceID refId = new LoadReferenceID(loadDataRules, ruleRoot, tenant, exclusionIdTypes, xmlFileStr);
			refIdTypes = refId.getReferenceIds();
			System.out.println(refIdTypes.toString());
		}
		List<DataElement> dataElements = null;
		if (ruleRoot.getRule().isMultiple()) {
			dataElements = processedData.populateElements(ruleRoot);
		} else {
			dataElements = new ArrayList<>();
			dataElements.add(processedData.populateElement(ruleRoot));
		}
		Map<String, Validate> validationMessages = new LinkedHashMap<String, Validate>();
		
		String reqFields = "National ID Type Code";
		String dateFormat = "yyyy-MM-dd-hh:mm";
		String dateField = "Issued Date";
		String phones = null;
		String emails = null;

		validateInput(dataElements, validationMessages, reqFields, dateFormat, phones, emails, dateField);
		validationMessageList.addAll(validationMessages.values());
	}

	private void validateInput(List<DataElement> children,
			final Map<String, Validate> validationMessages, String reqFields, String dateFormat, String phones, String emails, String dateField) {

		for (DataElement aDataElement : children) 
		{
			DataRule aDataRule = aDataElement.getRule();
			String fileName = aDataRule.getFileName();
			if (fileName == null) 
			{
				System.out.println("File name is null");
				return;
			}
			Validate currentFileValidation = validationMessages.get(fileName);

			if (currentFileValidation == null) 
			{
				currentFileValidation = new Validate();
				currentFileValidation.setFileName(fileName);
				currentFileValidation.setValid(true);
				currentFileValidation.setValidString("Yes");
				validationMessages.put(fileName, currentFileValidation);
			}
			if (aDataRule.isMultiple())
			{
				if (!aDataElement.getChildren().isEmpty()) 
				{
					validateInput(aDataElement.getChildren(), validationMessages, reqFields, dateFormat, phones, emails, dateField);
				} 
				else 
				{
					validateData(aDataElement, currentFileValidation, reqFields, dateFormat, phones, emails, dateField);
				}
			} 
			else 
			{
				validateData(aDataElement, currentFileValidation, reqFields, dateFormat, phones, emails, dateField);
				if (!aDataElement.getChildren().isEmpty()) 
				{
					validateInput(aDataElement.getChildren(), validationMessages, reqFields, dateFormat, phones, emails, dateField);
				}
			}
		}
	}

	private void validateData(DataElement dataElement, final Validate validation, String reqFields, String dateFormat, String phones, String emails, String dateField) {
		
		boolean isGood = true;
		boolean isValidDate = false;
		boolean isReq = false;
		boolean isReqDate = false;
		DataRule aDataRule = dataElement.getRule();
		
		System.out.println("Name:" + dataElement.getFieldName());
		System.out.println("Value:" + dataElement.getValue());
		
		if(reqFields != null)
		{
			isReq = checkRequiredFieldExist(reqFields,dataElement.getFieldName());
		}
		 
		if(isReq)	
		{
			if (dataElement.getValue() == null || dataElement.getValue().isEmpty()) 
			{
				addValidationError(validation, aDataRule.getColumnName(),FIELD_EMPTY, dataElement.getIdentifierValue());
				isGood = false;
			}
			else 
			{
				isGood = true;
			}
		}
		
		if (aDataRule.getFieldLength() > -1) {
			if (dataElement.getValue().length() > aDataRule.getFieldLength() && !dataElement.getRule().isStripOffBeyondMaxLength()) 
			{
				String message = FIELD_LENGTH;
				addValidationError(validation, dataElement.getRule().getColumnName(), message.replace(SUBSTITUE_INDEX_ONE, aDataRule.getFieldLength() + ""), dataElement.getIdentifierValue());
				isGood = false;
			} 
			else 
			{
				isGood = true;
			}
		}

		if (dataElement.getValue() != null && aDataRule.isNumericOnly()) 
		{
			try 
			{
				Double.parseDouble(dataElement.getValue());
				isGood = true;
			} 
			catch (NumberFormatException e) 
			{
				String message = FIELD_NUMERIC;
				addValidationError(validation, dataElement.getRule().getColumnName(), message.replace(SUBSTITUE_INDEX_ONE, dataElement.getValue()), dataElement.getIdentifierValue());
				isGood = false;
			}
		}
		
		if(dateField != null)
		{
			isReqDate = checkDateFieldExist(dateField,dataElement.getFieldName());
		}
		if(isReqDate)
		{
			isValidDate = isValidFormat(dateFormat, dataElement.getValue().trim());
			if(isValidDate)
			{
				isGood = true;
			}
			else
			{
				String columnName = dataElement.getRule().getColumnName();
				if(columnName != null)
				{
					String message = FIELD_DATE_FORMAT; 
					addValidationError(validation, dataElement.getRule().getColumnName(),message.replace(SUBSTITUE_INDEX_ONE,dataElement.getValue()).replace(SUBSTITUE_INDEX_TWO,dateFormat),dataElement.getIdentifierValue()); 
					isGood = false;
				}
			}
		}
		
		boolean isEmail = checkEmailFieldExist(emails,dataElement.getFieldName());
		if (isEmail) 
		{
			isGood = dataElement.getValue().matches(EMAIL_REGULAR_EXPRESSION);
			if (!isGood) 
			{
				addValidationError(validation, dataElement.getRule().getColumnName(), FIELD_EMAIL_FORMAT, dataElement.getIdentifierValue());
			}
		}

		boolean isPhNo = checkPhoneNoFieldExist(phones,dataElement.getFieldName());
		if (isPhNo) 
		{
			isGood = dataElement.getValue().matches(PHONE_NUMBER_REGULAR_EXPRESSION);
			if (!isGood) 
			{
				addValidationError(validation, dataElement.getRule().getColumnName(), FIELD_PHONE_NUMBER, dataElement.getIdentifierValue());
			}
		}

		if (dataElement.getValue() != null && aDataRule.isCheckFileExistence()) {
			isGood = new File(dataElement.getValue()).exists();
			if (!isGood) 
			{
				String message = FIELD_FILE_EXISTENCE;
				message = message.replace(SUBSTITUE_INDEX_ONE, dataElement.getValue());
				addValidationError(validation, dataElement.getRule().getColumnName(), message, dataElement.getIdentifierValue());
			}
		}
		
		if (refIdTypes != null && !refIdTypes.isEmpty()) 
		{
			if (!dataElement.getRule().isRequired() && (dataElement.getValue() == null || dataElement.getValue().isEmpty())) 
			{
				// Do nothing
			} 
			else 
			{
				String refIdTypeName = null;
				for (DataAttribute da : dataElement.getAttributes()) {
					if (da.getName().equals(REFERENCE_ID_TYPE)) 
					{
						refIdTypeName = da.getValue();
						break;
					}
				}
				if (refIdTypeName != null && !exclusionIdTypes.contains(refIdTypeName)) 
				{
					Set<String> refIdValues = refIdTypes.get(refIdTypeName);
					if (refIdValues != null) 
					{
						if (!isValidRefId(refIdValues, dataElement.getValue())) 
						{
							String message = FIELD_REFERENCE_ID;
							addValidationError(validation, dataElement.getRule().getColumnName(), message.replace(SUBSTITUE_INDEX_ONE, dataElement.getValue() + " for - "+ refIdTypeName),
									dataElement.getIdentifierValue());
						}
					}
				}
			}
		}
		
		if (isGood) 
		{
			if (validation.getMessages().isEmpty()) 
			{
				validation.setGeneralMessage(NO_VALIDATION_MESSAGE);
			}
		}
	}
	
	private boolean checkEmailFieldExist(String emails, String name) {

		boolean isExist = false;
		if(emails != null && name != null)
		{
			String [] emailField = emails.split(";");
			String csvField = name.replace("$", "");
			for(int i = 0;i<emailField.length;i++)
			{
				if(emailField[i].equals(csvField))
				{
					isExist = true;
					break;
				}
			}
		}
		return isExist;		
	}

	private boolean checkPhoneNoFieldExist(String phones, String name) {
		
		boolean isExist = false;
		if(phones != null && name != null)
		{
			String [] phField = phones.split(";");
			String csvField = name.replace("$", "");
			for(int i = 0;i<phField.length;i++)
			{
				if(phField[i].equals(csvField))
				{
					isExist = true;
					break;
				}
			}
		}
		return isExist;		
	}

	private boolean checkRequiredFieldExist(String reqFields, String name) {
		
		boolean isExist = false;
		if(reqFields != null && name != null)
		{
			String [] reqField = reqFields.split(";");
			String csvField = name.replace("$", "");
			for(int i = 0;i<reqField.length;i++)
			{
				if(reqField[i].equals(csvField))
				{
					isExist = true;
					break;
				}
			}
		}
		return isExist;		
	}
	
	private boolean checkDateFieldExist(String dateFields, String name) {
		
		boolean isExist = false;
		if(dateFields != null && name != null)
		{
			String [] reqField = dateFields.split(";");
			String csvField = name.replace("$", "");
			for(int i = 0;i<reqField.length;i++)
			{
				if(reqField[i].equals(csvField))
				{
					isExist = true;
					break;
				}
			}
		}
		return isExist;		
	}

	private boolean isValidFormat(String format, String value) {
		
		boolean isValid = false;
		if(format != null)
		{
	        try 
	        {
	        	SimpleDateFormat sdf = new SimpleDateFormat(format.trim());
	            Date date = sdf.parse(value);
	            if (value.equals(sdf.format(date))) 
	            {
	                isValid = true;
	            }
	        } 
	        catch (ParseException ex) 
	        {
	
	        }
		}
        return isValid;
    }


	private void addValidationError(Validate validation, String colName, String message, String value) {
		validation.setValid(false);
		validation.setValidString("No");
		validation.setGeneralMessage(VALIDATION_ERROR);
		ValidationMessage validationMessage = new ValidationMessage();
		validationMessage.setType(ERROR);
		validationMessage.setColumnName(colName.substring(1));
		validationMessage.setMessage(message);
		validationMessage.setValue(value);
		validation.addMessage(validationMessage);
	}

	public void execute() {
		try {
			validateRulesData();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		complete = true;
	}

	public boolean isComplete() {
		return complete;
	}

	public List<Validate> getValidationMessages() {
		return validationMessageList;
	}

	public boolean isValidRefId(Set<String> refIdValues, String id) {
		boolean isValid = false;
		for (String refId : refIdValues) {
			if (id.equalsIgnoreCase(refId))
				return true;
		}
		return isValid;
	}
	
	public Set<String> getDelimitedProperties(String excludeValues, String delimiter) {
		Set<String> props = new HashSet<>();
		StringTokenizer tokens = new StringTokenizer(excludeValues, delimiter);
		while (tokens.hasMoreTokens()) {
			String value = tokens.nextToken();
			if (value != null && !value.isEmpty()) {
				props.add(value);
			}
		}
		return props;
	}

	@Override
	public void run() {
		try {
			validateRulesData();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		complete = true;
	}

}
