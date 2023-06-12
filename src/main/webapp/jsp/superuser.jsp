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
<script type="text/javascript" src="../js/supercontroller.js"></script>
<script type="text/javascript" src="../js/angular-sanitize.js"></script>
<script type="text/javascript" src="../js/d3.min.js"></script>
<script type="text/javascript" src="../js/ng-knob.min.js"></script>
<script type="text/javascript" src="../js/ng-knob.js"></script>
<script type="text/javascript" src="../js/angular-tree-control.js"></script>
<script type="text/javascript" src="../js/context-menu.js"></script>

<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.10/d3.min.js"></script>
</head>
<body data-ng-app="superUser.app" class="mainBody">
    <div id="parentContainer" class="parentContainerStyle" data-ng-controller="superUser.controller"  data-ng-init="init()">
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
							<a href="javascript:void(0)" data-ng-click="initSuperUser(container='SU')"  data-ng-class="container=='SU'?'selected':''">Super User List</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initClientList(container='MC')"  data-ng-class="container=='MC'?'selected':''">Maintain Clients</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initAdminUser(container='AL')"  data-ng-class="container=='AL'?'selected':''">Admin List</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initUserAccess(container='UA')"  data-ng-class="container=='UA'?'selected':''">User Access</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initTenantDetails(container='TD')"  data-ng-class="container=='TD'?'selected':''">Tenant Details</a>
						</li>
					</ul>
					<md-divider></md-divider>
					<ul class="nav nav-sidebar">
						<li class="heading">
							<a href="javascript:void(0)" data-ng-click="initUploadFile(container='UF')"  data-ng-class="container=='UF'?'selected':''">Upload File</a>
						</li>
					</ul>
					<md-divider></md-divider>
		        </div>
		        <div id="Content" class="col-sm-10 col-sm-offset-2">
		            <div data-ng-show="container" class="contentClass">
		                <div id="superUserContent" data-ng-show="container=='SU'" class="row">
		                    <div class="statusPanel">
				                <form name="superUserForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Create SuperUser</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
									    <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Name</label>
							                <input required data-ng-model="superUser.name">
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Email</label>
							                <input required data-ng-model="superUser.email">
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Password</label>
							                <input type="password" required data-ng-model="superUser.password">
							            </md-input-container>			                    
				                    </div>
									<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								        <md-button aria-label="" type="reset" class="btn" data-ng-click="resetSuperUserForm()"><span class="glyphicon glyphicon-repeat"></span>&nbsp;Clear</md-button>
								        <md-button aria-label="" type="submit" class="btn" data-ng-click="submitSuperUserForm()"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
								    </section>				                    
				                </form>                    
		                    </div>
		                    <br></br>
				            <div class="statusPanel">
				                <h4 class="text-center pleaseWait">SuperUser</h4>
				                <div class="smallSpacer"></div>
				                <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-6">Name</th>
										    <th class="col-sm-6">Email</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="user in superUsers" >
											<td class="col-sm-6">{{ user.userName }}</td>
											<td class="col-sm-6">{{ user.userEmail }}</td>
										  </tr>
								    </tbody>				                
				                </table>
				            </div>		                    
		                </div>
		                <div id="clientContent" data-ng-show="container=='MC'" class="row">
		                    <div class="statusPanel">
				                <form name="clientForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Add Client</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
									    <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Client Name</label>
							                <input required data-ng-model="client.clientName">
							            </md-input-container>
                                        <md-datepicker data-ng-model="client.clientExpirationDate" md-placeholder="Enter date"></md-datepicker>						            
							            <md-input-container class="md-block" flex-gt-sm>
									        <section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								                <md-button aria-label="" type="submit" class="btn" data-ng-click="submitClientForm()"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
								            </section>
							            </md-input-container>			                    
				                    </div>				                    
				                </form>		                    
		                    </div>
		                    <br></br>
		                    <div class="statusPanel">
				                <h4 class="text-center pleaseWait">Client List</h4>
				                <div class="smallSpacer"></div>
				                <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-4">Client Name</th>
										    <th class="col-sm-4">Last Date</th>
										    <th class="col-sm-4">Action</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="client in clients" >
											<td class="col-sm-4">{{ client.clientName }}</td>
											<td class="col-sm-4">{{ getClientExpiration(client) }}</td>
											<td class="col-sm-4">
												 <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteClient(client)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
											</td>
										  </tr>
								    </tbody>				                
				                </table>		                    
		                    </div>		                    
		                </div>		                
		                <div id="adminContent" data-ng-show="container=='AL'" class="row">
		                    <div class="statusPanel">
				                <form name="adminForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Create Admin</h4>
				                    <div class="smallSpacer"></div>
				                    <div layout-gt-sm="row">
									    <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Name</label>
							                <input required data-ng-model="admin.userName">
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Email</label>
							                <input required data-ng-model="admin.userEmail">
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Password</label>
							                <input type="password" required data-ng-model="admin.userPassword">
							            </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
							                <label class="labelStyle">Client</label>
							                <md-select class="labelStyle" required data-ng-change="getSelectedClient(selectedValue1)" data-ng-model="selectedValue1">
								                <md-option data-ng-repeat="client in clients track by $index" data-ng-value="{{$index}}">
								                    {{ client.clientName }}
								                </md-option>
								            </md-select>
							            </md-input-container>							            		                    
				                    </div>
									<section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								        <md-button aria-label="" type="reset" class="btn" data-ng-click="resetAdminForm()"><span class="glyphicon glyphicon-repeat"></span>&nbsp;Clear</md-button>
								        <md-button aria-label="" type="submit" class="btn" data-ng-click="submitAdminForm(selectedClient)"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Save</md-button>
								    </section>				                    
				                </form>		                    
		                    </div>
		                    <br></br>
		                    <div class="statusPanel">
				                <h4 class="text-center pleaseWait">Admin List</h4>
				                <div class="smallSpacer"></div>
				                <div layout-gt-sm="row">
							        <md-input-container flex="30" class="md-block" flex-gt-sm>
							            <label class="labelStyle">Client</label>
							            <md-select class="labelStyle" required data-ng-change="getAdminsByClient(selectedValue2)" data-ng-model="selectedValue2">
								            <md-option data-ng-repeat="client in clients track by $index" data-ng-value="{{$index}}">
								                {{ client.clientName }}
								            </md-option>
								        </md-select>
							        </md-input-container>				                
				                </div>
				                <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-4">Name</th>
										    <th class="col-sm-4">Email</th>
										    <th class="col-sm-4">Client</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="admin in admins" >
											<td class="col-sm-4">{{ admin.userName }}</td>
											<td class="col-sm-4">{{ admin.userEmail }}</td>
											<td class="col-sm-4">{{ admin.client }}</td>
										  </tr>
								    </tbody>				                
				                </table>		                    
		                    </div>
		                </div>
		                <div id="UserContent" data-ng-show="container=='UA'" class="row">
		                    <div class="statusPanel">
		                        <h4 class="text-center pleaseWait">User List</h4>
				                <div class="smallSpacer"></div>
				                <div layout-gt-sm="row">
							        <md-input-container flex="30" class="md-block" flex-gt-sm>
							            <label class="labelStyle">Client</label>
							            <md-select class="labelStyle" required data-ng-change="getUsersByClient(selectedValue3)" data-ng-model="selectedValue3">
								            <md-option data-ng-repeat="client in clients track by $index" data-ng-value="{{$index}}">
								                {{ client.clientName }}
								            </md-option>
								        </md-select>
							        </md-input-container>				                
				                </div>
				                <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-2">Name</th>
										    <th class="col-sm-3">Email</th>
										    <th class="col-sm-2">Admin Access</th>
										    <th class="col-sm-2">User Access</th>
										    <th class="col-sm-3">Create Time</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="user in users" >
											<td class="col-sm-2">{{ user.userName }}</td>
											<td class="col-sm-3">{{ user.userEmail }}</td>
											<td class="col-sm-2">
											    <md-checkbox data-ng-model="isAdminAccess" aria-label="checkbox1" data-ng-init="isAdminAccess=user.admin" data-ng-change="toggleAdminAccess(user, isAdminAccess)"></md-checkbox>
                                            </td>
											<td class="col-sm-2">
											    <md-checkbox data-ng-model="isUserAccess" aria-label="checkbox2" data-ng-init="isUserAccess=user.userAccess" data-ng-change="toggleUserAccess(user, isUserAccess)"></md-checkbox>
											</td>
											<td class="col-sm-3">{{ getDate(user) }}</td>
										  </tr>
								    </tbody>				                
				                </table>				                				                
		                    </div>
		                </div>
		                <div id="TenantContent" data-ng-show="container=='TD'" class="row">
		                    <div class="statusPanel">
				                <form name="tenantForm" autocomplete="off">
				                    <h4 class="text-center pleaseWait">Create Tenant</h4>
									<div class="smallSpacer"></div>		
									<div layout-gt-sm="row">
									    <md-input-container flex="20" class="md-block" flex-gt-sm>
							                <label class="labelStyle">Tenant</label>
							                <input required data-ng-model="tenantEditable.tenantName">
							            </md-input-container>
									    <md-input-container flex="30" class="md-block" flex-gt-sm>
							                <label class="labelStyle">Data Center</label>
							                <md-select class="labelStyle" required data-ng-change="getTenantUrl(selectedValue4)" data-ng-model="selectedValue4">
								                <md-option data-ng-repeat="dataCenter in dataCenters track by $index" data-ng-value="{{$index}}">
								                    {{dataCenter}}
								                </md-option>
								            </md-select>								            
							            </md-input-container>
										<md-input-container flex="30" class="md-block" flex-gt-sm>
								            <label class="labelStyle">End Point URL</label>
                                            <input required data-ng-model="tenantEditable.tenantUrl">
								        </md-input-container>
							            <md-input-container flex="20" class="md-block" flex-gt-sm>
							                <label class="labelStyle">Client</label>
							                <md-select class="labelStyle" required data-ng-change="getSelectedClient(selectedValue5)" data-ng-model="selectedValue5">
								                <md-option data-ng-repeat="client in clients track by $index" data-ng-value="{{$index}}">
								                    {{ client.clientName }}
								                </md-option>
								            </md-select>
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
				                <h4 class="text-center pleaseWait">Tenant List</h4>
				                <div class="smallSpacer"></div>
				                <div layout-gt-sm="row">
							        <md-input-container flex="30" class="md-block" flex-gt-sm>
							            <label class="labelStyle">Client</label>
							            <md-select class="labelStyle" required data-ng-change="getTenantsByClient(selectedValue6)" data-ng-model="selectedValue6">
								            <md-option data-ng-repeat="client in clients track by $index" data-ng-value="{{$index}}">
								                {{ client.clientName }}
								            </md-option>
								        </md-select>
							        </md-input-container>				                
				                </div>
				                <table class="table statusTable tenantStatus">
									<thead>
										<tr>
										    <th class="col-sm-3">Tenant</th>
										    <th class="col-sm-3">Data Center</th>
										    <th class="col-sm-4">End Point URL</th>
										    <th class="col-sm-2">Actions</th>
										</tr>
									</thead>
									 <tbody>
										  <tr data-ng-repeat="tenant in tenants" >
											<td class="col-sm-3">{{ tenant.tenantName }}</td>
											<td class="col-sm-3">{{ tenant.tenantDataCenter }}</td>
											<td class="col-sm-4">{{ tenant.tenantUrl }}</td>
											<td class="col-sm-2">
												 <md-button aria-label="" class="md-icon-button md-primary" data-ng-click="editTenant(tenant)"><span class="glyphicon glyphiconPlus glyphicon-edit"></span></md-button>
												 <md-button aria-label="" class="md-icon-button md-warn" data-ng-click="deleteTenant(tenant)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span></md-button>
											</td>
										  </tr>
								    </tbody>				                
				                </table>		                    
		                    </div>
		                </div>
		                <div id="UploadFileContent" data-ng-show="container=='UF'" class="row">
<!-- 		                    <div class="statusPanel">
		                        <h4 class="text-center pleaseWait">Load Operation Directory Data</h4>
		                        <form name="operationDirForm" autocomplete="off">
								    <md-input-container flex="30" class="md-block" flex-gt-sm>
								        <label class="labelStyle">File Name</label>
								        <md-select class="labelStyle" required data-ng-model="fileIndex">
									        <md-option data-ng-repeat="file in fileList track by $index" data-ng-value="{{$index}}">
									            {{ file.fileName }}
									        </md-option>
									    </md-select>
								    </md-input-container>
							            <md-input-container class="md-block" flex-gt-sm>
									        <section layout="row" layout-sm="column" layout-align="center center" layout-wrap>
								                <md-button aria-label="" type="submit" class="btn" data-ng-click="submitOperationDirForm()"><span class="glyphicon glyphicon-floppy-disk"></span>&nbsp;Upload Data</md-button>
								            </section>
							            </md-input-container>
		                        </form>
		                    </div>
		                    <br></br>
-->
		                    <div class="statusPanel">
		                        <h4 class="text-center pleaseWait">Upload File</h4>
				                <form id="fileUploadForm" name="fileUploadForm" autocomplete="off">
				                    <div layout-gt-sm="row">
								        <md-input-container flex="30" class="md-block" flex-gt-sm>
								            <label class="labelStyle">File Name</label>
								            <input required data-ng-model="fileEditable.fileName">
								        </md-input-container>
								        <md-input-container flex="50" class="md-block" flex-gt-sm>
										    <label  flex="20" class="labelStyle labelAddStyle" required>Select File</label>
								            <input type="file" class="btn btn-sm" id="selectedFileId" name="selectedFile" accept=".xml" data-ng-model="selectedFile" /> 
										</md-input-container>
										<md-input-container flex="20" class="md-block" flex-gt-sm>
										    <md-button aria-label="" type="submit" class="btn"><span class="glyphicon glyphicon-transfer"></span>&nbsp;Upload File&nbsp;</md-button>
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
									    <tr data-ng-repeat="file in fileList" >
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