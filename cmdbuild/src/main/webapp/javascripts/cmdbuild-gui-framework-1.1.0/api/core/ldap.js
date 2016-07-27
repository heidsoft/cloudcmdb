(function($) {
	var ldap = {
		configurator: undefined,
		variables: undefined,
		form: undefined,
		write: function(param) {
			try {
				var output = function(msg) {
					console.log("cql.manager: " + msg.msg);
				};
				this.form = param.form;
				this.configurator = new Configurator(output);
				var ldapDefinition = $.Cmdbuild.elementsManager.getElement(param.ldapDescriptor);
				var fields = this.getXmlValues(ldapDefinition, "fields", "field");
				this.variables = this.getXmlValues(ldapDefinition, "variables", "variable");
				ldapRecord = {};
				this.evaluateFields(fields, ldapRecord, function() {
					ldapRecord = JSON.stringify(ldapRecord);
					$.Cmdbuild.utilities.proxyGuiServerSide.postLdap(ldapRecord, function(response) {
					}, this);
				}, this);
			}
			catch (e) {
				console.log("$.Cmdbuild.ldap.write " + e.message);
				$.Cmdbuild.errorsManager.popup(e);
			}
		},
		evaluateFields : function(fields, ldapRecord, callback, callbackScope) {
			try {
				if (fields.length <= 0) {
					callback.apply(callbackScope, [ldapRecord]);
					return;
				}
				var field = fields[0];
				fields.splice(0, 1);
				var arValues = field.values.split(",");
				var evaluatedValues = [field.format];
				this.evaluateSingleFields(arValues, evaluatedValues, function() {
					ldapRecord[field.ldapName] = sprintf.apply(null, evaluatedValues);
					this.evaluateFields(fields, ldapRecord, callback, callbackScope);
				}, this);
			}
			catch (e) {
				console.log("$.Cmdbuild.ldap.evaluateFields " + e.message);
				$.Cmdbuild.errorsManager.popup(e);
			}
		},
		evaluateSingleFields : function(values, evaluatedValues, callback, callbackScope) {
			try {
				if (values.length <= 0) {
					callback.apply(callbackScope);
					return;
				}
				var value = $.trim(values[0]);
				values.splice(0, 1);
				if (("" + value).substr(0, 4) == "cql:") {
					this.resolveCql(value, function(response) {
						evaluatedValues.push(response);
						this.evaluateSingleFields(values, evaluatedValues, callback, callbackScope);
					}, this);
				}
				else {
					var fieldValue = $.Cmdbuild.dataModel.getValue(this.form, $.trim(value));
					evaluatedValues.push(fieldValue);
					this.evaluateSingleFields(values, evaluatedValues, callback, callbackScope);
				}
			}
			catch (e) {
				console.log("$.Cmdbuild.ldap.evaluateSingleFields " + e.message);
				$.Cmdbuild.errorsManager.popup(e);
			}
		},
		resolveCql : function(value, callback, callbackScope) {
			try {
				var key = ("" + value).substr(4);
				var keys = key.split(".");
				if (keys.length != 2) {
					throw new $.Cmdbuild.errorsManager.getError({
						message : $.Cmdbuild.errorsManager.CMERROR,
						type : $.Cmdbuild.errorsManager.PARAMNOTCORRECT,
						param : "type",
						method : "ldap resolveCql",
						element : value
					});
				}
				this.getVariable(keys[0], function(response) {
					this.resolveCqlCallback(response, keys, callback, callbackScope);
				}, this);
			}
			catch (e) {
				console.log("$.Cmdbuild.ldap.resolveCql " + e.message);
				$.Cmdbuild.errorsManager.popup(e);
			}
		},
		resolveCqlCallback : function(value, keys, callback, callbackScope) {
			try {
				this.configurator.manager["cql"].getValue("", value, function(response) {
					if (response && response.length <= 0) {
						throw new $.Cmdbuild.errorsManager.getError({
							message : $.Cmdbuild.errorsManager.CMERROR,
							type : $.Cmdbuild.errorsManager.PARAMNOTCORRECT,
							param : "type",
							method : "ldap resolveCql: Cql failed",
							element : value
						});
					}
					var retValue = "";
					if (response && response.length == 1) {
						retValue = response[0][keys[1]];
						if (typeof retValue === "object") {
							retValue = retValue.description;
						}
					}
					callback.apply(callbackScope, [retValue]);
				}, this);
			}
			catch (e) {
				console.log("$.Cmdbuild.ldap.resolveCql " + e.message);
				$.Cmdbuild.errorsManager.popup(e);
			}
		},
		getVariable : function(key, callback, callbackScope) {
			for (var i = 0; i < this.variables.length; i++) {
				if (this.variables[i].key == key) {
					var param = {
							filter: this.variables[i].cql,
							meta: {},
							form: this.form
						};
					cqlEvaluate(param, function(response) {
						callback.apply(callbackScope, [response]);
					}, this);
					return;
				}
			}
			throw new $.Cmdbuild.errorsManager.getError({
				message : $.Cmdbuild.errorsManager.CMERROR,
				type : $.Cmdbuild.errorsManager.PARAMNOTCORRECT,
				param : "type",
				method : "ldap resolveCql",
				element : "variable " + key + " not defined"
			});
		},
		getXmlValues : function(xmlElement, parent, child) {
			var $xml = $(xmlElement);
			var $fields = $xml.find(parent);
			if (! $fields.length) {
				return undefined;
			}
			var response = [];
			var xmlfields = $($fields[0]);
			for(var i = 0; i < xmlfields.children(child).length; i++) {
				var $field = $(xmlfields.children(child)[i]);
				var $attributes = $field.children("*");
				var jsonAttribute = {};
				for(var j = 0; j < $attributes.length; j++) {
					var $attribute = $($attributes[j]);
					var value = $attribute.text();
					var tag = $attribute[0].tagName;
					jsonAttribute[tag] = value;
				}
				response.push(jsonAttribute);
			}
			return response;
		}
	};
	$.Cmdbuild.ldap = ldap;
}) (jQuery);
