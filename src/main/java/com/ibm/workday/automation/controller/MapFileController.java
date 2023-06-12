package com.ibm.workday.automation.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.MapFile;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.operation.DataElement;
import com.ibm.workday.automation.operation.LoadDataRules;
import com.ibm.workday.automation.service.MapFileService;
import com.ibm.workday.automation.service.OperationService;

import au.com.bytecode.opencsv.CSVReader;

@RestController
public class MapFileController implements CommonConstants {
	
	Operation operation;
	
	@Autowired
	MapFileService mapFileService;
	
	@Autowired
	OperationService operationService;
	
	@Autowired
	LoadDataRules loadDataRules;
	
	@RequestMapping(value = "getMapFileNames/{operationName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public Set<String> getMapFileNames(@PathVariable("operationName") String operationName, HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		operation = operationService.getOperation(operationName, userId);
		
		if(operation == null) {
			return new TreeSet<>();
		}
		
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		Set<String> fileNames = getFileNames(dataRoot);
		return fileNames;
	}
	
	@RequestMapping(value = "getMapFiles/{operationId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public Set<String> getMapFileNames(@PathVariable("operationId") Long operationId) {
		operation = operationService.getOperation(operationId);		
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		Set<String> fileNames = getFileNames(dataRoot);
		return fileNames;
	}
	
	@RequestMapping(value = "getMapFileList", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<MapFile> getMapFileList() {
		return mapFileService.getMapFileList();
	}
	
	@RequestMapping(value = "getMapFilesByOperation", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<MapFile> getMapFilesByOperation() {
		return mapFileService.getMapFileListByOeration(operation.getOperationId());
	}
	
	@RequestMapping(value = "saveMapFileData/{fileName}", method = RequestMethod.POST)
	public void saveMapFileData(@PathVariable("fileName")String fileName, @RequestParam("mapFile") MultipartFile mapFile) {
		byte[] mapFileData = null;
		String inputFileName = mapFile.getOriginalFilename();
		try 
		{
			if(inputFileName.endsWith(".csv"))
			{
				mapFileData = mapFile.getBytes();
			}
			else
			{
				InputStream in = mapFile.getInputStream();
				File sourceExcelFile = File.createTempFile(inputFileName.substring(0, inputFileName.indexOf(".")), ".xlsx");
			    String excelPath = sourceExcelFile.getAbsolutePath();
			    FileOutputStream f = new FileOutputStream(excelPath);
			    int ch = 0;
			    while ((ch = in.read()) != -1) 
			    {
			        f.write(ch);
			    }
			    f.flush();
			    f.close();
			    
			    InputStream inp = new FileInputStream(excelPath);
			    Workbook wb = WorkbookFactory.create(inp);
			    String csvStr = convertExcelToCSV(wb.getSheetAt(4), inputFileName); 
			    mapFileData = csvStr.getBytes();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		if(operation == null) {
			return;
		}
		
		List<MapFile> mapFiles = getMapFilesByOperation();
		
		if(mapFiles == null || mapFiles.isEmpty()) {
			MapFile file = new MapFile();
			file.setFileName(fileName);
			file.setFileLink(mapFile.getOriginalFilename());
			file.setFilePath("");
			file.setMapFileData(mapFileData);
			file.setOperationId(operation.getOperationId());
			mapFileService.addMapFile(file);
		} else {
			boolean found = false;
			for(MapFile file : mapFiles) {
				if(file.getFileName().equals(fileName)) {
					found = true;
					file.setFilePath("");
					file.setFileLink(mapFile.getOriginalFilename());
					file.setMapFileData(mapFileData);
					file.setOperationId(operation.getOperationId());
					mapFileService.updateMapFile(file);
					break;
				}
			}
			if(!found) {
				MapFile file = new MapFile();
				file.setFileName(fileName);
				file.setFileLink(mapFile.getOriginalFilename());
				file.setFilePath("");
				file.setMapFileData(mapFileData);
				file.setOperationId(operation.getOperationId());
				mapFileService.addMapFile(file);
			}
		}
	}
	
	private String convertExcelToCSV(Sheet sheet, String fileName) {
		
		StringBuilder data = new StringBuilder();
        String[] nextRecord = null;
		String cellValue = "";
		String csvValue = "";
        try 
        {
    		File sourceCsvFile = File.createTempFile(fileName.substring(fileName.indexOf("_")+1, fileName.indexOf(".")), ".csv");
    		int maxNumOfCells = sheet.getRow(1).getLastCellNum();//change made=0
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) 
            {
            	Row row = rowIterator.next();
            	if(row.getRowNum() >=10)//change made
            	{
	                for( int cellCounter = 1 ; cellCounter < maxNumOfCells; cellCounter ++)//change made=0
	                {
	                	Cell cell = null;
	                    if( row.getCell(cellCounter) == null)
	                    {
	                    	data.append(",");
	                    } 
	                    else 
	                    {
	                        cell = row.getCell(cellCounter);
	                        if(cell.getCellType().toString().equals("STRING"))
	                        {                    	
	                        	data.append(cell.getStringCellValue());
	                        }
	                        else if(cell.getCellType().toString().equals("NUMERIC"))
	                        {
	                        	if(String.valueOf(cell.getNumericCellValue()).contains("E"))
	                        	{
	                        		Object obj = cell.getNumericCellValue();
		                        	data.append(new BigDecimal(obj.toString()).toPlainString());
	                        	}
	                        	else if(String.valueOf(cell.getNumericCellValue()).contains(".0") && !String.valueOf(cell.getNumericCellValue()).contains("E"))
	                        	{
	                        		data.append(String.valueOf(cell.getNumericCellValue()).substring(0, String.valueOf(cell.getNumericCellValue()).indexOf(".")));
	                        	}
	                        	else
	                        	{
	                        		data.append(String.valueOf(cell.getNumericCellValue()));
	                        	}
	                        }
	                        else if(cell.getCellType().toString().equals("BOOLEAN"))
	                        {
	                        	data.append(cell.getBooleanCellValue());
	                        }
	                        data.append(",");
	                    }                    
	                }
	                data.append('\n');
            	}
            }

            Files.write(Paths.get(sourceCsvFile.getAbsolutePath()), data.toString().getBytes("UTF-8"));
            
			FileReader filereader = new FileReader(sourceCsvFile.getAbsolutePath());
			CSVReader csvReader = new CSVReader(filereader); 
            while ((nextRecord = csvReader.readNext()) != null) 
            {
            	cellValue = "";
                for (String cell : nextRecord) 
                {
                	if(cellValue.equals(""))
                	{
                		cellValue = cell;
                	}
                	else
                	{
                		cellValue = cellValue + "," + cell;
                	}	                	 
                }	                
            	if(csvValue.equals(""))
            	{
            		csvValue = cellValue;           		
            	}
            	else
            	{
            		csvValue = csvValue + "\n" + cellValue; 
            	}
            }
            csvReader.close();
        } 
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();	
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
		return csvValue;
		
	}
	
	@RequestMapping(value = "deleteMapFile/{mapFileId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteOperation(@PathVariable("mapFileId") Long mapFileId) {
		mapFileService.deleteMapFile(mapFileId);
	}
	
	@RequestMapping(value = "/downloadMappedFile/{mapFileId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void downloadMappedFile(@PathVariable("mapFileId") Long mapFileId, HttpServletResponse response) {
		MapFile mapFile = mapFileService.getMapFile(mapFileId);
		response.setHeader("Content-Disposition", "attachment;filename=" + mapFile.getFileLink() + "");
		response.setContentType("text/csv");
		try (ByteArrayInputStream bis = new ByteArrayInputStream(mapFile.getMapFileData())) {
			IOUtils.copy(bis, response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Set<String> getFileNames(DataElement rootElement) {
		Set<String> fileNames = new TreeSet<>();
		if(rootElement != null) {
		    String fileName = rootElement.getRule() != null ? rootElement.getRule()
				    .getFileName() : "";
		    if (fileName != null && !fileName.equalsIgnoreCase("")) {
			    fileNames.add(fileName);
		    }
		    if (rootElement.getChildren() != null) {
			    for (DataElement children : rootElement.getChildren()) {
				    fileNames.addAll(getFileNames(children));
			    }
		    }
		}
		return fileNames;
	}

}
