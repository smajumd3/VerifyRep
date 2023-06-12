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
<title>Verify</title>
	
<link rel="stylesheet" href="../css/tree-control-attribute.css">	
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/bootstrap.css" />
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
<script type="text/javascript" src="../js/validationRules.js"></script>
<script type="text/javascript" src="../js/maincontroller.js"></script>
<script type="text/javascript" src="../js/angular-sanitize.js"></script>
<script type="text/javascript" src="../js/d3.min.js"></script>
<script type="text/javascript" src="../js/ng-knob.min.js"></script>
<script type="text/javascript" src="../js/ng-knob.js"></script>
<script type="text/javascript" src="../js/angular-tree-control.js"></script>
<script type="text/javascript" src="../js/context-menu.js"></script>

<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
<script type="text/javascript" src="../js/angular-animate.min.js"></script>
<script type="text/javascript" src="../js/angular-touch.js"></script>
<script type="text/javascript" src="../js/ui-bootstrap-tpls-2.5.0.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.10/d3.min.js"></script>
<script src="https://cdn.amcharts.com/lib/4/core.js"></script>
<script src="https://cdn.amcharts.com/lib/4/charts.js"></script>
<script src="https://cdn.amcharts.com/lib/4/themes/animated.js"></script>
</head>

<body data-ng-app="hyperloader.app" class="mainBody">
    <div id="parentContainer" class="parentContainerStyle" data-ng-controller="hyperloader.controller"  data-ng-init="init()">
    	<nav class="navbar navbar-inverse navbar-fixed-top">
			<div class="container-fluid">
				<div class="navbar-header">
					<a class="navbar-brand" href="javascript:void(0)"><span><img src="../images/ibm-logo.png" alt="IBM">Verify</span></a>
				</div>
				<div id="navbar" class="navbar-collapse collapse">
					<ul class="nav navbar-nav navbar-right">
						<li><a href="javascript:void(0)" ><span class="glyphicon glyphicon-user"></span> {{currUser.userName}}</a></li>
						<li><a href="javascript:void(0)" ><span class="glyphicon glyphicon-globe"></span> {{currUser.client}}</a></li>
						<li><a href="logout" ><span class="glyphicon glyphicon-off"></span> Logout</a></li>
					</ul>
				</div>
			</div>
		</nav>
		<div class="container-fluid">
		    <div class="row">
		    	<div id="sidebarMenu" flex="17" class="sidebar">
<!-- 				<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initDashboard(container='DB')"  data-ng-class="container=='DB'?'selected':''">Dashboard
							</a>
						</li>
					</ul> -->
					<!--md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initPrjTemplateData(container='PM')"  data-ng-class="container=='PM'?'selected':''">Project Management
							</a>
						</li>
					</ul-->
					<md-divider></md-divider>					
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initTenant(container='TS')"  data-ng-class="container=='TS'?'selected':''">Tenant Configuration
							    <!-- <span data-ng-show="container=='TS'" class="glyphicon glyphicon-triangle-right  pull-right"></span> -->
							</a>
						</li>
					</ul>
					<!--md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initApplication(container='CL')"  data-ng-class="container=='CL'?'selected':''">Application Configuration
							</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initOperation(container='VL')"  data-ng-class="container=='VL'?'selected':''">Build Operation							    
							</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initRule(container='RL')"  data-ng-class="container=='RL'?'selected':''">Build Rule
							</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initMapFile(container='MF')"  data-ng-class="container=='MF'?'selected':''">Map Files
							</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initValidate(container='VD')"  data-ng-class="container=='VD'?'selected':''">Validate
							</a>
						</li>
					</ul>
					<md-divider></md-divider>					
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initSendToWD(container='SW')"  data-ng-class="container=='SW'?'selected':''">Send To Workday
							</a>
						</li>
					</ul-->
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initPostLoad(container='PL')"  data-ng-class="container=='PL'?'selected':''">Verify
							</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initPostLoad(container='PL1')"  data-ng-class="container=='PL1'?'selected':''">Verify - File Based
							</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initPostLoad(container='PL2')"  data-ng-class="container=='PL2'?'selected':''">Verify - Tenant Based
							</a>
						</li>
					</ul>
					<md-divider></md-divider>
				</div>
				<div id="Content" flex="83" class="col-sm-offset-2">
				    <div data-ng-show="container" class="contentClass">
				        <div id="dashboardContent" data-ng-show="container=='DB'" class="row">
				            <div class="statusPanel">
				                <div id="chartdiv">
                                </div>
				            </div>
				        </div>
				        <div id="prjTemplateContent" data-ng-show="container=='PM'" class="row">
				            <div class="statusPanel">
				                <div layout-gt-sm="row">
							        <md-input-container flex="30" class="md-block" flex-gt-sm>
							            <label class="labelStyle">Load Cycle</label>
							            <md-select class="labelStyle" required data-ng-change="getSelectedPage(value)" data-ng-model="value">
								            <md-option data-ng-repeat="page in pages track by $index" data-ng-value="{{$index}}">
								                {{ page.pageName }}
								            </md-option>
								        </md-select>
							        </md-input-container>
							        <md-input-container flex="25" class="md-block" flex-gt-sm>
							            <label class="labelStyle">Tenant Name</label>
							            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo(selectVal)" data-ng-model="selectVal">
								            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
								                {{ tenant.tenantName }}
								            </md-option>
								        </md-select>
							        </md-input-container>							        
				                </div>
				                <div style="overflow-x:auto">
				                <table class="table statusTable prjManagementStatus">
									<thead>
										<tr>
										    <th>Area Name</th>
										    <th>Task Name</th>
										    <th>Operation Name</th>
										    <th>Assigned To</th>										    
										    <th>Execute</th>
										    <th>Download</th>
										    <th>Modify Build Rule</th>
										    <th>Map Data Files</th>
										    <th>Pre Load Validation</th>
										    <th>Send to Workday</th>
										    <th>Result</th>
<!--									    <th>Post Load Validation</th>		-->
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="section in sections">
											<td>{{ section.areaName }}</td>
											<td>{{ section.taskName }}</td>
											<td>{{ section.operationName }}</td>
											<td>{{ section.assignedTo }}</td>
											<td>
								                <md-checkbox data-ng-model="isExecute" aria-label="checkbox1" data-ng-init="isExecute=section.execute" data-ng-change="toggleExecute(section, isExecute)"></md-checkbox>
								            </td>
								            <td>
								                <div data-ng-if="section.execute && section.isDownload">
					                                <a href="javascript:void(0)" data-ng-click="downloadXMLFile(section)">
					                                    &nbsp;XML&nbsp;
					                                </a>
					                                <a href="javascript:void(0)" data-ng-click="downloadCSVFile(section)">
					                                    &nbsp;CSV&nbsp;
					                                </a>
			                                    </div>								                
								            </td>
											<td>
											    <div data-ng-if="section.execute && section.isDownload">
									                <md-button aria-label="initRule" data-ng-click="initRule(container='RL')"><span class="glyphicon glyphiconPlus glyphicon-forward"></span></md-button>									                
								                </div>
								            </td>
								            <td>
								                <div data-ng-if="section.execute && section.isDownload">
									                <div>
											            <md-select class="labelStyle" required md-on-open="getMapFileNames(section)" md-on-close="setFileIndex(indexValue)" data-ng-model="indexValue">
												            <md-option data-ng-repeat="(index,fileName) in mapFileNames[section.index]" data-ng-value="{{$index}}">	<!-- Suman -4 eliminate [section.index-4] -->
												                {{ fileName }}
												            </md-option>
												        </md-select>
												        <div>
								                            <input type="file" class="btn btn-sm" id="fileId" accept=".csv" data-ng-model="srcFile" onchange="angular.element(this).scope().fileMapped(this.files)"/>
									                    </div>
									                </div>
								                </div>
								            </td>								            
								            <td>
											    <div data-ng-if="section.execute && section.isDownload">
									                <md-button aria-label="validateMapData" data-ng-click="validateMappedData(section)"><span class="glyphicon glyphiconPlus glyphicon-step-forward"></span></md-button>
								                    <div data-ng-show="validationPercentage > 0" class="pleaseWait">{{getValidationPercentagePrj(section)}}%</div>								    
								                    <div>
								                        <md-progress-linear md-mode="determinate" value="{{getValidationPercentagePrj(section)}}"></md-progress-linear>
								                    </div>
								                    <div>
								                        <md-progress-linear md-mode="determinate" value="{{getValidationPercentagePrj(section)}}"></md-progress-linear>
								                    </div>								                    
								                </div>
								            </td>
										<!-- 	<td>
							                   <md-select class="labelStyle" data-ng-disabled="!isExecute" md-on-close="saveStatus(section, statusValue)" data-ng-model="statusValue">
								                    <md-option data-ng-repeat="(index,status) in statuses" data-ng-value="{{$index}}" data-ng-selected="index == section.status">
								                        {{ status }}
								                    </md-option>
								                </md-select>											    
                                            </td> -->
                                            <td>
 											    <div data-ng-if="section.execute && section.isDownload">
 											        <div>
								                        <md-button aria-label="swd1" data-ng-click="sendToWDRequest(section)"><span class="glyphicon glyphiconPlus glyphicon-play"></span></md-button>
								                        <md-button aria-label="swd2" data-ng-click="stopSWExecution()"><span class="glyphicon glyphiconPlus glyphicon-stop"></span></md-button>
								                    </div>
								                    <div data-ng-show="percentageValue > 0" class="pleaseWait">{{getExecutedPercentage(section)}}%</div>								                    
								                    <div>
								                        <md-progress-linear md-mode="determinate" value="{{getExecutedPercentage(section)}}"></md-progress-linear>
								                    </div>
								                    <div>
								                        <md-progress-linear md-mode="determinate" value="{{getExecutedPercentage(section)}}"></md-progress-linear>
								                    </div>								                    
								                </div>
                                            </td>
											<td>
											    <div data-ng-if="section.execute && section.isDownload">
								                    <md-button aria-label="result" data-ng-click="showResult(section)"><span class="glyphicon glyphiconPlus glyphicon-hand-down"></span>&nbsp;</md-button>
								                </div>    
                                            </td>
<!-- 
                                            <td>                                             	                                            	                                          	
	                                            <md-button aria-label="sup1" data-ng-click="performPostLoad(section,$index)"><span class="glyphicon glyphiconPlus glyphicon-play"></span></md-button> 
                                            </td>
-->                                            
										  </tr>
								    </tbody>				                
				                </table>
				                </div>
								<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								    <md-button aria-label="" type="submit" class="btn" data-ng-click="upDateData()"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Update Configuration</md-button>
								</section>				                
				            </div>
				            <br></br>
				            <div class="statusPanel">
			                    <table class="table statusTable sendToWDStatus">
									<thead>
										<tr>
										    <th class="col-sm-4">Area Name</th>
										    <th class="col-sm-4">Task Name</th>
										    <th class="col-sm-4">Operation Name</th>
										</tr>
									</thead>
								    <tbody>
									     <tr>
										     <td class="col-sm-4">
											     <md-input-container class="md-block" flex-gt-sm>
									                 <input required data-ng-model="areaName">
									          	 </md-input-container>
									         </td>
										     <td class="col-sm-4">
											     <md-input-container class="md-block" flex-gt-sm>
									                 <input required data-ng-model="taskName">
									          	 </md-input-container>
									         </td>
										     <td class="col-sm-4">
											     <md-input-container class="md-block" flex-gt-sm>
									                 <input required data-ng-model="operationName">
									          	 </md-input-container>
									         </td>
										 </tr>
								    </tbody>
			                    </table>
								<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								    <md-button aria-label="" type="submit" class="btn" data-ng-click="addSection()"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Add Section</md-button>
								</section>				                
				            </div>
				            <br></br>
				            <div class="statusPanel" data-ng-if="result">
				            <h4 class="text-center pleaseWait">Result</h4>
								<table class="table statusTable resultStatus">
									<thead>
										<tr>
										    <th class="col-sm-3">Load Date</th>
										    <th class="col-sm-2">Total Records</th>
										    <th class="col-sm-2">Total Success</th>
										    <th class="col-sm-2">Total Failures</th>
										    <th class="col-sm-3">Download</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="result in results" >
											<td class="col-sm-3">{{ getDate(result.loadDate) }}</td>
											<td class="col-sm-2">{{ result.totalRecords }}</td>
											<td class="col-sm-2">{{ result.totalSuccess }}</td>
											<td class="col-sm-2">{{ result.totalFailures }}</td>
											<td class="col-sm-3">
					                            <a href="javascript:void(0)" data-ng-click="getWSXmlFiles(result)">
					                                &nbsp;XML&nbsp;
					                            </a>
					                            <a href="javascript:void(0)" data-ng-click="getWSErrorDataFiles(result)">
					                                &nbsp;Error&nbsp;
					                            </a>
					                        </td>
										  </tr>
									  </tbody>
								</table>				            
				            </div>
				            <div class="statusPanel" data-ng-if="sendToWD">
				            <h4 class="text-center pleaseWait">Send To Workday</h4>
								<table class="table statusTable sendToWDStatus">
									<thead>
										<tr>
										    <th class="col-sm-4">Total Records</th>
										    <th class="col-sm-4">Total Success</th>
										    <th class="col-sm-4">Total Failures</th>
										</tr>
									</thead>
									 <tbody>
									     <tr>
										    <td class="col-sm-4">{{ wsResponsesStatus.totalRecords }}</td>
										    <td class="col-sm-4">{{ wsResponsesStatus.totalSuccess }}</td>
										    <td class="col-sm-4">{{ wsResponsesStatus.totalFailures }}</td>
										</tr>
									  </tbody>
								</table>
						        <md-button aria-label="" class="btn" data-ng-click="getWWSXmlFiles()"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;Files(XML)</md-button>						    
							    <md-button aria-label="" class="btn" data-ng-click="getWWSErrorDataFiles()"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;Files(Error Data)</md-button>
				            </div>
				            <div class="statusPanel" data-ng-if="validate">
				            <h4 class="text-center pleaseWait">Validation</h4>
								<table class="table statusTable validationStatus">
									<thead>
										<tr>
										    <th class="col-sm-3">File Name</th>
										    <th class="col-sm-3">Is Valid File Data</th>
										    <th class="col-sm-3">Validation Message</th>
										    <th class="col-sm-3">Actions</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="message in validationMessages track by $index" >
											<td class="col-sm-3">{{ message.fileName }}</td>
											<td class="col-sm-3">{{ message.validString }}</td>
											<td class="col-sm-3">{{ message.generalMessage }}</td>
										    <td class="col-sm-3">
										        <div data-ng-if="!message.valid">
										            <md-button aria-label="" class="md-icon-button md-primary" data-ng-click="viewValidationMessage($index)"><span class="glyphicon glyphiconPlus glyphicon-hand-down"></span></md-button>
<!-- 									            <md-button aria-label="" class="md-icon-button md-primary" data-ng-click="downloadValidationMessage($index)"><span class="glyphicon glyphiconPlus glyphicon-save"></span></md-button>		-->										        
										        </div>
										    </td>											
										  </tr>
									  </tbody>
								</table>				            
				            </div>
						    <br></br>
						    <div class="statusPanel" data-ng-if="viewValidation">
								<table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-2">Type</th>
										    <th class="col-sm-2">Unique ID</th>
										    <th class="col-sm-3">Column Name</th>
										    <th class="col-sm-5">Message</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="msg in validationMessage.messages">
											<td class="col-sm-2">{{ msg.type }}</td>
											<td class="col-sm-2">{{ msg.value }}</td>
											<td class="col-sm-3">{{ msg.columnName }}</td>
											<td class="col-sm-5">{{ msg.message }}</td>
										  </tr>
									  </tbody>
								</table>
								 <br>														    
						    </div>
						    <br>						    
						    <div class="statusPanel" data-ng-if="viewValidation">
						    	<md-button aria-label="" class="btn" data-ng-click="exportErrorData()" data-ng-show="viewValidation"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;Export</md-button>
						    </div>			            
				        </div>
				        <div id="tenantSettingsContent" data-ng-show="container=='TS'" class="row">
				            <div class="statusPanel">
				            	<form name="tenantForm" autocomplete="off">
									<h4 class="text-center pleaseWait" data-ng-show="tenantEditable.id">Tenant Details</h4>
									<div class="smallSpacer"></div>		
									<div layout-gt-sm="row">
									    <md-input-container class="md-block" flex="30" flex-gt-sm>
							                <label class="labelStyle">Tenant</label>
							                <md-select class="labelStyle" required data-ng-change="getTenantInfo(selectedValue1)" data-ng-model="selectedValue1">
								                <md-option data-ng-repeat="tenant in clientTenants track by $index" data-ng-value="{{$index}}">
								                    {{tenant.tenantName}}
								                </md-option>
								            </md-select>								            
							            </md-input-container>
										<md-input-container class="md-block" flex="70" flex-gt-sm>
							                <label class="labelStyle">End Point URL</label>
							                <input required data-ng-model="selectedTenant.tenantUrl" readonly=true>
							          	</md-input-container>							            
									</div>
									<div layout-gt-sm="row">
										<md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">User</label>
							                <input required data-ng-model="tenantEditable.tenantUser">
							          	</md-input-container>
										<md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Password</label>
							                <input type="password" required data-ng-model="tenantEditable.tenantUserPassword">
							            </md-input-container>
									</div>
									<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								        <md-button aria-label="" type="reset" class="btn" data-ng-click="resetTenantForm()"><span class="glyphicon glyphicon-repeat"></span>&nbsp;Clear</md-button>
								        <md-button aria-label="" type="submit" class="btn" data-ng-click="submitTenantForm()"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
								    </section>
							    </form>
				            </div>
				            <br></br>
				            <div class="statusPanel">
				            	<h4 class="text-center pleaseWait">Tenant Configuration</h4>
				            	<div class="smallSpacer"></div>
								<table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-3">Tenant</th>
										    <th class="col-sm-2">Data Center</th>
										    <th class="col-sm-4">End Point URL</th>
										    <th class="col-sm-3">User</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="tenant in userTenants" >
											<td class="col-sm-3">{{ tenant.tenantName }}</td>
											<td class="col-sm-2">{{ tenant.tenantDataCenter }}</td>
											<td class="col-sm-4">{{ tenant.tenantUrl }}</td>
											<td class="col-sm-3">{{ tenant.tenantUser }}</td>
										  </tr>
									  </tbody>
								</table>				            		            
				            </div>
				        </div>
				        <div id="applicationConfigContent" data-ng-show="container=='CL'" class="row">
				            <div class="statusPanel">
				                <h4 class="text-center pleaseWait">Application Configuration</h4>
				            	<div class="smallSpacer"></div>
								<table class="table statusTable applicationStatus">
								    <thead>
									    <tr>
										    <th class="col-sm-5">Application Name</th>
										    <th class="col-sm-4">Application Version</th>
										    <th class="col-sm-3">Actions</th>
										</tr>
									</thead>
								    <tbody>
									    <tr data-ng-repeat="application in applications" >
										    <td class="col-sm-5">{{ application.applicationName }}</td>
										    <td class="col-sm-4">{{ application.version }}</td>
										    <td class="col-sm-3">
										        <md-button aria-label="" class="md-icon-button md-primary" data-ng-click="editApplication(application)"><span class="glyphicon glyphiconPlus glyphicon-edit"></span>&nbsp;</md-button>
										        <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteApplication(application)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span>&nbsp;</md-button>
										    </td>
									    </tr>
									</tbody>
								</table>
				            </div>
				            <br></br>
				            <div class="statusPanel">
				            	<form name="applicationForm" autocomplete="off">	
									<h4 class="text-center pleaseWait">Create Application</h4>	
									<div class="smallSpacer"></div>		
									<div layout-gt-sm="row">
									    <md-input-container class="md-block" flex-gt-sm>
								            <label class="labelStyle">Application Name</label>
								            <md-select class="labelStyle" required data-ng-model="appEditable.applicationName">
								                <md-option data-ng-repeat="application in applicationList" value="{{application}}">
								                    {{application}}
								                </md-option>
								            </md-select>
								        </md-input-container>										
									    <md-input-container class="md-block" flex-gt-sm>
								            <label class="labelStyle">Version</label>
								            <md-select class="labelStyle" required data-ng-model="indexVal">
									            <md-option data-ng-repeat="appVersion in appVersions track by $index" data-ng-value="{{$index}}">
									                {{ appVersion }}
									            </md-option>
									        </md-select>
								        </md-input-container>
									</div>
									<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								        <md-button aria-label="" type='reset' class="btn" ng-click="resetApplicationForm(applicationForm)"><span class="glyphicon glyphicon-repeat"></span>&nbsp;Clear</md-button>
								        <md-button aria-label="" type="submit" class="btn" ng-click="submitApplicationForm(applicationForm)"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
								    </section>
								</form>				            
				            </div>
				        </div>
				        <div id="OperationConfigContent" data-ng-show="container=='VL'" class="row">
				            <div class="statusPanel">
				                <form name="operationForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Select Operation For Application</h4>
				                    <div class="smallSpacer"></div>
									    <div layout-gt-sm="row">
										    <md-input-container flex="20" class="md-block" flex-gt-sm>
								                <label class="labelStyle">Application Name</label>
								                <md-select class="labelStyle" required data-ng-change="getApplicationOperations(selectedValue2)" data-ng-model="selectedValue2">
									                <md-option data-ng-repeat="application in applications track by $index" data-ng-value="{{$index}}">
									                    {{application.applicationName}}
									                </md-option>
									            </md-select>
								            </md-input-container>										
										    <md-input-container flex="20" class="md-block" flex-gt-sm>
								                <label class="labelStyle">Application Version</label>
								                <input required data-ng-model="applicationVersion" readonly=true>
								            </md-input-container>
										</div>
										<div>
									<!--       <h4 class="text-center" data-ng-show="operEditable.id != -1">Create/Edit Operation</h4>	-->
									        <div class="smallSpacer"></div>
										</div>
										<div layout-gt-sm="row">
										    <md-input-container flex="25" class="md-block" flex-gt-sm>
								                <label class="labelStyle">Operation Name</label>
								                <md-select class="labelStyle" required data-ng-change="getResponsePath(selectedValue3)" data-ng-model="selectedValue3">
								                    <md-option data-ng-repeat="operation in operationList track by $index" data-ng-value="{{$index}}">
								                        {{operation}}
								                    </md-option>
								                </md-select>
								            </md-input-container>										
									        <md-input-container flex="60" class="md-block" flex-gt-sm>
								                <label class="labelStyle">Response Path</label>
								                <input required data-ng-model="operEditable.responsePath">
								            </md-input-container>
								            <md-input-container flex="15" class="md-block" flex-gt-sm>
								                <label class="labelStyle">Assign Rule Name</label>
								                <input required data-ng-model="operEditable.ruleName">
								            </md-input-container>
										</div>
										<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
									        <md-button aria-label="" type='reset' class="btn" ng-click="resetOperationForm(operationForm)"><span class="glyphicon glyphicon-repeat"></span>&nbsp;Clear</md-button>
									        <md-button aria-label="" type="submit" class="btn" ng-click="submitOperationForm(operationForm)"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
									    </section>
								</form>					                
				            </div>
				            <br></br>
				            <div class="statusPanel">
				                <h4 class="text-center pleaseWait">Operation Details</h4>
				            	<div class="smallSpacer"></div>
								<table class="table statusTable applicationStatus">
								    <thead>
									    <tr>
									        <th class="col-sm-1">Rule Name</th>
										    <th class="col-sm-2">Operation Name</th>
										    <th class="col-sm-7">Response Path</th>
										    <th class="col-sm-2">Actions</th>
										</tr>
									</thead>
								    <tbody>
									    <tr data-ng-repeat="operation in operations" >
									        <td class="col-sm-1">{{ operation.ruleName }}</td>
										    <td class="col-sm-2">{{ operation.operationName }}</td>
										    <td class="col-sm-7">{{ operation.responsePath }}</td>
										    <td class="col-sm-2">
										        <md-button aria-label="" class="md-icon-button md-primary" data-ng-click="editOperation(operation)"><span class="glyphicon glyphiconPlus glyphicon-edit"></span></md-button>
										        <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteOperation(operation)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
										    </td>
									    </tr>
									</tbody>
								</table>
				            </div>
				        </div>
				        <div id="RuleConfigContent" data-ng-show="container=='RL'" class="row">
				            <div class="statusPanel">
				                <form name="RuleForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Build Rule</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
										<md-input-container flex="20" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Rule Name</label>
								            <md-select class="labelStyle" required data-ng-change="getOperationList(selectedValue4)" data-ng-model="selectedValue4">
									            <md-option data-ng-repeat="operation in operations track by $index" data-ng-value="{{$index}}">
									                {{ operation.ruleName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Application Name</label>
							                <input required data-ng-model="operationValues.applicationName" readonly=true>
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Application Version</label>
							                <input required data-ng-model="operationValues.applicationVersion" readonly=true>
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Operation Name</label>
							                <input required data-ng-model="operationValues.operationName" readonly=true>
							            </md-input-container>
								    </div>
				                </form>
				            </div>
				            <br></br>
				            <div class="statusPanel">
				            <!-- Adding Tree -->
				            	<div class="panel panel-default">
							   		<div class="panel-body">
							   			<div class="webServiceRequest_div">
								   			<h5><span class="label label-primary">Web Service Request</span></h5>
								   			<div>
										     	<div id='jqxWidget'></div>
										    </div>
										    <br>					    
									    <!-- <button type="button" id="webAdd" value="Add" class="btn btn-primary" disabled="disabled"><span class="glyphicon glyphicon-plus"></span> Add </button> -->
									    	<md-button aria-label="" id="webAdd" value="Add" class="md-icon-button md-primary"><span class="glyphicon glyphiconPlus glyphicon-plus"></span></md-button>
										<!--	<button type="button" id="webDel" value="`Delete" class="btn btn-primary" disabled="disabled"><span class="glyphicon glyphicon-trash"></span> Delete </button> -->
										    <md-button aria-label="" id="webDel" value="Delete" class="md-icon-button md-warn"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
							   			</div>
							   			<div class="attributes_rule_div">
							   			
							   					<h5><span class="label label-primary">Attributes</span></h5>
							   					<table id="attr" class="table table-bordered table-striped table-hover table-fixed table-sm">
														<thead>
											 				<tr>
											 					<th>Select</th>
																<th class="ruleBuilderTH">Name</th>
																<th class="ruleBuilderTH">Value</th>
															</tr>
														</thead>
														<tbody>
															 <tr ng-repeat="attr in attrDetails"> 
															 	<td><input type="checkbox"/></td>
																<td><input type="text" ng-model ="attr.name"></td>
																<td><input type="text" ng-model ="attr.value"></td> 
												  	        </tr>							
														</tbody>									
												</table>
												<!-- <button type="button" id="attrAdd" ng-click="addAttrRow('attr')" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span> Add </button> -->
												<md-button aria-label="" id="attrAdd" data-ng-click="addAttrRow('attr')" class="md-icon-button md-primary"><span class="glyphicon glyphiconPlus glyphicon-plus"></span></md-button>
											<!-- <button type="button" id="attrDel" ng-click="deleteAttrRow('attr')" class="btn btn-primary"><span class="glyphicon glyphicon-trash"></span> Delete </button> -->
												<md-button aria-label="" id="attrDel" data-ng-click="deleteAttrRow('attr')" class="md-icon-button md-warn"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
							   				
							   				<br><br>
							   				<div>		   					
							   					<h5><span class="label label-primary">Rules</span></h5>
							   					<table id="rules" class="table table-bordered table-striped table-hover table-fixed table-sm">
													<thead>
										 				<tr>
										 					<th>Select</th>
															<th class="ruleBuilderTH">Name</th>
															<th class="ruleBuilderTH">Value</th>
														</tr>
													</thead>
													<tbody>
														 <tr ng-repeat="rule in ruleDetails"> 
														 	<td><input type="checkbox"/></td>
															<td><input type="text" ng-model ="rule.name"></td> 
															<td><input type="text" ng-model ="rule.value"></td> 
											  	        </tr>
								
													</tbody>
												</table>
											<!-- <button type="button" id="ruleAdd" ng-click="addRuleRow('rules')" class="btn btn-primary"><span class="glyphicon glyphicon-plus"></span> Add </button> -->
												<md-button aria-label="" id="ruleAdd" data-ng-click="addRuleRow('rules')" class="md-icon-button md-primary"><span class="glyphicon glyphiconPlus glyphicon-plus"></span></md-button>
											<!--  <button type="button" id="ruleDel" ng-click="deleteRuleRow('rules')" class="btn btn-primary"><span class="glyphicon glyphicon-trash"></span> Delete </button> -->
												<md-button aria-label="" id="ruleDel" data-ng-click="deleteRuleRow('rules')" class="md-icon-button md-warn"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
							   				</div>
							   			</div>
							   		</div>
							   		<div>
							   		    <md-button aria-label="" value="Save" data-ng-click="saveBuildRule(selectedValue4)" class="btn"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
							   		</div>
							   	</div>
				            <!-- End Tree -->
			                    <form id="singleUploadForm" name="singleUploadForm">
			                        <div class="panel panel-default">
			                            <md-button aria-label="" value="Load XML" data-ng-click="loadRootXml(selectedValue4)" class="btn"><span class="glyphicon glyphiconPlus glyphicon-upload"></span>&nbsp;Load Root Version</md-button>
								        <table>
							   		      <tr>
				                            <td class="col-md-2"><label for="singleFileUploadInput" class="btn btn-primary btn-xl" style="margin-left:310px;margin-top: 17px;"><span class="glyphicon glyphiconPlus glyphicon-paperclip">&nbsp;File </span></label><input type="file" id="singleFileUploadInput" style="visibility:hidden;" name="file" ng-model="file" accept=".xml"/></td>			                       
				                            <td class="col-md-2"><button type="submit" class="btn btn-primary" style="margin-left:-610px;"><span class="glyphicon glyphiconPlus glyphicon-upload">&nbsp;Upload</span></button></td>
				                          </tr>
				                        </table>
			                        </div>
			                    </form>
			                    <md-button aria-label="" value="Download XML" data-ng-click="downloadXML(selectedValue4)" class="btn"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;XML</md-button>
			                    <md-button aria-label="" value="Download CSV" data-ng-click="downloadCSV(selectedValue4)" class="btn"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;CSV</md-button>
			                    <md-button aria-label="" value="Download Worksheet" data-ng-click="downloadWorksheet(selectedValue4)" class="btn"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;Data Gathering Worksheet</md-button>			                    
							</div>
						</div>
						<div id="MapFileConfig" data-ng-show="container=='MF'" class="row">
						    <div class="statusPanel">
				                <form name="mapFileForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Map Files</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
								        <md-input-container flex="20" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Rule Name</label>
								            <md-select class="labelStyle" required data-ng-change="listOfOperations(selectedValue5)" data-ng-model="selectedValue5">
									            <md-option data-ng-repeat="operation in operations track by $index" data-ng-value="{{$index}}">
									                {{ operation.ruleName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <md-input-container class="md-block" flex-gt-sm>
								            <label class="labelStyle">Application Name</label>
								            <input required data-ng-model="operationValues.applicationName" readonly=true>
								        </md-input-container>
								        <md-input-container class="md-block" flex-gt-sm>
								            <label class="labelStyle">Application Version</label>
								            <input required data-ng-model="operationValues.applicationVersion" readonly=true>
								        </md-input-container>
								        <md-input-container class="md-block" flex-gt-sm>
								            <label class="labelStyle">Operation Name</label>
								            <input required data-ng-model="operationValues.operationName" readonly=true>
								        </md-input-container>
								    </div>
				                </form>
				            </div>
				            <br></br>
				            <div class="statusPanel">
				                <form id="mapFileUploadForm" name="mapFileUploadForm" autocomplete="off">
				                    <div layout-gt-sm="row">
								        <md-input-container flex="30" class="md-block" flex-gt-sm>
								            <label class="labelStyle">File Name</label>
								            <md-select class="labelStyle" required data-ng-model="fileIndex">
									            <md-option data-ng-repeat="fileName in fileNames track by $index" data-ng-value="{{$index}}">
									                {{ fileName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <md-input-container flex="50" class="md-block" flex-gt-sm>
										    <label  flex="20" class="labelStyle labelAddStyle" required>Select File</label>
								            <input type="file" class="btn btn-sm" id="mapFileId" name="mapFile" data-ng-model="mapFile" /> 
										</md-input-container>
										<md-input-container flex="20" class="md-block" flex-gt-sm>
										    <md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-transfer"></span>&nbsp;Map File&nbsp;</md-button>
								        </md-input-container>
								    </div>
				                </form>
				            </div>
				            <br></br>
				            <div class="statusPanel">
				                <table class="table statusTable tenantStatus">
								    <thead>
									    <tr>
									        <th class="col-sm-5">File Name</th>
										    <th class="col-sm-5">File Link</th>
										    <th class="col-sm-2">Action</th>
									    </tr>
									</thead>
									<tbody>
									    <tr data-ng-repeat="mapFile in mappedFiles" >
										    <td class="col-sm-5">{{ mapFile.fileName }}</td>
										    <td class="col-sm-5">
				                                <a href="javascript:void(0)" data-ng-click="downloadMappedFile(mapFile)">
				                                    {{mapFile.fileLink}}
				                                </a>											  
										    </td>
										    <td class="col-sm-2">
										        <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteMapFile(mapFile)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span>&nbsp;</md-button>
										    </td>
									    </tr>
								    </tbody>
							    </table>
				            </div>
						</div>
						<div id="ValidateDataConfig" data-ng-show="container=='VD'" class="row">
						    <div class="statusPanel">
				                <form name="validateDataForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Validate</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Rule Name</label>
								            <md-select class="labelStyle" required data-ng-change="populateOperationList(selectedValue6)" data-ng-model="selectedValue6">
									            <md-option data-ng-repeat="operation in operations track by $index" data-ng-value="{{$index}}">
									                {{ operation.ruleName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Application Name</label>
								            <input required data-ng-model="operationValues.applicationName" readonly=true>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Application Version</label>
								            <input required data-ng-model="operationValues.applicationVersion" readonly=true>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Operation Name</label>
								            <input required data-ng-model="operationValues.operationName" readonly=true>
								        </md-input-container>
								    </div>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Tenant Name</label>
								            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo(selectedValue7)" data-ng-model="selectedValue7">
									            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
									                {{ tenant.tenantName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Data Center</label>
								            <input required data-ng-model="selectedTenant.tenantDataCenter" readonly=true>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Endpoint URL</label>
								            <input required data-ng-model="selectedTenant.tenantUrl" readonly=true>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">User Name</label>
								            <input required data-ng-model="selectedTenant.tenantUser" readonly=true>
								        </md-input-container>
								    </div>
								    <div data-ng-show="validationPercentage > 0">
				                    <div class="pleaseWait">{{getValidationPercentage()}}%</div>								    
				                    <div>
				                        <md-progress-linear md-mode="determinate" value="{{getValidationPercentage()}}"></md-progress-linear>
				                    </div>
				                    <div>
				                        <md-progress-linear md-mode="determinate" value="{{getValidationPercentage()}}"></md-progress-linear>
				                    </div>
				                    </div>				                    								    
									<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>									
								        <md-button aria-label="" class="btn" data-ng-click="validateErrorData(operationValues, selectedTenant, validateDataForm)"><span class="glyphicon glyphicon-send"></span>&nbsp;Validate</md-button>
								    </section>								    
				                </form>						    
						    </div>
						    <br></br>
						    <div class="statusPanel">
								<table class="table statusTable validationStatus">
									<thead>
										<tr>
										    <th class="col-sm-3">File Name</th>
										    <th class="col-sm-3">Is Valid File Data</th>
										    <th class="col-sm-3">Validation Message</th>
										    <th class="col-sm-3">Actions</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="message in validationMessages track by $index" >
											<td class="col-sm-3">{{ message.fileName }}</td>
											<td class="col-sm-3">{{ message.validString }}</td>
											<td class="col-sm-3">{{ message.generalMessage }}</td>
										    <td class="col-sm-3">
										        <md-button aria-label="" class="md-icon-button md-primary" data-ng-click="viewValidationMessage($index)"><span class="glyphicon glyphiconPlus glyphicon-hand-down"></span></md-button>
<!-- 									        <md-button aria-label="" class="md-icon-button md-primary" data-ng-click="downloadValidationMessage($index)"><span class="glyphicon glyphiconPlus glyphicon-save"></span></md-button>		-->								    
										    </td>											
										  </tr>
									  </tbody>
								</table>						    
						    </div>
						    <br></br>
						    <div class="statusPanel">
								<table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-2">Type</th>
										    <th class="col-sm-2">Unique ID</th>
										    <th class="col-sm-3">Column Name</th>
										    <th class="col-sm-5">Message</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="msg in validationMessage.messages">
											<td class="col-sm-2">{{ msg.type }}</td>
											<td class="col-sm-2">{{ msg.value }}</td>
											<td class="col-sm-3">{{ msg.columnName }}</td>
											<td class="col-sm-5">{{ msg.message }}</td>
										  </tr>
									  </tbody>
								</table>						    
						    </div>
						    <br>
						    <div class="statusPanel">
						    	<md-button aria-label="" class="btn" data-ng-click="exportErrorData()"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;Export</md-button>
						    </div>						    
						</div>							
						<div id="SendToWDConfig" data-ng-show="container=='SW'" class="row">
						    <div class="statusPanel">
				                <form name="SendToWDForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Send To Workday</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
							            <md-input-container flex="30" class="md-block" flex-gt-sm>
							                <label class="labelStyle">Load Cycle</label>
							                <md-select class="labelStyle" required data-ng-change="getSelectedPage(value)" data-ng-model="value">
								                <md-option data-ng-repeat="page in pages track by $index" data-ng-value="{{$index}}">
								                    {{ page.pageName }}
								                </md-option>
								            </md-select>
							            </md-input-container>
				                    </div>				                    
				                    <div layout-gt-sm="row">
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Rule Name</label>
								            <md-select class="labelStyle" required data-ng-change="populateOperationList(selectedValue8)" data-ng-model="selectedValue8">
									            <md-option data-ng-repeat="operation in operations track by $index" data-ng-value="{{$index}}">
									                {{ operation.ruleName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Application Name</label>
								            <input required data-ng-model="operationValues.applicationName" readonly=true>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Application Version</label>
								            <input required data-ng-model="operationValues.applicationVersion" readonly=true>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Operation Name</label>
								            <input required data-ng-model="operationValues.operationName" readonly=true>
								        </md-input-container>
								    </div>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Tenant Name</label>
								            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo(selectedValue9)" data-ng-model="selectedValue9">
									            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
									                {{ tenant.tenantName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Data Center</label>
								            <input required data-ng-model="selectedTenant.tenantDataCenter" readonly=true>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Endpoint URL</label>
								            <input required data-ng-model="selectedTenant.tenantUrl" readonly=true>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">User Name</label>
								            <input required data-ng-model="selectedTenant.tenantUser" readonly=true>
								        </md-input-container>
								    </div>
<!--
								    <div layout-gt-sm="row">
								    	<md-input-container flex="35" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Batch Request Size (1-500)</label>
								            <input required data-ng-model="batchRequestSize">
								        </md-input-container>
								    </div>
-->								    
									<section layout="row" layout-sm="column" layout-align="center" layout-wrap>
								        <md-button aria-label="" class="btn" data-ng-click="sendWDRequest(operationValues, selectedTenant, 'SWR')"><span class="glyphicon glyphicon-send"></span>&nbsp;Send To Workday</md-button>
								    </section>								    
				                </form>						    
						    </div>
						</div>
						<div id="PostLoadValidation" data-ng-show="container=='PL'" class="row">
					        <section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
						    	<md-radio-group class="labelStyle" ng-model="status" aria-label="filter" ng-model="status" layout="row" data-ng-change="getRadioVal(status)">
								     <md-radio-button value="File Based"> File Based </md-radio-button>
								     <md-radio-button value="Tenant Based"> Tenant Based </md-radio-button>
								</md-radio-group>
							</section>
						    <br><br>
				        	<div class="statusPanel" data-ng-hide="isFileBased">
				        		<form name="PostLoadForm" autocomplete="off">
				        		<h4 class="text-center pleaseWait">Post Load Validation</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
				                        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Build Cycle</label>
									          <md-select class="labelStyle" required  data-ng-change="getRuleNameList(selectedLoad)" data-ng-model="selectedLoad">
											    <!-- <md-option data-ng-value="load" data-ng-repeat="load in loadNames">{{ load }}</md-option> -->
											    <md-option data-ng-repeat="page in pages track by $index" data-ng-value="{{$index}}">
								                	{{ page.pageName }}
								            	</md-option>
											  </md-select> 
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Data Validation</label>
									        <md-select class="labelStyle" required  data-ng-model="selectedRule">
											    <!-- <md-option data-ng-value="operationName" data-ng-repeat="operationName in operationNames">{{ operationName }}</md-option>  -->
											    <md-option data-ng-repeat="operation in operationNames track by $index" data-ng-value="{{$index}}">
								                	{{ operation.operationName }}
								            	</md-option>
											</md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Tenant Name</label>
								            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo(selectedValue9)" data-ng-model="selectedValue9">
									            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
									                {{ tenant.tenantName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <!--md-input-container flex="20" class="md-block" flex-gt-sm data-ng-hide="isSupplier">
								        	<md-button aria-label="" class="btn" data-ng-click="performSupplierPostLoad()"><span class="glyphicon glyphicon-send"></span>&nbsp;Execute</md-button>
								        </md-input-container-->
								    </div>
				        		</form>
				        	</div>
				        	<div class="statusPanel" data-ng-hide="isTenantBased">
				        		<form name="PostLoadForm" autocomplete="off">
				        		<h4 class="text-center pleaseWait">Post Load Validation</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
				                        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Build Cycle</label>
									          <md-select class="labelStyle" required  data-ng-change="getRuleNameListTenantBased()" data-ng-model="selectedLoad">
											    <!-- <md-option data-ng-value="load" data-ng-repeat="load in loadNames">{{ load }}</md-option> -->
											    <md-option data-ng-repeat="page in pages track by $index" data-ng-value="{{$index}}">
								                	{{ page.pageName }}
								            	</md-option>
											  </md-select> 
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Data Validation</label>
									        <md-select class="labelStyle" required  data-ng-model="selectedRule">
											    <!-- <md-option data-ng-value="operationName" data-ng-repeat="operationName in operationNames">{{ operationName }}</md-option>  -->
											    <md-option data-ng-repeat="operation in operationNames track by $index" data-ng-value="{{$index}}">
								                	{{ operation.operationName }}
								            	</md-option>
											</md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Source Tenant</label>
								            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo(selectedValue9)" data-ng-model="selectedValue9">
									            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
									                {{ tenant.tenantName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Target Tenant</label>
								            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo(selectedValue99)" data-ng-model="selectedValue99">
									            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
									                {{ tenant.tenantName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								    </div>
								    <!--div layout-gt-sm="row">
								    	<md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Termination From</label>
      										<md-datepicker ng-model="trmFrom" md-placeholder="Enter date"
                     							input-aria-describedby="datepicker-description"
                     								input-aria-labelledby="datepicker-header "></md-datepicker>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Termination To</label>
      										<md-datepicker ng-model="trmTo" md-placeholder="Enter date"
                     							input-aria-describedby="datepicker-description"
                     								input-aria-labelledby="datepicker-header "></md-datepicker>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Hire From</label>
      										<md-datepicker ng-model="hireFrom" md-placeholder="Enter date"
                     							input-aria-describedby="datepicker-description"
                     								input-aria-labelledby="datepicker-header "></md-datepicker>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Hire To</label>
      										<md-datepicker ng-model="hireTo" md-placeholder="Enter date"
                     							input-aria-describedby="datepicker-description"
                     								input-aria-labelledby="datepicker-header "></md-datepicker>
								        </md-input-container>
								    </div>
								    <section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								    	<md-button aria-label="" class="btn" data-ng-click="performTenantBasedPostLoad()"><span class="glyphicon glyphicon-send"></span>&nbsp;Execute</md-button>
								    </section-->
				        		</form>
				        	</div>
				        	<br><br>
				        	<div class="statusPanel" data-ng-hide="isFileBased">
				                <form id="sourceFileUploadForm" name="sourceFileUploadForm" autocomplete="off">
				                  <div layout-gt-sm="row">
				                	<md-input-container flex="50" class="md-block" flex-gt-sm>
										 <label  flex="20" class="labelStyle labelAddStyle" required>Select Legacy File</label>
								         <input type="file" class="btn btn-sm" id="sourceFileId" name="sourceFile" data-ng-model="sourceFile" /> 
									</md-input-container>
									<md-input-container flex="20" class="md-block" flex-gt-sm>										
										<md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-transfer"></span>&nbsp;Execute Files&nbsp;</md-button>
									</md-input-container>
								 </div>								 
				                </form>
				            </div>				            
						    <div class="statusPanel" data-ng-hide="isTenantBased">
				                <form id="wd2wdFileUploadForm" name="wd2wdFileUploadForm" autocomplete="off">
				                  <div layout-gt-sm="row">
				                	<md-input-container flex="50" class="md-block" flex-gt-sm>
										 <label  flex="20" class="labelStyle labelAddStyle" required>Select WD2WD File</label>
								         <input type="file" class="btn btn-sm" id="wdFileId" name="wdFile" data-ng-model="wdFile" /> 
									</md-input-container>
									<md-input-container flex="20" class="md-block" flex-gt-sm>										
										<md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-transfer"></span>&nbsp;Execute Files&nbsp;</md-button>
									</md-input-container>
								 </div>								 
				                </form>
				            </div>
				            <br><br>
				            <div class="statusPanel" data-ng-hide="isAllMapped">
				                <table id="columnTable" class="table statusTable tenantStatus">
								    <thead>
									    <tr>
									        <th class="col-sm-2"><input type="checkbox" ng-model="IsAllChecked" ng-change="checkUncheckAll()" /> Select </th>
									        <th class="col-sm-4" data-ng-hide="isFileBased">Source Column Name</th>
										    <th class="col-sm-4" data-ng-hide="isFileBased">Workday Column Name</th>
										    <th class="col-sm-4" data-ng-hide="isTenantBased">Source Fields</th>
										    <th class="col-sm-4" data-ng-hide="isTenantBased">Target Fields</th>
										    <th class="col-sm-2">Identifier</th>
									    </tr>
									</thead>
									<tbody>
									    <tr data-ng-repeat="mapFile in mapFiles" >
									    	<td class="col-sm-2"><input type="checkbox" ng-model="mapFile.isSelect" ng-change="checkUncheckHeader()"/></td>
										    <td class="col-sm-4">{{ mapFile.headingSource }}</td>
										    <td class="col-sm-4" ng-if="mapFile.heading != ''">{{ mapFile.heading }}</td>
										    <td class="col-sm-4" ng-if="mapFile.heading == ''">
												<md-select class="labelStyle" data-ng-model="selectedColumn[$index]">
											    	<md-option data-ng-repeat="heading in headerValues track by $index" data-ng-value="{{$index}}">
											    		{{ heading.headingWD }}
											    	</md-option>
											   </md-select> 									  
										    </td>
										    <td class="col-sm-2"><input type="checkbox" ng-model="mapFile.isChecked" ng-change="toggleKey(mapFile)"/></td>
									    </tr>
								    </tbody>
							    </table>
				            </div>
				            <br><br>
		            		<section layout="row" layout-sm="column" layout-align="center" layout-wrap data-ng-hide="isFileBased">							      
						        <md-button aria-label="" class="btn" data-ng-click="performComparison()" data-ng-hide="isCompared"><span class="glyphicon glyphicon-send"></span>&nbsp;Compare</md-button>							        
						    </section>
						    <section layout="row" layout-sm="column" layout-align="center" layout-wrap data-ng-hide="isTenantBased">							      
						        <md-button aria-label="" class="btn" data-ng-click="performComparisonTenantBased()" data-ng-hide="isCompared"><span class="glyphicon glyphicon-send"></span>&nbsp;Compare</md-button>							        
						    </section>
				        </div>
				        <div id="PostLoadValidation1" data-ng-show="container=='PL1'" class="row">
				        	<div class="statusPanel" >
				        		<form name="PostLoadForm" autocomplete="off">
				        		<h4 class="text-center pleaseWait">Post Load Validation</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
				                        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Build Cycle</label>
									          <md-select class="labelStyle" required  data-ng-change="getRuleNameListGF(selectedLoad)" data-ng-model="selectedLoad">
											    <!-- <md-option data-ng-value="load" data-ng-repeat="load in loadNames">{{ load }}</md-option> -->
											    <md-option data-ng-repeat="page in pages track by $index" data-ng-value="{{$index}}">
								                	{{ page.pageName }}
								            	</md-option>
											  </md-select> 
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Data Validation</label>
									        <md-select class="labelStyle" required  data-ng-model="selectedRule">
											    <!-- <md-option data-ng-value="operationName" data-ng-repeat="operationName in operationNames">{{ operationName }}</md-option>  -->
											    <md-option data-ng-repeat="operation in operationNames track by $index" data-ng-value="{{$index}}">
								                	{{ operation.operationName }}
								            	</md-option>
											</md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Tenant Name</label>
								            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo(selectedValue9)" data-ng-model="selectedValue9">
									            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
									                {{ tenant.tenantName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								    </div>
				        		</form>
				        	</div>
				        	<br><br>
				        	<div class="statusPanel">
				                <form id="srcFileUploadFormGF" name="srcFileUploadFormGF" autocomplete="off">
				                  <div layout-gt-sm="row">
				                	<md-input-container flex="50" class="md-block" flex-gt-sm>
										 <label  flex="20" class="labelStyle labelAddStyle" required>Select Legacy File</label>
								         <input type="file" class="btn btn-sm" id="srcFileIdGF" name="sourceFileGF" data-ng-model="sourceFileGF" /> 
									</md-input-container>
									<md-input-container flex="20" class="md-block" flex-gt-sm>										
										<md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-transfer"></span>&nbsp;Execute Files&nbsp;</md-button>
									</md-input-container>
								 </div>							 
				                </form>
				            </div>				            
				            <br><br>
				            <div class="statusPanel" data-ng-hide="isAllMapped">
				                <table id="columnTable" class="table statusTable tenantStatus">
								    <thead>
									    <tr>
									        <th class="col-sm-2"><input type="checkbox" ng-model="IsAllChecked" ng-change="checkUncheckAll()" /> Select </th>
									        <th class="col-sm-4">Source Column Name</th>
										    <th class="col-sm-4">Workday Column Name</th>
										    <th class="col-sm-2">Identifier</th>
									    </tr>
									</thead>
									<tbody>
									    <tr data-ng-repeat="mapFile in mapFiles" >
									    	<td class="col-sm-2"><input type="checkbox" ng-model="mapFile.isSelect" ng-change="checkUncheckHeader()"/></td>
										    <td class="col-sm-4">{{ mapFile.headingSource }}</td>
										    <td class="col-sm-4" ng-if="mapFile.heading != ''">{{ mapFile.heading }}</td>
										    <td class="col-sm-4" ng-if="mapFile.heading == ''">
												<md-select class="labelStyle" data-ng-model="selectedColumn[$index]">
											    	<md-option data-ng-repeat="heading in headerValues track by $index" data-ng-value="{{$index}}">
											    		{{ heading.headingWD }}
											    	</md-option>
											   </md-select> 									  
										    </td>
										    <td class="col-sm-2"><input type="checkbox" ng-model="mapFile.isChecked" ng-change="toggleKey(mapFile)"/></td>
									    </tr>
								    </tbody>
							    </table>
				            </div>
				            <br><br>
		            		<section layout="row" layout-sm="column" layout-align="center" layout-wrap>							      
						        <md-button aria-label="" class="btn" data-ng-click="performComparisonGF()"><span class="glyphicon glyphicon-send"></span>&nbsp;Compare</md-button>							        
						    </section>
				        </div>
				        
				        <div id="PostLoadValidation2" data-ng-show="container=='PL2'" class="row">
				        	<div class="statusPanel" >
				        		<form name="PostLoadForm" autocomplete="off">
				        		<h4 class="text-center pleaseWait">Post Load Validation</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
				                        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Build Cycle</label>
									          <md-select class="labelStyle" required  data-ng-change="getRuleNameListKD(selectedLoad)" data-ng-model="selectedLoad">
											    <md-option data-ng-repeat="page in pages track by $index" data-ng-value="{{$index}}">
								                	{{ page.pageName }}
								            	</md-option>
											  </md-select> 
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Data Validation</label>
									        <md-select class="labelStyle" required  data-ng-model="selectedRule">
											    <md-option data-ng-repeat="operation in operationNames track by $index" data-ng-value="{{$index}}">
								                	{{ operation.operationName }}
								            	</md-option>
											</md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Source Tenant</label>
								            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo(selectedValue9)" data-ng-model="selectedValue9">
									            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
									                {{ tenant.tenantName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								        <md-input-container flex="25" class="md-block" flex-gt-sm>
								            <label class="labelStyle">Target Tenant</label>
								            <md-select class="labelStyle" required data-ng-change="getUserTenantInfo1(selectedValue99)" data-ng-model="selectedValue99">
									            <md-option data-ng-repeat="tenant in userTenants track by $index" data-ng-value="{{$index}}">
									                {{ tenant.tenantName }}
									            </md-option>
									        </md-select>
								        </md-input-container>
								    </div>
				        		</form>
				        	</div>
				        	<br><br>
				        	<div class="statusPanel">
				                <form id="srcFileKyndryl" name="srcFileKyndryl" autocomplete="off">
				                  <div layout-gt-sm="row">
				                	<md-input-container flex="50" class="md-block" flex-gt-sm>
										 <label  flex="20" class="labelStyle labelAddStyle" required>Select File</label>
								         <input type="file" class="btn btn-sm" id="srcFileIdKyndryl" name="sourceFileKyndryl" data-ng-model="sourceFileKyndryl" /> 
									</md-input-container>
									<md-input-container flex="20" class="md-block" flex-gt-sm>										
										<md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-transfer"></span>&nbsp;Execute Files&nbsp;</md-button>
									</md-input-container>
								 </div>							 
				                </form>
				            </div>				            
				            <br><br>
				            <div class="statusPanel" data-ng-hide="isAllMapped">
				                <table id="columnTable" class="table statusTable tenantStatus">
								    <thead>
									    <tr>
									        <th class="col-sm-2"><input type="checkbox" ng-model="IsAllChecked" ng-change="checkUncheckAll()" /> Select </th>
									        <th class="col-sm-4">Source Fields</th>
										    <th class="col-sm-4">Target Fields</th>
										    <th class="col-sm-2">Identifier</th>
									    </tr>
									</thead>
									<tbody>
									    <tr data-ng-repeat="mapFile in mapFiles" >
									    	<td class="col-sm-2"><input type="checkbox" ng-model="mapFile.isSelect" ng-change="checkUncheckHeader()"/></td>
										    <td class="col-sm-4">{{ mapFile.headingSource }}</td>
										    <td class="col-sm-4" ng-if="mapFile.heading != ''">{{ mapFile.heading }}</td>
										    <td class="col-sm-4" ng-if="mapFile.heading == ''">
												<md-select class="labelStyle" data-ng-model="selectedColumn[$index]">
											    	<md-option data-ng-repeat="heading in headerValues track by $index" data-ng-value="{{$index}}">
											    		{{ heading.headingWD }}
											    	</md-option>
											   </md-select> 									  
										    </td>
										    <td class="col-sm-2"><input type="checkbox" ng-model="mapFile.isChecked" ng-change="toggleKey(mapFile)"/></td>
									    </tr>
								    </tbody>
							    </table>
				            </div>
				            <br><br>
		            		<section layout="row" layout-sm="column" layout-align="center" layout-wrap>							      
						        <md-button aria-label="" class="btn" data-ng-click="performComparisonTenantBasedKD()"><span class="glyphicon glyphicon-send"></span>&nbsp;Compare</md-button>							        
						    </section>
				        </div>
				        
						<div id="SendToWDResultConfig" data-ng-show="container=='SWR'" class="row">
					        <div id="navbar" class="navbar-collapse collapse">
					            <ul class="nav navbar-nav navbar-left">
						            <li>
						                <a href="javascript:void(0)" data-ng-click="flip()">
						                    <span data-ng-show="!flipValue" class="glyphicon glyphiconPlus glyphicon-menu-right"></span>
						                    <span data-ng-show="flipValue" class="glyphicon glyphiconPlus glyphicon-menu-left"></span>
						                </a>
						            </li>
					            </ul>
								<form class="form-horizontal" data-ng-if="flipValue">
								    <div class="row">
								        <div class="col-sm-3">
								            <div class="form-group">
								                <label class="control-label col-xs-2">Batch</label>
								                <div class="col-xs-4">
								                    <input type="text" class="form-control" id="inputBatch" data-ng-model="inputBatchNum">
								                </div>
								                <label class="control-label col-xs-1">/</label>
								                <label class="control-label col-xs-1">{{ wsResponsesStatus.totalBatches }}</label>
								            </div>
								        </div>
								        <div class="col-sm-1">
								            <button type="button" class="btn btn-primary" data-ng-click="viewBatchDetails(inputBatchNum)">Refresh</button>
								        </div>
								    </div>
								</form>					            
					        </div>
                            <section class="container1">
                                <div class="card">
                                    <div class="front">
                                    	<div class="statusPanel">
    							        	<div class="col-sm-12">
								            	<ui-knob value="percentageValue" options="uiKnobOptions" class="center-block text-center"></ui-knob>
							            	</div>
							        	</div>    
                                    </div>
                                    <div class="back">
                                    	<div class="statusPanel">                             	
								        	<table class="table statusTable sendWDStatus">
									        	<thead>
										        	<tr>
											            <th class="col-sm-1">Result ID</th>
											            <th class="col-sm-1">Result</th>
											            <th class="col-sm-3">Message</th>
											            <th class="col-sm-1">Batch</th>
											            <th class="col-sm-2">Request Date Time</th>
											            <th class="col-sm-2">Response Date Time</th>
											            <th class="col-sm-1">Total Time (ms)</th>
											            <th class="col-sm-1">Actions</th>
										        	</tr>
									        	</thead>
									        	<tbody>
										        	<tr data-ng-repeat="wsResponse in wsResponses">
												        <td class="col-sm-1">{{ wsResponse.name }}</td>
												        <td class="col-sm-1">{{ wsResponse.result }}</td>
												        <td class="col-sm-3">{{ wsResponse.message }}</td>
												        <td class="col-sm-1">{{ wsResponse.batchNo }}</td>
												        <td class="col-sm-2">{{ wsResponse.requestDateTimeText }}</td>
												        <td class="col-sm-2">{{ wsResponse.responseDateTimeText }}</td>
												        <td class="col-sm-1 ">{{ wsResponse.totalTime }}</td>
												        <td class="col-sm-1">
												        	<md-button aria-label="" class="md-icon-button md-primary" data-ng-click=""><span class="glyphicon glyphiconPlus glyphicon-edit"></span></md-button>
											        	</td>
										        	</tr>
									        	</tbody>
								        	</table>				            		            
                                    	</div>
                                    </div>
                                </div>
                            </section>
						    <br></br>
						    <div class="statusPanel" data-ng-if="!flipValue">
						    	<table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-3">Batch Count</th>
										    <th class="col-sm-3">Batch Running</th>
										    <th class="col-sm-3">Total Success</th>
										    <th class="col-sm-3">Total Failures</th>
										</tr>
									</thead>
									<tbody>
									    <tr>
											<td class="col-sm-3">
											    <div class="knobContainer">
												    <div class="outercircle	">
													    <div class="innercircle">
														    <div class="knob">
															    <div class="knoblabel">
																    {{ wsResponsesStatus.totalBatches }}
																</div>
															</div>
															<div class="knobrolling" data-ng-hide="isAllComplete">
															    <div class="pointer">
																</div>
															    <div class="leftdivider">
																</div>
																<div class="rightdivider">
																</div>
																<div class="divider">
																</div>
															</div>
														</div>
													</div>
													<div class="loader">
													</div>
												</div>	
											</td>
											<td class="col-sm-3">
											    <div class="knobContainer">
												    <div class="outercircle	">
													    <div class="innercircle">
														    <div class="knob">
															    <div class="knoblabel">
																    {{ wsResponsesStatus.currentRunningBatch }}
																</div>
															</div>
															<div class="knobrolling" data-ng-hide="isAllComplete">
															    <div class="pointer">
																</div>
															    <div class="leftdivider">
																</div>
																<div class="rightdivider">
																</div>
																<div class="divider">
																</div>
															</div>
														</div>
													</div>
													<div class="loader">
													</div>
												</div>	
											</td>
											<td class="col-sm-3">
											    <div class="knobContainer">
												    <div class="outercircle	">
													    <div class="innercircle">
														    <div class="knob">
															    <div class="knoblabel">
																    {{ wsResponsesStatus.totalSuccess }}
																</div>
															</div>
															<div class="knobrolling" data-ng-hide="isAllComplete">
															    <div class="pointer">
																</div>
															    <div class="leftdivider">
																</div>
																<div class="rightdivider">
																</div>
																<div class="divider">
																</div>
															</div>
														</div>
													</div>
													<div class="loader">
													</div>
												</div>	
											</td>
											<td class="col-sm-3">
											    <div class="knobContainer">
												    <div class="outercircle	">
													    <div class="innercircle">
														    <div class="knob">
															    <div class="knoblabel">
																    {{ wsResponsesStatus.totalFailures }}
																</div>
															</div>
															<div class="knobrolling" data-ng-hide="isAllComplete">
															    <div class="pointer">
																</div>
															    <div class="leftdivider">
																</div>
																<div class="rightdivider">
																</div>
																<div class="divider">
																</div>
															</div>
														</div>
													</div>
													<div class="loader">
													</div>
												</div>	
											</td>											
										</tr>
									</tbody>
								</table>
						    </div>
						    <section layout="row" layout-sm="column" layout-align="center center" layout-wrap data-ng-if="!flipValue">
						        <md-button aria-label="" class="btn" data-ng-click="getWWSXmlFiles()"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;Files(XML)</md-button>						    
							    <md-button aria-label="" class="btn" data-ng-click="getWWSErrorDataFiles()"><span class="glyphicon glyphiconPlus glyphicon-download"></span>&nbsp;Files(Error Data)</md-button>
							    <md-button aria-label="" class="btn" data-ng-click="stopSWExecution()"><span class="glyphicon glyphiconPlus glyphicon-stop"></span>&nbsp;Stop Request</md-button>
							</section>
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