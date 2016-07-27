function Configurator(output) {
	var HOOKMANAGERNOTDEFINED = "Non e' stato definito un manager Cql";
	this.debug = false;
	this.output = output;
	this.output({
		type: "debug",
		msg: "Configurator!"
	});
	this.manager = {
		xa: {
			isVariableManager: true,
			getValue: function(form, name, callback, callbackScope) {
				var values = name.split(".");
				if (values.length == 2 && values[1] == "Id") {
					callback.apply(callbackScope, [4444]);
				} else {
					callback.apply(callbackScope, ["xa__" + form + " " + name]);
				}
			}
		},
		user: {
			isVariableManager: true,
			getValue: function(form, name, callback, callbackScope) {
				var authToken = $.Cmdbuild.authentication
						.getAuthenticationToken();
				$.Cmdbuild.utilities.proxy.getSession(authToken,
						function(data) {
							var config = {
								page: 0,
								start: 0,
								limit: 1,
								filter: {
									attribute: {
										simple: {
											attribute: "Username",
											operator: "equal",
											value: [data[0].username]
										}
									}
								},
								filterType: "attribute"
							};
							$.Cmdbuild.utilities.proxy.getCardList("User",
									config, function(response, metadata) {
										if (response.length) {
											callback.apply(callbackScope,
													[response[0]._id]);
										}
									}, this);
						}, this, []);
			}
		},
		client: {
			isVariableManager: true,
			getValue: function(formName, name, callback, callbackScope) {
				var xmlForm = $.Cmdbuild.elementsManager.getElement(formName);
				var param = {};
				$.Cmdbuild.elementsManager.getParams(xmlForm, param);
				var form = $.Cmdbuild.dataModel.forms[formName];
				var ar = name.split(".");
				if (ar.length > 1 && ar[1].toUpperCase() == "DESCRIPTION") {
					var val = {
						field: ar[0],
						form: formName
					};
					form.getClientDescription(val, function(response) {
						callback.apply(callbackScope, [response]);
					}, this);
				} else {
					var value = form.getValue({
						field: ar[0],
						form: formName
					});
					callback.apply(callbackScope, [value || undefined]);
				}
			}
		},
		server: {
			isVariableManager: true,
			getValue: function(formName, name, callback, callbackScope) {
				try {
					var form = $.Cmdbuild.dataModel.forms[formName];
					var ar = name.split(".");
					if (ar.length > 1 && ar[1].toUpperCase() == "DESCRIPTION") {
						var val = {
							field: ar[0],
							form: formName
						};
						form.getServerDescription(val, function(response) {
							callback.apply(callbackScope, [response]);
						}, this);
					} else {
						var value = $.Cmdbuild.dataModel.getValue(formName,
								ar[0]);
						callback.apply(callbackScope, [value || undefined]);
					}
				} catch (e) {
					console.log("Error on server variable " + formName + " "
							+ name + " " + e.message);
					callback.apply(callbackScope, [undefined]);
				}
			}
		},
		cql: {
			isVariableManager: false,
			getValue: function(form, filter, callback, callbackScope) {
				if (filter.indexOf($.Cmdbuild.global.CQLUNDEFINEDVALUE) != -1) {
					callback.apply(callbackScope, [undefined]);
					return;
				}
				var config = {
					filter: {
						CQL: filter
					}
				};
				$.Cmdbuild.utilities.proxy.getCqlResult(config, function(
						response) {
					callback.apply(callbackScope, [response]);
				}, this);
			}
		},
		js: {
			isVariableManager: false,
			getValue: function(form, name, callback, callbackScope) {
				var patt = new RegExp($.Cmdbuild.global.CQLUNDEFINEDVALUE, "g");
				var nameSub = name.replace(patt, "undefined");
				var value = this.safeJSEval(nameSub);
				callback.apply(callbackScope, [value]);
			},
			safeJSEval: function(stringTOEvaluate) {
				var resultOfEval = "";
				output({
					type: "debug",
					msg: "***********safeJSEval " + stringTOEvaluate
				});
				try {
					resultOfEval = eval(stringTOEvaluate);
				} catch (e) {
					/*
					 * happens that some jsExpr contains characters that break
					 * the eval() so try again replacing the characters that was
					 * already identified as problematic
					 */
					try {
						resultOfEval = eval(stringTOEvaluate
								.replace(
										/(\r\n|\r|\n|\u0085|\u000C|\u2028|\u2029)/g,
										""));
					} catch (ee) {
						output({
							type: "debug",
							msg: "Error evaluating javascript expression "
									+ stringTOEvaluate
						});
					}
				}
				output({
					type: "debug",
					msg: "***********safeJSEval evaluated " + resultOfEval
				});

				return resultOfEval;
			}
		}
	};
	this.changed = function(form, field) {
		var param = {
			form: form,
			field: field
		};
		if ($.Cmdbuild.custom.commands
				&& $.Cmdbuild.custom.commands.fieldRefresh) {
			$.Cmdbuild.custom.commands.fieldRefresh(param);
		} else {
			$.Cmdbuild.standard.commands.fieldRefresh(param);
		}
	};
};

