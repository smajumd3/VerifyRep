<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.4/angular.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script type="text/javascript" src="../js/tenant.js"></script>

<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<link rel="stylesheet" href="../css/tenant.css">

<title>Tenant Setup</title>
</head>

<body data-ng-app="tenantManagement" data-ng-controller="tenantController">
	<h3 class="text-center">Tenant Configuration</h3>
	<form data-ng-submit="submitTenant()">
		<table>
			<tr>
				<th colspan="2">Add/Edit tenant</th>
			</tr>
			<tr>
				<td>Tenant Name</td>
				<td><input type="text" data-ng-model="tenantForm.tenantName" /></td>
			</tr>
			<tr>
				<td>Data Center</td>
				<td>
				<select data-ng-model="tenantForm.tenantDataCenter" data-ng-options="x for x in dataCenters">
                </select>
                </td>
			</tr>
			<tr>
				<td>End Point URL</td>
				<td><input type="text" data-ng-model="tenantForm.tenantUrl" /></td>
			</tr>
			<tr>
				<td>User Name</td>
				<td><input type="text" data-ng-model="tenantForm.tenantUser" /></td>
			</tr>
			<tr>
				<td>Password</td>
				<td><input type="password" data-ng-model="tenantForm.tenantUserPassword" /></td>
			</tr>
			<tr>
				<td colspan="2"><input type="submit" value="Submit"
					class="blue-button" /></td>
			</tr>
		</table>
	</form>
	<br><br>
	<table>
		<tr>
			<th>Tenant Name</th>
			<th>Data Center</th>
			<th>End Point URL</th>
			<th>User Name</th>
			<th>Password</th>
			<th>Actions</th>
		</tr>

		<tr data-ng-repeat="tenant in tenants">
			<td>{{ tenant.tenantName }}</td>
			<td>{{ tenant.tenantDataCenter }}</td>
			<td>{{ tenant.tenantUrl }}</td>
			<td>{{ tenant.tenantUser }}</td>
			<td>******</td>
			<td class="col-sm-2">
				 <md-button aria-label="" class="md-icon-button md-primary" ng-click="editTenant(tenant)"><span class="glyphicon glyphiconPlus glyphicon-edit"></span>&nbsp;</md-button>
				 <md-button aria-label="" class="md-icon-button md-warn" ng-click="deleteTenant(tenant)"><span class="glyphicon glyphiconPlus glyphicon-trash"></span>&nbsp;</md-button>
			</td>				
		</tr>
	</table>
</body>
</html>