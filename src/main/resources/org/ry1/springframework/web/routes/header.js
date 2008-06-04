/* $Id$ */

var Routes = {
	url: function(name, parameters) {
		var pattern = Routes.get(name);
		if (parameters == null) {
			parameters = {};
		}
		
		return pattern.parameterize(parameters);
	},
	
	get: function(name) {
		return Routes.NAMED_ROUTES[name];
	}
};
String.prototype.parameterize = function(parameters) {
	var result = this;
	for (var name in parameters) {
		result = result.replace('${' + name + '}', parameters[name]);
	}
	return result;
};
Routes.NAMED_ROUTES = 