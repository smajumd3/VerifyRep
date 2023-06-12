var myApp = angular.module('hyperloader.app', [ 'ngAnimate', 'ngMaterial',
		'ngMessages', 'ngSanitize', 'ui.bootstrap', 'chart.js', 'ui.knob' ]); // 'treeControl'

myApp.directive('validFile', function () {
	return {
	    require: 'ngModel',
	    link: function (scope, el, attrs, ngModel) {
	        ngModel.$render = function () {
	            ngModel.$setViewValue(el.val());
	        };

	        el.bind('change', function () {
	            scope.$apply(function () {
	                ngModel.$render();
	            });
	        });
	       
	    }
	};
});

myApp.directive("required", function() {
    return {
        restrict: 'A', // only for attributes
        compile: function(element) {
            // insert asterisk after element 
            element.append("&nbsp;<span class='required'>*</span>");
        }
    };
});

myApp.directive('fileModel', [ '$parse', function($parse) {
	function fn_link(scope, element, attrs) {
		var onChange = $parse(attrs.fileModel);
		element.on('change', function(event) {	
			onChange(scope, {
				$files : event.target.files
			});
		});			
	}
	return {
		link : fn_link
	};
} ]);

myApp.directive('customOnChange', function() {
	  return {
	    restrict: 'A',
	    link: function (scope, element, attrs) {
	      var onChangeFunc = scope.$eval(attrs.customOnChange);
	      element.bind('change', onChangeFunc);
	    }
	  };
});

myApp.controller('hyperloader.controller', [
	'$scope',
	'$http',
	'$interval',
	'$window',
	'$mdDialog',
	'$mdToast',
	'$uibModal',
	function($scope, $http, $interval, $window, $mdDialog, $mdToast, $uibModal) {
		
		var data;
		var isAdded = false;
		var isDeleted = false;
		var ruleLen;
		var attrLen;
		var oldItem;
		var editId;
		var selectedItems;
		var add;
		var del;
		var currentTreeNode;
		var prevTreeNode;
		var selectedItemAdd;
		var selectedItemDel;
		
		/*$scope.ruleNames = ["Applicant", "Position", "Hire", "Hire CW", "Termination", "Worker Address", "End Contingent Worker", "Worker Biographic", "Worker Demographic","Service Dates",
			"Applicant Phone", "Leave of Absence", "EE Base Compensation", "Bonus Plan", "Allowance Plan", "Stock Plan", "Merit Plan", "Pay Group"];*/
							
		$scope.isAllMapped = true;
		$scope.isCompared = true;
		$scope.isFileBased = true;
		$scope.isTenantBased = true;
		$scope.projectManagementRowIndex = "";
		$scope.postLoad = false;
		$scope.fileName = "";
		
		$scope.options = function () {
			
			return {
				  displayPrevious: false,
				  unit: "%",
				  readOnly: true,
				  skin: {
				      type: 'tron',
					  width: 5,
					  color: '#337ab7',
					  spaceWidth: 3
				  },
				  subText: {
					  enabled: true,
					  text: 'Complete',
					  color: 'gray',
					  font: 'auto'
				  },
				  barCap: 25,
				  trackWidth: 35,
				  barWidth: 30,
				  barColor:'#337ab7',
				  trackColor: 'rgba(0,0,255,.3)',
				  textColor: '#337ab7',
				  dynamicOptions: true
				};
		};	
		
		$scope.uiKnobOptions = $scope.options();
		
		$scope.showAlert = function(message, severity) {

			var message = (!typeof message == "string") ? JSON
					.stringify(message) : message;

			if(severity == undefined) {

				$mdToast.show({
					controller : function($scope, $mdToast) {
						$scope.severity = "info";
						$scope.message = message;
					},
					position : 'bottom right left',
					templateUrl : 'pop-template.html',
					parent : $("body")[0],
					hideDelay : 3000,
				});

			} else {
				
				$mdToast.show({
					controller : function($scope, $mdToast) {
						$scope.severity = "error";
						$scope.message = message;
					},
					position : 'bottom right left',
					templateUrl : 'pop-template.html',
					parent : $("body")[0],
					hideDelay : 5000,
				});
			}
		}

		$scope.waitDialog = {};
		$scope.showWaitDialog = function(waitMessage) {
			$scope.waitMessage = waitMessage;
			$mdDialog.show({
				  controller: function () { this.parent = $scope; },
				  controllerAs: 'ctrl',
				templateUrl : 'wait.dialog.tmpl.html',
				parent : $("body")[0],
				clickOutsideToClose : false,
				fullscreen : false
			});
		}

		$scope.hideDialog = function(ev) {
			$mdDialog.hide();
		}
		
		var ruleLists;
		
		messageResource.init({
			  filePath : 'resource'
			});
		
		messageResource.load('rule', function(){ 

			rulesName = messageResource.get('application.rules', 'rule');
			ruleLists = rulesName.split(';');
		});

		$scope.dataCenters = ["Atlanta", "Dublin", "Portland", "Atlanta(Production)", "Dublin(Production)", "Portland(Production)"];
		
		$scope.init = function () {
			var request = {
					method : 'GET',
					url : 'getUser'
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.currUser = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});		    
		}
		
		$scope.pages = [];
		$scope.selectedPage = {};
		$scope.sections = [];
		
		$scope.statuses = ["Pending","Complete"];
		
		$scope.initPrjTemplateData = function(container) {
		    $scope.validationMessages = [];
		    $scope.sendToWD = false;
		    $scope.result = false;
		    $scope.validate = false;
		    $scope.viewValidation = false;
		    clearPageFormData();
		    $scope.saveTemplateFileData();
		    $scope.populatePageData();
		    $scope.postLoad = false;
		    $scope.populateTenantData();
		    $scope.validationPercentage = 0;
		}
		
		$scope.getSelectedPage = function(value) {
		    $scope.selectedPage = $scope.pages[value];
		    $scope.sections = $scope.selectedPage.sections;
		    $scope.mapFileNames = new Array($scope.sections.length);
		    $scope.executedPercentage = new Array($scope.sections.length);
		    $scope.validatedPercentage = new Array($scope.sections.length);
		}
		
		function clearPageFormData() {
			$scope.pages = [];
			$scope.value = undefined;
		    $scope.selectedPage = undefined;
		    $scope.sections = [];
		}
		
		$scope.populatePageData = function() {
			
			var request = {
					method : 'GET',
					url : 'getPagesByUserId'
				};
				$scope.showWaitDialog("Loading Data..");
				
				$http(request).then(function(response) {
					$scope.pages = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});
		}
		
		$scope.toggleExecute = function(section, checked) {
		   
		   $scope.showWaitDialog("Updating..");
     
               $http({
                   method : 'PUT',
                   url : 'updateSectionAccess/' + section.sectionId + '/' +  checked
               }).then(function(response) {
			       $scope.hideDialog();
               }, function(response) {
				   $scope.hideDialog();
				   console.error(response);
				   $scope.showAlert("Operation Failed", "error");
			   })
		}		
		
		$scope.saveStatus = function(section, value) {
		   $scope.showWaitDialog("Processing..");
		    
           $http({
               method : 'PUT',
               url : 'updateSectionStatus/' + section.sectionId + '/' +  value
           }).then(function(response) {
		       $scope.hideDialog();
           }, function(response) {
			   $scope.hideDialog();
			   console.error(response);
			   $scope.showAlert("Operation Failed", "error");
		   })		    
		}
		
		$scope.saveTemplateFileData = function() {
		    
			var request = {
					method : 'POST',
					url : 'saveTemplateFileData/'
			};
			$scope.showWaitDialog("Please Wait..");
				
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.showAlert("Data Saved..");					
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});		    
		}
		
		$scope.upDateData = function() {
			var request = {
				method : 'POST',
				url : 'upDateData/' + $scope.selectedPage.pageId
			};
		    $scope.showWaitDialog("Please Wait..");
		    				
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.showAlert("Data updated..");					
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.addSection = function() {
			var request = {
				method : 'POST',
				url : 'addSection/' + $scope.selectedPage.pageId + '/' + $scope.areaName + '/' + $scope.taskName + '/' + $scope.operationName
			};
		    $scope.showWaitDialog("Adding section..");
		    				
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.showAlert("Section added..");					
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});		
		}
		
		$scope.results = [];
		$scope.showResult = function(section) {
			var request = {
				method : 'GET',
				url : 'getPagesByUserId'
			};
			$scope.showWaitDialog("Loading Data..");
			
			$http(request).then(function(response) {
				$scope.pages = response.data;
				$scope.selectedPage = $scope.pages[$scope.value];
				$scope.sections = $scope.selectedPage.sections;
				var newSection = $scope.getSection($scope.sections, section);
				$scope.sendToWD = false;
			    $scope.validate = false;
			    $scope.viewValidation = false;
			    $scope.result = true;
	            $scope.getResults(newSection);
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		let newSection;
		$scope.getSection = function(sections, section) {
		    sections.forEach(element => {
		        if(element.sectionId == section.sectionId)
		            newSection = section;
		    });
		    return newSection;
		}
		
		$scope.getResults = function(section) {
		    var results = section.results;
			results.sort(function ( a, b ) { return b.resultId - a.resultId; });
			$scope.results = results;
		}
		
		$scope.getDate = function(date) {
		    var d = new Date(date);
		    return d.toDateString();
		}
		
		$scope.wsResponsesStatus = [];
		$scope.initDashboard = function(container) {
		    $scope.resultData = [];
			$scope.resultData.push({category : "Total Success", value : $scope.wsResponsesStatus.totalSuccess});
			$scope.resultData.push({category : "Total Failures", value : $scope.wsResponsesStatus.totalFailures});
			
            am4core.ready(function() {

				// Themes begin
				am4core.useTheme(am4themes_animated);
				// Themes end
				
				var chart = am4core.create("chartdiv", am4charts.PieChart3D);
				chart.hiddenState.properties.opacity = 0; // this creates initial fade-in
				
				chart.legend = new am4charts.Legend();
				
				chart.data = $scope.resultData;
				
				var series = chart.series.push(new am4charts.PieSeries3D());
				series.dataFields.value = "value";
				series.dataFields.category = "category";
				
				});			
		}
		
		$scope.clientTenants = [];
		$scope.userTenants = [];
		$scope.tenantEditable = {};
		$scope.selectedTenant = {};
		$scope.selectedTenant1 = {};
		
        function successTenantResponse() {
        	$scope.populateTenantData();
        	clearTenantFormData();
        }		
		
		$scope.initTenant = function(container) {
		    clearTenantFormData();
            $scope.getTenantsByClient();
            $scope.populateTenantData();
		}
		
		$scope.getTenantUrl = function(selectedValue) {
			$scope.tenantEditable.tenantDataCenter = $scope.dataCenters[selectedValue];
			var request = {
					method : 'GET',
					url : 'property/' + $scope.tenantEditable.tenantDataCenter
				};
			
			$http(request).then(function(response) {
				$scope.tenantEditable.tenantUrl = response.data[0];
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.getTenantsByClient = function() {
			var request = {
					method : 'GET',
					url : 'getTenantsByClient'
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.clientTenants = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});
		}		
		
		$scope.populateTenantData = function() {
			
			var request = {
					method : 'GET',
					url : 'getAllTenantsByUser'
				};
//Suman				$scope.showWaitDialog("Please Wait 1");
				
				$http(request).then(function(response) {
					$scope.userTenants = response.data;
//					$scope.hideDialog();
				}, function(response) {
//					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});
		}
		
        //Clear the form
        function clearTenantFormData() {
            $scope.tenantEditable.id = -1;
            $scope.tenantEditable.tenantName = "";
            $scope.tenantEditable.tenantDataCenter = "";
            $scope.tenantEditable.tenantUrl = "";
            $scope.tenantEditable.tenantUser = "";
            $scope.tenantEditable.tenantUserPassword = "";
            $scope.tenantEditable.userId = -1;
            $scope.tenantEditable.client = "";
            $scope.selectedValue1 = undefined;
            $scope.selectedTenant = {};
            $scope.selectedTenant1 = {};
//            $scope.tenantForm.$setPristine();
        }
		
		$scope.deleteTenant = function(tenant) {
			var request = {
					method : 'DELETE',
					url : 'deleteTenant/' + tenant.id
				};

			$scope.showWaitDialog("Deleting Tenant");
			
			$http(request).then(function(response) {
				successTenantResponse();
				$scope.hideDialog();
				$scope.showAlert("Tenant Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Tenant failed to delete", "error");
			});
		}
		
		$scope.editTenant = function(tenant) {
            $scope.tenantEditable.tenantName = tenant.tenantName;
            $scope.tenantEditable.tenantDataCenter = tenant.tenantDataCenter;
            $scope.tenantEditable.id = tenant.id;
            $scope.tenantEditable.tenantUrl = tenant.tenantUrl;
            $scope.tenantEditable.tenantUser = tenant.tenantUser;
            $scope.tenantEditable.tenantUserPassword = tenant.tenantUserPassword;
            $scope.tenantEditable.userId = tenant.userId;
            $scope.tenantEditable.client = tenant.client;     
		}
		
		$scope.resetTenantForm = function() {
			clearTenantFormData();			
		}
		
		$scope.submitTenantForm = function() {
		
		    $scope.tenantEditable.tenantName = $scope.selectedTenant.tenantName;
            $scope.tenantEditable.tenantDataCenter = $scope.selectedTenant.tenantDataCenter;
            $scope.tenantEditable.id = $scope.selectedTenant.id;
            $scope.tenantEditable.tenantUrl = $scope.selectedTenant.tenantUrl;
            $scope.tenantEditable.client = $scope.selectedTenant.client;
			
			if($scope.tenantForm.$valid) {
			                   
                $scope.showWaitDialog("Updating Tenant");
     
                $http({
                    method : 'POST',
                    url : 'addTenant',
                    data : angular.toJson($scope.tenantEditable),
                    headers : {
                        'Content-Type' : 'application/json'
                    }
                }).then(function(response) {
                	successTenantResponse();
					$scope.hideDialog();
					$scope.showAlert("Tenant Created");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Tenant failed to create", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}
		
		$scope.getTenantInfo = function(selectedValue) {
			$scope.selectedTenant = $scope.clientTenants[selectedValue];	
		}

		$scope.getUserTenantInfo = function(selectedValue) {
			$scope.selectedTenant = $scope.userTenants[selectedValue];	
		}
		
		$scope.getUserTenantInfo1 = function(selectedValue) {
			$scope.selectedTenant1 = $scope.userTenants[selectedValue];	
		}
		
		$scope.applications = [];
		$scope.appEditable = {};
		$scope.applicationList = [];
		$scope.appVersions = ["36.0", "35.2", "35.1", "35.0", "34.2", "34.1", "34.0", "33.2", "33.1", "33.0"];
		
		$scope.initApplication = function(container) {
			$scope.getApplicationList();
			$scope.populateAppData();
			clearAppFormData();
		}
		
		$scope.getApplicationList =  function() {
			var request = {
					method : 'GET',
					url : 'getApplications'
				};
			$scope.showWaitDialog("Populating Application List");
			
			$http(request).then(function(response) {
				$scope.applicationList = response.data;
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.populateAppData = function() {
			
			var request = {
					method : 'GET',
					url : 'getAllApplicationsByUser'
				};
				$scope.showWaitDialog("Processing..");
				
				$http(request).then(function(response) {
					$scope.applications = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});
		}
		
        function successAppResponse() {
        	$scope.populateAppData();
        	clearAppFormData();
        }
		
        //Clear the form
        function clearAppFormData() {
            $scope.appEditable.applicationId = -1;
            $scope.appEditable.applicationName = "";
            $scope.appEditable.version = "";
            $scope.indexVal = undefined;
            $scope.appEditable.userId = -1;
        }
        
		$scope.deleteApplication = function(application) {
			var request = {
					method : 'DELETE',
					url : 'deleteApplication/' + application.applicationId
				};

			$scope.showWaitDialog("Deleting Application");
			
			$http(request).then(function(response) {
				successAppResponse();
				$scope.hideDialog();
				$scope.showAlert("Application Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Application - failed to delete", "error");
			});
		}
		
		$scope.editApplication = function(application) {
            $scope.appEditable.applicationName = application.applicationName;
            $scope.appEditable.version = application.version;
            $scope.appEditable.applicationId = application.applicationId;
            $scope.appEditable.userId = application.userId;
		}
		
		$scope.resetApplicationForm = function(applicationForm) {
			$scope.appEditable = {};
		}
		
		$scope.submitApplicationForm = function(applicationForm) {
			
			if(applicationForm.$valid) {
			    $scope.appEditable.version = $scope.appVersions[$scope.indexVal];
                var method = "";
                var url = "";
                if ($scope.appEditable.applicationId == -1) {
                    //Id is absent in form data, it creates new application operation
                    method = "POST";
                    url = 'addApplication';
                    $scope.showWaitDialog("Creating Application Details");
                } else {
                    //Id is present in form data, it edits application operation
                    method = "PUT";
                    url = 'updateApplication';
                    $scope.showWaitDialog("Updating Application Details");
                }
                
                $http({
                    method : method,
                    url : url,
                    data : angular.toJson($scope.appEditable),
                    headers : {
                        'Content-Type' : 'application/json'
                    }
                }).then(function(response) {
                	successAppResponse();
					$scope.hideDialog();
					$scope.showAlert("Application added");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Application failed to add", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}
		
		$scope.operations = [];
		$scope.operEditable = {};
		$scope.operationList = [];
		$scope.application = {};
		$scope.operationValues = {};	
		
		$scope.initOperation = function(container) {
			clearOperFormData();
			$scope.populateAppData();
		}
		
		$scope.getApplicationOperations =  function(selectedValue) {
			$scope.application = $scope.applications[selectedValue];
			$scope.applicationVersion = $scope.application.version;
			
			var request = {
					method : 'GET',
					url : 'getApplicationOperations/' + $scope.applications[selectedValue].applicationId
				};
			$scope.showWaitDialog("Populating Operation List");
			
			$http(request).then(function(response) {
				$scope.operationList = response.data;
				$scope.hideDialog();
				$scope.populateOperForApp();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.getResponsePath = function(selectedValue) {
			$scope.operEditable.operationName = $scope.operationList[selectedValue];
			var request = {
					method : 'GET',
					url : 'property/' + $scope.operEditable.operationName
				};
			
			$http(request).then(function(response) {
				$scope.operEditable.responsePath = response.data[0];
			}, function(response) {
				console.error(response);
			});
		}
		
		$scope.populateOperForApp = function() {
			var request = {
					method : "GET",
					url : 'getOperationsForApplication/' + $scope.application.applicationId
			};
			
			$scope.showWaitDialog("Loading details..");
			
			$http(request).then(function(response) {
				$scope.operations = response.data;
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.deleteOperation = function(operation) {
			var request = {
					method : 'DELETE',
					url : 'deleteOperation/' + operation.operationId
				};

			$scope.showWaitDialog("Deleting Operation");
			
			$http(request).then(function(response) {
				successOperResponse();
				$scope.hideDialog();
				$scope.showAlert("Operation Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation - failed to delete", "error");
			});
		}
		
		$scope.submitOperationForm = function(operationForm) {
			
			if(operationForm.$valid) {
                var method = "";
                var url = "";
                if ($scope.operEditable.operationId == -1) {
                    //Id is absent in form data, it creates new operation
                    method = "POST";
                    url = 'addOperation';
                    $scope.showWaitDialog("Creating Operation Details");
                } else {
                    //Id is present in form data, it edits operation
                    method = "PUT";
                    url = 'addOperation';
                    $scope.showWaitDialog("Updating Operation Details");
                }

                $http({
                    method : method,
                    url : url,
                    data : angular.toJson($scope.operEditable),
                    headers : {
                        'Content-Type' : 'application/json'
                    }
                }).then(function(response) {
                	successOperResponse();
					$scope.hideDialog();
					$scope.showAlert("Operation Added");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation failed to Add", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}
		
        function successOperResponse() {
        	$scope.populateOperForApp();
        	clearOperFormData();
        }
		
        //Clear the form
        function clearOperFormData() {
            $scope.operEditable.operationId = -1;
            $scope.operEditable.operationName = "";
            $scope.operEditable.responsePath = ""; 
            $scope.operEditable.ruleName = "";
            $scope.operEditable.userId = -1;
            $scope.selectedValue2 = undefined;
            $scope.selectedValue3 = undefined;
        }
		
		$scope.editOperation = function(operation) {
            $scope.operEditable.operationName = operation.operationName;
            $scope.operEditable.responsePath = operation.responsePath;
            $scope.operEditable.operationId = operation.operationId;
            $scope.operEditable.ruleName = operation.ruleName;
            $scope.operEditable.userId = operation.userId;
		}
		
		$scope.resetOperationForm = function(operationForm) {
			clearOperFormData();
		}
		
		$scope.initRule = function(container) {
		    $scope.container = container;
			$scope.populateSavedOperations();
		}
		
		$scope.populateSavedOperations = function() {
			var request = {
					method : "GET",
					url : 'getAllOperationsByUser'
			};
			
			$scope.showWaitDialog("Loading details..");
			
			$http(request).then(function(response) {
				$scope.operations = response.data;
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.getOperationList = function(selectedValue) {
			var operation = $scope.operations[selectedValue];
			
			var request = {
					method : "GET",
					url : 'getOperationValue/' + operation.operationId
			};
			
			$scope.showWaitDialog("Getting Operation List...");
			
			$http(request).then(function(response) {
				$scope.operationValues = response.data;
				$scope.findRuleBuilder();
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});		
		}
		
		$scope.findRuleBuilder = function() {
			
			document.getElementById("attrAdd").disabled = false;
			document.getElementById("webAdd").disabled = false;
			document.getElementById("webDel").disabled = false;
			var request = {
					method : "GET",
					url : 'getRuleDetailsInTree'
			};
			
			$http(request).then(function(response) {
				$scope.ruleBuilders = response.data;
				data = $scope.ruleBuilders;
				
				$(document).ready(function () {	
					
					var source =
	                {
	                    datatype: "json",
	                    datafields: [
	                    	{ name: 'id' },
	                        { name: 'parentid' },
	                        { name: 'text' }
	                    ],
	                    id: 'id',
	                    localdata: data
	                };
	                
	                var dataAdapter = new $.jqx.dataAdapter(source);
	                dataAdapter.dataBind();
	                var records = dataAdapter.getRecordsHierarchy('id', 'parentid', 'items', [{ name: 'text', map: 'label'}]);
	                $('#jqxWidget').jqxTree({ source: records, width: '300px', keyboardNavigation: false});
	                
	                $("#jqxWidget .jqx-fill-state-pressed").attr('contentEditable',false);
	                
	                /*Web Service Request Add*/
	                $('#webAdd').click(function () {
			    	    
	                	if(!isAdded && !isDeleted)
	                	{
		                    selectedItemAdd = $('#jqxWidget').jqxTree('selectedItem');
		                    if ((selectedItemAdd != null || selectedItemAdd != undefined) && selectedItems != undefined) 
		                    {
			                    //console.log(selectedItemAdd.id);
			                    //console.log(selectedItemAdd.label);
		                        $('#jqxWidget').jqxTree('addTo', { label: 'Node' }, selectedItemAdd.element, false);
		                        $('#jqxWidget').jqxTree('render');
			                    currentTreeNode = 'Node';
			                    add = 'a';
			                    isAdded = true;
		                    }
		                    else 
		                    {
		                    	alert("Please select a node first before adding a new node");
		                    	return;
		                    }
	                	}
	                	else
	                	{
	                		alert("Please save the previously added/deleted/edited node first before proceed to any other operation");
	                		$('#webAdd').attr('disabled',true);
	                		return;
	                	}
	                });
	                
	                /*Web Service Request Delete*/
	                $('#webDel').click(function () {			            
			    	    
	                	if(!isDeleted && !isAdded)
	                	{
	                		selectedItemDel = $('#jqxWidget').jqxTree('selectedItem');
	                		if(selectedItemDel.parentElement == null)
	                		{
		                    	alert("Root node can not be deleted");
		                    	return;
	                		}
	                		
		                    if ((selectedItemDel != null || selectedItemAdd != undefined) && selectedItems != undefined) 
		                    {
		                    	//console.log(selectedItemDel.id);
		                        $('#jqxWidget').jqxTree('removeItem', selectedItemDel.element, false);
		                        $('#jqxWidget').jqxTree('render');
			                    del = 'd';
			                    add = '';
			                    isDeleted = true;
			                    currentTreeNode = null;
		                    }
		                    else
		                    {
		                    	alert("Please select a node to delete");
		                    	return;
		                    }
	                	}
	                	else
	                	{
	                		alert("Please save the previously added/deleted/edited node first before proceed to any other operation");
	                		$('#webDel').attr('disabled',true);
	                		return;
	                	}

	                });
	                
	                $("#jqxWidget").on("keyup", ".jqx-rc-all", function(){

	                	if(oldItem == undefined)
	                	{
		                    currentTreeNode = $(this).context.innerText;
		                    //console.log(currentTreeNode);
		                    oldItem = selectedItems.label;
		                    editId = selectedItems.id;
	                	}
	                	else if(oldItem == selectedItems.label)
	                	{
		                    currentTreeNode = $(this).context.innerText;
		                    //console.log(currentTreeNode);
		                    oldItem = selectedItems.label;
	                	}
	                	else
	                	{
	                		alert("Please save the previously edited node first before proceed to any other operation");
	                		return;
	                	}
	                });
	                
	                $('#jqxWidget').on('select', function (event) {
	                	
	                	$("#jqxWidget .jqx-rc-all").attr('contentEditable',false);
	                	$("#jqxWidget .jqx-fill-state-pressed").attr('contentEditable',true);	
	                	
	                    var args = event.args;			                    			       			                    

	                    selectedItems = $('#jqxWidget').jqxTree('getItem', args.element);			                

	                    if(selectedItems.id.indexOf('jqxWidget') >= 0)
	                    {
	                    	selectedItems.id = '0';
	                    }
	                    
	                    //console.log(selectedItems.id);
	                    var request = {
	        					method : "GET",
	        					url : 'retriveAttrAndRule/' + selectedItems.id
	        			};
	                    
	                    $http(request).then(function(response) {
							$scope.ruleAttrRule = response.data;									
							var ruleObj = [];
							$scope.ruleDetails = '';
							var attrObj = [];
							$scope.attrDetails = '';
							$scope.deleteAllRowAttr();
							$scope.deleteAllRowRules();
							
							for (var i = 0; i < $scope.ruleAttrRule.length; i++) {										
								if($scope.ruleAttrRule[i].type == 'Rule')
								{
									var tempRuleObj = {
								      "name": $scope.ruleAttrRule[i].name,
								      "value": $scope.ruleAttrRule[i].value,
								      "type": $scope.ruleAttrRule[i].type
								    };									
									ruleObj.push(tempRuleObj);
								}
								else if($scope.ruleAttrRule[i].type == 'Attribute')
								{
									var tempAttrObj = {
								      "name": $scope.ruleAttrRule[i].name,
								      "value": $scope.ruleAttrRule[i].value,
								      "type": $scope.ruleAttrRule[i].type
								    };									
									attrObj.push(tempAttrObj);
								}
							}
							$scope.ruleDetails = ruleObj;
							ruleLen = $scope.ruleDetails.length;
							$scope.attrDetails = attrObj;
							attrLen = $scope.attrDetails.length;
	        				$scope.hideDialog();
	        			}, function(response) {
	        				$scope.hideDialog();
	        				console.error(response);
	        				$scope.showAlert("Operation Failed", "error");
	        			});	
	                });	                
				});
				
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});		
		}
		
		$scope.addAttrRow = function (tableID) {						
			
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
			cell2.innerHTML = '<input type="text" style="width:100%;" value="name">';
			
			var cell3 = row.insertCell(2);
			var element3 = document.createElement("input");
			element3.type = "text";
			element3.name = "txtbox[]";
			cell3.appendChild(element3);
			cell3.innerHTML = '<input type="text" style="width:100%;" value="value">';
        };
        
        $scope.deleteAttrRow = function (tableID) {
     
        	try {
    			var table = document.getElementById(tableID);
    			var rowCount = table.rows.length;
    			for(var i=0; i<rowCount; i++) 
    			{
    				if( i != 0)
    				{
    					var row = table.rows[i];
	    				var chkbox = row.cells[0].childNodes[0];
	    				if(null != chkbox && true == chkbox.checked) 
	    				{
	    					table.deleteRow(i);
	    					rowCount--;
	    					i--;
	    				}
    				}			    				
    			}
    			}catch(e) {
    				alert(e);
    			}
        };
        
        $scope.addRuleRow = function (tableID) {	

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
			for (var i = 0; i < ruleLists.length; i++) {
			    option = document.createElement("option");
			    option.value = ruleLists[i];
			    option.text = ruleLists[i];
			    element2.appendChild(option);
			}
			cell2.appendChild(element2);

			var cell3 = row.insertCell(2);
			var element3 = document.createElement("input");
			element3.type = "text";
			element3.name = "txtbox[]";
			cell3.appendChild(element3);
			cell3.innerHTML = '<input type="text" style="width:100%;" value="value">';

        };
        
        $scope.deleteRuleRow = function (tableID) {
    	    
        	try {
    			var table = document.getElementById(tableID);
    			var rowCount = table.rows.length;
    			for(var i=0; i<rowCount; i++) 
    			{
    				if( i != 0)
    				{
    					var row = table.rows[i];
	    				var chkbox = row.cells[0].childNodes[0];
	    				if(null != chkbox && true == chkbox.checked) 
	    				{
	    					table.deleteRow(i);
	    					rowCount--;
	    					i--;
	    				}
    				}			    				
    			}
    			}catch(e) {
    				alert(e);
    			}
        };
        
        $scope.deleteAllRowAttr = function () {
        	try 
        	{
    			var table = document.getElementById('attr');
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
        
		$scope.deleteAllRowRules = function () {
        	try 
        	{
    			var table = document.getElementById('rules');
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
		
		$scope.returnFileRequired = function() {
			  if($scope.edit) 
				  return true;
			  else
				  return false;
		}
		
		$('#singleUploadForm').submit(function(event) {
		    isAdded = false;
			isDeleted = false;
		    // You can directly create form data from the form element
		    // (Or you could get the files from input element and append them to FormData as we did in vanilla javascript)
		    var formData = new FormData(this);
		    $scope.showWaitDialog("Uploading Rules Data...");
		    $.ajax({
		        type: "POST",
		        enctype: 'multipart/form-data',
		        url: "/uploadRuleFileData",
		        data: formData,
		        processData: false,
		        contentType: false,
		        success: function (response) {
		        	$scope.hideDialog();
		            console.log(response);
		            $scope.updateRuleBuilder();
		            // process response
		        },
		        error: function (error) {
		        	$scope.hideDialog();
		            console.log(error);
		            // process error
		        }
		    });
		    
		    event.preventDefault();
		});
		
		$scope.loadRuleBuilder = function() {
			
			var request = {
					method : "GET",
					url : 'getRootDataElement'
			};
			$scope.showWaitDialog("Getting Root data element...");
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.values = response.data;
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});	
		}
		
		$scope.saveBuildRule = function(selectedValue) {
			
			var operation = $scope.operations[selectedValue];
			var addId;
			var delId;
			
			$('#webAdd').attr('disabled',false);
			isAdded = false;
			
			$('#webDel').attr('disabled',false);
			isDeleted = false;
			
			oldItem = undefined;
			
			if(editId == undefined || editId == null)
			{
				editId = '0';
			}
			
			if(selectedItemAdd == undefined)
			{
				if(selectedItems == undefined)
				{
            		alert("Please select a node first before saving the same");
            		return;
				}
				else
				{
					addId = selectedItems.id;
				}
			}
			else
			{
				addId = selectedItemAdd.id;
			}
			
			if(selectedItemDel == undefined)
			{
				if(selectedItems == undefined)
				{
            		alert("Please select a node first before deleting the same");
            		return;
				}
				else
				{
					delId = selectedItems.id;
				}
			}
			else
			{
				delId = selectedItemDel.id;
			}
			
			if(currentTreeNode == undefined)
			{
				currentTreeNode = null;
			}
			//console.log(currentTreeNode);
			
			var myTab1 = document.getElementById('attr');
			var myTab2 = document.getElementById('rules');
            var bRuleValues = new Array();
            var element;

            for (row = 1; row < myTab1.rows.length; row++) {
            	var obj = {
	            	    attrName:"",
	            	    attrValue:""
	            	};
            	
            	bRuleValues.push('Attribute');
            	
                for (c = 0; c < myTab1.rows[row].cells.length; c++) {   
                	if(c != 0){
                		if(c == 1)
                		{
                			element = myTab1.rows.item(row).cells[c];	
                			obj.attrName = element.childNodes[0].value;
                			if(obj.attrName != undefined && obj.attrName != null)
                			{
                				bRuleValues.push(obj.attrName);
                			}
                		}
                		if(c == 2)
                		{
                			element = myTab1.rows.item(row).cells[c];
                			obj.attrValue = element.childNodes[0].value;
                			if(obj.attrValue != undefined && obj.attrValue != null)
                			{		                				
                				bRuleValues.push(obj.attrValue);		                						
                			}			                			
                		}		                		          		
                   }
                }
                if(bRuleValues.length >0)
                {
                	bRuleValues.push("=");
                }
            }
            
            for (row = 1; row < myTab2.rows.length; row++) {
            	var obj = {
	            	    attrName:"",
	            	    attrValue:""
	            	};
            	
            	bRuleValues.push('Rule');
            	
                for (c = 0; c < myTab2.rows[row].cells.length; c++) {   
                	if(c != 0){
                		if(c == 1)
                		{
                			element = myTab2.rows.item(row).cells[c];	
                			obj.attrName = element.childNodes[0].value;
                			if(obj.attrName != undefined && obj.attrName != null)
                			{
                				bRuleValues.push(obj.attrName);
                			}
                		}
                		if(c == 2)
                		{
                			element = myTab2.rows.item(row).cells[c];
                			obj.attrValue = element.childNodes[0].value;
                			if(obj.attrValue != undefined && obj.attrValue != null)
                			{		                				
                				bRuleValues.push(obj.attrValue);		                						
                			}			                			
                		}		                		          		
                   }
                }
                if(bRuleValues.length >0)
                {
                	bRuleValues.push("=");
                }
            }
            
            if(del == '')
            {
            	del = 'X';
            }
            
            if(add == '')
            {
            	add = 'X';
            }
            
            if(bRuleValues == '' || bRuleValues == undefined)
            {
            	bRuleValues = null;
            }
            
//            alert(operation.operationId + '/' + selectedItems.id + '/' + bRuleValues + '/'+ del + '/' + addId + '/' +  delId + '/' + add + '/' + currentTreeNode + '/' + editId);
            
            var request = {
					method : 'POST',
					url : 'saveBuildRulesData/' + operation.operationId + '/' + selectedItems.id + '/' + bRuleValues + '/'+ del + '/' + addId + '/' +  delId + '/' + add + '/' + currentTreeNode + '/' + editId 										
			};
            $scope.showWaitDialog("Saving modified Root data element...");
            $http(request).then(function(response) {
            		$scope.updateRuleBuilder();
	            	add = 'X';
	            	del = 'X';			            	
	            	currentTreeNode = null;
	            	editId = '0';
	            	selectedItems = undefined;
	            	selectedItemAdd = undefined;
	            	selectedItemDel = undefined;
	            	$scope.hideDialog();
				}, 
				function(response) {
					alert("Failure");	
				});
		}
		
		$scope.updateRuleBuilder = function () {
			
			var request = {
					method : "GET",
					url : 'getRuleDetailsInTree' 
			};
			
			$scope.showWaitDialog("updating root Element...");
			
			$http(request).then(function(response) {
				$scope.ruleBuilders = response.data;
				data = $scope.ruleBuilders;
				$scope.hideDialog();
				$(document).ready(function () {	
					
					var source =
	                {
	                    datatype: "json",
	                    datafields: [
	                    	{ name: 'id' },
	                        { name: 'parentid' },
	                        { name: 'text' }
	                    ],
	                    id: 'id',
	                    localdata: data
	                };
	                
	                var dataAdapter = new $.jqx.dataAdapter(source);
	                dataAdapter.dataBind();
	                var records = dataAdapter.getRecordsHierarchy('id', 'parentid', 'items', [{ name: 'text', map: 'label'}]);
	                $('#jqxWidget').jqxTree({ source: records, width: '300px', keyboardNavigation: false});
				});
				
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});		
		}
		
		$scope.loadRootXml = function(selectedValue) {
		    var operation = $scope.operations[selectedValue];
		    
			var request = {
					method : 'POST',
					url : 'loadRootXmlData/' + operation.operationId
			};
			$scope.showWaitDialog("Loading Root Build File..");
				
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.showAlert("Data Updated..");	
				$scope.updateRuleBuilder();				
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});		    
		}
		
		$scope.downloadXML = function(selectedValue) {
			var operation = $scope.operations[selectedValue];
			$window.open('downloadXMLFiles/' + operation.operationName,'_self');
		}
		
		$scope.downloadXMLFile = function(section) {
			$window.open('downloadXMLFiles/' + section.operationName,'_self');
		}		
		
		$scope.downloadCSV = function(selectedValue) {
			var operation = $scope.operations[selectedValue];	
			$window.open('downloadCSVFiles/' + operation.operationName,'_self');
		}
		
		$scope.downloadWorksheet = function(selectedValue) {
			var operation = $scope.operations[selectedValue];	
			$window.open('downloadWorksheet/' + operation.operationName,'_self');
		}

		$scope.downloadCSVFile = function(section) {	
			$window.open('downloadCSVFiles/' + section.operationName,'_self');
		}
		
		$scope.setFileIndex = function(value) {
		    $scope.indexValue = value;
		}
		
		$scope.fileMapped = function (files) {
		 
		   var file = files[0];
//		   var fileName = file.name;
		   var index = this.$index;

			if(file) {
		       var formData = new FormData();
		       formData.append("mapFile", file);
			   var fileName = $scope.mapFileNames[$scope.section.index][$scope.indexValue];		// Suman - [$scope.section.index-4]
			   $scope.showWaitDialog(" Mapping File..");
			   $.ajax({
			       type: "POST",
			       enctype: 'multipart/form-data',
			       url: "saveMapFileData/" + fileName,
			       data: formData,
			       processData: false,
			       contentType: false,
			       success: function (response) {
			           $scope.hideDialog();
			       },
			       error: function (error) {
			           $scope.hideDialog();
			           console.log(error);
			           // process error
			       }
			    });
			    
			    event.preventDefault();
			} else {
				$scope.showAlert("Please choose a file to proceed !!!", "error");
			}
		}			
		
		$scope.fileNames = [];
		$scope.mapFile = {};
		$scope.mappedFiles = [];
		
		$scope.initMapFile = function(container) {
		    $scope.operationValues = {};
		    $scope.mapFile = {};
		    $scope.fileNames = [];
		    $scope.fileIndex = undefined;
		    $scope.selectedValue5 = undefined;
			$scope.populateSavedOperations();
		}
		
		$scope.listOfOperations = function(selectedValue) {
			var operation = $scope.operations[selectedValue];
			
			var request = {
					method : "GET",
					url : 'getOperationValue/' + operation.operationId
			};
			
			$scope.showWaitDialog("Getting Operation List...");
			
			$http(request).then(function(response) {
				$scope.operationValues = response.data;
				$scope.getMapFiles(operation.operationId);
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});		
		}
		
		$scope.getMapFiles = function(operationId) {
			var request = {
					method : "GET",
					url : 'getMapFiles/' + operationId
			};
			
			$scope.showWaitDialog("Loading Map Files...");
			
			$http(request).then(function(response) {
				$scope.fileNames = response.data;
				$scope.populateMapFileData();
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.getMapFileNames = function(section) {
		    $scope.fileNames = [];
		    $scope.section = section;
			var request = {
					method : "GET",
					url : 'getMapFileNames/' + section.operationName
			};
			
			$scope.showWaitDialog("Loading Map Files...");
			
			$http(request).then(function(response) {
			    createFileNamesArray(response.data, section);
				$scope.fileNames = response.data;
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		function createFileNamesArray(data, section) {
		    $scope.mapFileNames[section.index] = new Array(data.length);		// Suman - [$scope.section.index-4]
		    for(var i = 0; i < data.length; i++) {
		        $scope.mapFileNames[section.index][i] = data[i];		// Suman - [$scope.section.index-4]
		    }
		}
		
		$('#mapFileUploadForm').submit(function(event) {
			if($scope.mapFileForm.$valid && $scope.mapFileUploadForm.$valid) {
				var file = $('#mapFileId').val().trim();
//				var inputFileName = file.substring(12, file.length);
				if(file) {
				    var formData = new FormData(this);
				    var fileName = $scope.fileNames[$scope.fileIndex];
				    $scope.showWaitDialog(" Mapping File..");
				    $.ajax({
				        type: "POST",
				        enctype: 'multipart/form-data',
				        url: "saveMapFileData/" + fileName,
				        data: formData,
				        processData: false,
				        contentType: false,
				        success: function (response) {
				        	$scope.hideDialog();
				        	$scope.populateMapFileData();
				        },
				        error: function (error) {
				        	$scope.hideDialog();
				            console.log(error);
				            // process error
				        }
				    });
				    
				    event.preventDefault();
				} else {
					$scope.showAlert("Please choose a file to proceed !!!", "error");
				}
		} else {
			$scope.showAlert("Input fields can not be left empty !!!", "error");
		}
		});
		
		$scope.populateMapFileData = function() {
			
			var request = {
					method : 'GET',
					url : 'getMapFilesByOperation'
				};
				$scope.showWaitDialog("Loading details..");
				
				$http(request).then(function(response) {
					$scope.mappedFiles = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});
		}
		
		$scope.deleteMapFile = function(mapFile) {
			var request = {
					method : 'DELETE',
					url : 'deleteMapFile/' + mapFile.mapFileId
				};

			$scope.showWaitDialog("Deleting Mapped File");
			
			$http(request).then(function(response) {
				$scope.populateMapFileData();
				$scope.hideDialog();
				$scope.showAlert("File Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Failed to delete file", "error");
			});
		}
		
		$scope.downloadMappedFile = function(mapFile) {
			$window.open('downloadMappedFile/' + mapFile.mapFileId,'_self');
		}
		
        $scope.validationMessages = [];
        $scope.validationMessage = {};
        $scope.validationPercentage = 0;
        		
		$scope.initValidate = function(container) {
		    $scope.validationMessages = [];
            $scope.validationMessage = {};
			clearValidateForm();
			$scope.populateSavedOperations();
			$scope.populateTenantData();
		}
		
        function clearValidateForm() {
            $scope.validationPercentage = 0;
        	$scope.operations = [];
        	$scope.userTenants = [];
        	$scope.operationValues = {};
        	$scope.selectedTenant = {};
            $scope.selectedValue6 = undefined;
            $scope.selectedValue7 = undefined;
            $scope.tenantForm.$setPristine();
        }
        
        $scope.validateErrorData = function(selectedOperation, selectedTenant, validateDataForm) {
            $scope.validationPercentage = 0;
        
            if(validateDataForm.$valid) {
				var request = {
						method : "POST",
						url : 'validateError/' + selectedOperation.operationId + '/' + selectedTenant.id
				};
				$scope.showWaitDialog("Initializing..");
				
				$http(request).then(function(response) {
					$scope.hideDialog();
					var validateErr = response.data;
					if(!validateErr.error) {
					    $scope.validateData(selectedOperation, selectedTenant, validateDataForm);
					} else {
					    alert(validateErr.errorMsg);
					}
				}, function(response) {
				    $scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});				
            }
        }
        
		$scope.validateData = function(selectedOperation, selectedTenant, validateDataForm) {
			if(validateDataForm.$valid) {
				var request = {
						method : "POST",
						url : 'validateRequest/' + selectedOperation.operationId + '/' + selectedTenant.id
				};
				$scope.showWaitDialog("Initializing..");
				
				$http(request).then(function(response) {
					$scope.hideDialog();
					$scope.validationMessages = response.data;
					$scope.validationResponseStatus = $interval($scope.validationResponseStatusInterval, 1500);
					$scope.validationPercentageComplete = $interval($scope.validationPercentageCompleteInterval, 1500);
				    $scope.validationIsAllComplete = $interval($scope.validationIsAllCompleteInterval, 1500);
				}, function(response) {
				    $scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});
			}
		}
		
		$scope.validationPercentageCompleteInterval = function() {
		    if($scope.validationPercentage <= 95) {
		        $scope.validationPercentage = $scope.validationPercentage + 1;
		        if($scope.currentSection != undefined) {
                    $scope.validatedPercentage[$scope.currentSection.index] = $scope.validationPercentage;
                }
		    }
		}
		
		$scope.validationIsAllCompleteInterval = function() {
			var request = {
					method : "GET",
					url : 'validationIsAllComplete'
			};
			
			$http(request).then(function(response) {
				$scope.isAllValidationComplete = response.data;
				if($scope.isAllValidationComplete) {
				    $scope.validationPercentage = 100;
				    if($scope.currentSection != undefined) {
				        $scope.validatedPercentage[$scope.currentSection.index] = 100;
				    }
				    
					if (angular.isDefined($scope.validationResponseStatus)) {
						$interval.cancel($scope.validationResponseStatus);
						$scope.validationResponseStatus = undefined;
					}
					if (angular.isDefined($scope.validationPercentageComplete)) {
						$interval.cancel($scope.validationPercentageComplete);
						$scope.validationPercentageComplete = undefined;
					}					
					if (angular.isDefined($scope.validationIsAllComplete)) {
						$interval.cancel($scope.validationIsAllComplete);
						$scope.validationIsAllComplete = undefined;
					}
				}
			}, function(response) {
				console.error(response);
			});
		}
		
		$scope.validationResponseStatusInterval = function() {
			var request = {
					method : "GET",
					url : 'getvalidationResponseStatus'
			};
			
			$http(request).then(function(response) {
				$scope.validationMessages = response.data;
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.getValidationPercentage =  function() {
		    return $scope.validationPercentage;
		}
		
		$scope.getValidationPercentagePrj =  function(section) {
		    return $scope.validatedPercentage[section.index];
		}		
		
		$scope.validateMapData = function(section) {
			var request = {
					method : "POST",
					url : 'validateMapData/' + section.sectionId + '/' + $scope.selectedTenant.id
			};
			$scope.showWaitDialog("Initializing..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.validationMessages = response.data;
				$scope.validationResponseStatus = $interval($scope.validationResponseStatusInterval, 1500);
				$scope.validationPercentageComplete = $interval($scope.validationPercentageCompleteInterval, 1500);
			    $scope.validationIsAllComplete = $interval($scope.validationIsAllCompleteInterval, 1500);				
			}, function(response) {
			    $scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});		
		}
		
		$scope.validateMappedData = function(section) {
		    if($scope.selectedTenant.id == undefined) {
		        $scope.showAlert("Please select Tenant name", "error");
		        return;
		    }
		    $scope.sendToWD = false;
		    $scope.result = false;
		    $scope.viewValidation = false;
		    $scope.currentSection = section;
		    $scope.validationPercentage = 0;
		    
			var request = {
				method : "POST",
				url : 'validateMapDataRequest/' + section.sectionId + '/' + $scope.selectedTenant.id
			};
			$scope.showWaitDialog("Initializing..");
			
			$http(request).then(function(response) {
					$scope.hideDialog();
					var validateErr = response.data;
					if(!validateErr.error) {
					    $scope.validate = true;
					    $scope.validateMapData(section);
					} else {
					    alert(validateErr.errorMsg);
					}
				}, function(response) {
				    $scope.hideDialog();
					console.error(response);
					$scope.showAlert("Operation Failed", "error");
				});	
		}
		
		$scope.viewValidationMessage = function(index) {
		    $scope.viewValidation = true;
			$scope.validationMessage = $scope.validationMessages[index];
			$scope.fileName = $scope.validationMessage.fileName;
		}
		
		$scope.wsResponses = [];

		$scope.inputBatchNum = 1;
		
		$scope.initSendToWD = function(container) {
			clearPageFormData();
			$scope.clearSendToWDForm();
		    $scope.populatePageData();
			$scope.populateSavedOperations();
			$scope.populateTenantData();
		}
		
		$scope.clearSendToWDForm = function() {
		    $scope.selectedValue8 = undefined;
		    $scope.selectedValue9 = undefined;
		    $scope.operationValues = {};
		    $scope.selectedTenant = undefined;
		    $scope.batchRequestSize = "";		
		}
		
		$scope.populateOperationList = function(selectedValue) {
			var operation = $scope.operations[selectedValue];
			
			var request = {
					method : "GET",
					url : 'getOperationValue/' + operation.operationId
			};
			
			$scope.showWaitDialog("Getting Operation List...");
			
			$http(request).then(function(response) {
				$scope.operationValues = response.data;
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});	
		}
		
		$scope.sendWDRequest = function(selectedOperation, selectedTenant, container) {
		
			if($scope.selectedPage == undefined || $scope.selectedPage.pageId == undefined) {
		        $scope.showAlert("Please select Load Cycle", "error");
		        return;
		    }
			if(selectedTenant == undefined) {
		        $scope.showAlert("Please select Tenant name", "error");
		        return;
		    }
		    if(selectedOperation == undefined || selectedOperation.operationId == undefined) {
		    	$scope.showAlert("Please select Rule", "error");
		        return;
		    }
		    
			var request = {
					method : "POST",
					url : 'sendWWSRequest/' + selectedOperation.operationId + '/' + selectedTenant.id + '/' + $scope.selectedPage.pageId
			};
			$scope.showWaitDialog("Sending Data to Workday..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.wsResponses = response.data;
				if($scope.wsResponses.length > 0) {
				    $scope.container = container;
				    $scope.responseStatusCheck = $interval($scope.wwsResponseStatusInterval, 1000);
				    $scope.responsePercentageComplete = $interval($scope.responsePercentageCompleteInterval, 1000);
				    $scope.responseisAllComplete = $interval($scope.wwsIsAllCompleteInterval, 1000);
				} else {
				    $scope.showAlert("Validation error. Please run the validation before sending to Workday", "error");
				}
			}, function(response) {
			    $scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.sendToWDRequest = function(section) {
		    if($scope.selectedTenant.id == undefined) {
		        $scope.showAlert("Please select Tenant name", "error");
		        return;
		    }
		    $scope.result = false;
		    $scope.validate = false;
		    $scope.viewValidation = false;
		    $scope.currentSection = section;
			var request = {
					method : "POST",
					url : 'sendToWWSRequest/' + section.operationName + '/' + $scope.selectedPage.pageId + '/' + $scope.selectedTenant.id
			};
			$scope.showWaitDialog("Sending Data to Workday..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.sendToWD = true;
				$scope.wsResponses = response.data;
				if($scope.wsResponses.length > 0) {
					$scope.responseStatusCheck = $interval($scope.wwsResponseStatusInterval, 1000);
					$scope.responsePercentageComplete = $interval($scope.responsePercentageCompleteInterval, 1000);
					$scope.responseisAllComplete = $interval($scope.wwsIsAllCompleteInterval, 1000);
				} else {
				    alert("Validation error. Please run the validation before sending data to Workday");
				}
			}, function(response) {
			    $scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.viewBatchDetails = function(batchNum) {
			var request = {
					method : "GET",
					url : 'getWWSBatchResponse/' + batchNum
			};
			
			$http(request).then(function(response) {
				$scope.wsResponses = response.data;
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.wwsResponseStatusInterval = function() {
			var request = {
					method : "GET",
					url : 'getWWSResponseStatus'
			};
			
			$http(request).then(function(response) {
				$scope.wsResponsesStatus = response.data;
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.percentageValue = 0;
		
		$scope.responsePercentageCompleteInterval = function() {
		    var section = $scope.currentSection;
			var request = {
					method : "GET",
					url : 'getPercentageComplete'
			};
			
			$http(request).then(function(response) {
				$scope.percentageValue = response.data;
				if(section != undefined) {
                    $scope.executedPercentage[section.index] = response.data;		// Suman - [$scope.section.index-4]
                }
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.wwsIsAllCompleteInterval = function() {
			var request = {
					method : "GET",
					url : 'wwsIsAllComplete'
			};
			
			$http(request).then(function(response) {
				$scope.isAllComplete = response.data;
				if($scope.isAllComplete) {
					if (angular.isDefined($scope.responseCheck)) {
						$interval.cancel($scope.responseCheck);
						$scope.responseCheck = undefined;
					}
					if (angular.isDefined($scope.responseStatusCheck)) {
						$interval.cancel($scope.responseStatusCheck);
						$scope.responseStatusCheck = undefined;
					}
					if (angular.isDefined($scope.responsePercentageComplete)) {
						$interval.cancel($scope.responsePercentageComplete);
						$scope.responsePercentageComplete = undefined;
					}					
					if (angular.isDefined($scope.responseisAllComplete)) {
						$interval.cancel($scope.responseisAllComplete);
						$scope.responseisAllComplete = undefined;
					}
					if($scope.responseisAllComplete == undefined) {
					    $scope.publishResult();
					}
				}
			}, function(response) {
				console.error(response);
			});
		}
		
		$scope.getExecutedPercentage = function(section) {
		    return $scope.executedPercentage[section.index];		// Suman - [$scope.section.index-4]
		}
		
		$scope.publishResult = function() {
			var request = {
					method : 'POST',
					url : 'publishResult'
			};
			$scope.showWaitDialog("Publishing result..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
			}, function(response) {
			    $scope.hideDialog();
				console.error(response);
				$scope.showAlert("Publish result Failed", "error");
			});
		}
		
		$scope.getWWSXmlFiles = function() {
			$window.open('getWWSXmlFiles','_self');
		}
		
		$scope.getWWSErrorDataFiles = function() {
			$window.open('getWWSErrorDataFiles','_self');
		}
		
		$scope.getWSXmlFiles = function(result) {
			$window.open('getWSXmlFiles/' + result.resultId,'_self');
		}
		
		$scope.getWSErrorDataFiles = function(result) {
			$window.open('getWSErrorDataFiles/' + result.resultId,'_self');
		}		
		
		$scope.goBack = function(container) {
			
		}
		
		$scope.stopSWExecution = function() {

			var request = {
					method : 'GET',
					url : 'stopSWExecution'
				};

			$scope.showWaitDialog("Stopping Execution");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.showAlert("Execution Stopped");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Stopping Execution failed", "error");
			});
		}
		
		$scope.flipValue = false;
		
		$scope.flip = function() {
		    $('.card').toggleClass('flipped');
		    $scope.flipValue = !$scope.flipValue;
		}
		
		$scope.initPostLoad = function(container) {
			
//		    $scope.saveTemplateFileData();
			clearPostLoadFormData();
			$scope.populatePageData();
			$scope.populateTenantData();
			$scope.postLoad = true;
		}
		
		$('#sourceFileUploadForm').submit(function(event) {
			
			$scope.isAllMapped = true;
            $scope.isCompared = true;
            
			var loadCycle = $scope.selectedLoad;
			$scope.selectedPage = $scope.pages[loadCycle];
			loadCycle = $scope.selectedPage.pageId
			var ruleName = $scope.selectedRule;
			$scope.ruleNames = $scope.operationNames[ruleName];
			ruleName = $scope.ruleNames.operationName;
			
			var file = $('#sourceFileId').val().trim();			
			if(file) 
			{
				var formData = new FormData(this);
				var filename = file.substring(12, file.length);
			    $scope.showWaitDialog(" Executing Files..");
			    $scope.postloadIsAllComplete = $interval($scope.postloadIsAllCompleteInterval, 10000);
			    $.ajax({
			        type: "POST",
			        enctype: 'multipart/form-data',
			        url: "executeSourceFile/" + filename + '/' + loadCycle + '/' + ruleName + '/' + $scope.selectedTenant.id,
			        data: formData,
			        processData: false,
			        contentType: false,
			        success: function (response) {
			        	console.log("It is successful");        	
			        },
			        error: function (error) {
			        	$scope.hideDialog();
			            console.log(error);
			        }
			    });
			    event.preventDefault();
			} 
			else 
			{
				$scope.showAlert("Please choose a file to proceed !!!", "error");
			}
		});
		
		$scope.postloadIsAllCompleteInterval = function() {
			var request = {
					method : "GET",
					url : 'postloadIsAllComplete'
			};
			
			$http(request).then(function(response) {
				$scope.isPostLoadRunComplete = response.data;
				if($scope.isPostLoadRunComplete) {				   
					if (angular.isDefined($scope.postloadIsAllComplete)) {
						$interval.cancel($scope.postloadIsAllComplete);
						$scope.postloadIsAllComplete = undefined;
					}
					
					$scope.getMapResponse();
				}
			}, function(response) {
				console.error(response);
			});
				}
		
		$scope.getMapResponse = function() {
			var request = {
			    method : "GET",
				url : 'getMapResponse'
			};
			
			$http(request).then(function(response) {
			    $scope.hideDialog();
				$scope.isAllMapped = false;
			    $scope.isCompared = false;
			    $scope.mapFiles = response.data;
	            $scope.headerValues = $scope.mapFiles[0].headingAllWD;
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.getSupplierChanged = function(selectedRule) {
			/*if(selectedRule == 0)
			{
				$scope.isSupplier = false;
				$scope.isNoSupplier = true;
			}
			else
			{
				$scope.isSupplier = true;
				$scope.isNoSupplier = false;
			}*/
		}
		
		$scope.performSupplier = function(section) {
			
			var tenantId = $scope.selectedTenant.id;
			$window.open('performSupplierJournal/' + tenantId  + '/' + section.operationName + '/' + section.sectionId,'_self');
		}
		
		$scope.performSupplierPostLoad = function() {
			
			var tenantId = $scope.selectedTenant.id;
			$window.open('performSupplierJournal/' + tenantId  + '/' + 'Import_Supplier_Invoice' + '/' + 143,'_self');
		}
		
		$scope.performComparison = function() {
			
			var myTab = document.getElementById('columnTable');
            var colValues = new Array();
            var rowValues = new Array();
            var loadCycle;
            var ruleName;

            for (row = 1; row < myTab.rows.length; row++)
            {
            	var obj = {
	            	    colSource:"",
	            	    colWorkday:""
	            	};
                for (c = 0; c < myTab.rows[row].cells.length; c++) 
                {   
                	if(c != 0)
                	{			                		
                		if(c == 1)
                		{
                			obj.colSource = myTab.rows.item(row).cells[c].childNodes[0].data;
                		}                		
                		if(c == 2)
                		{
                			obj.colWorkday = myTab.rows.item(row).cells[c].innerText;	
                		}			                		
                   }
                }
                rowValues.push(obj.colSource + "|" + obj.colWorkday);
            }

            for (row = 1; row < myTab.rows.length; row++)
            {
            	var primaryKey;
                for (c = 0; c < myTab.rows[row].cells.length; c++) 
                { 
                	if(c == 0)
                	{
                		if(row > 1)
                		{
                			//if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
                			{
                    			if(myTab.rows.item(row).cells[c+2].innerText.length <= 0)
                    			{
                    				alert("Please Map Source Column with Workday Column!!!");
                    				return;
                    			}
                    			if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
                    			{
                    				colValues.push(myTab.rows.item(row).cells[c+2].innerText);
                    			}              			
                			}
                		}                		
                	}
            		if(c == 3)
            		{
            			if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
            			{
            				primaryKey = myTab.rows.item(row).cells[c-1].innerText;	
            			}
            		}                					                		                  
                }
            }
            
            if(primaryKey == undefined)
            {
            	alert("Please select a Primary Key!!!");
            	return;
            }
            
            if(colValues == undefined || colValues == null || colValues == "")
            {
            	alert("Please select the Fields to Validate!!!");
            	return;
            }
            
            if(!$scope.postLoad)
            {
    		    $scope.selectedPage = $scope.pages[$scope.value];
    		    loadCycle = $scope.selectedPage.pageId;
    		    $scope.sections = $scope.selectedPage.sections;	
    			ruleName = $scope.sections[$scope.projectManagementRowIndex].operationName;
            }
            else
            {
    			loadCycle = $scope.selectedLoad;
    			$scope.selectedPage = $scope.pages[loadCycle];
    			loadCycle = $scope.selectedPage.pageId
    			ruleName = $scope.selectedRule;
    			$scope.ruleNames = $scope.operationNames[ruleName];
    			ruleName = $scope.ruleNames.operationName;
            }
           
            $window.open('performComparison/' + primaryKey + '/' + colValues + '/' + loadCycle + '/' + ruleName + '/' + rowValues,'_self');
		}
		
        $scope.toggleKey = function(item) {
        	
        	angular.forEach($scope.mapFiles, function(mapFile) {
        	      if(mapFile != item) {
        	    	  mapFile.isChecked = false;  
        	      }
        	    });			        	
        };
        
        $scope.checkUncheckHeader = function () {
            $scope.IsAllChecked = true;
            for (var i = 0; i < $scope.mapFiles.length; i++) 
            {
                if (!$scope.mapFiles[i].isSelect) 
                {
                    $scope.IsAllChecked = false;
                    break;
                }
            };
        };
        
        $scope.checkUncheckAll = function () {
            for (var i = 0; i < $scope.mapFiles.length; i++) 
            {
                $scope.mapFiles[i].isSelect = $scope.IsAllChecked;
            }
        };
        
        $scope.checkUncheckAllModal = function (IsAllChecked) {
            for (var i = 0; i < $scope.mapFiles.length; i++) 
            {
            	$scope.mapFiles[i].isSelect = IsAllChecked;
            }
        };
        
		$scope.getRuleNameList = function(selectedLoad) {
			
			$scope.selectedPage = $scope.pages[selectedLoad];
			var request = {
				method : 'POST',
				url : 'getRuleNameList/' + $scope.selectedPage.pageId
			};
			$scope.showWaitDialog("Please Wait..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.operationNames = response.data;
				$scope.showAlert("Data updated..");					
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
			});
	     }
		
		$scope.getRuleNameListTenantBased = function() {
			
			var request = {
				method : 'POST',
				url : 'getRuleNameListTenantBased'
			};
			$scope.showWaitDialog("Please Wait..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.operationNames = response.data;
				$scope.showAlert("Data updated..");					
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
			});
	     }
		
		function clearPostLoadFormData() {
			$scope.pages = [];
			$scope.value = undefined;
		    $scope.selectedPage = undefined;
		    $scope.sections = [];
		    $scope.operationNames = [];
		    $scope.userTenants = [];
		    $scope.isAllMapped = true;
            $scope.isCompared = true;
            $scope.selectedLoad = undefined;
            $scope.selectedRule = undefined;
            $scope.selectedValue9 = undefined;
            $('#sourceFileId').val(null); 
		}
		
		$scope.fileChanged = function (files, index) {
			
	        if (files != null) 
	        {
	            var file = files[0];
	            var fileName = file.name;
	            var index = this.$index;
	            $scope.projectManagementRowIndex = index;
			    $scope.selectedPage = $scope.pages[$scope.value];
			    var loadCycle = $scope.selectedPage.pageId;
			    $scope.sections = $scope.selectedPage.sections;	
				var operationName = $scope.sections[index].operationName;
				var execute = $scope.sections[index].execute;
				var status = $scope.sections[index].status;
	            var formData = new FormData(); 
			    formData.append("myFile", file);
	            if(execute)		//if(execute == true && status == 1)
	            {
	            	$scope.showWaitDialog(" Executing Files..");
				    $.ajax({
				        type: "POST",
				        enctype: 'multipart/form-data',
				        url: "executeFilePostLoad/" + operationName + '/' + loadCycle + '/' + fileName,
				        data: formData,
				        processData: false,
				        contentType: false,
				        success: function (response) {
				        	$scope.hideDialog();
							$scope.isAllMapped = false;
				            $scope.isCompared = false;
				        	$scope.mapFiles = response;
				        	$scope.headerValues = $scope.mapFiles[0].headingAllWD;
						    var modalinstance = $uibModal.open({
							      scope: $scope,
							      templateUrl: '../jsp/modalContent.jsp',
							      windowClass: 'large-Modal',
							      backdrop: 'static',
							      resolve: {
							    	  mapFiles: function() {
							          return $scope.mapFiles;
							        },
							        headerValues: function() {
							          return $scope.headerValues;
							        }
							      }
							    })
				        },
				        error: function (error) {
				        	$scope.hideDialog();
				            console.log(error);
				        }
				    });
	             }
	            else
	            {
	            	$scope.showAlert("The operation is either not executed or not completed !!!")
	            }	            
	        }
	        else 
			{
				$scope.showAlert("Please choose a file to proceed !!!");
			}
		};
		
		$scope.exportErrorData = function() {
			var fileName = $scope.fileName;
			$window.open('exportErrorData/' + fileName,'_self');
		}
		
		$scope.performPostLoad = function(section, index) {

			if(section.operationName == "Import_Supplier_Invoice")
			{
				$scope.performSupplier(section);
			}
			else
			{				
				$scope.projectManagementRowIndex = index;
				$scope.showWaitDialog(" Executing Files..");
			    $.ajax({
			        type: "POST",
			        url: "performPostLoad/" + section.operationName + '/' + $scope.selectedPage.pageId + '/' + $scope.selectedTenant.id + '/' + section.sectionId,
			        processData: false,
			        contentType: false,
			        success: function (response) {
			        	$scope.hideDialog();
						$scope.isAllMapped = false;
			            $scope.isCompared = false;
			        	$scope.mapFiles = response;
			        	if($scope.mapFiles[0].errorMsg != undefined)
			        	{
				        	if($scope.mapFiles[0].errorMsg.length >0)
				        	{
				        		alert($scope.mapFiles[0].errorMsg);
				        	}
			        	}
			        	else
			        	{
			        		$scope.headerValues = $scope.mapFiles[0].headingAllWD;
						    var modalinstance = $uibModal.open({
							      scope: $scope,
							      templateUrl: '../jsp/modalContent.jsp',
							      windowClass: 'large-Modal',
							      backdrop: 'static',
							      resolve: {
							    	  mapFiles: function() {
							          return $scope.mapFiles;
							        },
							        headerValues: function() {
							          return $scope.headerValues;
							        }
							      }
							    })
			        	}
			        	
			        },
			        error: function (error) {
			        	$scope.hideDialog();
			            console.log(error);
			        }
			    });
			}
		}
		
		$scope.getRadioVal = function(status) {

			$scope.isAllMapped = true;
            $scope.isCompared = true;
            $scope.selectedLoad = undefined;
            $scope.selectedRule = undefined;
            $scope.selectedValue9 = undefined;
            
            if(status === "File Based")
            {
    			$scope.isFileBased = false;
    			$scope.isTenantBased = true;
            }
            else
            {
    			$scope.isFileBased = true;
    			$scope.isTenantBased = false;
            }			
		}
		
		/*$scope.performTenantBasedPostLoad = function() {
			
			var loadCycle = $scope.selectedLoad;
			$scope.selectedPage = $scope.pages[loadCycle];
			loadCycle = $scope.selectedPage.pageId
			var ruleName = $scope.selectedRule;
			$scope.ruleNames = $scope.operationNames[ruleName];
			ruleName = $scope.ruleNames.operationName;
			
			var request = {
					method : "POST",
					url : 'performTenantBasedPostLoad/' + loadCycle + '/' + ruleName + '/' + $scope.trmFrom + '/' + $scope.trmTo + '/' + $scope.hireFrom + '/' + $scope.hireTo + '/' + $scope.selectedTenant.id
			};
			$scope.postloadIsAllCompleteTenant = $interval($scope.postloadIsAllCompleteIntervalTenant, 5000);
			$scope.showWaitDialog("Extracting Data..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.isAllMapped = false;
	            $scope.isCompared = false;
	        	$scope.mapFiles = response.data;
	        	$scope.headerValues = $scope.mapFiles[0].headingAllWD;
				console.log("It is successful");  
	        	
			}, function(response) {
				console.error(response);
			});
			
			//$window.open('performTenantBasedPostLoad/' + loadCycle + '/' + ruleName + '/' + $scope.trmFrom + '/' + $scope.trmTo + '/' + $scope.hireFrom + '/' + $scope.hireTo + '/' + $scope.selectedTenant.id,'_self');
		}*/
		
		$('#wd2wdFileUploadForm').submit(function(event) {
			
			$scope.isAllMapped = true;
            $scope.isCompared = true;
            
			var loadCycle = $scope.selectedLoad;
			$scope.selectedPage = $scope.pages[loadCycle];
			loadCycle = $scope.selectedPage.pageId
			var ruleName = $scope.selectedRule;
			$scope.ruleNames = $scope.operationNames[ruleName];
			ruleName = $scope.ruleNames.operationName;
			
			var file = $('#wdFileId').val().trim();			
			if(file) 
			{
				var formData = new FormData(this);
				var filename = file.substring(12, file.length);
			    $scope.showWaitDialog(" Executing Files..");
			    $scope.postloadIsAllCompleteTenant = $interval($scope.postloadIsAllCompleteIntervalTenant, 10000);
			    $.ajax({
			        type: "POST",
			        enctype: 'multipart/form-data',
			        url: "performTenantBasedPostLoad/" + filename + '/' + loadCycle + '/' + ruleName + '/' + $scope.selectedTenant.id,
			        data: formData,
			        processData: false,
			        contentType: false,
			        success: function (response) {
			        	console.log("It is successful");        	
			        },
			        error: function (error) {
			        	$scope.hideDialog();
			            console.log(error);
			        }
			    });
			    event.preventDefault();
			} 
			else 
			{
				$scope.showAlert("Please choose a file to proceed !!!", "error");
			}
		});
		
		$scope.postloadIsAllCompleteIntervalTenant = function() {
			var request = {
					method : "GET",
					url : 'postloadIsAllCompleteTenant'
			};
			
			$http(request).then(function(response) {
				$scope.isPostLoadRunComplete = response.data;
				if($scope.isPostLoadRunComplete) {				   
					if (angular.isDefined($scope.postloadIsAllCompleteTenant)) {
						$interval.cancel($scope.postloadIsAllCompleteTenant);
						$scope.postloadIsAllCompleteTenant = undefined;
					}
					
					$scope.getMapResponseTenant();
				}
			}, function(response) {
				console.error(response);
			});
				}
		
		$scope.getMapResponseTenant = function() {
			var request = {
			    method : "GET",
				url : 'getMapResponseTenant'
			};
			
			$http(request).then(function(response) {
			    $scope.hideDialog();
				$scope.isAllMapped = false;
			    $scope.isCompared = false;
			    $scope.mapFiles = response.data;
	            $scope.headerValues = $scope.mapFiles[0].headingAllWD;
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.performComparisonTenantBased = function() {
			
			var myTab = document.getElementById('columnTable');
            var colValues = new Array();
            var rowValues = new Array();
            var loadCycle;
            var ruleName;

            /*for (row = 1; row < myTab.rows.length; row++)
            {
            	var obj = {
	            	    colSource:"",
	            	    colWorkday:""
	            	};
                for (c = 0; c < myTab.rows[row].cells.length; c++) 
                {   
                	if(c != 0)
                	{			                		
                		if(c == 1)
                		{
                			obj.colSource = myTab.rows.item(row).cells[c].childNodes[0].data;
                		}                		
                		if(c == 2)
                		{
                			obj.colWorkday = myTab.rows.item(row).cells[c].innerText;	
                		}			                		
                   }
                }
                rowValues.push(obj.colSource + "|" + obj.colWorkday);
            }*/

            for (row = 1; row < myTab.rows.length; row++)
            {
            	var primaryKey;
                for (c = 0; c < myTab.rows[row].cells.length; c++) 
                { 
                	if(c == 0)
                	{
                		if(row > 1)
                		{
                			//if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
                			{
                    			if(myTab.rows.item(row).cells[c+2].innerText.length <= 0)
                    			{
                    				alert("Please Map Source Column with Workday Column!!!");
                    				return;
                    			}
                    			if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
                    			{
                    				colValues.push(myTab.rows.item(row).cells[c+2].innerText);
                    			}              			
                			}
                		}                		
                	}
            		if(c == 3)
            		{
            			if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
            			{
            				primaryKey = myTab.rows.item(row).cells[c-1].innerText;	
            			}
            		}                					                		                  
                }
            }
            
            if(primaryKey == undefined)
            {
            	alert("Please select a Primary Key!!!");
            	return;
            }
            
            if(colValues == undefined || colValues == null || colValues == "")
            {
            	alert("Please select the Fields to Validate!!!");
            	return;
            }
            
            if(!$scope.postLoad)
            {
    		    $scope.selectedPage = $scope.pages[$scope.value];
    		    loadCycle = $scope.selectedPage.pageId;
    		    $scope.sections = $scope.selectedPage.sections;	
    			ruleName = $scope.sections[$scope.projectManagementRowIndex].operationName;
            }
            else
            {
    			loadCycle = $scope.selectedLoad;
    			$scope.selectedPage = $scope.pages[loadCycle];
    			loadCycle = $scope.selectedPage.pageId
    			ruleName = $scope.selectedRule;
    			$scope.ruleNames = $scope.operationNames[ruleName];
    			ruleName = $scope.ruleNames.operationName;
            }
           
            $window.open('performComparisonTenantBased/' + primaryKey + '/' + colValues + '/' + loadCycle + '/' + ruleName,'_self');//+ '/' + rowValues
		}
		
		$('#srcFileUploadFormGF').submit(function(event) {
			
			$scope.isAllMapped = true;
            $scope.isCompared = true;
            
			var loadCycle = $scope.selectedLoad;
			$scope.selectedPage = $scope.pages[loadCycle];
			loadCycle = $scope.selectedPage.pageId
			var ruleName = $scope.selectedRule;
			$scope.ruleNames = $scope.operationNames[ruleName];
			ruleName = $scope.ruleNames.operationName;
			
			var file = $('#srcFileIdGF').val().trim();			
			if(file) 
			{
				var formData = new FormData(this);
				var filename = file.substring(12, file.length);
			    $scope.showWaitDialog(" Executing Files..");
			    $scope.postloadIsAllCompleteGF = $interval($scope.postloadIsAllCompleteIntervalGF, 5000);
			    $.ajax({
			        type: "POST",
			        enctype: 'multipart/form-data',
			        url: "executeSourceFileGF/" + filename + '/' + loadCycle + '/' + ruleName + '/' + $scope.selectedTenant.id,
			        data: formData,
			        processData: false,
			        contentType: false,
			        success: function (response) {
			        	console.log("It is successful"); 
			        	//$scope.showAlert("File Saved..");	
			        },
			        error: function (error) {
			        	$scope.hideDialog();
			            console.log(error);
			        }
			    });
			    event.preventDefault();
			} 
			else 
			{
				$scope.showAlert("Please choose a file to proceed !!!", "error");
			}
		});
		
		$scope.postloadIsAllCompleteIntervalGF = function() {
			var request = {
					method : "GET",
					url : 'postloadIsAllCompleteGF'
			};
			
			$http(request).then(function(response) {
				$scope.isPostLoadRunComplete = response.data;
				if($scope.isPostLoadRunComplete) {				   
					if (angular.isDefined($scope.postloadIsAllCompleteGF)) {
						$interval.cancel($scope.postloadIsAllCompleteGF);
						$scope.postloadIsAllCompleteGF = undefined;
					}
					
					$scope.getMapResponseGF();
				}
			}, function(response) {
				console.error(response);
			});
				}
		
		$scope.getMapResponseGF = function() {
			var request = {
			    method : "GET",
				url : 'getMapResponseGF'
			};
			
			$http(request).then(function(response) {
			    $scope.hideDialog();
				$scope.isAllMapped = false;
			    $scope.isCompared = false;
			    $scope.mapFiles = response.data;
	            $scope.headerValues = $scope.mapFiles[0].headingAllWD;
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.getRuleNameListGF = function(selectedLoad) {
			
			$scope.selectedPage = $scope.pages[selectedLoad];
			var request = {
				method : 'POST',
				url : 'getRuleNameListGF/' + $scope.selectedPage.pageId
			};
			$scope.showWaitDialog("Please Wait..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.operationNames = response.data;
				$scope.showAlert("Data updated..");					
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
			});
	     }
		
		$scope.performComparisonGF = function() {
			
			var myTab = document.getElementById('columnTable');
            var colValues = new Array();
            var rowValues = new Array();
            var loadCycle;
            var ruleName;

            for (row = 1; row < myTab.rows.length; row++)
            {
            	var obj = {
	            	    colSource:"",
	            	    colWorkday:""
	            	};
                for (c = 0; c < myTab.rows[row].cells.length; c++) 
                {   
                	if(c != 0)
                	{			                		
                		if(c == 1)
                		{
                			obj.colSource = myTab.rows.item(row).cells[c].childNodes[0].data;
                		}                		
                		if(c == 2)
                		{
                			obj.colWorkday = myTab.rows.item(row).cells[c].innerText;	
                		}			                		
                   }
                }
                rowValues.push(obj.colSource + "|" + obj.colWorkday);
            }

            for (row = 1; row < myTab.rows.length; row++)
            {
            	var primaryKey;
                for (c = 0; c < myTab.rows[row].cells.length; c++) 
                { 
                	if(c == 0)
                	{
                		if(row > 1)
                		{
                			//if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
                			{
                    			if(myTab.rows.item(row).cells[c+2].innerText.length <= 0)
                    			{
                    				alert("Please Map Source Column with Workday Column!!!");
                    				return;
                    			}
                    			if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
                    			{
                    				colValues.push(myTab.rows.item(row).cells[c+2].innerText);
                    			}              			
                			}
                		}                		
                	}
            		if(c == 3)
            		{
            			if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
            			{
            				primaryKey = myTab.rows.item(row).cells[c-1].innerText;	
            			}
            		}                					                		                  
                }
            }
            
            if(primaryKey == undefined)
            {
            	alert("Please select a Primary Key!!!");
            	return;
            }
            
            if(colValues == undefined || colValues == null || colValues == "")
            {
            	alert("Please select the Fields to Validate!!!");
            	return;
            }
            
            if(!$scope.postLoad)
            {
    		    $scope.selectedPage = $scope.pages[$scope.value];
    		    loadCycle = $scope.selectedPage.pageId;
    		    $scope.sections = $scope.selectedPage.sections;	
    			ruleName = $scope.sections[$scope.projectManagementRowIndex].operationName;
            }
            else
            {
    			loadCycle = $scope.selectedLoad;
    			$scope.selectedPage = $scope.pages[loadCycle];
    			loadCycle = $scope.selectedPage.pageId
    			ruleName = $scope.selectedRule;
    			$scope.ruleNames = $scope.operationNames[ruleName];
    			ruleName = $scope.ruleNames.operationName;
            }
           
            $window.open('performComparisonGF/' + primaryKey + '/' + colValues + '/' + loadCycle + '/' + ruleName + '/' + rowValues,'_self');
		}
		
		$('#srcFileKyndryl').submit(function(event) {
			
			$scope.isAllMapped = true;
            $scope.isCompared = true;
            
			var loadCycle = $scope.selectedLoad;
			$scope.selectedPage = $scope.pages[loadCycle];
			loadCycle = $scope.selectedPage.pageId
			var ruleName = $scope.selectedRule;
			$scope.ruleNames = $scope.operationNames[ruleName];
			ruleName = $scope.ruleNames.operationName;
			
			var file = $('#srcFileIdKyndryl').val().trim();			
			if(file) 
			{
				var formData = new FormData(this);
				var filename = file.substring(12, file.length);
			    $scope.showWaitDialog(" Executing Files..");
			    $scope.postloadIsAllCompleteTenantKD = $interval($scope.postloadIsAllCompleteIntervalTenantKD, 10000);
			    $.ajax({
			        type: "POST",
			        enctype: 'multipart/form-data',
			        url: "performPostLoadKyndryl/" + filename + '/' + loadCycle + '/' + ruleName + '/' + $scope.selectedTenant.id + '/' + $scope.selectedTenant1.id,
			        data: formData,
			        processData: false,
			        contentType: false,
			        success: function (response) {
			        	console.log("It is successful");        	
			        },
			        error: function (error) {
			        	$scope.hideDialog();
			            console.log(error);
			        }
			    });
			    event.preventDefault();
			} 
			else 
			{
				$scope.showAlert("Please choose a file to proceed !!!", "error");
			}
		});
		
		$scope.postloadIsAllCompleteIntervalTenantKD = function() {
			var request = {
					method : "GET",
					url : 'postloadIsAllCompleteTenantKD'
			};
			
			$http(request).then(function(response) {
				$scope.isPostLoadRunComplete = response.data;
				if($scope.isPostLoadRunComplete) {				   
					if (angular.isDefined($scope.postloadIsAllCompleteTenantKD)) {
						$interval.cancel($scope.postloadIsAllCompleteTenantKD);
						$scope.postloadIsAllCompleteTenantKD = undefined;
					}
					
					$scope.getMapResponseTenantKD();
				}
			}, function(response) {
				console.error(response);
			});
				}
		
		$scope.getMapResponseTenantKD = function() {
			var request = {
			    method : "GET",
				url : 'getMapResponseTenantKD'
			};
			
			$http(request).then(function(response) {
			    $scope.hideDialog();
				$scope.isAllMapped = false;
			    $scope.isCompared = false;
			    $scope.mapFiles = response.data;
	            $scope.headerValues = $scope.mapFiles[0].headingAllWD;
			}, function(response) {
				console.error(response);
				$scope.showAlert("Operation Failed", "error");
			});
		}
		
		$scope.getRuleNameListKD = function(selectedLoad) {
			
			$scope.selectedPage = $scope.pages[selectedLoad];
			var request = {
				method : 'POST',
				url : 'getRuleNameListKD/' + $scope.selectedPage.pageId
			};
			$scope.showWaitDialog("Please Wait..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.operationNames = response.data;
				$scope.showAlert("Data updated..");					
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
			});
	     }
		
		$scope.performComparisonTenantBasedKD = function() {
			
			var myTab = document.getElementById('columnTable');
            var colValues = new Array();
            var rowValues = new Array();
            var loadCycle;
            var ruleName;

            for (row = 1; row < myTab.rows.length; row++)
            {
            	var primaryKey;
                for (c = 0; c < myTab.rows[row].cells.length; c++) 
                { 
                	if(c == 0)
                	{
                		if(row > 1)
                		{
                			//if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
                			{
                    			if(myTab.rows.item(row).cells[c+2].innerText.length <= 0)
                    			{
                    				alert("Please Map Source Column with Workday Column!!!");
                    				return;
                    			}
                    			if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
                    			{
                    				colValues.push(myTab.rows.item(row).cells[c+2].innerText);
                    			}              			
                			}
                		}                		
                	}
            		if(c == 3)
            		{
            			if(myTab.rows.item(row).cells[c].childNodes[0].checked == true)
            			{
            				primaryKey = myTab.rows.item(row).cells[c-1].innerText;	
            			}
            		}                					                		                  
                }
            }
            
            if(primaryKey == undefined)
            {
            	alert("Please select a Primary Key!!!");
            	return;
            }
            
            if(colValues == undefined || colValues == null || colValues == "")
            {
            	alert("Please select the Fields to Validate!!!");
            	return;
            }
            
            if(!$scope.postLoad)
            {
    		    $scope.selectedPage = $scope.pages[$scope.value];
    		    loadCycle = $scope.selectedPage.pageId;
    		    $scope.sections = $scope.selectedPage.sections;	
    			ruleName = $scope.sections[$scope.projectManagementRowIndex].operationName;
            }
            else
            {
    			loadCycle = $scope.selectedLoad;
    			$scope.selectedPage = $scope.pages[loadCycle];
    			loadCycle = $scope.selectedPage.pageId
    			ruleName = $scope.selectedRule;
    			$scope.ruleNames = $scope.operationNames[ruleName];
    			ruleName = $scope.ruleNames.operationName;
            }
           
            $window.open('performComparisonTenantBasedKD/' + primaryKey + '/' + colValues + '/' + loadCycle + '/' + ruleName,'_self');//+ '/' + rowValues
		}
		
	} ]);