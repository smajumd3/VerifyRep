var myApp = angular.module('adminUser.app', [ 'ngAnimate', 'ngMaterial',
		'ngMessages', 'ngSanitize', 'ui.bootstrap', 'chart.js', 'ui.knob' ]);
		
myApp.controller('adminUser.controller', [
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

		$scope.hideDialog = function() {
			$mdDialog.hide();
		}
	
	    $scope.init = function () { 
	    
	    }

		$scope.members = [];
		$scope.member = {};
		
		function successMemberResponse() {
        	$scope.populateMemberData();
        	clearMemberFormData();
        }
			
		$scope.initProjectMember = function(container) {
			successMemberResponse();
		}
		
		$scope.populateMemberData = function() {
			
			var request = {
					method : 'GET',
					url : 'getProjectMembers'
				};
				$scope.showWaitDialog("Populating Member List");
				
				$http(request).then(function(response) {
					$scope.members = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		//Clear the form
        function clearMemberFormData() {
            $scope.member.id = -1;
            $scope.member.userName = "";
            $scope.member.userEmail = "";
            $scope.member.userPassword = "";
            $scope.memberForm.$setPristine();
        }
        
		$scope.submitMemberForm = function() {
			
			if($scope.memberForm.$valid) {
                $scope.showWaitDialog("Creating Member");
                
                var method = "";
                var url = "";
                if ($scope.member.id == -1) {
                    method = "POST";
                    url = 'addProjectMember';
                } else {
                    method = "PUT";
                    url = 'updateProjectMember';
                }                
     
                $http({
                    method : method,
                    url : url,
                    data : angular.toJson($scope.member),
                    headers : {
                        'Content-Type' : 'application/json'
                    }
                }).then(function(response) {
                	successMemberResponse();
					$scope.hideDialog();
					$scope.showAlert("Member Created");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Failed to create", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}         
        
		$scope.deleteMember = function(member) {
			var request = {
					method : 'DELETE',
					url : 'deleteProjectMember/' + member.id
				};

			$scope.showWaitDialog("Deleting Project Member");
			
			$http(request).then(function(response) {
				successMemberResponse();
				$scope.hideDialog();
				$scope.showAlert("Member Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Failed to delete", "error");
			});
		}
		
		$scope.editMember = function(member) {
            $scope.member.userName = member.userName;
            $scope.member.userEmail = member.userEmail;
            $scope.member.id  = member.id; 		
		}
		
		$scope.resetMemberForm = function(member) {
		    clearMemberFormData();
		}
		
		$scope.initMainMenu = function(container) {

		}
		
		$scope.fileEditable = {};
		$scope.file = {};
		
		$scope.initUploadFile = function(container) {
		    $scope.fileEditable.fileId = -1;
		    $scope.fileEditable.fileName = "";
		    $scope.selectedFile = undefined;
		    $scope.fileIndex = undefined;
		    $scope.file = {};
		    $scope.populateSavedFiles();
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
				        url: "saveFileData/" + $scope.fileEditable.fileName,
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
					url : 'getProjectTemplateByClient'
				};
				$scope.showWaitDialog("Loading..");
				
				$http(request).then(function(response) {
					$scope.file = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		$scope.deleteSavedFile = function(file) {
			var request = {
					method : 'DELETE',
					url : 'deleteSavedFile/' + file.fileId
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
			$window.open('downloadSavedFile/' + file.fileId,'_self');
		}
		
		$scope.buildFileEditable = {};
		$scope.buildRuleFileList = [];
		
		$scope.initBuildRuleFiles = function(container) {
		    $scope.buildFileEditable.fileId = -1;
		    $scope.buildFileEditable.fileName = "";
		    $scope.selectedFile = undefined;
		    $scope.fileIndex = undefined;
		    $scope.buildRuleFileList = [];
		    $scope.populateBuildRuleFiles();
		}
		
		$('#buildFileUploadForm').submit(function(event) {
			if($scope.buildFileUploadForm.$valid) {
				var file = $('#selectedBuildFileId').val().trim();
				if(file) {
				    var formData = new FormData(this);
				    $scope.showWaitDialog(" Processing..");
				    $.ajax({
				        type: "POST",
				        enctype: 'multipart/form-data',
				        url: "saveBuildFileData/" + $scope.buildFileEditable.fileName,
				        data: formData,
				        processData: false,
				        contentType: false,
				        success: function (response) {
				        	$scope.hideDialog();
				        	$scope.populateBuildRuleFiles();
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
		
		$scope.populateBuildRuleFiles = function() {
			var request = {
					method : 'GET',
					url : 'getBuildRuleFilesByClient'
				};
				$scope.showWaitDialog("Loading..");
				
				$http(request).then(function(response) {
					$scope.buildRuleFileList = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		$scope.deleteBuildRuleFile = function(file) {
			var request = {
					method : 'DELETE',
					url : 'deleteBuildRuleFile/' + file.fileId
				};

			$scope.showWaitDialog("Deleting..");
			
			$http(request).then(function(response) {
				$scope.populateBuildRuleFiles();
				$scope.hideDialog();
				$scope.showAlert("File Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Failed to delete file", "error");
			});
		}
		
		$scope.downloadBuildRuleFile = function(file) {
			$window.open('downloadBuildRuleFile/' + file.fileId,'_self');
		}		
		
		$scope.appVersions = ["35.2", "35.1", "35.0", "34.2", "34.1", "34.0", "33.2", "33.1", "33.0"];
		
		$scope.initAppVersion = function(container) {
		    $scope.populateAppVersion();
		    $scope.selectedValue = undefined;
		}
		
		$scope.submitAppVersionForm = function(value) {
			if($scope.appVersionForm.$valid) {
                $scope.showWaitDialog("Updating..");
				var version = $scope.appVersions[value];
     
                $http({
                    method : 'POST',
                    url : 'addAppVersion/' + version
                }).then(function(response) {
                	$scope.populateAppVersion();
					$scope.hideDialog();
					$scope.showAlert("Version updated.");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Failed to Update", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}
		
		$scope.populateAppVersion = function() {
			
			var request = {
					method : 'GET',
					url : 'getVersionByClient'
				};
				$scope.showWaitDialog("Loading..");
				
				$http(request).then(function(response) {
					$scope.savedVersion = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		$scope.initTenantMapping = function(container) {
		    $scope.getTenantsByClient();
		    $scope.getWorkbookPages();
		    $scope.populateTenantMapList();
		    $scope.selectedValue1 = undefined;
		    $scope.selectedValue2 = undefined;
		}
		
		$scope.populateTenantMapList = function() {
		    	var request = {
					method : 'GET',
					url : 'getTenantMappingByClient'
				};
				$scope.showWaitDialog("Loading..");
				
				$http(request).then(function(response) {
					$scope.mapList = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		$scope.deleteTenantMapping = function(mapping) {
			var request = {
					method : 'DELETE',
					url : 'deleteTenantMapping/' + mapping.mappingId
				};

			$scope.showWaitDialog("Deleting..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.populateTenantMapList();
				$scope.showAlert("Deleted..");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Failed to delete", "error");
			});
		}
		
		$scope.tenantMapping = {};
		
		$scope.submitTenantMappingForm = function(pageIndex, tenantIndex) {
			if($scope.tenantMappingForm.$valid) {
                $scope.showWaitDialog("Updating..");
				var pageName = $scope.pages[pageIndex];
				var tenantName = $scope.clientTenants[tenantIndex].tenantName;
				
				$scope.tenantMapping.pageName = pageName;
				$scope.tenantMapping.pageIndex = pageIndex;
				$scope.tenantMapping.tenantName = tenantName;
     
                $http({
                    method : 'POST',
                    url : 'addTenantMapping',
                    data : angular.toJson($scope.tenantMapping),
                    headers : {
                        'Content-Type' : 'application/json'
                    }
                }).then(function(response) {
                	$scope.populateTenantMapList();
					$scope.hideDialog();
					$scope.showAlert("Mapped..");
                }, function(response) {
					$scope.hideDialog();
					console.error(response);
					$scope.showAlert("Failed to Update", "error");
				})
			} else {
				$scope.showAlert("Input fields can not be left empty !!!", "error");
			}
		}		
		
		$scope.getWorkbookPages = function() {
				var request = {
					method : 'GET',
					url : 'getWorkbookPages'
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.pages = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
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
				});
		}		
		
		$scope.initRequestXML = function(container) {
			$scope.getAllRequestXML();
		}
		
		$('#reqXMLForm').submit(function(event) {
			
			var reqId = $scope.getReqId;
			if(reqId == undefined)
			{
				reqId = -1;
			}
			var reqName = $scope.requestName;
			var file = $('#requestFileId').val().trim();			
			if(file) 
			{
				var formData = new FormData(this);
				var filename = file.substring(12, file.length);
			    $scope.showWaitDialog(" Executing Request File..");
			    $.ajax({
			        type: "POST",
			        enctype: 'multipart/form-data',
			        url: "executeRequestFile/" + filename + '/' + reqName + '/' + reqId,
			        data: formData,
			        processData: false,
			        contentType: false,
			        success: function (response) {
			        	$scope.hideDialog();
			        	$scope.getAllRequestXML();
			        },
			        error: function (error) {
			        	$scope.hideDialog();
			            console.log(error);
			            // process error
			        }
			    });
			    
			    event.preventDefault();

			} 
			else 
			{
				$scope.showAlert("Please choose a file to proceed !!!", "error");
			}
		});
		
		$scope.getAllRequestXML = function() {
			var request = {
					method : 'GET',
					url : 'getAllRequestXML/' 
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.reqXMLs = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		$scope.editRequest = function(request) {
            $scope.requestName = request.requestName;
            $scope.requestXMLName = request.requestXMLName;
            $scope.getReqId = request.getReqId;
            $scope.requestXMLContent = request.requestXMLContent;
            $scope.userId = request.userId;
		}
		
		$scope.deleteRequest = function(request) {
			var request = {
					method : 'DELETE',
					url : 'deleteRequest/' + request.getReqId
				};

			$scope.showWaitDialog("Deleting Request XML");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.getAllRequestXML();
				$scope.showAlert("XML Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("XML failed to delete", "error");
			});
		}
		
		$scope.initReferenceXML = function(container) {
			$scope.getAllReferenceXML();
		}
		
		$('#refXMLForm').submit(function(event) {
			
			var refId = $scope.refId;
			if(refId == undefined)
			{
				refId = -1;
			}
			var refName = $scope.referenceName;
			var file = $('#referenceFileId').val().trim();			
			if(file) 
			{
				var formData = new FormData(this);
				var filename = file.substring(12, file.length);
			    $scope.showWaitDialog(" Executing Reference File..");
			    $.ajax({
			        type: "POST",
			        enctype: 'multipart/form-data',
			        url: "executeReferenceFile/" + filename + '/' + refName + '/' + refId,
			        data: formData,
			        processData: false,
			        contentType: false,
			        success: function (response) {
			        	$scope.hideDialog();
			        	$scope.getAllReferenceXML();
			        },
			        error: function (error) {
			        	$scope.hideDialog();
			            console.log(error);
			            // process error
			        }
			    });
			    
			    event.preventDefault();

			} 
			else 
			{
				$scope.showAlert("Please choose a file to proceed !!!", "error");
			}
		});
		
		$scope.getAllReferenceXML = function() {
			var request = {
					method : 'GET',
					url : 'getAllReferenceXML/'
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.refXML = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		$scope.deleteReference = function(reference) {
			var request = {
					method : 'DELETE',
					url : 'deleteReference/' + reference.fileId
				};

			$scope.showWaitDialog("Deleting Reference XML");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.getAllReferenceXML();
				$scope.showAlert("XML Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("XML failed to delete", "error");
			});
		}
		
		$scope.initExclusionReference = function(container) {
			$scope.getAllExclusionReference();
		}
		
		$scope.submitExclusion = function() {
			
			var refName = $scope.excRefTypeName;			
			var request = {
				method : 'POST',
				url : 'executeExcRefType/' + refName
			};
			$scope.showWaitDialog("Please Wait..");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.getAllExclusionReference();
				$scope.showAlert("Data added..");					
			}, function(response) {
				$scope.hideDialog();
				console.error(response);
			});
		}
		
		$scope.getAllExclusionReference = function() {
			var request = {
					method : 'GET',
					url : 'getAllExclusionReference/'
				};
				$scope.showWaitDialog("Please Wait");
				
				$http(request).then(function(response) {
					$scope.excRefs = response.data;
					$scope.hideDialog();
				}, function(response) {
					$scope.hideDialog();
					console.error(response);
				});
		}
		
		$scope.deleteExcRef = function(reference) {
			var request = {
					method : 'DELETE',
					url : 'deleteExcRef/' + reference.exclusionRefId
				};

			$scope.showWaitDialog("Deleting Exclusion Reference");
			
			$http(request).then(function(response) {
				$scope.hideDialog();
				$scope.getAllExclusionReference();
				$scope.showAlert("Exclusion Type Deleted");

			}, function(response) {
				$scope.hideDialog();
				console.error(response);
				$scope.showAlert("Exclusion Type failed to delete", "error");
			});
		}
	}
])	