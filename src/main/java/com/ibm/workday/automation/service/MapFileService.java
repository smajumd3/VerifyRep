package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.MapFileDao;
import com.ibm.workday.automation.model.MapFile;

@Service("MapFileService")
public class MapFileService {
	
	@Autowired
	MapFileDao mapFileDao;
	
	@Transactional
	public List<MapFile> getMapFileList() {
		return mapFileDao.getMapFileList();
	}
	
	@Transactional
	public List<MapFile> getMapFileListByOeration(long operationId) {
		return mapFileDao.getMapFileListByOeration(operationId);
	}
	
	@Transactional
	public MapFile getMapFile(Long mapFileId) {
		return mapFileDao.getMapFile(mapFileId);
	}
	
	@Transactional
	public void addMapFile(MapFile mapFile) {
		mapFileDao.addMapFile(mapFile);
	}
	
	@Transactional
	public void updateMapFile(MapFile mapFile) {
		mapFileDao.updateMapFile(mapFile);
	}
	
	@Transactional
	public void deleteMapFile(Long mapFileId) {
		mapFileDao.deleteMapFile(mapFileId);
	}

}
