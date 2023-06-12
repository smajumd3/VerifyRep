
var Validation = {
		
		validName : function(text) {
			return text != undefined && text.length > 2 && text.length < 21; 
		},
		
		validEmail : function(text) {
			var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
			return text != undefined && regex.test(text);
		},
		
		validPassword : function(text) {
			return text != undefined && text.length > 5 && text.length < 13; 
		}
}