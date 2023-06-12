<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
<meta http-equiv="Pragma" content="no-cache" />
<meta http-equiv="Expires" content="0" />

<link rel="icon" href="../images/logo.png">
<title>Hyperloader</title>
	
<link rel="stylesheet" href="../css/bootstrap.min.css">
<link rel="stylesheet" href="../css/font-awesome.min.css">
<link rel="stylesheet" href="../css/angular-material.min.css">
<link rel="stylesheet" href="../css/logintheme.css">

<script type="text/javascript" src="../js/angular.js"></script>
<script type="text/javascript" src="../js/angular-animate.js"></script>
<script type="text/javascript" src="../js/angular-aria.min.js"></script>
<script type="text/javascript" src="../js/angular-messages.min.js"></script>
<script type="text/javascript" src="../js/angular-material.min.js"></script>
<script type="text/javascript" src="../js/ui-bootstrap-tpls-2.0.2.js"></script>
<script type="text/javascript" src="../js/jquery-3.3.1.min.js"></script>
<script type="text/javascript" src="../js/Chart.min.js"></script>
<script type="text/javascript" src="../js/angular-chart.min.js"></script>
<script type="text/javascript" src="../js/moment.js"></script>
<script type="text/javascript" src="../js/validationRules.js"></script>
<script type="text/javascript" src="../js/logincontroller.js"></script>
<script type="text/javascript" src="../js/angular-sanitize.js"></script>
<script type="text/javascript" src="../js/d3.min.js"></script>
<script type="text/javascript" src="../js/messageResource-min.js"></script>
<script type="text/javascript" src="../js/ng-knob.min.js"></script>
<script type="text/javascript" src="../js/angular-tree-control.js"></script>
<script type="text/javascript" src="../js/context-menu.js"></script>
</head>

<body data-ng-app="loginApp">
    <div class="video-container">
        <video src="../videos/binary.mov" autoplay muted loop></video>
    </div>
<div class="container" id="container" data-ng-controller="loginController">
	<div class="form-container sign-up-container">
		<form method="post" action="registerUser">
			<h1>Sign Up</h1>
			<span>Please provide your details for registration</span>
			<input type="text" placeholder="Name" name="userName"/>
			<input type="email" placeholder="Email" name="email"data-ng-model="email1"/>
			<input type="password" placeholder="Password" name="password"data-ng-model="password1"/>
<!--			<input type="text" placeholder="Client" name="client"/>    -->
  		    <select name="client" data-ng-focus="getClientsForUser(email1, password1)">
				<option data-ng-repeat="client in clients">
			        {{client}}
			    </option>
			</select>
			<input type="password" placeholder="New Password" name="newpassword"/>
			<input type="password" placeholder="Confirm Password" name="confpassword"/>
			<button type="submit">Sign Up</button>
		</form>
	</div>
	<div class="form-container sign-in-container">
		<form method="post" action="initiate">
			<h1>Sign In</h1>
			<span>Please use your registered email id and password to sign in</span>
			<input type="email" placeholder="Email" name="email" data-ng-model="email2"/>
			<input type="password" placeholder="Password" name="password" data-ng-model="password2"/>
			<select name="client" data-ng-focus="getClientsForUser(email2, password2)">
			    <option data-ng-repeat="client in clients">
			        {{client}}
			    </option>
			</select>
			<a href="#">Forgot your password?</a>
			<button type="submit">Sign In</button>
		</form>
	</div>
	<div class="overlay-container">
		<div class="overlay">
			<div class="overlay-panel overlay-left">
				<h1>Welcome!!</h1>
				<p>Please use your registered email id and password to login</p>
				<button class="ghost" id="signIn">Sign In</button>
			</div>
			<div class="overlay-panel overlay-right">
				<h1>Hello!!</h1>
				<p>Please provide your required details to sign up and stay connected</p>
				<button class="ghost" id="signUp">Sign Up</button>
			</div>
		</div>
	</div>
</div>
</body>
</html>