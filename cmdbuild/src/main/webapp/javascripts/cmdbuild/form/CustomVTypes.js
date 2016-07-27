var fullemailspec = /^[\w ]*\<([^\<\>]+)\>[ ]*$/;
var numericRegExp = /^(([+,\-]?[0-9]+)|[0-9]*)(\.[0-9]+)?$/;
var numericValidation = function (value, scale, precision) {
	var out = {
		valid: true,
		message: ''
	};

	if (value.match(numericRegExp) == null) {
		out = {
			valid: false,
			message: CMDBuild.Translation.vtypeNumericInvalidCharacterText
		};
	}
	var splitByDecimalSeparator = value.split(".");
	var integerPart = Math.abs(splitByDecimalSeparator[0]);
	var decimalPart = splitByDecimalSeparator[1];

	if (precision !== undefined) {
		var integerPartMaxlength = precision - (scale || 0);
		if (integerPart && new String(integerPart).length > integerPartMaxlength) {
			out = {
				valid: false,
				message: Ext.String.format(CMDBuild.Translation.vtypeNumericMaxIntegerDigitsText ,integerPartMaxlength)
			};
		}
	};

	if (scale !== undefined) {
		if (decimalPart && decimalPart.length > scale) {
			out = {
				valid: false,
				message: Ext.String.format(CMDBuild.Translation.vtypeNumericMaxDecimalDigitsText, scale)
			};
		}
	}

	return out;
};

Ext.apply(Ext.form.VTypes, {
	emailaddrspec : function(v) {
		 var inner = v.match(fullemailspec);
		 if (inner) {
			 v = inner[1];
		 }
		 return Ext.form.VTypes.email(v);
	},
	emailaddrspecText : Ext.form.VTypes.emailText,

	emailaddrspeclist : function(v) {
		var a = v.split(",");
		for (var i=0,len=a.length; i<len; ++i) {
			var sv = Ext.String.trim(a[i]);
			if (sv && !Ext.form.VTypes.emailaddrspec(sv)) {
				return false;
			}
		}
			return true;
	},
	emailaddrspeclistText : Ext.form.VTypes.emailText,

	emailOrBlank: function(v) {
		return (v.length == 0 || this.email(v));
	},

	emailOrBlankText : Ext.form.VTypes.emailText,

	/**
	 * FIXME: don't avoids to type alphabetical digits in field
	 */
	numeric: function(val, field) {
		var valid = numericValidation(val, field.scale, field.precision);
		field.vtypeText = valid.message;

		return valid.valid;
	}
});
