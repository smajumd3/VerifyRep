var myApp = angular.module('superUser.app', [ 'ngAnimate', 'ngMaterial',
		'ngMessages', 'ngSanitize', 'ui.bootstrap', 'chart.js', 'ui.knob' ]);
		
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
		
myApp.controller('superUser.controller', [
	'$scope',
	'$http',
	'$interval',
	'$window',
	'$mdDialog',
	'$mdToast',
	'$uibModal',
	function($scope, $http, $interval, $window, $mdDialog, $mdToast, $uibModal) {
	
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
		
		$scope.dataCenters = ["Atlanta", "Dublin", "Portland", "Atlanta(Production)", "Dublin(Production)", "Portland(Production)"];
		
	      $scope.clientLastDate = new Date();
	
	    $scope.init = function () { 
          $scope.populateClientData();
	    }

		$scope.superUsers = [];
		$scope.superUser = {};
		
		function successSuperUserResponse() {
        	$scope.populateSuperUserData();
        	clearSuperUserFormData();
        }
			
		$scope.initSuperUser = function(container) {
			successSuperUserResponse();
		}
		
		$scope.populateSuperUserData = function() {
			
			var request = {
					method : 'GET',
					url : 'getAllSuperUsers'
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.superUsers = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		//Clear the form
        function clearSuperUserFormData() {
            $scope.superUser.id = -1;
            $scope.superUser.name = "";
            $scope.superUser.email = "";
            $scope.superUser.password = "";
            $scope.superUserForm.$setPristine();
        }
        
		$scope.submitSuperUserForm = function() {
			
			if($scope.superUserForm.$valid) {
                $scope.showWaitDialog("Creating SuperUser");
     
                $http({
                    method : 'POST',
                    url : 'addSuperUser',
                    data : angular.toJson($scope.superUser),
                    headers : {
                        'Content-Type' : 'application/json'
                    }
                }).then(function(response) {
                	successSuperUserResponse();
					$scope.hideDialog();
					$scope.showAlert("SuperUser Created");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Failed to create", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}        
        
		$scope.clients = [];
		$scope.client = {};
		$scope.selectedClient = {};
		
		function successClientResponse() {
        	$scope.populateClientData();
        	clearClientFormData();
        }
        
		$scope.initClientList = function(container) {
            successClientResponse();
        }
        
		$scope.populateClientData = function() {
			
			var request = {
					method : 'GET',
					url : 'getAllClients'
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.clients = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		//Clear the form
        function clearClientFormData() {
            $scope.client.clietnId = -1;
            $scope.client.clientName = "";
            $scope.client.clientExpirationDate = undefined;
            $scope.clientForm.$setPristine();
        }
        
		$scope.deleteClient = function(client) {
			var request = {
					method : 'DELETE',
					url : 'deleteClient/' + client.clietnId
				};

			$scope.showWaitDialog("Deleting Client");
			
			$http(request).then(function(response) {
				successClientResponse();
				$scope.hideDialog();
				$scope.showAlert("Client Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Failed to delete", "error");
			});
		}

		$scope.resetClientForm = function() {
			clearClientFormData();			
		}		             

		$scope.submitClientForm = function() {
		
		    var date = new Date($scope.client.clientExpirationDate).getTime();
		    $scope.client.clientExpirationDate = date;
			
			if($scope.clientForm.$valid) {
                $scope.showWaitDialog("Creating Client");
     
                $http({
                    method : 'POST',
                    url : 'addClient/'+ date,
                    data : angular.toJson($scope.client),
                    headers : {
                        'Content-Type' : 'application/json'
                    }
                }).then(function(response) {
                	successClientResponse();
					$scope.hideDialog();
					$scope.showAlert("Client Created");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Failed to create", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}
		
		$scope.getSelectedClient = function(selectedValue) {
			$scope.selectedClient = $scope.clients[selectedValue];	
		}
		
		$scope.admins = [];
		$scope.admin = {};
		
		$scope.initAdminUser = function(container) {
            $scope.resetAdminForm();
		}
		
		$scope.getAdminsByClient = function(selectedValue) {
		    var clientName = $scope.clients[selectedValue].clientName;
		    
			var request = {
					method : 'GET',
					url : 'getAdminsByClient/' + clientName
				};

			$scope.showWaitDialog("Populating Admin List");
			
			$http(request).then(function(response) {
				$scope.admins = response.data;
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation failed", "error");
			});		    
		}		
		
		$scope.submitAdminForm = function(selectedClient) {
		
		    $scope.admin.client = $scope.selectedClient.clientName;
			
			if($scope.adminForm.$valid) {
                $scope.showWaitDialog("Creating Admin");
     
                $http({
                    method : 'POST',
                    url : 'addAdminUser',
                    data : angular.toJson($scope.admin),
                    headers : {
                        'Content-Type' : 'application/json'
                    }
                }).then(function(response) {
					$scope.hideDialog();
					$scope.resetAdminForm();
					$scope.showAlert("Admin Created");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Failed to create", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}
		
		$scope.resetAdminForm = function() {
			$scope.admin.id = -1;
			$scope.admin.userName = "";
			$scope.admin.userEmail = "";
			$scope.admin.userPassword = "";
			$scope.admin.client = "";
			$scope.selectedValue1 = undefined;
			$scope.selectedValue2 = undefined;
		}
		
		$scope.toggleAdminAccess = function(admin, checked) {
		   
		   $scope.showWaitDialog("Processing..");
     
               $http({
                   method : 'PUT',
                   url : 'updateAdminAccess/' + admin.id + '/' +  checked
               }).then(function(response) {
			       $scope.hideDialog();
               }, function(response) {
				   $scope.hideDialog();
				   console.error(response);
				   $scope.showAlert("Operation Failed", "error");
			   })
		}
		
		$scope.users = [];
		$scope.user = {};
		
		$scope.initUserAccess = function(container) {
		    $scope.selectedValue3 = undefined;
		}		
		
		$scope.getUsersByClient = function(selectedValue) {
		    var clientName = $scope.clients[selectedValue].clientName;
		    
			var request = {
					method : 'GET',
					url : 'getUsersByClient/' + clientName
				};

			$scope.showWaitDialog("Populating User List");
			
			$http(request).then(function(response) {
				$scope.users = response.data;
				$scope.hideDialog();
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Operation failed", "error");
			});		    
		}
		
		$scope.toggleUserAccess = function(user, checked) {
		   
		   $scope.showWaitDialog("Processing..");
     
               $http({
                   method : 'PUT',
                   url : 'updateUserAccess/' + user.id + '/' +  checked
               }).then(function(response) {
			       $scope.hideDialog();
               }, function(response) {
				   $scope.hideDialog();
				   console.error(response);
				   $scope.showAlert("Operation Failed", "error");
			   })
		}
		
		$scope.getDate = function(user) {
		    var date = new Date(user.createTime);
		    return date.toDateString();
		}
		
		$scope.getClientExpiration = function(client) {
			var date = new Date(client.clientExpirationDate);
		    return date.toDateString();
		}
		
		$scope.tenants = [];
		$scope.tenantEditable = {};
		$scope.selectedTenant = {};
		
		
		$scope.initTenantDetails = function(container) {
		    successTenantResponse();
		}
		
        function successTenantResponse() {
        	clearTenantFormData();
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
			});
		}
		
		$scope.getTenantsByClient = function(selectedValue) {
			var clientName = $scope.clients[selectedValue].clientName;
			var request = {
					method : 'GET',
					url : 'getAllTenantsByClient/' + clientName
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.tenants = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
        function clearTenantFormData() {
            $scope.tenantEditable.id = -1;
            $scope.tenantEditable.tenantName = "";
            $scope.tenantEditable.tenantDataCenter = "";
            $scope.tenantEditable.tenantUrl = "";
            $scope.tenantEditable.tenantUser = "";
            $scope.tenantEditable.tenantUserPassword = "";
            $scope.tenantEditable.client = "";
            $scope.tenantEditable.userId = -1;
            $scope.selectedValue4 = undefined;
            $scope.selectedValue5 = undefined;
            $scope.selectedValue6 = undefined;
        }
        
		$scope.deleteTenant = function(tenant) {
			var request = {
					method : 'DELETE',
					url : 'deleteTenant/' + tenant.id
				};

			$scope.showWaitDialog("Deleting Tenant");
			
			$http(request).then(function(response) {
				successTenantResponse();
				$scope.getTenantsByClient($scope.selectedValue6);
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
            $scope.tenantEditable.client = tenant.client;
            $scope.tenantEditable.userId = tenant.userId;            
		}
		
		$scope.resetTenantForm = function() {
			clearTenantFormData();			
		}
		
		$scope.submitTenantForm = function() {
		
		    $scope.tenantEditable.client = $scope.clients[$scope.selectedValue5].clientName;
			
			if($scope.tenantForm.$valid) {
                var method = "";
                var url = "";
                if ($scope.tenantEditable.id == -1) {
                    //Id is absent in form data, it creates new tenant operation
                    method = "POST";
                    url = 'createTenant';
                } else {
                    //Id is present in form data, it edits tenant operation
                    method = "PUT";
                    url = 'updateTenant';
                }
                
                $scope.showWaitDialog("Creating Tenant");
     
                $http({
                    method : method,
                    url : url,
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
			$scope.selectedTenant = $scope.tenants[selectedValue];	
		}
		
		$scope.fileEditable = {};
		$scope.fileList = [];
		
		$scope.initUploadFile = function(container) {
		    $scope.fileEditable.fileId = -1;
		    $scope.fileEditable.fileName = "";
		    $scope.selectedFile = undefined;
		    $scope.fileIndex = undefined;
		    $scope.fileList = [];
		    $scope.populateSavedFiles();
		}
		
		$scope.submitOperationDirForm = function() {
		    var file = $scope.fileList[$scope.fileIndex];
		    
			var request = {
					method : 'POST',
					url : 'submitOperationDirForm/' + file.fileId
				};
				$scope.showWaitDialog("Please Wait..");
				
				$http(request).then(function(response) {
					$scope.hideDialog();
					$scope.showAlert("Data uploaded..");					
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});		    
		}
		
		$('#fileUploadForm').submit(function(event) {
			if($scope.fileUploadForm.$valid) {
				var file = $('#selectedFileId').val().trim();
				if(file) {
				    var formData = new FormData(this);
				    $scope.showWaitDialog(" Processing..");
				    $.ajax({
				        type: "POST",
				        enctype: 'multipart/form-data',
				        url: "saveBuildRuleFile/" + $scope.fileEditable.fileName,
				        data: formData,
				        processData: false,
				        contentType: false,
				        success: function (response) {
				        	$scope.hideDialog();
				        	$scope.populateSavedFiles();
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
		
		$scope.populateSavedFiles = function() {
			
			var request = {
					method : 'GET',
					url : 'getBuildRuleFilesByClient'
				};
				$scope.showWaitDialog("Loading..");
				
				$http(request).then(function(response) {
					$scope.fileList = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		$scope.deleteSavedFile = function(file) {
			var request = {
					method : 'DELETE',
					url : 'deleteBuildRuleFile/' + file.fileId
				};

			$scope.showWaitDialog("Deleting..");
			
			$http(request).then(function(response) {
				$scope.populateSavedFiles();
				$scope.hideDialog();
				$scope.showAlert("File Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Failed to delete file", "error");
			});
		}
		
		$scope.downloadSavedFile = function(file) {
			$window.open('downloadBuildRuleFile/' + file.fileId,'_self');
		}		
	}
])