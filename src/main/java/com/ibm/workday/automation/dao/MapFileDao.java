package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.MapFile;

public interface MapFileDao {
	
	List<MapFile> getMapFileList();
	
	List<MapFile> getMapFileListByOeration(Long operationId);
	
	MapFile getMapFile(Long mapFileId);
	
	void addMapFile(MapFile mapFile);

	void updateMapFile(MapFile mapFile);
	
	void deleteMapFile(Long mapFileId);

}
