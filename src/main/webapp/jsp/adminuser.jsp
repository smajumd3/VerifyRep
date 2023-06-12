<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />

<link rel="icon" href="../images/logo.png">
<title>Hyperloader</title>
	
<link rel="stylesheet" href="../css/tree-control-attribute.css">	
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/font-awesome.min.css">
<link rel="stylesheet" href="../css/angular-material.min.css">
<link rel="stylesheet" href="../css/main-theme.css">
<link rel="stylesheet" href="../css/jqx.base.css">
<link rel="stylesheet" href="../css/style.min.css" />

<script type="text/javascript" src="../js/messageResource-min.js"></script>
<script type="text/javascript" src="../js/jquery-3.3.1.min.js"></script>
<script src="../js/jstree.min.js"></script>
<script src="../js/jquery-1.12.4.min.js"></script>
<script src="../js/demos.js"></script>
<script src="../js/jqxcore.js"></script>
<script src="../js/jqxdata.js"></script>
<script src="../js/jqxbuttons.js"></script>
<script src="../js/jqxscrollbar.js"></script>
<script src="../js/jqxpanel.js"></script>
<script src="../js/jqxtree.js"></script>

<script type="text/javascript" src="../js/angular.js"></script>
<script type="text/javascript" src="../js/angular-animate.js"></script>
<script type="text/javascript" src="../js/angular-aria.min.js"></script>
<script type="text/javascript" src="../js/angular-messages.min.js"></script>
<script type="text/javascript" src="../js/angular-material.min.js"></script>
<script type="text/javascript" src="../js/ui-bootstrap-tpls-2.0.2.js"></script>
<script type="text/javascript" src="../js/Chart.min.js"></script>
<script type="text/javascript" src="../js/angular-chart.min.js"></script>
<script type="text/javascript" src="../js/moment.js"></script>
<script type="text/javascript" src="../js/admincontroller.js"></script>
<script type="text/javascript" src="../js/angular-sanitize.js"></script>
<script type="text/javascript" src="../js/d3.min.js"></script>
<script type="text/javascript" src="../js/ng-knob.min.js"></script>
<script type="text/javascript" src="../js/ng-knob.js"></script>
<script type="text/javascript" src="../js/angular-tree-control.js"></script>
<script type="text/javascript" src="../js/context-menu.js"></script>

<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.10/d3.min.js"></script>
</head>
<body data-ng-app="adminUser.app" class="mainBody">
    <div id="parentContainer" class="parentContainerStyle" data-ng-controller="adminUser.controller"  data-ng-init="init()">
    	<nav class="navbar navbar-inverse navbar-fixed-top">
			<div class="container-fluid">
				<div class="navbar-header">
					<a class="navbar-brand" href="javascript:void(0)"><span><img src="../images/ibm-logo.png" alt="IBM">Hyperloder</span></a>
				</div>
				<div id="navbar" class="navbar-collapse collapse">
					<ul class="nav navbar-nav navbar-right">
						<li>
						    <a href="logout" ><span class="glyphicon glyphicon-off"></span> Logout</a>
						</li>
					</ul>
				</div>
			</div>
		</nav>
		<div class="container-fluid">
		    <div class="row">
		        <div id="sidebarMenu" class="col-sm-2 sidebar">
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initUploadFile(container='PT')"  data-ng-class="container=='PT'?'selected':''">Project template</a>
						</li>
					</ul>
					<md-divider></md-divider>		        
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initProjectMember(container='UL')"  data-ng-class="container=='UL'?'selected':''">Member List</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initAppVersion(container='AV')"  data-ng-class="container=='AV'?'selected':''">Application Version</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initTenantMapping(container='TM')"  data-ng-class="container=='TM'?'selected':''">Tenant Mapping</a>
						</li>
					</ul>
					<md-divider></md-divider>					
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initRequestXML(container='RX')"  data-ng-class="container=='RX'?'selected':''">Request XML</a>
						</li>
					</ul>
					<md-divider></md-divider>					
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initReferenceXML(container='RFX')"  data-ng-class="container=='RFX'?'selected':''">Reference XML</a>
						</li>
					</ul>
					<md-divider></md-divider>					
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initExclusionReference(container='ER')"  data-ng-class="container=='ER'?'selected':''">Exclusion Reference</a>
						</li>
					</ul>								
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initBuildRuleFiles(container='BF')"  data-ng-class="container=='BF'?'selected':''">Build Rule Files</a>
						</li>
					</ul>
					<md-divider></md-divider>					
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="mainView" data-ng-click="initMainMenu(container='MM')"  data-ng-class="container=='MM'?'selected':''">Main Menu</a>
						</li>
					</ul>
					<md-divider></md-divider>
		        </div>
		        <div id="Content" class="col-sm-10 col-sm-offset-2">
		            <div data-ng-show="container" class="contentClass">
		                <div id="memberContent" data-ng-show="container=='UL'" class="row">
		                    <div class="statusPanel">
				                <form name="memberForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Create Member</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
									    <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Name</label>
							                <input required data-ng-model="member.userName">
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Email</label>
							                <input required data-ng-model="member.userEmail">
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Password</label>
							                <input type="password" required data-ng-model="member.userPassword">
							            </md-input-container>				                    
				                    </div>
									<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								        <md-button aria-label="" type="reset" class="btn" data-ng-click="resetMemberForm()"><span class="glyphicon glyphicon-repeat"></span>&nbsp;Clear</md-button>
								        <md-button aria-label="" type="submit" class="btn" data-ng-click="submitMemberForm()"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
								    </section>				                    
				                </form>		                    
		                    </div>
		                    <br></br>
				            <div class="statusPanel">
				                <h4 class="text-center pleaseWait">Member List</h4>
				                <div class="smallSpacer"></div>
				                <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-4">Name</th>
										    <th class="col-sm-4">Email</th>
										    <th class="col-sm-4">Action</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="member in members" >
											<td class="col-sm-4">{{ member.userName }}</td>
											<td class="col-sm-4">{{ member.userEmail }}</td>
											<td class="col-sm-4">
												 <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="editMember(member)"><span class="glyphicon glyphiconPlus glyphicon-edit"></span></md-button>
											</td>
										  </tr>
								    </tbody>				                
				                </table>
				            </div>		                    
		                </div>
		                <div id="appVersionContent" data-ng-show="container=='AV'" class="row">
		                    <div class="statusPanel">
		                        <form id="appVersionForm" name="appVersionForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Application Web Service Version</h4>                        
		                            <div layout-gt-sm="row">
									    <md-input-container flex="30" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Version</label>
								                <md-select class="labelStyle" required data-ng-model="selectedValue">
									                <md-option data-ng-repeat="appVersion in appVersions track by $index" data-ng-value="{{$index}}">
									                    {{ appVersion }}
									                </md-option>
									            </md-select>           
								        </md-input-container>
							            <md-input-container flex="30" class="md-block" flex-gt-sm>
									        <section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								                <md-button aria-label="" type="submit" class="btn" data-ng-click="submitAppVersionForm(selectedValue)"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
								            </section>
							            </md-input-container>
							        </div>
							        <div layout-gt-sm="row">
								        <div class="col-sm-3">
								            <div class="form-group">
								                <label class="control-label col-xs-3">Version: </label>
								                <label class="control-label col-xs-3">{{ savedVersion.version }}</label>
								            </div>
								        </div>							        
							        </div>
		                        </form>
		                    </div>
		                </div>
		                <div id="mappingContent" data-ng-show="container=='TM'" class="row">
		                    <div class="statusPanel">
		                        <form id="tenantMappingForm" name="tenantMappingForm" autocomplete="off">
		                            <h4 class="text-center pleaseWait">Tenant Mapping</h4>
		                            <div layout-gt-sm="row">
									    <md-input-container flex="30" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Load Cycle</label>
								                <md-select class="labelStyle" required data-ng-model="selectedValue1">
									                <md-option data-ng-repeat="page in pages track by $index" data-ng-value="{{$index}}">
									                    {{ page }}
									                </md-option>
									            </md-select>								            
								        </md-input-container>
									    <md-input-container flex="30" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Tenant</label>
								                <md-select class="labelStyle" required data-ng-model="selectedValue2">
									                <md-option data-ng-repeat="tenant in clientTenants track by $index" data-ng-value="{{$index}}">
									                    {{ tenant.tenantName }}
									                </md-option>
									            </md-select>								            
								        </md-input-container>								    
		                            </div>
							        <section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								        <md-button aria-label="" type="submit" class="btn" data-ng-click="submitTenantMappingForm(selectedValue1, selectedValue2)"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
								    </section>		                        
		                        </form>
		                    </div>
		                    <br></br>
		                    <div class="statusPanel">
								<table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-4">Load Cycle</th>
										    <th class="col-sm-5">Tenant</th>
										    <th class="col-sm-3">Action</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="map in mapList" >
											<td class="col-sm-4">{{ map.pageName }}</td>
											<td class="col-sm-5">{{ map.tenantName }}</td>
											<td class="col-sm-3">
                                                <md-button aria-label="delTenantMapping" class="md-icon-button md-warn" data-ng-click="deleteTenantMapping(map)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span>&nbsp;</md-button>
                                            </td>
										  </tr>
									  </tbody>
								</table>		                        
		                    </div>
		                </div>
                        <div id="projectTemplateContent" data-ng-show="container=='PT'" class="row">
                            <div class="statusPanel">
		                        <h4 class="text-center pleaseWait">Upload Project Template</h4>
				                <form id="fileUploadForm" name="fileUploadForm" autocomplete="off">
				                    <div layout-gt-sm="row">
								        <md-input-container flex="30" class="md-block" flex-gt-sm>
								            <label class="labelStyle">File Name</label>
								            <input required data-ng-model="fileEditable.fileName">
								        </md-input-container>
								        <md-input-container flex="50" class="md-block" flex-gt-sm>
										    <label  flex="20" class="labelStyle labelAddStyle" required>Select File</label>
								            <input type="file" class="btn btn-sm" id="selectedFileId" name="selectedFile" accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" data-ng-model="selectedFile" /> 
										</md-input-container>
										<md-input-container flex="20" class="md-block" flex-gt-sm>
										    <md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-transfer"></span>&nbsp;Upload&nbsp;</md-button>
								        </md-input-container>
								    </div>
				                </form>	                            
                            </div>
                            <br></br>
                            <div class="statusPanel">
		                        <h4 class="text-center pleaseWait">File</h4>
				                <table class="table statusTable tenantStatus">
								    <thead>
									    <tr>
									        <th class="col-sm-5">File Name</th>
										    <th class="col-sm-5">File Link</th>
										    <th class="col-sm-2">Action</th>
									    </tr>
									</thead>
									<tbody>
									    <tr>
										    <td class="col-sm-5">{{ file.fileName }}</td>
										    <td class="col-sm-5">
				                                <a href="javascript:void(0)" data-ng-click="downloadSavedFile(file)">
				                                    {{file.fileLink}}
				                                </a>											  
										    </td>
										    <td class="col-sm-2">
										        <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteSavedFile(file)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span>&nbsp;</md-button>
										    </td>
									    </tr>
								    </tbody>
							    </table>
                            </div>                            
                        </div>
		                <div id="BuildRuleFileContent" data-ng-show="container=='BF'" class="row">
		                    <div class="statusPanel">
		                        <h4 class="text-center pleaseWait">Upload Build Rule File</h4>
				                <form id="buildFileUploadForm" name="buildFileUploadForm" autocomplete="off">
				                    <div layout-gt-sm="row">
								        <md-input-container flex="30" class="md-block" flex-gt-sm>
								            <label class="labelStyle">File Name</label>
								            <input required data-ng-model="buildFileEditable.fileName">
								        </md-input-container>
								        <md-input-container flex="50" class="md-block" flex-gt-sm>
										    <label  flex="20" class="labelStyle labelAddStyle" required>Select File</label>
								            <input type="file" class="btn btn-sm" id="selectedBuildFileId" name="selectedBuildFile" accept=".xml" data-ng-model="selectedFile" /> 
										</md-input-container>
										<md-input-container flex="20" class="md-block" flex-gt-sm>
										    <md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-transfer"></span>&nbsp;Upload Build File&nbsp;</md-button>
								        </md-input-container>
								    </div>
				                </form>		                        
		                    </div>
		                    <br></br>
		                    <div class="statusPanel">
		                        <h4 class="text-center pleaseWait">File List</h4>
				                <table class="table statusTable tenantStatus">
								    <thead>
									    <tr>
									        <th class="col-sm-5">File Name</th>
										    <th class="col-sm-5">File Link</th>
										    <th class="col-sm-2">Action</th>
									    </tr>
									</thead>
									<tbody>
									    <tr data-ng-repeat="file in buildRuleFileList">
										    <td class="col-sm-5">{{ file.fileName }}</td>
										    <td class="col-sm-5">
				                                <a href="javascript:void(0)" data-ng-click="downloadBuildRuleFile(file)">
				                                    {{file.fileLink}}
				                                </a>											  
										    </td>
										    <td class="col-sm-2">
										        <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteBuildRuleFile(file)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span>&nbsp;</md-button>
										    </td>
									    </tr>
								    </tbody>
							    </table>		                    
		                    </div>		                    
		                </div>                        
                        <div id="XMLContent" data-ng-show="container=='RX'" class="row">
		                   <div class="statusPanel">
				                <form id="reqXMLForm" name="reqXMLForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Add Request XML</h4>
									<div layout-gt-sm="row">
									   <md-input-container flex="50" class="md-block" flex-gt-sm>
							                <label class="labelStyle">Request Name</label>
							                <input required data-ng-model="requestName">
							           </md-input-container>
							           <md-input-container flex="50" class="md-block" flex-gt-sm>
										 	<label  flex="20" class="labelStyle labelAddStyle" required>Select Request XML</label>
								         	<input type="file" class="btn btn-sm" id="requestFileId" name="requestFile" accept=".xml" data-ng-model="requestFile" /> 
									   </md-input-container>
									   <md-input-container flex="20" class="md-block" flex-gt-sm>										
											<md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Submit&nbsp;</md-button>
									   </md-input-container>
									</div>
				                </form>
				          </div>
				          <br></br>
				          <div class="statusPanel">
				             	 <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-4">Request Name</th>
										    <th class="col-sm-5">Request File Name</th>
										    <th class="col-sm-3">Actions</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="reqXML in reqXMLs" >
											<td class="col-sm-4">{{ reqXML.requestName }}</td>
											<td class="col-sm-5">{{ reqXML.requestXMLName }}</td>
											<td class="col-sm-3">
												 <md-button aria-label="" class="md-icon-button md-primary" data-ng-click="editRequest(reqXML)"><span class="glyphicon glyphiconPlus glyphicon-edit"></span></md-button>												 
												 <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteRequest(reqXML)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
											</td>
										  </tr>
								    </tbody>				                
				                </table>
		                  </div>		                
		                </div>
		                <div id="RefXMLContent" data-ng-show="container=='RFX'" class="row">
		                   <div class="statusPanel">
				                <form id="refXMLForm" name="refXMLForm" autocomplete="off"> 
				                    <h4 class="text-center pleaseWait">Add Reference XML</h4>
									<div layout-gt-sm="row">
									   <md-input-container flex="50" class="md-block" flex-gt-sm>
							                <label class="labelStyle">Reference Name</label>
							                <input required data-ng-model="referenceName">
							           </md-input-container>
							           <md-input-container flex="50" class="md-block" flex-gt-sm>
										 	<label  flex="20" class="labelStyle labelAddStyle" required>Select Reference XML</label>
								         	<input type="file" class="btn btn-sm" id="referenceFileId" name="referenceFile" accept=".xml" data-ng-model="referenceFile" /> 
									   </md-input-container>
									   <md-input-container flex="20" class="md-block" flex-gt-sm>										
											<md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Submit&nbsp;</md-button>
									   </md-input-container>
									</div>
				                </form>
				          </div>
				          <br></br>
				          <div class="statusPanel">
				             	 <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-4">File Name</th>
										    <th class="col-sm-5">File Link</th>
										    <th class="col-sm-3">Actions</th>
										</tr>
									</thead>
									 <tbody>
										  <tr>
											<td class="col-sm-4">{{ refXML.fileName }}</td>
											<td class="col-sm-5">{{ refXML.fileLink }}</td>
											<td class="col-sm-3">												 												 
												 <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteReference(refXML)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
											</td>
										  </tr>
								    </tbody>				                
				                </table>
		                  </div>		                
		                </div> 
		                <div id="ExclusionContent" data-ng-show="container=='ER'" class="row">
		                   <div class="statusPanel">
			                    <h4 class="text-center pleaseWait">Add Exclusion Reference Type</h4>
								<div layout-gt-sm="row">
								   <md-input-container flex="70" class="md-block" flex-gt-sm>
						                <label class="labelStyle">Exclusion Reference Type Name</label>
						                <input required data-ng-model="excRefTypeName">
						           </md-input-container>
								   <md-input-container flex="20" class="md-block" flex-gt-sm>										
										<md-button aria-label="" type="submit" class="btn" data-ng-click="submitExclusion()"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Submit&nbsp;</md-button>
								   </md-input-container>
								</div>
				          </div>
				          <br></br>
				          <div class="statusPanel">
				             	 <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-9">Exclusion Reference Type Name</th>
										    <th class="col-sm-3">Actions</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="excRef in excRefs">
											<td class="col-sm-9">{{ excRef.exclusionRefName }}</td>
											<td class="col-sm-3">												 												 
												 <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteExcRef(excRef)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
											</td>
										  </tr>
								    </tbody>				                
				                </table>
		                  </div>		                
		                </div>                                                                              
		           </div>
		         </div>
		    </div>
		</div>       
    </div>
    <script type="text/ng-template" id="pop-template.html">
		<md-toast class="{{severity}}">
			<div>{{ message }}</div>
		</md-toast>
	</script>
    <script type="text/ng-template" id="wait.dialog.tmpl.html">
		<md-dialog aria-label="wait dialog">
			<md-dialog-content>
				<div class="md-dialog-content">
					<div layout="row" layout-sm="column" layout-align="center center" aria-label="wait" >
						<md-progress-circular md-mode="indeterminate" ></md-progress-circular>
					</div>
					<br/>
					<h3>{{ctrl.parent.waitMessage}}</h3>
					<span> {{ctrl.parent.status | limitTo: 100}}</span>
				</div>
			</md-dialog-content>
		</md-dialog>
	</script>    
</body>
</html>