<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" href="../css/main-theme.css">
<script type="text/javascript" src="../js/maincontroller.js"></script>
<meta charset="ISO-8859-1">
<title>PostLoad Validation</title>
</head>

<body>
	<h3 class="text-center">Source - Workday Column Mapping</h3>
      <div class="statusPanel">
         <table id="columnTable" class="table statusTable tenantStatus">
		    <thead>
			    <tr>
			        <th class="col-sm-2"><input type="checkbox" ng-model="IsAllChecked" ng-change="checkUncheckAllModal(IsAllChecked)" /> Select </th>
			        <th class="col-sm-4">Source Column Name</th>
				    <th class="col-sm-4">Workday Column Name</th>
				    <th class="col-sm-2">Identifier</th>
			    </tr>
			</thead>
			<tbody>
			    <tr data-ng-repeat="mapFile in mapFiles" >
			    	<td class="col-sm-2"><input type="checkbox" ng-model="mapFile.isSelect" ng-change="checkUncheckHeader()"/></td>
				    <td class="col-sm-4">{{ mapFile.headingSource }}</td>
				    <td class="col-sm-4" ng-if="mapFile.heading != ''">{{ mapFile.heading }}</td>
				    <td class="col-sm-4" ng-if="mapFile.heading == ''">
						<md-select data-ng-model="selectedColumn[$index]">  <!-- class="labelStyle" -->
					    	<md-option data-ng-repeat="heading in headerValues track by $index" data-ng-value="{{$index}}">
					    		{{ heading.headingWD }}
					    	</md-option>
					   </md-select> 									  
				    </td>
				    <td class="col-sm-2"><input type="checkbox" ng-model="mapFile.isChecked" ng-change="toggleKey(mapFile)"/></td>
			    </tr>
		    </tbody>
	    </table>
	    <section layout="row" layout-sm="column" layout-align="center" layout-wrap>							      						        						        	
	        <md-button aria-label="" class="btn" data-ng-click="performComparison()"><span class="glyphicon glyphicon-send"></span>&nbsp;Compare</md-button>	
	        <md-button class="btn" ng-click="$close()">Close</md-button>						        
	   </section>
       </div>

</body>
</html>