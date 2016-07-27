/**
 * Custom validation made by Tecnoteca
 */
$.validator.addMethod("decimal", function(value, element, param) {
	var precision = param[0];
	var scale = param[1];
	var rgx = new RegExp('^\\d{1,'+ (precision - scale) +'}(\\.\\d{1,'+ scale +'})?$');
	return this.optional(element) || rgx.test(value);
}, "Decimal number must have precision {0} and scale {1}.");


