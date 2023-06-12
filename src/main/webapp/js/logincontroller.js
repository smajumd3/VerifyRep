var loginApp = angular.module('loginApp', [ 'ngAnimate', 'ngMaterial',
		'ngMessages', 'ngSanitize', 'ui.bootstrap', 'chart.js', 'ui.knob' ]);

loginApp.controller('loginController', [
	'$scope',
	'$http',
	'$interval',
	'$window',
	'$mdDialog',
	'$mdToast',
	function($scope, $http, $interval, $window, $mdDialog, $mdToast) {
	window.onload=function() {
		const signUpButton = document.getElementById('signUp');
		const signInButton = document.getElementById('signIn');
		const container = document.getElementById('container');
	
		signUpButton.addEventListener('click', () => {
			container.classList.add("right-panel-active");
		});
	
		signInButton.addEventListener('click', () => {
			container.classList.remove("right-panel-active");
		});
	}
	
	$scope.clients = [];
	
	$scope.getClientsForUser = function(email, password) {
	       $scope.clients = [];
			var request = {
					method : 'GET',
					url : 'getClientListForUser/' + email + '/' + password
			};
				
			$http(request).then(function(response) {
				$scope.clients = response.data;
			}, function(response) {
				console.error(response);
			});	
	}

}])