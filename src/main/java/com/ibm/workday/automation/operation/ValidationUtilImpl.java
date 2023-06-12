package com.ibm.workday.automation.operation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.workday.automation.model.MapFile;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.service.MapFileService;

@Component
public class ValidationUtilImpl implements ValidationUtil {
	
	@Autowired
	LoadDataRules loadDataRules;
	
	@Autowired
	MapFileService mapFileService;
	
	@Autowired
	LoadCSVFile csvFile;
	
	@Override
	public boolean areAllFilesMapped(Operation operation) {
		Set<String> ruleFiles = getFileNames(operation);
		List<MapFile> mapFileList = mapFileService.getMapFileListByOeration(operation.getOperationId());
	
		boolean countMatch = (ruleFiles.size() == mapFileList.size());		
	
		return countMatch;	
	}

	@Override
	public Map<String, List<String>> checkFileColumnExistence(Operation operation) {
		Map<String, List<String>> mappings = new LinkedHashMap<>();
		Map<String, Set<String>> ruleColLabelsMap = getRuleColumnLabels(operation);
		Map<String, List<String>> fileColLabelsMap = getFileColumnLabels(operation);
		for (String fileName : ruleColLabelsMap.keySet()) {
			Set<String> ruleCols = ruleColLabelsMap.get(fileName);
			List<String> fileCols = fileColLabelsMap.get(fileName);
			ruleCols.removeAll(fileCols);

			if (!ruleCols.isEmpty()) {
				mappings.put(fileName, new ArrayList<>(ruleCols));
			}
		}
		return mappings;
	}
	
	@Override
	public Map<String, List<String>> checkFileColumnForDuplicates(Operation operation) {
		Map<String, List<String>> duplicateCols = new LinkedHashMap<>();
		Map<String, Set<String>> ruleColLabelsMap = getRuleColumnLabels(operation);
		Map<String, List<String>> fileColLabelsMap = getFileColumnLabels(operation);
		for (String fileName : ruleColLabelsMap.keySet()) {
			Set<String> ruleCols = ruleColLabelsMap.get(fileName);
			List<String> fileCols = fileColLabelsMap.get(fileName);
			fileCols.retainAll(ruleCols);

			if (!fileCols.isEmpty()) {
				Set<String> dupCols = getDuplicateCols(fileCols);
				if (!dupCols.isEmpty()) {
					duplicateCols.put(fileName, new ArrayList<>(dupCols));
				}
			}
		}
		return duplicateCols;
	}
	
	@Override
	public Map<String, List<String>> checkFileForUniqueId(Operation operation) {
		Map<String, List<String>> UniqueIdMissingMap = new LinkedHashMap<>();
		Map<String, List<String>> fileColLabelsMap = getFileColumnLabels(operation);
		DataElement rootElement = loadDataRules.getDataRules(operation, false);

		String UniqueIdCol = rootElement != null && rootElement.getRule() != null
				&& rootElement.getRule().getUniqueID() != null ? rootElement
				.getRule().getUniqueID() : null;
		if (UniqueIdCol != null) {
			for (String fileName : fileColLabelsMap.keySet()) {
				List<String> cols = fileColLabelsMap.get(fileName);
				if (UniqueIdCol.startsWith("$")) {
					UniqueIdCol = UniqueIdCol.substring(1);
				}
				if (!cols.contains(UniqueIdCol)) {
					List<String> aList = new ArrayList<>();
					aList.add(UniqueIdCol);
					UniqueIdMissingMap.put(fileName, aList);
				}
			}
		}
		return UniqueIdMissingMap;
	}
	
	private static Set<String> getDuplicateCols(List<String> cols) {
		Set<String> dupCols = new LinkedHashSet<>();
		List<String> cols2 = new ArrayList<>(cols);
		
		for (String col : cols) {
			int counter = 0;
			for (String col1 : cols2) {
				if (col1.equals(col))
					counter++;
			}
			if (counter > 1) {
				dupCols.add(col);
			}
		}

		return dupCols;
	}
	
	private Map<String, Set<String>> getRuleColumnLabels(Operation operation) {
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		final Map<String, Set<String>> colLabelsMap = new LinkedHashMap<>();
		updateColLabels(dataRoot, colLabelsMap);
		return colLabelsMap;
	}
	
	private Map<String, List<String>> getFileColumnLabels(Operation operation) {
		Map<String, List<String>> fileColLabels = new LinkedHashMap<>();
		List<MapFile> mapFileList = mapFileService.getMapFileListByOeration(operation.getOperationId());
		
		if(mapFileList != null) {
			for(MapFile mapFile : mapFileList ) {
				InputStream in = new ByteArrayInputStream(mapFile.getMapFileData());
				InputStream iStream = new BufferedInputStream(in);
				List<String> cols = csvFile.getFileColumnLabels(iStream);
				fileColLabels.put(mapFile.getFileName(), new ArrayList<>(cols));
			}
		}


		return fileColLabels;
	}
	
	private static void updateColLabels(DataElement rootElement,
			                            final Map<String, Set<String>> colLabelsMaps) {

		String fileName = rootElement.getRule() != null ? rootElement.getRule()
				.getFileName() : "";
		String colName = rootElement.getRule() != null ? rootElement.getRule()
				.getColumnName() : "";
		if (!"".equals(fileName)) {
			Set<String> colLabels = colLabelsMaps.get(fileName);
			if (colLabels == null) {
				colLabels = new LinkedHashSet<>();
				colLabelsMaps.put(fileName, colLabels);
			}
			if (colName != null && !"".equals(colName)) {
				if (colName.startsWith("$")) {
					colName = colName.substring(1);
				}
				colLabels.add(colName);
			}
			if (rootElement.getChildren() != null
					&& rootElement.getChildren().size() > 0) {
				for (DataElement child : rootElement.getChildren()) {
					updateColLabels(child, colLabelsMaps);
				}
			}
		}
	}
	
	private Set<String> getFileNames(Operation operation) {
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		return getFileNames(dataRoot);
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
