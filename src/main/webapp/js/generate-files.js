var myApp = angular.module('generate.files', [ 'ngAnimate', 'ngSanitize', 'ui.bootstrap', 'chart.js' ]);

myApp.controller(
		'generateFilesCtrl',
		[
			'$scope',
			'$http',
			'$uibModal',
			function($scope, $http, $uibModal) {
				
				var ruleId;
				var resultId;
				var ruleName;
				var operationName;
				
				$scope.openWaitModal = function () {
					$scope.waitModal = $uibModal.open({
				        backdrop: 'static',
				        ariaDescribedBy: 'modal-body',
				        templateUrl: 'modalPleaseWait.html',
				        size: 'sm'
					});
				},

				$scope.closeWaitModal = function () {
					$scope.waitModal.close();
				};
				
				$scope.generateFiles = function ()
				{
				    var url_string = window.location.href;				
				    var url = new URL(url_string);
				    var result = url.searchParams.get("parameter");
				    var wdResponse = result.split(',');
				    $scope.resultId = wdResponse[0];
				    resultId = $scope.resultId;
				    $scope.ruleId = wdResponse[1];
				    ruleId = $scope.ruleId;
				    $scope.ruleName = wdResponse[2];
				    ruleName = $scope.ruleName;
				    $scope.operationName = wdResponse[3];
				    operationName = $scope.operationName;
				    console.log(ruleId);		
				};
				
				$scope.generateXMLFiles = function ()
				{
					$scope.openWaitModal();
				    $http.get("RuleDataServlet?requestType=generateXMLFiles&ruleId="+ruleId + "&resultId=" +resultId + "&ruleName=" +ruleName + "&operationName=" +operationName)
					.then(function(response) {
						$scope.closeWaitModal();
						$scope.xmlDetails = response.data;
						if($scope.xmlDetails[0].status == "Success"){
		   					alert("XML generated successfully in Download folder");
		   				}else{
		   					alert("XML generation Failed");
		   				}
					});			
				};
				
				$scope.createXMLFilesReport = function(){
					$scope.openWaitModal();
					$http.get("RuleDataServlet?requestType=createXMLFilesReport")
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
				
				$scope.close = function ()
				{ 
				    window.opener = self;
				    window.close();
				};
				 
			} ]);