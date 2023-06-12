var myApp = angular.module('testing.automation', [ 'ngAnimate', 'ngMaterial', 'ngMessages', 'ngSanitize', 'ui.bootstrap', 'chart.js' ]);

var contents;
var filesData =[];
myApp.directive('fileReader', function() {
	  return {
	    scope: {
	      fileReader:"="
	    },
	    link: function(scope, element) {
	      $(element).on('change', function(changeEvent) {
	        var files = changeEvent.target.files;
	        angular.forEach(files, function(value,key) {
				var formData = new FormData();
				formData.append(0, value);
				filesData.push(formData);
			});
	        if (files.length) {
	          var r = new FileReader();
	          r.onload = function(e) {
	              contents = e.target.result;
	              scope.$apply(function () {
	                scope.fileReader = contents;
	                scope.testing = contents;
	              });
	          };	          
	          r.readAsText(files[0]);
	        }
	      });
	    }
	  };
	});
		
	myApp.controller(
			'testingCtrl',
			[
				'$scope',
				'$http',
				'$uibModal',
				'$rootScope',
				function($scope, $http, $uibModal, $rootScope) {
				
				$scope.waitModal = undefined;
				
				$testingCtrl = $scope;

				$scope.openWaitModal = function () {
					$scope.waitModal = $uibModal.open({
				        backdrop: 'static',
				        ariaDescribedBy: 'modal-body',
				        templateUrl: 'modalPleaseWait.html',
				        size: 'sm'
					});
				}

				$scope.closeWaitModal = function () {
					$scope.waitModal.close();
				}
				
		        $scope.deleteAllRowTenant = function () {

		        	try {
		    			var table = document.getElementById('dataTable');
		    			var rowCount = table.rows.length;
		    			var remValues = new Array();
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;				    				
		    				}			    				
		    			}
		    			}
		        	catch(e) 
		        	{
		    			//alert(e);
		    		}
		        };
		        
		        $scope.deleteAllRowRule = function () {

		        	try {
		    			var table = document.getElementById('buildTable');
		    			var rowCount = table.rows.length;
		    			var remValues = new Array();
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;				    				
		    				}			    				
		    			}
		    			}
		        	catch(e) 
		        	{
		    			//alert(e);
		    		}
		        };
		        
		        $scope.deleteAllRowMap = function () {

		        	try {
		    			var table = document.getElementById('MapfileTable');
		    			var rowCount = table.rows.length;
		    			var remValues = new Array();
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;				    				
		    				}			    				
		    			}
		    			}
		        	catch(e) 
		        	{
		    			//alert(e);
		    		}
		        };
		        
		        $scope.deleteAllRowValidate = function () {

		        	try {
		    			var table = document.getElementById('ValidateDataTable');
		    			var rowCount = table.rows.length;
		    			var remValues = new Array();
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;				    				
		    				}			    				
		    			}
		    			}
		        	catch(e) 
		        	{
		    			//alert(e);
		    		}
		        };
		        
		        $scope.deleteAllRowSentToWd = function () {
		        	
		        	document.getElementById('tenPwd').value = "";		        	
		        	try {
		    			var table = document.getElementById('SendToWorkdayTable');
		    			var rowCount = table.rows.length;
		    			var remValues = new Array();
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;				    				
		    				}			    				
		    			}
		    			}
		        	catch(e) 
		        	{
		    			//alert(e);
		    		}
		        };
		        
		        $scope.deleteAllRowAppl = function () {

		        	try 
		        	{
		    			var table = document.getElementById('appTable');
		    			var rowCount = table.rows.length;
		    			var remValues = new Array();
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;				    				
		    				}			    				
		    			}
		    		}
		        	catch(e) 
		        	{
		    			//alert(e);
		    		}
		        };
		        
		        $scope.deleteAllRowSFTP = function(){
					
					try 
					{
		    			var table = document.getElementById('SFTPDataTable');
		    			var rowCount = table.rows.length;
		    			for(var i=0; i<rowCount; i++) {
		    				if( i != 0)
		    				{
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;			    				
		    				}
		    		}
		    		}
					catch(e) 
		    		{
		    			//alert(e);
		    		}
				};
				
				$scope.logedInAs =  (administrator ? "Logged in as Admin ": "Logged in as " )+ userName;
				
			   $scope.alerts = [];
               $scope.addAlert = function(type, msg) {
                //  $scope.alerts.push({type: type, msg: msg});
            	   $scope.alerts = [{type: type, msg: msg}];
               };

               $scope.closeAlert = function(index) {
                 $scope.alerts.splice(index, 1);
               };
					
				$scope.initTab = function(tabName) {
					
	            	   $scope.tabName = tabName;
	            	   if('Tenant' == tabName)
	            	   {
	            		   $scope.deleteAllRowTenant();
						   $scope.openWaitModal();
	            		   $scope.populateTenants();	            		   
	            	   }
	            	   else if('Appl' == tabName)
	            	   {
	            		   $scope.deleteAllRowAppl();
						   $scope.openWaitModal();
	            		   $scope.populateAppl();
	            	   }
	            	   else if('Rule' == tabName)
	            	   {
						   $scope.openWaitModal();
	            		   $scope.populateAppl();
	            	   }
	            	   else if('SFTP' == tabName)
	            	   {
	            		   $scope.deleteAllRowSFTP();
						   $scope.openWaitModal();
	            		   $scope.populateSFTP();
	            	   }
	            	   else if('mapFile' == tabName) 
	            	   {
	            		   //$scope.deleteAllRowMap();
						   $scope.openWaitModal();
	            		   $scope.retrieveAllRuleDataForMap();
	            		   $scope.populateSFTP();
	            	   }
	            	   else if('validate' == tabName)
	            	   {
	            		   $scope.deleteAllRowValidate();
						   $scope.openWaitModal();
	            		   $scope.retrieveAllRuleDataForMap();
	            		   $scope.populateTenants();
	            	   }
	            	   else if('sendToWorkday' == tabName) 
	            	   {
	            		   $scope.deleteAllRowSentToWd();
						   $scope.openWaitModal();
	            		   $scope.retrieveAllRuleDataForMap();
	            		   $scope.populateTenants();
	            	   } 
	            	   else if('Settings' == tabName) 
	            	   {
	            		   $scope.populateUserList();
	            	   }
	            	   else if('PGPEncryption' == tabName) 
	            	   {
	            		   $scope.populatePublicKeyList();
	            	   }
	            	   else if('HistoricalTransaction' == tabName) 
	            	   {
	            		   $scope.openWaitModal();
	            		   $scope.populateTenants();
	            	   }
		           };
		           
					$scope.populateTenants = function() {
						$http.get("tenantServlet?requestType=tenant")
								.then(function(response) {
									$scope.closeWaitModal();
									$scope.tenants=response.data;
									if(response.data == '' && $scope.tabName == 'Settings')
										$scope.addAlert('warning','There are no tenants configured yet. Please create a tenant first.')	
								});
					}
					
					$scope.populateAppl = function() {
						$http.get("MaintApplServlet?requestType=application")
								.then(function(response) {
									$scope.closeWaitModal();
									$scope.applications = response.data;
									if(response.data == '' && $scope.tabName == 'Settings')
										$scope.addAlert('warning','There are no application configured yet. Please create an application first.')	
								});

					}	
					
					$scope.populateSFTP = function() {
						$http.get("SFTPServlet?requestType=sftp")
								.then(function(response) {
									$scope.closeWaitModal();
									$scope.sftps = response.data;
									if(response.data == '' && $scope.tabName == 'Settings')
										$scope.addAlert('warning','There are no application configured yet. Please create an application first.')	
								});

					}
					
					$scope.retrieveAllRuleDataForMap = function(){
			        	
						$scope.mappedFileH2=true;
						$scope.mappedFileH1=false;
			   			$http.get("MapFileServlet?requestType=retrieveAllRuleData").then(function(response) {
			   				$scope.closeWaitModal();
			   				$scope.ruleData=response.data;							
			   				if(response.data == '')
			   					$scope.addAlert('warning','There are no test suites configured yet. Please create a test suite.')
			   			},
			   			function(response) { 
			   				console.log(response);
			   			});
		   			};
		    		   
	    		   $scope.populateUserList = function() {
						if(administrator){
							$scope.openWaitModal();
							$http.get("LoginTrigger?requestType=getAllUserInfo")
							.then(function(response) {	
								$scope.allUserInfo=response.data;
								$scope.closeWaitModal();
							});
						}
					}
	    		   
	    		   $scope.populatePublicKeyList = function() {	    			   
	    			   $scope.openWaitModal();
	    			   $http.get("PGPServlet?requestType=pgpPulicKey")
						.then(function(response) {
							$scope.closeWaitModal();
							$scope.publicKey = response.data;	
						});
					}
	    		   
					/*$scope.retrieveAllOperationDataHistory = function(){
			        	
			   			$http.get("HistDataServlet?requestType=retrieveAllOperationByUser").then(function(response) {
			   				$scope.closeWaitModal();
			   				$scope.operationData=response.data;							
			   				if(response.data == '')
			   					$scope.addAlert('warning','There are no test suites configured yet. Please create a test suite.')
			   			},
			   			function(response) { 
			   				console.log(response);
			   			});
		   			};*/
			} ]);
	
	myApp.controller(
			'BuildCtrl',
			[
				'$scope',
				'$http',				
				function($scope, $http) {
					
				var applNm;
				var applVr;
					
				$scope.getVersionOperation = function(appName){

					applNm = appName;
					if(appName != null)
					{
						applVr = appName.applicationVersion;
						
						$http.get("RuleServlet?appOperation=" +appName.applicationId)
						.then(function(response) {								
							$scope.appOperation = response.data;
							if(response.data == '' && $scope.tabName == 'Settings')
								$scope.addAlert('warning','There are no application configured yet. Please create an application first.')	
						});
						
						$http.get("RuleServlet?appIdVer=" +appName.applicationId)
						.then(function(response) {								
							$scope.appVersions = response.data;
							if(response.data == '' && $scope.tabName == 'Settings')
								$scope.addAlert('warning','There are no application configured yet. Please create an application first.')	
						});	
					}																				
				};
				
				$scope.getRules = function(appName, appVers){

					$scope.deleteAllBuildRow();
					if(appVers != null 	&& appName != null)
					{
						$http.get("RuleServlet?appRules=" +appName.applicationId)
						.then(function(response) {								
							$scope.rules = response.data;
							if(response.data == '' && $scope.tabName == 'Settings')
								$scope.addAlert('warning','There are no application configured yet. Please create an application first.')	
						});		
					}
				};
				
				$scope.enableAllOnEditBuild = function(appName,appVer) {
					
					 if(appName != undefined && appVer != undefined)
					 {
						 document.getElementById("buildAdd").disabled = false;
						 document.getElementById("buildDel").disabled = false;
						 document.getElementById("ruleSave").disabled = false;
						 document.getElementById("ruleCan").disabled = false;
					 }
					 else
					 {
						 alert("Please select Application Name and Application Version");
					 }

				 };
				 
		        $scope.disableAllOnCancelBuild = function() {
		        	
					 document.getElementById("buildAdd").disabled = true;
					 document.getElementById("buildDel").disabled = true;
					 document.getElementById("ruleSave").disabled = true;
					 document.getElementById("ruleCan").disabled = true;
					 
					 var myTable = document.getElementById('buildTable');
					 var rowCount = myTable.rows.length;
					 var dataSize = $scope.rules.length;
					 if(rowCount > dataSize)
					 {
						 var actDataSize = dataSize + 1;
						 for(var i = actDataSize; i < rowCount; i++)
						 {
							 myTable.deleteRow(i);
		    				 rowCount--;
		    				 i--;
						 }
					 }
		        }
		        
		        $scope.addBuildRow = function (tableID) {						

		        	if(applNm != null && applVr != undefined)
					{
		        		if($scope.appOperation.length > 0)
		        		{
		        			var option;						
							var table = document.getElementById(tableID);
		
							var rowCount = table.rows.length;
							var row = table.insertRow(rowCount);
							
							var cell1 = row.insertCell(0);
							var element1 = document.createElement("input");
							element1.type = "checkbox";
							element1.name="chkbox[]";
							cell1.appendChild(element1);
							
							var cell2 = row.insertCell(1);
							var element2 = document.createElement("input");
							element2.type = "text";
							element2.name = "txtbox[]";
							cell2.appendChild(element2);
							cell2.innerHTML = '<input type="text" style="width:100%;" value="Rule Name">';
		
							var cell3 = row.insertCell(2);
							var element3 = document.createElement("select");
							for (var i = 0; i < $scope.appOperation.length; i++) {
							    option = document.createElement("option");
							    option.value = $scope.appOperation[i].operationName;
							    option.text = $scope.appOperation[i].operationName;
							    element3.appendChild(option);
							}
							cell3.appendChild(element3);
		
							var cell4 = row.insertCell(3);
							var element4 = document.createElement("input");
							element4.type = "text";
							element4.name = "txtbox[]";
							cell4.appendChild(element4);
							cell4.innerHTML = '<input type="text" style="width:100%;" disabled="disabled">';
		        		}
		        		else
		        		{
		        			alert("Please add operation for the selected application");
							return;
		        		}						
					}
		        	else
					{
						 alert("Please select Application Name and Application Version");
						 return;
					}

		        };
		        
		        $scope.deleteAllBuildRow = function () {

		        	try {
		    			var table = document.getElementById('buildTable');
		    			var rowCount = table.rows.length;
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{		    					
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;			    				
		    				}			    				
		    			}
		    			}catch(e) {
		    				//alert(e);
		    			}
		        };
		        
		        $scope.deleteBuildRow = function (tableID) {

		        	if(applNm != null && applVr != undefined)
					{
			        	var cnt = -1;
			        	try {
			    			var table = document.getElementById(tableID);
			    			var rowCount = table.rows.length;
			    			var remBuildVal = new Array();
			    			for(var i=0; i<rowCount; i++) 
			    			{
			    				if( i != 0)
			    				{
			    					cnt++;
			    					var row = table.rows[i];
				    				var chkbox = row.cells[0].childNodes[0];
				    				if(null != chkbox && true == chkbox.checked) 
				    				{
				    					table.deleteRow(i);
				    					remBuildVal.push($scope.rules[cnt].ruleId);
				    					rowCount--;
				    					i--;
				    				}
			    				}			    				
			    			}
			    			}catch(e) {
			    				alert(e);
			    			}
	
			    			var paramBuildRemove = '?removeBuildArray=' + JSON.stringify(remBuildVal);
					        $.ajax({
					             url: 'RuleServlet'+paramBuildRemove,
					             type: 'POST', 
					             dataType: 'json',  
					             success: function(result) {
					            	 //$scope.deleteAllRowMap();
					            	 $scope.deleteAllRowValidate();
					            	 $scope.deleteAllRowSentToWd();
					            	 $scope.getRules(applNm, applVr);
					             }
					        });
					}
		        	else
		        	{
		        		alert("Please select Application Name and Application Version");
						return;
		        	}
		        };
		        
		        $scope.checkRuleName = function () {
		        	
		        	var myTab = document.getElementById('buildTable');
	    			var rowCount = myTab.rows.length;
		            var element;

		            for (var i=1; i < rowCount; i++) {	
		            	var row1 = myTab.rows[i];
		            	var currentElement = row1.cells[1].childNodes[0].data;
		            	if(currentElement == undefined)
		            	{
		            		currentElement = myTab.rows.item(i).cells[1].childNodes[0].value;
		            	}
		            	for (var j = i + 1; j < rowCount; j++) {
		            		var row2 = myTab.rows[j];
		                    var nextElement = row2.cells[1].childNodes[0].data;
		                    if(nextElement == undefined)
		                    {
		                    	nextElement = myTab.rows.item(j).cells[1].childNodes[0].value;
		                    }
		                    
		                    if(currentElement == nextElement) 
		                    {
		                    	alert("Rule name can not be same. Please select a different Rule")
		                        return;
		                    }
		                }
		            }
		            $scope.saveRuleData();
		        };
		        
		        $scope.saveRuleData =  function() {
		            var myTab = document.getElementById('buildTable');
		            var ruleValues = new Array();
		            var element;

		            for (row = 1; row < myTab.rows.length; row++) {
		            	var obj = {
			            	    ruleName:"",
			            	    operationName:""
			            	};
		            	
		            	if(row <= $scope.rules.length)
		            	{
		            		ruleValues.push($scope.rules[row-1].ruleId);
		            	}
		            	else
		            	{
		            		ruleValues.push(0);
		            	}
		            	
		                for (c = 0; c < myTab.rows[row].cells.length; c++) {   
		                	if(c != 0){			                		
		                		if(c == 1)
		                		{
		                			element = myTab.rows.item(row).cells[c];
		                			obj.ruleName = element.childNodes[0].value;
		                			if(obj.ruleName != undefined && obj.ruleName != null)
		                			{
		                				ruleValues.push(obj.ruleName);
		                			}				                			
		                		}
		                		if(c == 2)
		                		{
		                			element = myTab.rows.item(row).cells[c];
		                			obj.operationName = element.childNodes[0].value;
		                			if(obj.operationName != undefined && obj.operationName != null)
		                			{
		                				ruleValues.push(obj.operationName);
		                			}
		                			else
		                			{
		                				ruleValues.push(myTab.rows.item(row).cells[c].childNodes[0].data);
		                			}	
		                		}			                		
		                   }
		                }
		                if(ruleValues.length >0)
		                {
		                	ruleValues.push(";");
		                }

		            }
		            console.log(ruleValues);
		            console.log(applNm);
		            console.log(applVr);
		            
		            var paramRule = '?ruleArray=' + JSON.stringify(ruleValues);
			        $.ajax({
			             url: 'RuleServlet'+paramRule,
			             type: 'POST', 
			             dataType: 'json',  
			             success: function(result) {
			            	 $scope.getRules(applNm, applVr);
			               //alert('SUCCESS');
			             }
			        });
		        };
		        
		        $scope.toggleRule = function(item) {
		        	
		        	angular.forEach($scope.rules, function(rule) {
		        	      if(rule != item) {
		        	    	  rule.isChecked = false;  
		        	      }
		        	    });			        	
		        };
		        
		        $scope.ruleBuilder = function() {
		        	
    				if($scope.appRule != null && $scope.appVer != null)
    				{
			        	var count = -1;
			            var table = document.getElementById('buildTable');
		    			var rowCount = table.rows.length;
		    			var ruleBuild = new Array();
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					count++;
		    					var row = table.rows[i];
			    				var chkbox = row.cells[0].childNodes[0];
			    				if(null != chkbox && true == chkbox.checked) 
			    				{
			    					if($scope.rules[count] == undefined)
			    					{
			    						alert("Please save the rule first before proceed further");
			    						return;
			    					}
			    					else
			    					{
			    						ruleBuild.push($scope.rules[count].ruleId);
				    					$scope.ruleName = row.cells[1].childNodes[0].value;
				    					ruleBuild.push($scope.ruleName);
				    					$scope.operName = row.cells[2].childNodes[0].data;
				    					if($scope.operName == undefined)
				    					{
				    						$scope.operName = row.cells[2].childNodes[0].value;
				    					}
				    					ruleBuild.push($scope.operName);
				    					ruleBuild.push($scope.appRule.applicationName);
				    					ruleBuild.push($scope.appVer.appVersion);
			    					}			    					
			    				}
		    				}			    				
		    			}		    			
		    			console.log(ruleBuild);		    			
		    			if(ruleBuild.length >0)
		    			{	
		    				window.open("rulebuilder-main.jsp?parameter="+ruleBuild, "_blank", "width=1000,height=600,scrollbars=yes, menubar=no,resizable=yes");		    				
		    			}
		    			else
		    			{
		    				alert("Please select one Rule");
		    			}
		        }
		        else
				{
					alert("Please select a proper application name/version");
				}
		        
		     };
					
		} ]);
	
	myApp.controller(
			'MapFileCtrl',
			['$scope','$http','$window','$timeout', 
			function($scope, $http, $window, $timeout) {
				
				var operationId;
				var ruleId;
				$scope.listFileData = [];
				var objList = [];
				var csvContent = null;
				var sftpPath;
				
				
				$scope.selectedRuleNameChangedMap = function(){
					
					if($scope.selectedRuleData != null)
					{
						operationId = ($scope.selectedRuleData.operationId).toString();	
						ruleId = ($scope.selectedRuleData.ruleId).toString();
					}
					
					if(operationId != null && operationId != undefined)
					{
						$http.get("MapFileServlet?requestType=retriveOperationAndApplicationData&operationId="+operationId).then(function(response) {	
			   				$scope.opprApplData=response.data;			   				
			   				if(response.data == '')
			   					$scope.addAlert('warning','There are no test Operation and Application configured yet.');
			   			},
			   			function(response) { 
			   				console.log(response);
			   			});
					}
					
					if(ruleId!=null){
						$http.get("MapFileServlet?requestType=retriveMapFileByRule&ruleId="+ruleId).then(function(response) {	
			   				$scope.listFileData = response.data;
			   				objList = $scope.listFileData;
			   				if(response.data == '')
			   					$scope.addAlert('warning','There are no Mapped File configured yet.');
			   			},
			   			function(response) { 
			   				console.log(response);
			   			});
					}
					
					if($scope.checkStatus==true)
					{
						$scope.mappedFileH1=true;
						$scope.mappedFileH2=false;
						$scope.hideSftpPath=false;
						$scope.hideMyFile=true;
						$scope.hideMappedDate=true;
						$scope.hideMappedDateSftp=false;
						$scope.hideMyEncFile=true;
					}
					else
					{
						$scope.mappedFileH2=true;
						$scope.mappedFileH1=false;
						$scope.hideSftpPath=true;
						$scope.hideMyFile=false;
						$scope.hideMappedDate=false;
						$scope.hideMappedDateSftp=true;
						$scope.hideMyEncFile=true;
					}
					
					if($scope.checkEncStatus==true)
					{
						$scope.hideMyEncFile=false;
						$scope.hideMyFile=true;
					}
					else
					{
						$scope.hideMyEncFile=true;
						$scope.hideMyFile=false;
					}
					
				};
				
				$scope.updateMapTable = function(){
					
					$http.get("MapFileServlet?requestType=retriveMapFileByRule&ruleId="+ruleId).then(function(response) {	
		   				$scope.listFileData = response.data;
		   				objList = $scope.listFileData;
		   				if(response.data == '')
		   					$scope.addAlert('warning','There are no Mapped File configured yet.');
		   			},
		   			function(response) { 
		   				console.log(response);
		   			});
				};
				
				$scope.deleteAllRowMapTable = function () {

		        	try 
		        	{
		    			var table = document.getElementById('MapfileTable');
		    			var rowCount = table.rows.length;
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					table.deleteRow(i);
		    					rowCount--;
		    					i--;				    				
		    				}			    				
		    			}
		    		}
		        	catch(e) 
		        	{
		    			//alert(e);
		    		}
		        };
				//============================================================================================================
				
				/*this section act on check bob click*/
				$scope.selectedPath = function(){
					if($scope.checkStatus==true)
					{
						$scope.mappedFileH1=true;
						$scope.mappedFileH2=false;
						$scope.hideSftpPath=false;
						$scope.hidePath=true;
						$scope.hideMappedDate=true;
						$scope.hideMappedDateSftp=false;
						$scope.hideMyFile=true;
						$scope.hideMyEncFile=true;
						$scope.disableEncFile = true;
						$scope.disableSource = true;
						$scope.sPath="";
						$scope.checkEncStatus=false;
						$scope.checkPath=false;
					}
					else
					{
						$scope.mappedFileH2=true;
						$scope.mappedFileH1=false;
						$scope.hideSftpPath=true;
						$scope.hidePath=false;
						$scope.hideMappedDate=false;
						$scope.hideMappedDateSftp=true;
						$scope.hideMyFile=false;
						$scope.hideMyEncFile=true;
						$scope.disableEncFile = false;
						$scope.disableSource = false;
						$scope.sPath="";
					}
				};
				
				$scope.selectedEncPath = function(){
					
					if($scope.checkEncStatus==true)
					{
						$scope.hideMyEncFile=false;
						$scope.hideMyFile=true;
					}
					else
					{
						$scope.hideMyEncFile=true;
						$scope.hideMyFile=false;
					}
				}
				
				$scope.selectedFolder = function()
				{
					if($scope.checkPath==true)
					{
						$scope.sPath="..\\Hyperloder\\Upload";
					}
					else
					{
						$scope.sPath="";
					}
				};
				
				$scope.mapFiles = function(){
					
					if($scope.selectedRuleData != null || $scope.selectedRuleData != undefined)
					{
						console.log($scope.selectedRuleData.ruleName);						
						if($scope.checkStatus != true)
						{
							if($scope.sPath == undefined || $scope.sPath == '')
							{
								if(filesData.length > 0)
								{
									console.log(filesData);
								}
								else
								{
									alert("Please select a proper csv file");
									return;
								}
								
								for(row = 0; row < $scope.listFileData.length; row++){
									$scope.listFileData[row].mapFilePath = filesData[row];
								}
							}
							else
							{
								if($scope.listFileData.length > 0)
								{
									$scope.listFileData.length = 1;
								}
								else
								{
									alert("No csv file is defined for the Rule");
									return;
								}
								
							}
						}
						
						if($scope.sftps[0] != undefined)
						{ 
							sftpPath = $scope.sftps[0].defaultPath;
						}
						
						if($scope.sPath == undefined)
						{
							$scope.sPath = '';
						}
						
						var fileType;
						if($scope.checkEncStatus==true)
						{
							fileType = ".pgp";
						}
						else
						{
							fileType = ".csv";
						}
						
						$scope.openWaitModal();
						for(row=0; row<$scope.listFileData.length; row++){
							var request = {
									method : 'POST',
									url : 'MapFileServlet?requestType=updateMapFileByRule&ruleId=' +$scope.listFileData[row].ruleId + "&mapFileId="+$scope.listFileData[row].mapFileId
									+ "&checkStatus="+$scope.checkStatus + "&sftpPath="+sftpPath + "&ruleName="+$scope.listFileData[row].ruleName + "&resourcePath="+$scope.sPath 
									+ "&fileType="+fileType + "&fileCount="+$scope.listFileData.length + "&rowCount="+row,
									data : $scope.listFileData[row].mapFilePath,
									headers : { 
										'Content-Type' : undefined
									}
							};
							$http(request).then(function(response) {
								$scope.closeWaitModal();
								$scope.deleteAllRowMapTable();
								$scope.updateMapTable();
								$scope.fetch=response.data.status
								if($scope.fetch == "Success")
								{
									alert("Mapping Successful");										
								}
								else if($scope.fetch == "In Progress")
								{
									
								}
								else
								{
									alert("File could not found");										
								}
							},
							function(response) {
								//alert("File could not found");	
							});
						}
						filesData = [];
					}
					else
					{
						alert("Please select the Rule name first");
					}
					
				};
				
				$scope.removeMap = function(){
					
					if($scope.selectedRuleData != null || $scope.selectedRuleData != undefined)
					{
						for(row=0; row<$scope.listFileData.length; row++){
							var request = {
									method : 'POST',
									url : 'MapFileServlet?requestType=removeMapFileByRule&ruleId=' +$scope.listFileData[row].ruleId + "&mapFileId="+$scope.listFileData[row].mapFileId
									+ "&checkStatus="+$scope.checkStatus,
									headers : { 
										'Content-Type' : undefined
									}
							};
							$http(request).then(function(response) {
								//alert("Success");
								$scope.deleteAllRowMapTable();
								$scope.updateMapTable();
								}, 
								function(response) {
									alert("Failure");	
								});
						}
					}
					else
					{
						alert("Please select the Rule name first");
					}
				};
			}]); 
					
				//============================================================================================================
	
	myApp.controller(
			'ValidDataCtrl',
			['$scope','$http','$window','$timeout', 
			function($scope, $http, $window, $timeout) {
				
				var operationId;	
				var selectedRuleName;
				
				$scope.selectedRuleNameChangedValid = function(){
					
					if($scope.selectedRuleData != null)
					{
						operationId = ($scope.selectedRuleData.operationId).toString();
					}				
					
					if(operationId != null && operationId != undefined)
					{
						$http.get("MapFileServlet?requestType=retriveOperationAndApplicationData&operationId="+operationId).then(function(response) {	
			   				$scope.opprApplData=response.data;			   				
			   				if(response.data == '')
			   					$scope.addAlert('warning','There are no test Operation and Application configured yet.');
			   			},
			   			function(response) { 
			   				console.log(response);
			   			});
					}
									
				};
				
				//============================================================================================================
				
				$scope.validateData = function(){
					
				if($scope.selectedRuleData == null || $scope.selectedRuleData == undefined)
				{
					alert("Please select the Rule name first");
					return;
				}
					
				if($scope.checkStatus == true && $scope.tenant == undefined)
				{
					alert("Please select a Tenant");
					return;
				}
					
				var ruleArray = new Array();
				ruleArray.push($scope.selectedRuleData.ruleId);
				ruleArray.push($scope.selectedRuleData.ruleName);
				if($scope.tenant != undefined)
				{
					ruleArray.push($scope.tenant.tenantId);
					ruleArray.push($scope.tenant.tenantName);
					ruleArray.push($scope.opprApplData.applicationName);
				}
				else
				{
					ruleArray.push(0);
					ruleArray.push('');
					ruleArray.push('');
				}
				ruleArray.push($scope.selectedRuleData.operationName);
				
				console.log(ruleArray);
				
				$scope.openWaitModal();				
				$http.get("RuleDataServlet?requestType=" +ruleArray)
				.then(function(response) {
					$scope.closeWaitModal();
					$scope.validationStatus = response.data;
					if(response.data[0].isError == true)
					{
						alert(response.data[0].errorMsg);
					}						
					});
				$scope.tenant = undefined;
		};
				
				$scope.toggleValidate = function(item) {		        	
		        	angular.forEach($scope.validationStatus, function(app) {
		        	      if(app != item) {
		        	        app.isChecked = false;  
		        	      }
		        	    });			        	
		        };
		        
		        $scope.createValidReport = function(){
		        	$scope.openWaitModal();
					$http.get("RuleDataServlet?requestType=createValidReport")
					.then(function(response) {
						$scope.closeWaitModal();
		   				$scope.report=response.data.status;							
		   				if($scope.report == "Success"){
		   					alert("Report created successfully in Download folder");
		   				}else{
		   					alert("Report Creation Failed");
		   				}
		   			});
				};
				
				$scope.selectedPath = function()
				{
					if($scope.checkStatus==true)
					{
						$scope.hideTenant=false;
					}
					else
					{
						$scope.hideTenant=true;
					}
				};
				
				$scope.detailsLog = function() {	
		        	var count = -1;
		            var table = document.getElementById('ValidateDataTable');
	    			var rowCount = table.rows.length;
	    			var valFile;
	    			var valMessage;
	    			for(var i=0; i<rowCount; i++) 
	    			{
	    				if( i != 0)
	    				{
	    					count++;
	    					var row = table.rows[i];
		    				var chkbox = row.cells[0].childNodes[0];
		    				if(null != chkbox && true == chkbox.checked) 
		    				{
		    					$scope.file = row.cells[1].childNodes[0].value;
		    					valFile = $scope.file;
		    					$scope.message = row.cells[3].childNodes[0].value;
		    					valMessage = $scope.message;
		    				}
	    				}			    				
	    			}
	    			
	    			if(valFile != undefined)
	    			{
	    				if(valMessage != 'No validation issue')
		    			{
		    				window.open("validation-main.jsp?parameter="+valFile, "_blank", "width=1000,height=500,scrollbars=yes, menubar=no,resizable=yes");
		    			}
		    			else
		    			{
		    				alert("There is no validation issue for this csv file");
		    			}
	    			}
	    			else
	    			{
	    				alert("Please select one csv file");
	    			}	    			
		        };
				
			}]);
	
	myApp.controller(
			'SendWorkDayCtrl',
			['$scope','$http','$window','$timeout', 
			function($scope, $http, $window, $timeout) {

				var operationId;
				var tenantId;
				$scope.opprApplData=null;
				
				$scope.selectedRuleNameChangedSent = function(){
					
					if($scope.selectedRuleData != null)
					{
						operationId = ($scope.selectedRuleData.operationId).toString();	
						ruleId = ($scope.selectedRuleData.ruleId).toString();
					}
					
					if(operationId != null && operationId != undefined)
					{
						$http.get("MapFileServlet?requestType=retriveOperationAndApplicationData&operationId="+operationId).then(function(response) {	
			   				$scope.opprApplData=response.data;			   				
			   				if(response.data == '')
			   					$scope.addAlert('warning','There are no test Operation and Application configured yet.');
			   			},
			   			function(response) { 
			   				console.log(response);
			   			});
					}
					
					$scope.refreshSentToWorkday();
				};
				
				$(document).ready(function()               
			    {
			        $(document).bind('keypress', function(e) {
			            if(e.keyCode==13){
			                 $('#button_send_to_wd').trigger('click');
			             }
			        });

			        $('#button_send_to_wd').click(function(){
			        	$scope.sendWwsRequest();
			        });
			    });

				$scope.sendWwsRequest = function(){

					if($scope.selectedRuleData == undefined)
					{
						alert("Please select 'Data to Send'");
						return;
					}
					else
					{
						if($scope.selectedTenantData == undefined)
						{
							alert("Please select 'Tenant Name'");
							return;
						}
						else
						{
							if($scope.tenantPWD == undefined)
							{
								alert("Please select password");
								return;
							}
							else if($scope.tenantPWD != $scope.selectedTenantData.tenantPassword)
							{
								alert("Please enter a valid password");
								return;
							}
							else
							{
								$scope.openWaitModal();	
								$http.get("RuleDataServlet?requestType=sendWwsRequest&ruleId=" +$scope.selectedRuleData.ruleId + "&ruleName="+$scope.selectedRuleData.ruleName 
										      + "&tenantName="+$scope.selectedTenantData.tenantName +"&endPointURL="+ $scope.selectedTenantData.tenantUrl + $scope.opprApplData.applicationName
										      + "&applicationName="+$scope.opprApplData.applicationName + "&operationName="+$scope.opprApplData.operationName
										      + "&tenantUserName="+$scope.selectedTenantData.tenantUsername + "&operationId="+$scope.selectedRuleData.operationId + "&parallelReqSize="+$scope.parReq 
										      + "&requestDelay="+$scope.reqDelay + "&tenantId="+$scope.selectedTenantData.tenantId).then(function(response) {
								$scope.closeWaitModal();
					   				$scope.sentToWorkday=response.data;			   				
					   				if(response.data.isError == true)
					   				{
					   					alert(response.data.errorMsg);
					   				}					   					
					   			},
					   			function(response) { 
					   				console.log(response);
					   			});
							}
						}
					}					
				};
				
				$scope.refreshSentToWorkday = function(){
					
					$http.get("RuleDataServlet?requestType=sendWWSRequestProxy")
					.then(function(response) {
						console.log($scope.sentToWorkday);
		   				$scope.sentToWorkday=response.data;			   									   					
		   			},
		   			function(response) { 
		   				console.log(response);
		   			});
					
					$scope.tenantPWD = undefined;
				};
				
				 $scope.createSentToWorkdayReport = function(){
					 $scope.openWaitModal();
						$http.get("RuleDataServlet?requestType=createSentToWorkdayReport")
						.then(function(response) {	
							$scope.closeWaitModal();
			   				$scope.report=response.data.status;							
			   				if($scope.report == "Success"){
			   					alert("Report created successfully in Download folder");
			   				}else{
			   					alert("Report Creation Failed");
			   				}
			   			});
					};
					
				$scope.toggleWorkday = function(item) {		        	
		        	angular.forEach($scope.sentToWorkday.sentToWdArr, function(app) {
		        	      if(app != item) {
		        	        app.isChecked = false;  
		        	      }
		        	    });			        	
		        };
		        
		        $scope.detailsLogWorkday = function() {	
		        	var count = -1;
		            var table = document.getElementById('SendToWorkdayTable');
	    			var rowCount = table.rows.length;
	    			var result = new Array();
	    			var wdResult;
	    			for(var i=0; i<rowCount; i++) 
	    			{
	    				if( i != 0)
	    				{
	    					count++;
	    					var row = table.rows[i];
		    				var chkbox = row.cells[0].childNodes[0];
		    				if(null != chkbox && true == chkbox.checked) 
		    				{
		    					$scope.result = row.cells[2].childNodes[0].value;
		    					wdResult = $scope.result;
		    					result.push(row.cells[1].childNodes[0].value);
		    					result.push(row.cells[2].childNodes[0].value);
		    					result.push(row.cells[3].childNodes[0].value);
		    					result.push(row.cells[5].childNodes[0].value);
		    					result.push(row.cells[6].childNodes[0].value);
		    					result.push(row.cells[7].childNodes[0].value);
		    					result.push(row.cells[8].childNodes[0].value);
		    				}
	    				}			    				
	    			}
	    			console.log(result);
	    			
	    			if(wdResult != undefined)
	    			{
	    				if(wdResult != 'Success')
		    			{
		    				window.open("validation-workday.jsp?parameter="+result, "_blank", "width=1000,height=600,scrollbars=yes, menubar=no,resizable=yes");
		    			}
		    			else
		    			{
		    				alert("There is no issue with the result");
		    			}
	    			}
	    			else
	    			{
	    				alert("Please select one result");
	    			}	    			
		        };
		        
		        $scope.generateFiles = function() {	
		        	var count = -1;
		            var table = document.getElementById('SendToWorkdayTable');
	    			var rowCount = table.rows.length;
	    			var wdResult;
	    			var resultArr = new Array();
	    			for(var i=0; i<rowCount; i++) 
	    			{
	    				if( i != 0)
	    				{
	    					count++;
	    					var row = table.rows[i];
		    				var chkbox = row.cells[0].childNodes[0];
		    				if(null != chkbox && true == chkbox.checked) 
		    				{
		    					$scope.result = row.cells[2].childNodes[0].value;
		    					wdResult = $scope.result;
		    					resultArr.push(row.cells[1].childNodes[0].value);
		    					resultArr.push($scope.selectedRuleData.ruleId);
		    					resultArr.push($scope.selectedRuleData.ruleName);
		    					resultArr.push($scope.selectedRuleData.operationName);
		    				}
	    				}			    				
	    			}
	    			
	    			if(wdResult != undefined)
	    			{
	    				if(wdResult != 'Success')
		    			{
		    				window.open("generate-files.jsp?parameter="+resultArr, "_blank", "width=1200,height=600,scrollbars=yes, menubar=no,resizable=yes");
		    			}
		    			else
		    			{
		    				alert("There is no issue with the result");
		    			}
	    			}
	    			else
	    			{
	    				alert("Please select one result");
	    			}	    			
		        };
		        
			}]); 
	
	myApp.controller(
			'ApplCtrl',
			[
				'$scope',
				'$http',				
				function($scope, $http) {
					
					var applicationName;
					var applications;
					
					messageResource.init({
						  filePath : 'resource'
						});

					messageResource.load('application', function(){ 

						  applicationName = messageResource.get('application.names', 'application');
						  applications = applicationName.split(';');
						  applications.sort();
					});
					
					 $scope.enableAllOnEditAppl = function() {
						 document.getElementById("applAdd").disabled = false;
						 document.getElementById("applDel").disabled = false;
						 document.getElementById("applSave").disabled = false;
						 document.getElementById("applCan").disabled = false;
					 };
					 
			        $scope.disableAllOnCancelAppl = function() {
						 document.getElementById("applAdd").disabled = true;
						 document.getElementById("applDel").disabled = true;
						 document.getElementById("applSave").disabled = true;
						 document.getElementById("applCan").disabled = true;
						 
						 var myTable = document.getElementById('appTable');
						 var rowCount = myTable.rows.length;
						 var dataSize = $scope.applications.length;
						 if(rowCount > dataSize)
						 {
							 var actDataSize = dataSize + 1;
							 for(var i = actDataSize; i < rowCount; i++)
							 {
								 myTable.deleteRow(i);
			    				 rowCount--;
			    				 i--;
							 }
						 }
			     }
			     
			     $scope.addApplRow = function (tableID) {						

						var option;
						
						var table = document.getElementById(tableID);

						var rowCount = table.rows.length;
						var row = table.insertRow(rowCount);
						
						var cell1 = row.insertCell(0);
						var element1 = document.createElement("input");
						element1.type = "checkbox";
						element1.name="chkbox[]";
						cell1.appendChild(element1);					

						var cell2 = row.insertCell(1);
						var element2 = document.createElement("select");
						for (var i = 1; i < applications.length; i++) {
						    option = document.createElement("option");
						    option.value = applications[i];
						    option.text = applications[i];
						    element2.appendChild(option);
						}
						cell2.appendChild(element2);

						var cell3 = row.insertCell(2);
						var element3 = document.createElement("input");
						element3.type = "text";
						element3.name = "txtbox[]";
						cell3.appendChild(element3);
						cell3.innerHTML = '<input type="text" style="width:100%;" value="version">';

			        };
			        
			        $scope.deleteApplRow = function (tableID) {

			        	var cnt = -1;;
			        	try {
			    			var table = document.getElementById(tableID);
			    			var rowCount = table.rows.length;
			    			var remApplVal = new Array();
			    			for(var i=0; i<rowCount; i++) 
			    			{
			    				if( i != 0)
			    				{
			    					cnt++;
			    					var row = table.rows[i];
				    				var chkbox = row.cells[0].childNodes[0];
				    				if(null != chkbox && true == chkbox.checked) 
				    				{
				    					table.deleteRow(i);
				    					remApplVal.push($scope.applications[cnt].applicationId +";"+ row.cells[1].childNodes[0].data +";"+ row.cells[2].childNodes[0].data);
				    					rowCount--;
				    					i--;
				    				}
			    				}			    				
			    			}
			    			}catch(e) {
			    				alert(e);
			    			}

			    			var paramApplRemove = '?removeApplArray=' + JSON.stringify(remApplVal);
					        $.ajax({
					             url: 'MaintApplServlet'+paramApplRemove,
					             type: 'POST', 
					             dataType: 'json',  
					             success: function(result) {
					            	 $scope.deleteAllRowRule();
					            	 //$scope.deleteAllRowMap();
					            	 $scope.deleteAllRowValidate();
					            	 $scope.deleteAllRowSentToWd();
				            		 $scope.deleteAllRowAppl();
				            		 $scope.populateAppl();
					               //alert('SUCCESS');
					             }
					        });
					        
					        //window.location.reload();
			        };
			        
			        $scope.checkAppName = function () {
			        	
			        	var myTab = document.getElementById('appTable');
		    			var rowCount = myTab.rows.length;
			            var element;

			            for (var i=1; i < rowCount; i++) {	
			            	var row1 = myTab.rows[i];
			            	var currentElement = row1.cells[1].childNodes[0].data;
			            	if(currentElement == undefined)
			            	{
			            		currentElement = myTab.rows.item(i).cells[1].childNodes[0].value;
			            	}
			            	for (var j = i + 1; j < rowCount; j++) {
			            		var row2 = myTab.rows[j];
			                    var nextElement = row2.cells[1].childNodes[0].data;
			                    if(nextElement == undefined)
			                    {
			                    	nextElement = myTab.rows.item(j).cells[1].childNodes[0].value;
			                    }
			                    
			                    if(currentElement == nextElement) 
			                    {
			                    	alert("Application name can not be same. Please select a different application")
			                        return;
			                    }
			                }
			            }
			            $scope.saveApplData();
			        };
			        
			        $scope.saveApplData =  function() {
			        	
			            var myTab = document.getElementById('appTable');
			            var applValues = new Array();
			            var element;

			            for (row = 1; row < myTab.rows.length; row++) {
			            	var obj = {
				            	    applicationName:"",
				            	    version:""
				            	};
			            	if(row <= $scope.applications.length)
			            	{
			            		applValues.push($scope.applications[row-1].applicationId);
			            	}
			            	else
			            	{
			            		applValues.push(0);
			            	}
			            	
			                for (c = 0; c < myTab.rows[row].cells.length; c++) {   
			                	if(c != 0){
			                		if(c == 1)
			                		{
			                			element = myTab.rows.item(row).cells[c];	
			                			obj.applicationName = element.childNodes[0].value;
			                			if(obj.applicationName != undefined && obj.applicationName != null)
			                			{
			                				applValues.push(obj.applicationName);
			                			}
			                			else
			                			{
			                				applValues.push(myTab.rows.item(row).cells[c].childNodes[0].data);
			                			}
			                		}
			                		if(c == 2)
			                		{
			                			element = myTab.rows.item(row).cells[c];
			                			obj.version = element.childNodes[0].value;
			                			if(obj.version != undefined && obj.version != null)
			                			{
			                				if(obj.version.startsWith("v", 0))
			                				{
			                					if (!(isNaN(obj.version.substr(1,2)))) 
			                					{
			                						if(obj.version.substr(1,(obj.version.indexOf(".")-1)) > 27)
			                						{
			                							applValues.push(obj.version);
			                						}
			                						else
			                						{
			                							alert("Version should be higher than 28.0 and above");
				                						return;
			                						}
			                					}
			                					else
			                					{
			                						alert("Version must be number and it should be higher than 28.0 and above");
			                						return;
			                					}
			                				}
			                				else
				                			{
				                				alert("Version should starts with v and it will be higher than 28.0 and above");
				                				return;
				                			}
			                			}			                			
			                		}		                		          		
			                   }
			                }
			                if(applValues.length >0)
			                {
			                	applValues.push(";");
			                }
			            }
			            console.log(applValues);
			            
			            var paramAppl = '?applObjArray=' + JSON.stringify(applValues);
				        $.ajax({
				             url: 'MaintApplServlet'+paramAppl,
				             type: 'POST', 
				             dataType: 'json',  
				             success: function(result) {
			            		   $scope.deleteAllRowAppl();
			            		   $scope.populateAppl();
				               //alert('SUCCESS');
				             }
				        });
				        
				      //window.location.reload();
			        };
			        
			        $scope.openOperation = function() {	
			        	var count = -1;
			            var table = document.getElementById('appTable');
		    			var rowCount = table.rows.length;
		    			var remApplVal = new Array();
		    			for(var i=0; i<rowCount; i++) 
		    			{
		    				if( i != 0)
		    				{
		    					count++;
		    					var row = table.rows[i];
			    				var chkbox = row.cells[0].childNodes[0];
			    				if(null != chkbox && true == chkbox.checked) 
			    				{
			    					if($scope.applications[count] == undefined)
			    					{
			    						alert("Please save the application first before proceed further");
			    						return;
			    					}
			    					else
			    					{
				    					remApplVal.push($scope.applications[count].applicationId);
				    					$scope.appName = row.cells[1].childNodes[0].data;
				    					remApplVal.push($scope.appName);
				    					$scope.appVersion = row.cells[2].childNodes[0].value;
				    					remApplVal.push($scope.appVersion);
			    					}
			    				}			    				
		    				}
		    			}
		    			
		    			if(remApplVal.length >0)
		    			{
		    				window.open("operation-main.jsp?parameter="+remApplVal, "_blank", "width=1000,height=600,scrollbars=yes, menubar=no,resizable=yes");
		    			}
		    			else
		    			{
		    				alert("Please select one application");
		    			}
			        };
			        
			        $scope.toggleApp = function(item) {
			        	
			        	angular.forEach($scope.applications, function(app) {
			        	      if(app != item) {
			        	        app.isChecked = false;  
			        	      }
			        	    });			        	
			        };
					
				} ]);
	
	myApp.controller(
			'TenantCtrl',
			[
				'$scope',
				'$http',				
				function($scope, $http) {
					    
					var values;					
					var dataCenterText;

					messageResource.init({
						  filePath : 'resource'	
						});

					messageResource.load('config', function(){ 

					});
					
					 $scope.enableAllOnEditTenant = function() {
							 document.getElementById("addR").disabled = false;
							 document.getElementById("remR").disabled = false;
							 document.getElementById("saveB").disabled = false;
							 document.getElementById("cancelB").disabled = false;
							 
							 var cnt = -1;
							 var myTable = document.getElementById('dataTable');
							 var rowCount = myTable.rows.length;
			    			 for(var i=0; i<rowCount; i++) 
			    			 {
			    				if( i != 0)
			    				{
			    					var row = myTable.rows[i];
			    					cnt++;
			    					row.cells[5].childNodes[0].value = $scope.tenants[cnt].tenantPassword;
			    				}			    				
			    			 }
						 };
						 
				     $scope.disableAllOnCancelTenant = function() {
							 document.getElementById("addR").disabled = true;
							 document.getElementById("remR").disabled = true;
							 document.getElementById("saveB").disabled = true;
							 document.getElementById("cancelB").disabled = true;
							 
							 var myTable = document.getElementById('dataTable');
							 var rowCount = myTable.rows.length;
							 var dataSize = $scope.tenants.length;

							 var cnt = -1;
							 for(var i=0; i<rowCount; i++) 
			    			 {
			    				if( i != 0)
			    				{
			    					var row = myTable.rows[i];
			    					cnt++;
			    					row.cells[5].childNodes[0].value = "**********";
			    				}			    				
			    			 }
							 
							 if(rowCount > dataSize)
							 {
								 var actDataSize = dataSize + 1;
								 for(var i = actDataSize; i < rowCount; i++)
								 {
									 myTable.deleteRow(i);
				    				 rowCount--;
				    				 i--;
								 }
							 }
						 };
					
					$scope.addRow = function (tableID) {
						
						var option;
						var selectlist = ["", "Ashburn(Production)", "Atlanta", "Atlanta(Sandbox)", "Dublin", "Portland","Dublin(Production)","Atlanta(Production)","Portland(Production)"];

						var table = document.getElementById(tableID);

						var rowCount = table.rows.length;
						var row = table.insertRow(rowCount);
						
						var cell1 = row.insertCell(0);
						var element1 = document.createElement("input");
						element1.type = "checkbox";
						element1.name="chkbox[]";
						cell1.appendChild(element1);						

						var cell2 = row.insertCell(1);
						var element2 = document.createElement("input");
						element2.type = "text";
						element2.name="txtbox[]";
						cell2.appendChild(element2);
						cell2.innerHTML = '<input type="text" value="Tenant Name">';

						var cell3 = row.insertCell(2);
						var element3 = document.createElement("select");
						for (var i = 0; i < selectlist.length; i++) {
						    option = document.createElement("option");
						    option.value = selectlist[i];
						    option.text = selectlist[i];
						    element3.appendChild(option);
						}
						cell3.appendChild(element3);
						element3.onchange = function() {
							$scope.displayEndPointURL(tableID); 	
						}

						var cell4 = row.insertCell(3);
						var element4 = document.createElement("input");
						element4.type = "text";
						element4.name = "txtbox[]";
						cell4.appendChild(element4);
						cell4.innerHTML = '<input type="text" ng-model="url" style="width:100%;" value="End Point URL" disabled="disabled">';
						
						var cell5 = row.insertCell(4);
						var element5 = document.createElement("input");
						element5.type = "text";
						element5.name = "txtbox[]";
						cell5.appendChild(element5);
						cell5.innerHTML = '<input type="text" value="User Name">';
						
						var cell6 = row.insertCell(5);
						var element6 = document.createElement("input");
						element6.type = "password";
						element6.name = "txtbox[]";
						cell6.appendChild(element6);
						cell6.innerHTML = '<input type="password" value="********">';

			        };
			        
			        $scope.deleteRow = function (tableID) {

			        	var cnt = -1;
			        	try {
			    			var table = document.getElementById(tableID);
			    			var rowCount = table.rows.length;
			    			var remValues = new Array();
			    			for(var i=0; i<rowCount; i++) 
			    			{
			    				if( i != 0)
			    				{
			    					cnt++;
			    					var row = table.rows[i];
				    				var chkbox = row.cells[0].childNodes[0];
				    				if(null != chkbox && true == chkbox.checked) 
				    				{
				    					table.deleteRow(i);
				    					remValues.push($scope.tenants[cnt].tenantId +";"+ row.cells[1].childNodes[0].data +";"+ row.cells[2].childNodes[0].data +";"+ row.cells[3].childNodes[0].data +";"+ row.cells[4].childNodes[0].data +";"+ row.cells[5].childNodes[0].data);
				    					rowCount--;
				    					i--;
				    				}
			    				}			    				
			    			}
			    			}catch(e) {
			    				alert(e);
			    			}

			    			var paramRemove = '?removeTenantArray=' + JSON.stringify(remValues);
					        $.ajax({
					             url: 'tenantServlet'+paramRemove,
					             type: 'POST', 
					             dataType: 'json',  
					             success: function(result) {
				            		   $scope.deleteAllRowTenant();
				            		   $scope.populateTenants();
					             }
					        });
			        };
			        
			        $scope.displayEndPointURL = function (tableID) {
			        	
			        	var dataCenterCell;
			        	try {
			    			var table = document.getElementById(tableID);
			    			var rowCount = table.rows.length;

			    			for(var i=0; i<rowCount; i++) {
			    				var row = table.rows[i];
				            	var currentElement = row.cells[2].childNodes[0].data;
				            	if(currentElement == undefined)
				            	{
				            		dataCenterCell = table.rows.item(i).cells[2].childNodes[0].value;
				            		row.cells[3].childNodes[0].value = messageResource.get(dataCenterCell, 'config');;
				            	}
			    			}
			    			}catch(e) {
			    				alert(e);
			    			}			  	
			        }
			        
		        	$scope.checkTenantName = function () {
		        	
		        	var myTab = document.getElementById('dataTable');
	    			var rowCount = myTab.rows.length;
		            var element;

		            for (var i=1; i < rowCount; i++) {	
		            	var row1 = myTab.rows[i];
		            	var currentElement = row1.cells[1].childNodes[0].data;
		            	if(currentElement == undefined)
		            	{
		            		currentElement = myTab.rows.item(i).cells[1].childNodes[0].value;
		            	}
		            	for (var j = i + 1; j < rowCount; j++) {
		            		var row2 = myTab.rows[j];
		                    var nextElement = row2.cells[1].childNodes[0].data;
		                    if(nextElement == undefined)
		                    {
		                    	nextElement = myTab.rows.item(j).cells[1].childNodes[0].value;
		                    }
		                    
		                    if(currentElement == nextElement) 
		                    {
		                    	alert("Tenant name can not be same. Please select a different tenant")
		                        return;
		                    }
		                }
		            }
		            $scope.saveTabData();

			        };
			        
			       $scope.saveTabData =  function() {
			            var myTab = document.getElementById('dataTable');
			            values = new Array();
			            var element;

			            for (row = 1; row < myTab.rows.length; row++) {
			            	var obj = {
				            	    tenantName:"",
				            	    dataCenter:"",
				            	    url:"",
				            	    userName:"",
				            	    password:""
				            	};
			            	
			            	if(row <= $scope.tenants.length)
			            	{
			            		values.push($scope.tenants[row-1].tenantId);
			            	}
			            	else
			            	{
			            		values.push(0);
			            	}
			            	
			                for (c = 0; c < myTab.rows[row].cells.length; c++) {   
			                	if(c != 0){
			                		if(c == 1)
			                		{
			                			element = myTab.rows.item(row).cells[c];
			                			obj.tenantName = element.childNodes[0].value;
			                			if(obj.tenantName != undefined && obj.tenantName != null)
			                			{
			                				values.push(obj.tenantName);
			                			}			                			
			                		}
			                		if(c == 2)
			                		{
			                			element = myTab.rows.item(row).cells[c];
			                			obj.dataCenter = element.childNodes[0].value;
			                			if(obj.dataCenter != null && obj.dataCenter != undefined )
			                			{
			                				values.push(obj.dataCenter);
			                			}
			                			else
			                			{
			                				values.push(myTab.rows.item(row).cells[c].childNodes[0].data);
			                			}			                			
			                		}
			                		if(c == 3)
			                		{
			                			element = myTab.rows.item(row).cells[c];
			                			obj.url = element.childNodes[0].value;
			                			if(obj.url != undefined && obj.url != null)
			                			{
			                				values.push(obj.url);
			                			}
			                			else
			                			{
			                				values.push(myTab.rows.item(row).cells[c].childNodes[0].data);
			                			}
			                		}
			                		if(c == 4)
			                		{
			                			element = myTab.rows.item(row).cells[c];
			                			obj.userName = element.childNodes[0].value;
			                			if(obj.userName != undefined && obj.userName != null)
			                			{
			                				values.push(obj.userName);
			                			}			                			
			                		}
			                		if(c == 5)
			                		{
			                			element = myTab.rows.item(row).cells[c];
			                			obj.password = element.childNodes[0].value;
			                			if(obj.password != undefined && obj.password != null)
			                			{
			                				values.push(btoa(obj.password));
			                			}			                			
			                		}			                		
			                   }
			                }
			                if(values.length >0)
			                {
			                	values.push(";");
			                }
			            }
			            console.log(values);
			            
			            var param = '?tenantObjArray=' + JSON.stringify(values);
				        $.ajax({
				             url: 'tenantServlet'+param,
				             type: 'POST', 
				             dataType: 'json',  
				             success: function(result) {
			            		   $scope.deleteAllRowTenant();
			            		   $scope.populateTenants();			            		 
				             }
				        });
			        };
			        
			        $scope.toggleTenant = function(item) {
			        	
			        	angular.forEach($scope.tenants, function(tenant) {
			        	      if(tenant != item) {
			        	    	  tenant.isChecked = false;  
			        	      }
			        	    });			        	
			        };
			        
			        $scope.createTenantReport = function(){
			        	$scope.openWaitModal();
						$http.get("tenantServlet?requestType=createTenantReport")
						.then(function(response) {	
							$scope.closeWaitModal();
			   				$scope.report=response.data.status;							
			   				if($scope.report == "Success"){
			   					alert("Report created successfully in Download folder");
			   				}else{
			   					alert("Report Creation Failed");
			   				}
			   			});
					};
			        
				} ]);
	
	myApp.controller(
			'SFTPCtrl',
			['$scope','$http','$window','$timeout', 
			function($scope, $http, $window, $timeout) {
				
				var sFTPTable;
				var sFTPRowCount;
				var sFTPRow;
				
				//$scope.serverPorts = [{"password":"P1","defaultPath":"DP1","sftpId":1,"userName":"U1","sftpServerPort":"SP1"},{"password":"P2","defaultPath":"DP2","sftpId":2,"userName":"U2","sftpServerPort":"SP2"}];
				
				//===================================================================================================================================================================
				$scope.initSFTPConfiguration = function(){
					$scope.addRB_D = true;
					$scope.remRB_D = true;
					$scope.saveB_D = true;
					$scope.cancelB_D = true;
					$scope.editB_D = false;
					$scope.testB_D = false;
					$scope.reportB_D = false;
				};
				//===================================================================================================================================================================
				$scope.enableAllOnEdit = function() {			
					$scope.addRB_D = false;
					$scope.remRB_D = false;
					$scope.saveB_D = false;
					$scope.cancelB_D = false;
					$scope.editB_D = true;
					$scope.reportB_D = true;
					
					var passwordBox = document.getElementById('PASSWORD');
					if(passwordBox != null || passwordBox != undefined)
					{
						passwordBox.value = $scope.sftps[0].password;
					}					

				 };
				//===================================================================================================================================================================
				 $scope.disableAllOnCancel = function() {
						$scope.addRB_D = true;
						$scope.remRB_D = true;
						$scope.saveB_D = true;
						$scope.cancelB_D = true;
						$scope.editB_D = false;
						//$scope.testB_D = false;
						$scope.reportB_D = false;
						
						var passwordBox = document.getElementById('PASSWORD');
						passwordBox.value = "**********";

				 };		
				//===================================================================================================================================================================
				$scope.sFTPRowAddRow = function (tableID) {
					
					sFTPTable = document.getElementById(tableID);
					sFTPRowCount = sFTPTable.rows.length;
					if(sFTPRowCount>1){
						alert("You can save SFTP Configuration for One SFTP Server only.");
						$scope.disableAllOnCancel();
					}
					else{
						sFTPRow = sFTPTable.insertRow(sFTPRowCount);
						
						var cell1 = sFTPRow.insertCell(0);
						var element1 = document.createElement("input");
						element1.type = "checkbox";
						element1.name="chkbox[]";
						cell1.appendChild(element1);
						
						var cell2 = sFTPRow.insertCell(1);
						var element2 = document.createElement("input");
						element2.type = "text";
						element2.name="txtbox[]";
						cell2.appendChild(element2);
						cell2.innerHTML = '<input type="text" placeholder="SFTP Server Port" size="40%">';
						
						var cell3 = sFTPRow.insertCell(2);
						var element3 = document.createElement("input");
						element3.type = "text";
						element3.name="txtbox[]";
						cell3.appendChild(element3);
						cell3.innerHTML = '<input type="text" placeholder="User Name">';
						
						var cell4 = sFTPRow.insertCell(3);
						var element4 = document.createElement("input");
						element4.type = "text";
						element4.name="txtbox[]";
						cell4.appendChild(element4);
						cell4.innerHTML = '<input type="password" value="********" placeholder="Password">';
						
						var cell5 = sFTPRow.insertCell(4);
						var element5 = document.createElement("input");
						element5.type = "text";
						element5.name="txtbox[]";
						cell5.appendChild(element5);
						cell5.innerHTML = '<input type="text" placeholder="Default Path" size="40%">';	
					}					
				};

				//==============================================================================================================
				$scope.sFTPDeleteRow = function(tableID){
					try {
		    			var table = document.getElementById(tableID);
		    			var rowCount = table.rows.length;
		    			var remSFTPValues = new Array();
		    			for(var i=0; i<rowCount; i++) {
		    				if( i != 0)
		    				{
			    				var row = table.rows[i];
			    				var chkbox = row.cells[0].childNodes[0];
			    				if(null != chkbox && true == chkbox.checked) {
			    					table.deleteRow(i);
			    					remSFTPValues.push($scope.sftps[0].sftpId)
			    					rowCount--;
			    					i--;
			    				}
		    				}
		    			}
		    			}catch(e) {
		    				alert(e);
		    			}
		    			
		    			var paramSFTPRemove = '?removeSFTPArray=' + JSON.stringify(remSFTPValues);
				        $.ajax({
				             url: 'SFTPServlet'+paramSFTPRemove,
				             type: 'POST', 
				             dataType: 'json',  
				             success: function(result) {
			            		   $scope.deleteAllRowSFTP();
			            		   $scope.populateSFTP();
				               //alert('SUCCESS');
				             }
				        });
				};
				
				//==============================================================================================================
				$scope.sFTPSaveData = function(){
					var mySFTPTab = document.getElementById('SFTPDataTable');
					var sFTPRowCount = mySFTPTab.rows.length;
					var sftpDet = new Array();;			
					var element;			
					var tabLength = mySFTPTab.rows.length;			
					for (row = 1; row < tabLength; row++) {				
						var obj = {
								sftpServerPort:"",
								userName:"",
								password:"",
								defaultPath:""
			            	};
						
		            	if(row <= $scope.sftps.length)
		            	{
		            		sftpDet.push($scope.sftps[row-1].sftpId);
		            	}
		            	else
		            	{
		            		sftpDet.push(0);
		            	}
		            	
						for (c = 1; c < mySFTPTab.rows[row].cells.length; c++) {							
								if(c == 1){
									element = mySFTPTab.rows.item(row).cells[c];							
									obj.sftpServerPort = element.childNodes[0].value;
									if(obj.sftpServerPort != null && obj.sftpServerPort != undefined)
									{
										sftpDet.push(obj.sftpServerPort);
									}
								}						
								if(c == 2){
									element = mySFTPTab.rows.item(row).cells[c];
									obj.userName = element.childNodes[0].value;
									if(obj.userName != null && obj.userName != undefined)
									{
										sftpDet.push(obj.userName);
									}
								}						
								if(c == 3){
									element = mySFTPTab.rows.item(row).cells[c];
									obj.password = element.childNodes[0].value;	
									if(obj.password != null && obj.password != undefined)
									{
										sftpDet.push(btoa(obj.password));
									}
								}						
								if(c == 4){
									element = mySFTPTab.rows.item(row).cells[c];
									obj.defaultPath = element.childNodes[0].value;
									if(obj.defaultPath != null && obj.defaultPath != undefined)
									{
										sftpDet.push(obj.defaultPath);
									}
								}						
						}				
					}
					
					console.log(sftpDet);
		            
		            var param = '?sftpObjArray=' + JSON.stringify(sftpDet);
			        $.ajax({
			             url: 'SFTPServlet'+param,
			             type: 'POST', 
			             dataType: 'json',  
			             success: function(result) {
		            		   $scope.deleteAllRowSFTP();
		            		   $scope.populateSFTP();
			               //alert('SUCCESS');
			             }
			        });
			        
			        //window.location.reload();
					
				};
				//============================================================================================================
				$scope.sftpEditData = function(){
					
				};
				//============================================================================================================
				$scope.testSFTPConnection = function(){
					$http.get("SFTPServlet?requestType=testSFTPConnection")
					.then(function(response) {								
						$scope.sftpConnectionStatus=response.data.status;							
		   				if($scope.sftpConnectionStatus == "Success"){
		   					alert("Connection to sFTP server successfully verified");
		   				}else{
		   					alert("Connection Failed");
		   				}	
					});
				};
				//============================================================================================================
				
				$scope.createSFTPReport = function(){
					$scope.openWaitModal();
					$http.get("SFTPServlet?requestType=createSFTPReport")
					.then(function(response) {
						$scope.closeWaitModal();
		   				$scope.report=response.data.status;							
		   				if($scope.report == "Success"){
		   					alert("Report created successfully in Download folder");
		   				}else{
		   					alert("Report Creation Failed");
		   				}
		   			});
				};
				
			}]);
	
	myApp.controller(
			'HistTranCtrl',
			['$scope',
			'$http',
			function($scope, $http) {
				
				//$scope.myDate = new Date();
				//$scope.allDetails = [];				
				$scope.hideTopTable = true;
				$scope.hideBottomTable = true;
				$scope.hideSentToWD = true;
				var totalCount;
					
				$scope.uploadData=function() {
/*					var newRow = {};
					$scope.tabDetails = [];
					newRow.seqnum = $scope.seqnum;
					newRow.operation = $scope.operation.operationName;
					newRow.application = $scope.operation.applicationName;
					var dd = $scope.myDate.getDate();
					var mm = $scope.myDate.getMonth() + 1;
					var yyyy = $scope.myDate.getFullYear();
					newRow.myDate = mm + "/" + dd + "/" + yyyy;	
					$scope.allDetails.push(newRow);	*/
					
					if(filesData.length <1)
					{
						alert("Please select master Excel Sheet");
						return;
					}
					
					var request = {
							method : 'POST',
							url : 'HistDataServlet?requestType=uploadData',
							data : filesData[0],
							headers : { 
								'Content-Type' : undefined
							}
					};
					$http(request).then(function(response) {
						//alert("Success");
						$scope.upload=response.data.status;
						$scope.totalCount = response.data.count;
						if($scope.upload == "Success")
						{
							alert($scope.totalCount + " rows of Master Sheet Uploaded Successful");										
						}
						else 
						{
							alert("Data Upload Failed");
						}
						}, 
						function(response) {
							//alert("Failure");	
						});
					filesData = [];
				}
				
				$scope.displayPageList=function() {
					
					$scope.openWaitModal();
					$http.get("HistDataServlet?requestType=displayPage&pageRange=" +$scope.rowNum)
					.then(function(response) {
						$scope.closeWaitModal();
		   				$scope.pageRow=response.data;
		   				for(x=0;x<$scope.pageRow.length;x++)
		   				{
		   					if($scope.pageRow[x].validExec == false)
							{
		   						$scope.hideTopTable = true;
		   						$scope.hideBottomTable = true;
		   						break;
							}
		   					else
		   					{
		   						$scope.hideTopTable = false;
		   						$scope.hideBottomTable = true;
		   					}
		   				}
		   			});
				}
				
				$scope.displayPageNext=function() {
					
					$scope.hideSentToWD = true;
					var table = document.getElementById('HistoricalDataTable');
					var rowNo = table.rows.length - 1;
					var start = table.rows.item(rowNo).cells[1].childNodes[0].value;
					var count = $scope.totalCount;					
					
					if(start < count)
					{
						$scope.openWaitModal();
						$http.get("HistDataServlet?requestType=displayPageNext&pageStart=" +start + "&pageEnd=" +rowNo)
						.then(function(response) {
							$scope.closeWaitModal();
			   				$scope.pageRow=response.data;
			   				for(x=0;x<$scope.pageRow.length;x++)
			   				{
			   					if($scope.pageRow[x].validExec == false)
								{
			   						$scope.hideTopTable = true;
			   						$scope.hideBottomTable = true;
			   						break;
								}
			   					else
			   					{
			   						$scope.hideTopTable = false;
			   						$scope.hideBottomTable = true;
			   					}
			   				}
			   			});
					}
					else
					{
						alert("No data exist!!!!");
					}
				}
				
				$scope.displayPagePrevious=function() {
					
					$scope.hideSentToWD = true;
					var rowNo = $scope.rowNum;
					var table = document.getElementById('HistoricalDataTable');
					var start = table.rows.item(1).cells[1].childNodes[0].value;
					var startNo = (start - rowNo -1);
					if(startNo >= 0)
					{
						$scope.openWaitModal();
						$http.get("HistDataServlet?requestType=displayPagePrevious&pageStart=" +startNo + "&pageEnd=" +rowNo)
						.then(function(response) {
							$scope.closeWaitModal();
			   				$scope.pageRow=response.data;
			   				for(x=0;x<$scope.pageRow.length;x++)
			   				{
			   					if($scope.pageRow[x].validExec == false)
								{
			   						$scope.hideTopTable = true;
			   						$scope.hideBottomTable = true;
			   						break;
								}
			   					else
			   					{
			   						$scope.hideTopTable = false;
			   						$scope.hideBottomTable = true;
			   					}
			   				}
			   			});
					}
					else
					{
						alert("No data exist!!!!");
					}
				}
				
				$scope.validateRecords=function(tableID) {
					
					if($scope.selectedTenantData == undefined)
					{
						alert("Please select 'Tenant Name'");
						return;
					}
					
					var myTab = document.getElementById('HistoricalDataTable');
		            var values = new Array();
		            var element;

		            for (row = 1; row < myTab.rows.length; row++) {
		            	var obj = {
			            	    seqNo:"",
			            	    operationName:"",
			            	    applicationName:"",
			            	    effDate:"",
			            	    posId:"",
			            	    filePath:""
			            	};
		            	
		            	for (c = 0; c < myTab.rows[row].cells.length; c++) {   
		                	if(c != 0){
		                		if(c == 1)
		                		{
		                			element = myTab.rows.item(row).cells[c];
		                			obj.seqNo = element.childNodes[0].value;
		                			if(obj.seqNo != undefined && obj.seqNo != null)
		                			{
		                				values.push(obj.seqNo);
		                			}
		                			else
		                			{
		                				values.push(myTab.rows.item(row).cells[c].childNodes[0].data);
		                			}
		                		}
		                		if(c == 2)
		                		{
		                			element = myTab.rows.item(row).cells[c];
		                			obj.operationName = element.childNodes[0].value;
		                			if(obj.operationName != null && obj.operationName != undefined )
		                			{
		                				values.push(obj.operationName);
		                			}
		                			else
		                			{
		                				values.push(myTab.rows.item(row).cells[c].childNodes[0].data);
		                			}			                			
		                		}
		                		if(c == 3)
		                		{
		                			element = myTab.rows.item(row).cells[c];
		                			obj.applicationName = element.childNodes[0].value;
		                			if(obj.applicationName != undefined && obj.applicationName != null)
		                			{
		                				values.push(obj.applicationName);
		                			}
		                			else
		                			{
		                				values.push(myTab.rows.item(row).cells[c].childNodes[0].data);
		                			}
		                		}
		                		if(c == 4)
		                		{
		                			element = myTab.rows.item(row).cells[c];
		                			obj.effDate = element.childNodes[0].value;
		                			if(obj.effDate != undefined && obj.effDate != null)
		                			{
		                				values.push(obj.effDate);
		                			}
		                			else
		                			{
		                				values.push(myTab.rows.item(row).cells[c].childNodes[0].data);
		                			}
		                		}
		                		if(c == 5)
		                		{
		                			element = myTab.rows.item(row).cells[c];
		                			obj.posId = element.childNodes[0].value;
		                			if(obj.posId != undefined && obj.posId != null)
		                			{
		                				values.push(obj.posId);
		                			}
		                			else
		                			{
		                				values.push(myTab.rows.item(row).cells[c].childNodes[0].data);
		                			}
		                		}
		                		if(c == 6)
		                		{
		                			element = myTab.rows.item(row).cells[c];
		                			obj.filePath = element.childNodes[0].value;
		                			if(obj.filePath != undefined && obj.filePath != null)
		                			{
		                				values.push(obj.filePath);
		                			}
		                			else
		                			{
		                				values.push(myTab.rows.item(row).cells[c].childNodes[0].data);
		                			}
		                		}			 
		                   }
		                }
		                if(values.length >0)
		                {
		                	values.push(";");
		                }
		            }
		            
		            $scope.openWaitModal();
					var request = {
							method : 'POST',
							url : 'HistDataServlet?requestType=mapValidateData&histDataObjArray='+JSON.stringify(values) + "&tenantName="+$scope.selectedTenantData.tenantName,
							headers : { 
								'Content-Type' : undefined
							}
					};
					$http(request).then(function(response) {
						$scope.closeWaitModal();
						$scope.validationHistStatus = response.data;
						if(response.data[0].isError == true)
						{
							alert(response.data[0].errorMsg);
						}
						else
						{
							$scope.refreshPageList($scope.validationHistStatus[0].seqNo, $scope.rowNum)
							$scope.hideTopTable = true;
							$scope.hideBottomTable = false;
						}
					},
					function(response) {
						//alert("File could not found");	
					});
				}
		        
		        $scope.detailsLog = function(tableID) {	
		        	var count = -1;
		            var table = document.getElementById(tableID);
	    			var rowCount = table.rows.length;
	    			var valFile;
	    			var valMessage;
	    			for(var i=0; i<rowCount; i++) 
	    			{
	    				if( i != 0)
	    				{
	    					count++;
	    					var row = table.rows[i];
		    				var chkbox = row.cells[0].childNodes[0];
		    				if(null != chkbox && true == chkbox.checked) 
		    				{
		    					$scope.file = row.cells[3].childNodes[0].value;
		    					valFile = $scope.file;
		    					$scope.message = row.cells[5].childNodes[0].value;
		    					valMessage = $scope.message;
		    				}
	    				}			    				
	    			}
	    			
	    			if(valFile != undefined)
	    			{
	    				if(valMessage != 'No validation issue')
		    			{
		    				window.open("validation-hist.jsp?parameter="+valFile, "_blank", "width=1000,height=500,scrollbars=yes, menubar=no,resizable=yes");
		    			}
		    			else
		    			{
		    				alert("There is no validation issue for this csv file");
		    			}
	    			}
	    			else
	    			{
	    				alert("Please select one csv file");
	    			}	    			
		        };
		        
				$scope.toggleValidateHist = function(item, val) {		        	
		        	angular.forEach(val, function(app) {
		        	      if(app != item) {
		        	        app.isChecked = false;  
		        	      }
		        	    });			        	
		        };
		        
		        $scope.createPageReport=function() {
					
					var table = document.getElementById('HistoricalDataTable');
					var start = table.rows.item(1).cells[1].childNodes[0].value;
					var end = $scope.rowNum;					
					
					$scope.openWaitModal();
					$http.get("HistDataServlet?requestType=createPageReport&pageStart=" +start + "&pageEnd=" +end)
					.then(function(response) {
						$scope.closeWaitModal();
						$scope.report=response.data.status;							
		   				if($scope.report == "Success"){
		   					alert("Report created successfully in Download folder");
		   				}else{
		   					alert("Report Creation Failed");
		   				}
		   			});										
				}
		        
		        $scope.refreshPageList=function(secNo, rowNo) {
		        	
		        	$scope.openWaitModal();
					$http.get("HistDataServlet?requestType=refreshPage&rowStart=" +secNo + "&rowEnd=" +rowNo)
					.then(function(response) {
						$scope.closeWaitModal();
		   				$scope.pageRow=response.data;
		   			});
		        }
		        
		        $scope.sendToWorkday = function(row,csvCount) {
		        	
		        	$scope.hideSentToWD = false;
		        	var myTab = document.getElementById('ValidateHistDataTopTable');
		        	
		        	if(myTab.rows.length <= 0)
		        	{
		        		myTab = document.getElementById('ValidateHistDataBottomTable');
		        	}		        	
		        	var totCsvCount = myTab.rows.item(row).cells[6].childNodes[0].value;
		        	
		        	if(csvCount == 1)
		        	{
		        		$scope.openWaitModal();
		        	}		        	
					var request = {
							method : 'POST',
							url : 'HistDataServlet?requestType=sentToWorkday' + "&tenantName="+$scope.selectedTenantData.tenantName + "&tenantId="+$scope.selectedTenantData.tenantId 
							+ "&endPointURL=" + $scope.selectedTenantData.tenantUrl + "&tenantUserName="+$scope.selectedTenantData.tenantUsername + "&tenantPassword="+$scope.tenantPWD
							+ "&parallelReqSize="+$scope.parReq + "&requestDelay="+$scope.reqDelay + "&seqNo="+myTab.rows.item(row).cells[1].childNodes[0].value
							+ "&operationName="+myTab.rows.item(row).cells[2].childNodes[0].value + "&fileName="+myTab.rows.item(row).cells[3].childNodes[0].value 
							+ "&totalRow="+myTab.rows.length + "&rowNo="+row + "&totalCsvCount="+totCsvCount + "&csvCount="+csvCount, 
							headers : { 
								'Content-Type' : undefined
							}
					};
					$http(request).then(function(response) {
						
						if(csvCount == 1)
			        	{
							$scope.closeWaitModal();
			        	}
						$scope.sentToWorkdayList = response.data;
						if(response.data.isError == true)
						{
							alert(response.data.errorMsg);
							return;
						}
						if(response.data.status != "Success")
						{
							alert("Unable to process into Workday for "+response.data.operationName +"!!!");
							return;
						}
						csvCount++;
						if(csvCount<=totCsvCount)
						{
							$scope.sendToWorkday(row,csvCount);
						}
						else
						{
							row++
							if(row<myTab.rows.length)
							{
								csvCount = 1;
								$scope.sendToWorkday(row,csvCount);
							}
						}							
					},
					function(response) {	
					});						
		        }
		        
		        $scope.generateErrorReport = function(tableID) {
		        	
		        	var count = -1;
		            var table = document.getElementById('sentToWdHistTable');
	    			var rowCount = table.rows.length;
	    			var error = new Array();
	    			var completed;
	    			for(var i=0; i<rowCount; i++) 
	    			{
	    				if( i != 0)
	    				{
	    					count++;
	    					var row = table.rows[i];
		    				var chkbox = row.cells[0].childNodes[0];
		    				 
		    				if(null != chkbox && true == chkbox.checked) 
		    				{
		    					completed = row.cells[5].childNodes[0];
		    					error.push(row.cells[1].childNodes[0].value);
		    					error.push(row.cells[2].childNodes[0].value);
		    				}
	    				}			    				
	    			}
	    			console.log(error);
	    			
	    			if(completed != undefined)
	    			{
	    				if(completed.checked == false)
		    			{
		    				window.open("generate-report.jsp?parameter="+error, "_blank", "width=1000,height=600,scrollbars=yes, menubar=no,resizable=yes");
		    			}
		    			else
		    			{
		    				alert("There is no issue with the result");
		    			}
	    			}
	    			else
	    			{
	    				alert("Please select one record!!!");
	    			}
		        }
		        
				$scope.downloadTemplate=function() {
					
					$scope.openWaitModal();
					$http.get("HistDataServlet?requestType=downloadTemplate&csvName=" +$scope.templateName)
					.then(function(response) {
						$scope.closeWaitModal();
						$scope.template=response.data.status;							
		   				if($scope.template == "Success"){
		   					alert("Template created successfully in Download folder");
		   				}else{
		   					alert("Template Creation Failed");
		   				}
		   			});
				}
		        
			}]);
	
	myApp.controller(
			'PGPCtrl',
			['$scope',
			'$http',
			function($scope, $http) {
				
				var csvFileName;
				var pubKeyFileName;
				
				$scope.createKeys = function(keyName){
					
					if(keyName == undefined)
					{
						alert("Please provide the public key name");
						return;
					}
					$scope.openWaitModal();
					$http.get("PGPServlet?requestType=createKeys&keyName=" +keyName)
					.then(function(response) {
						$scope.closeWaitModal();
		   				$scope.keys=response.data.status;							
		   				if($scope.keys == "Success"){
		   					alert("Keys are successfully generated in the Download folder");
		   				}else{
		   					alert("Public key of the same name is already exists in the database.");
		   				}
		   			});
				};
			    
				$scope.encryptFile = function() {
					
					if(filesData.length <2)
					{
						alert("Please select CSV/Public key file");
						return;
					}
					
			    	$scope.openWaitModal();			    	
			    	for(row=0; row<filesData.length; row++){
			    		var request = {
								method : 'POST',
								url : 'PGPServlet?requestType=encryptFile',
								data : filesData[row],
								headers : { 
									'Content-Type' : undefined
								}
						};
						$http(request).then(function(response) {
							$scope.closeWaitModal();
							$scope.encrpt=response.data.status;							
				   				if($scope.encrpt == "Success")
								{
									alert("Encrypted file is successfully generated in the Download folder");	
								}
				   				else if($scope.encrpt == "In Progress")
								{
										
								}
								else
								{
									alert("File Encryption failed");	
								}
							},
							function(response) {	
							});						
			    	}
			    	filesData = [];
			    };			    			   
			    
			    $scope.decryptFile = function(publicKey){
			    	
					if(filesData.length <1)
					{
						alert("Please select PGP file");
						return;
					}
					if(publicKey == undefined)
					{
						alert("Please select a public key");
						return;
					}
					$scope.openWaitModal();
			    	$scope.listFileAsc = filesData[0];						
					var request = {
							method : 'POST',
							url : "PGPServlet?requestType=decryptFile&publicKeyName=" +publicKey.publicKeyName,
							data : $scope.listFileAsc,
							headers : { 
								'Content-Type' : undefined
							}
					};
					$http(request).then(function(response) {
						$scope.closeWaitModal();
						$scope.decrpt=response.data.status;							
			   				if($scope.decrpt == "Success")
							{
								alert("Decrypted file is successfully generated in the Download folder");	
							}
			   				else if($scope.encrpt == "In Progress")
							{
									
							}
							else
							{
								alert("File Decryption failed");	
							}
						},
						function(response) {	
						});
					filesData = [];
			    };
				
			}]);
	
	
	myApp.controller(
			'SettingsCtrl',
			['$scope',
			'$http',
			function($scope, $http) {	
				
				 $scope.formSubmitted = false;				 
				 
				$scope.edit = true;
				$scope.hideform = true;
				
			   $scope.showEditUserPasswordSettings = true;
			   $scope.showEditUserEmailSettings = false;
    		   $scope.showAdminSettings = false;
    		   
    		   var ing;
    		   
    		   messageResource.init({
    				  filePath : 'resource'
    				});

    			messageResource.load('env', function(){ 

    				ing = messageResource.get('1', 'env');
    			});
    		   
    		   $scope.showSettings = function (settings) {
    			   if('EditUserPasswordSettings' == settings){	
    				   $scope.showEditUserPasswordSettings = true;
    				   $scope.showEditUserEmailSettings = false;
		    		   $scope.showAdminSettings = false;
		    	   } else if('EditUserEmailSettings' == settings){	
    				   $scope.showEditUserPasswordSettings = false;
    				   $scope.showEditUserEmailSettings = true;
		    		   $scope.showAdminSettings = false;
		    	   } else if('AdminSettings' == settings){		    
		    		   $scope.showEditUserPasswordSettings = false;
					   $scope.showEditUserEmailSettings = false;		   	
		    		   $scope.showAdminSettings = true;
		    	   }
		       }

    		   $scope.changePassword = function (e) {
    			   $scope.message = "";
    			   var invalidInputName = "";
    			   
    			   if($scope.editPasswordForm.currentPassword.$modelValue == undefined  ) {
    					$scope.message = "Please provide Current Password";
    					invalidInputName = $scope.editPasswordForm.currentPassword.$name;
    				} else if($scope.editPasswordForm.newPassword.$modelValue == undefined 
    						|| !Validation.validPassword($scope.editPasswordForm.newPassword.$modelValue)  ) {
    					$scope.message = "Please provide proper Password having 6 to 12 characters.";
    					invalidInputName = $scope.editPasswordForm.newPassword.$name;
    				} else if ($scope.editPasswordForm.confirmNewPassword.$modelValue != $scope.editPasswordForm.newPassword.$modelValue ) {
    					$scope.message = "Confirm Password does not match.";
    					invalidInputName = $scope.editPasswordForm.confirmNewPassword.$name;
    				} else {
    					$http({method: 'POST', url: 'LoginTrigger?requestType=changePassword',
    						 data: { 'currentPassword' : $scope.editPasswordForm.currentPassword.$modelValue,
    							 	'newPassword' : $scope.editPasswordForm.newPassword.$modelValue },
    			    		contentType: 'application/json'
    			    	}).then(function(response) {	
    			    		$scope.message = response.data.status;
    					});
    				}
    			   
    				$('.has-error').removeClass("has-error");
    				$('[name="'+invalidInputName+'"]').addClass("has-error");
    		   }
    		   
    		   $scope.changeEmail = function (e) {
    			   $scope.message = "";
    			   var invalidInputName = "";
    			   
    			   if($scope.editEmailForm.currentEmail.$modelValue == undefined  ) {
	   					$scope.message = "Please provide Current Email Id.";
	   					invalidInputName = $scope.editEmailForm.currentEmail.$name;
	   				} else if($scope.editEmailForm.newEmail.$modelValue == undefined 
	   						|| !Validation.validEmail($scope.editEmailForm.newEmail.$modelValue) ) {//|| $scope.editEmailForm.newEmail.$modelValue.indexOf(ing) == -1
	   					//$scope.message = "Please provide proper new Email Id. of " + ing +".";
	   					invalidInputName = $scope.editEmailForm.newEmail.$name;
	   				} else if ($scope.editEmailForm.newEmail.$modelValue == $scope.editEmailForm.currentEmail.$modelValue) {
	   					$scope.message = "New email Id. is same as current email Id.";
	   					invalidInputName = $scope.editEmailForm.newPassword.$name;
	   				} else {
	   					$http({method: 'POST', url: 'LoginTrigger?requestType=changeEmailId',
	   						 data: { 'currentEmail' : $scope.editEmailForm.currentEmail.$modelValue,
	   							 	'newEmail' : $scope.editEmailForm.newEmail.$modelValue },
	   			    		contentType: 'application/json'
	   			    	}).then(function(response) {	
	   			    		$scope.message = response.data.status;
	   					});
	   				}

	   				$('.has-error').removeClass("has-error");
	   				$('[name="'+invalidInputName+'"]').addClass("has-error");
    		   }
		       		       
	          
	          $scope.deleteUser = function (id, email) {
	        	  $scope.openWaitModal();
	        	  var user = "{\"inputEmailId\":\""+email+"\"}";
	        	  $http({url: 'LoginTrigger?requestType=deleteUser', method:'POST',  data:user, contentType: 'application/json'})
					.then(function(response) {
						$scope.closeWaitModal();
						if(response.data.status =='Success') {
							$scope.addAlert('success','User successfully deleted.');
							$scope.allUserInfo = response.data.userInfos;
							$scope.$apply();
						} else {
							$scope.addAlert('danger', response.data.errorMessage);
						}
					});	
	          }
	          
	          $scope.adminAdd = function (adminToBeState, email) {
	        	  
	        	  if(adminToBeState) {
	        		  var user = "{\"inputEmailId\":\""+email+"\"}";
	        		  $http({url: 'LoginTrigger?requestType=adminAdd', method:'POST',  data:user, contentType: 'application/json'})
	        		  .then(function(response) {									
	        			  if(response.data.status =='Success') {
	        				  $scope.addAlert('success','User successfully updated.');
	        			  } else {
	        				  $scope.addAlert('danger', response.data.errorMessage);
	        			  }
	        		  });	
	        	  } else {
	        		  $scope.adminRemove(email);
	        	  }
	          }
	          
	          $scope.adminRemove = function (email) {
	        	  var user = "{\"inputEmailId\":\""+email+"\"}";
	        	  $http({url: 'LoginTrigger?requestType=adminRemove', method:'POST',  data:user, contentType: 'application/json'})
					.then(function(response) {									
						if(response.data.status =='Success') {
							$scope.addAlert('success','User successfully updated.');
						} else {
							$scope.addAlert('danger', response.data.errorMessage);
						}
					});	
	          }
	          
		        $scope.administrator = administrator;
			} ]);
				