            var app = angular.module("tenantManagement", []);
         
            //Controller Part
            app.controller("tenantController", function($scope, $http) {
         
               
                $scope.tenants = [];
                $scope.dataCenters = ["Dublin","Atlanta","Portland"];
                $scope.tenantForm = {
                    id : -1,
                    tenantName : "",
                    tenantDataCenter : "",
                    tenantUrl : "",
                    tenantUser : "",
                    tenantUserPassword : ""
                };
         
                //Now load the data from server
                _refreshTenantData();
         
                //HTTP POST/PUT methods for add/edit tenant 
                // with the help of id, we are going to find out whether it is put or post operation
                
                $scope.submitTenant = function() {
         
                    var method = "";
                    var url = "";
                    if ($scope.tenantForm.id == -1) {
                        //Id is absent in form data, it is create new tenant operation
                        method = "POST";
                        url = '/addTenant';
                    } else {
                        //Id is present in form data, it is edit tenant operation
                        method = "PUT";
                        url = '/addTenant';
                    }
         
                    $http({
                        method : method,
                        url : url,
                        data : angular.toJson($scope.tenantForm),
                        headers : {
                            'Content-Type' : 'application/json'
                        }
                    }).then( _success, _error );
                };
         
                //HTTP DELETE- delete tenant by Id
                $scope.deleteTenant = function(tenant) {
                    $http({
                        method : 'DELETE',
                        url : '/deleteTenant/' + tenant.id
                    }).then(_success, _error);
                };
 
             // In case of edit, populate form fields and assign form.id with tenant id
                $scope.editTenant = function(tenant) {
                  
                    $scope.tenantForm.tenantName = tenant.tenantName;
                    $scope.tenantForm.tenantDataCenter = tenant.tenantDataCenter;
                    $scope.tenantForm.id = tenant.id;
                    $scope.tenantForm.tenantUrl = tenant.tenantUrl;
                    $scope.tenantForm.tenantUser = tenant.tenantUser;
                    $scope.tenantForm.tenantUserPassword = tenant.tenantUserPassword;
                };
         
                /* Private Methods */
                //HTTP GET- get all tenants collection
                function _refreshTenantData() {
                    $http({
                        method : 'GET',
                        url : 'getAllTenantsByUser'
                    }).then(function successCallback(response) {
                        $scope.tenants = response.data;
                    }, function errorCallback(response) {
                        console.log(response.statusText);
                    });
                }
         
                function _success(response) {
                    _refreshTenantData();
                    _clearFormData();
                }
         
                function _error(response) {
                    console.log(response.statusText);
                }
         
                //Clear the form
                function _clearFormData() {
                    $scope.tenantForm.id = -1;
                    $scope.tenantForm.tenantName = "";
                    $scope.tenantForm.tenantDataCenter = "";
                    $scope.tenantForm.tenantUrl = "";
                    $scope.tenantForm.tenantUser = "";
                    $scope.tenantForm.tenantUserPassword = "";                
                };
            });
