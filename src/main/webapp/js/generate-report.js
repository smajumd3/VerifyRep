var myApp = angular.module('generate-report.automation', [ 'ngAnimate', 'ngSanitize', 'ui.bootstrap', 'chart.js' ]);

myApp.controller(
		'GenRptCtrl',
		[
			'$scope',
			'$http',
			'$uibModal',
			function($scope, $http, $uibModal) {
				
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
								
				$scope.generateErrorReport = function ()
				{
					$scope.openWaitModal();
				    var url_string = window.location.href;				
				    var url = new URL(url_string);
				    var result = url.searchParams.get("parameter");
				    var wdDet = result.split(',');
				    $scope.sequence = wdDet[0];
				    $scope.operation = wdDet[1];
				    var mapKey = $scope.operation + "_" + $scope.sequence;
				    
				    $http.get("HistDataServlet?requestType=genErrRpt&mapKey="+mapKey)
					.then(function(response) {
						$scope.closeWaitModal();
						$scope.errorDetails = response.data;
						if(response.data == '' && $scope.tabName == 'Settings')
							$scope.addAlert('warning','There are no operations configured yet. Please create a operations first.')	
					});
				}
				
				$scope.close = function ()
				{ 
				    window.opener = self;
				    window.close();
				}
				
				$scope.downloadWorkdayXML = function () {
		        	
				    $http.get("RuleDataServlet?requestType=downloadWorkdayXML&strXML=" +strXML + "&resName=" +name)
					.then(function(response) {								
						$scope.dndXml=response.data.status;							
		   				if($scope.dndXml == "Success"){
		   					alert("XML successfully downloaded in Download folder");
		   				}else{
		   					alert("XML download Failed");
		   				}							
					});
						
		        };
				 
			} ]);