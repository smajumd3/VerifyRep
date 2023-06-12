package com.ibm.workday.automation.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.BuildRuleFile;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.service.BuildRuleFileService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class BuildRuleFileController implements CommonConstants {
	
	@Autowired
	BuildRuleFileService buildRuleFileService;
	
	@Autowired
	UserService userService;
	
	@RequestMapping(value = "/getBuildRuleFileList", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<BuildRuleFile> getBuildRuleFileList() {
		return buildRuleFileService.getBuildFileList();
	}
	
	@RequestMapping(value = "/getBuildRuleFile/{fileId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public BuildRuleFile getBuildFileById(@PathVariable("fileId") Long fileId) {
		return buildRuleFileService.getBuildFileById(fileId);
	}
	
	@RequestMapping(value = "/getBuildRuleFilesByUserId", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<BuildRuleFile> getBuildFilesByUser(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		return buildRuleFileService.getBuildFilesByUser(userId);
	}
	
	@RequestMapping(value = "/getBuildRuleFilesByClient", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<BuildRuleFile> getBuildFilesByClient(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		return buildRuleFileService.getBuildFilesByClient(user.getClient());
	}
	
	@RequestMapping(value = "/getSuperUserBuildRuleFiles", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<BuildRuleFile> getSuperUserBuildRuleFiles() {
		return buildRuleFileService.getBuildFilesByClient(SUPER_USER_CLIENT);
	}
	
	@RequestMapping(value = "saveBuildFileData/{fileName}", method = RequestMethod.POST)
	public void saveBuildFileData(@PathVariable("fileName")String fileName, 
			                 @RequestParam("selectedBuildFile") MultipartFile selectedBuildFile,
			                 HttpSession httpSession) {
		saveFileData(fileName, selectedBuildFile, httpSession);
	}
	
	@RequestMapping(value = "saveBuildRuleFile/{fileName}", method = RequestMethod.POST)
	public void saveFileData(@PathVariable("fileName")String fileName, 
			                 @RequestParam("selectedFile") MultipartFile selectedFile,
			                 HttpSession httpSession) {
		byte[] fileData = null;

		System.out.println(selectedFile.getContentType());
		
		try {
			fileData = selectedFile.getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		
		BuildRuleFile newFile = new BuildRuleFile();
		newFile.setFileName(fileName);
		newFile.setFileLink(selectedFile.getOriginalFilename());
		newFile.setFileData(fileData);
		newFile.setUserId(userId);
		newFile.setClient(user.getClient());
		buildRuleFileService.addBuildFile(newFile);
	}
	
	@RequestMapping(value = "/deleteBuildRuleFile/{fileId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteSavedFile(@PathVariable("fileId") Long fileId) {
		buildRuleFileService.deleteBuildFile(fileId);
	}
	
	@RequestMapping(value = "/downloadBuildRuleFile/{fileId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void downloadBuildRuleFile(@PathVariable("fileId") Long fileId, HttpServletResponse response) {
		BuildRuleFile file = buildRuleFileService.getBuildFileById(fileId);
		response.setHeader("Content-Disposition", "attachment;filename=" + file.getFileLink() + "");
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		try (ByteArrayInputStream bis = new ByteArrayInputStream(file.getFileData())) {
			IOUtils.copy(bis, response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
