(function($) {
	var FIELD_CONTAINER_CLASS = "cmdbuildGuiFormField";
	
	var fieldsManager = {
		types : {
			BOOLEAN : "boolean",
			CHAR : "char",
			DATE : "date",
			DECIMAL : "decimal",
			DOUBLE : "double",
			IPADDRESS : "ipAddress",
			INTEGER : "integer",
			LOOKUP : "lookup",
			REFERENCE : "reference",
			STRING : "string",
			TEXT : "text",
			TIME : "time",
			DATETIME : "dateTime"
		},

		/**
		 * @param {Object} attribute CMDBuild attribute
		 * @param {String} formName
		 * @param {*} value
		 * @return {jQuery} Form field
		 */
		generateHTML : function(attribute, formName, formContainer, value) {
			var writable = attribute.writable === true || attribute.writable === "true";
			var mandatory = attribute.mandatory === true || attribute.mandatory === "true";
			var $fieldContainer = this.getFormFieldContainer(formName,
					attribute._id, attribute.type, attribute.description,
					attribute.hidden, !writable, mandatory);

			var $field = this.getFormField(attribute, formName, formContainer, value);
			if (attribute.hidden) {
				$fieldContainer.append($field);
			} else {
				$fieldContainer.find("dd").append($field);
			}

			return $fieldContainer;
		},

		getFieldValidator : function(attribute) {
			var rules = {};
			if (attribute.mandatory) {
				rules["required"] = true;
			}
			switch (attribute.type) {
			case this.types.DECIMAL:
				rules["decimal"] = [attribute.precision, attribute.scale];
				break;
			case this.types.DOUBLE:
				rules["number"] = true;
				break;
			case this.types.IPADDRESS:
				if (attribute.ipType === "ipv4") {
					rules["ipv4"] = true;
				} else if (attribute.ipType === "ipv6") {
					rules["ipv6"] = true;
				}
				break;
			case this.types.INTEGER:
				rules["integer"] = true;
				break;

			default:
				$output = $("<span></span>").text(
						attribute.type + " not implemented");
				// TODO: Raise error/warning
				break;
			}
			return {
				rules : rules
			};
		},

		/**
		 * @param {String} form
		 * @param {String} name
		 * @param {String} type
		 * @param {String} label
		 * @param {Boolean} hidden
		 * @param {Boolean} readonly
		 * @param {Boolean} mandatory
		 * @return {jQuery}
		 */
		getFormFieldContainer : function(form, name, type, label, hidden, readonly, mandatory) {
			var id = getFieldId(form, name);
			var $container;
			if (hidden) {
				$container = $("<div></div>");
			} else {
				$container = $("<dl></dl>").addClass(FIELD_CONTAINER_CLASS).addClass(FIELD_CONTAINER_CLASS + "-" + type);
				if (readonly) {
					$container.addClass(FIELD_CONTAINER_CLASS + "ReadOnly");
				} else if (mandatory) {
					label = "* " + label;
				}
				var $dt = $("<dt></dt>").append($("<label></label>").addClass("cmdbuildLabel").attr("for", id).text(label));
				$container.append($dt).append($("<dd></dd>"));
			}
			$container.attr("id", id + "_container");
			return $container;
		},

		/**
		 * @param {Object} attribute
		 * @param {String} formName
		 * @parma {*} value
		 * @return {jQuery}
		 */
		getFormField : function(attribute, formName, formContainer, value) {
			var me = this;
			var $output;
			if (attribute.hidden) {
				$output = getHiddenField(formName, attribute._id, value);
			} else {
				switch (attribute.type) {
				case me.types.BOOLEAN:
					$output = getBooleanField(formName, attribute, value);
					break;
				case me.types.CHAR:
					$output = getCharField(formName, attribute, value);
					break;
				case me.types.DATE:
					$output = getDateTimeField(formName, attribute, value);
					break;
				case me.types.DATETIME:
					$output = getDateTimeField(formName, attribute, value);
					break;
				case me.types.DECIMAL:
					$output = getDecimalField(formName, attribute, value);
					break;
				case me.types.DOUBLE:
					$output = getDoubleField(formName, attribute, value);
					break;
				case me.types.IPADDRESS:
					attribute.ipType = "ipv4";
					$output = getIpAddressField(formName, attribute, value);
					break;
				case me.types.INTEGER:
					$output = getIntegerField(formName, attribute, value);
					break;
				case me.types.LOOKUP:
					$output = getLookupField(formName, attribute, value);
					break;
				case me.types.REFERENCE:
					$output = getReferenceField(formName, formContainer, attribute, value);
					break;
				case me.types.STRING:
					$output = getStringField(formName, attribute, value);
					break;
				case me.types.TEXT:
					$output = getTextField(formName, attribute, value);
					break;
				case me.types.TIME:
					$output = getDateTimeField(formName, attribute, value);
					break;

				default:
					$output = $("<span></span>").text(attribute.type + " not implemented");
					// TODO: Raise error/warning
					break;
				}
			}
			
			return $output;
		},

		/**
		 * @param {String} form
		 * @param {String} field
		 * @return {String}
		 */
		getFieldId : function(form, field) {
			return getFieldId(form, field);
		}
	};

	/**
	 * @param {String} form
	 * @param {String} field
	 * @return {String}
	 */
	var getFieldId = function(form, field) {
		return form + "-field-" + field;
	};

	/**
	 * @param {jQuery} $field
	 * @param {Object} attribute
	 * @param {String} form
	 */
	var setFieldCommonAttributes = function($field, attribute, form) {
		// set id
		$field.attr("id", getFieldId(form, attribute._id));
		// set form name
		$field.attr("formName", form).attr("form", form);
		// field name
		$field.attr("name", attribute._id).attr("fieldName", attribute._id);
		// className
		if (attribute.className) {
			$field.addClass(attribute.className);
		}
		// title
		if (attribute.title || attribute.tooltip) {
			var title = attribute.title ? attribute.title : attribute.tooltip;
			$field.attr("title", title);
		}
		// width
		if (attribute.width) {
			$field.attr("width", attribute.width);
		}
		// height
		if (attribute.height) {
			$field.attr("height", attribute.height);
		}
	};

	/**
	 * @param {String} form
	 * @param {String} field
	 * @param {*} value
	 * @return {jQuery}
	 */
	var getHiddenField = function(form, field, value) {
		var $input = $("<input></input>").attr("type", "hidden");
		setFieldCommonAttributes($input, {_id : field}, form);
		if (value) {
			$input.val(value);
		}
		return $input;
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {Boolean|String} value
	 * @return {jQuery}
	 */
	var getBooleanField = function(form, attribute, value) {
		// TODO: manage onClick event

		if (attribute.writable === true || attribute.writable === "true") {
			var $input = $("<input></input>").attr("type", "checkbox");
			setFieldCommonAttributes($input, attribute, form);
			if (value === true || value === "true") {
				$input.attr("checked", "checked");
			}
			return $input;
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			// TODO: translate labels
			var $text = (value === true || value === "true") ? "Yes" : "No";
			return [$text, $input];
		}
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {String} value
	 * @return {jQuery}
	 */
	var getCharField = function(form, attribute, value) {
		if (attribute.writable === true || attribute.writable === "true") {
			var $input = $("<input></input>").attr("type", "text");
			setFieldCommonAttributes($input, attribute, form);
			$input.attr("maxlength", 1).attr("size", 1);
			if (value) {
				$input.val(value);
			}
			return $input;
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			var $text = $("<span></span>").text(value ? value : "");
			return [$text, $input];
		}
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {String|Date} value
	 * @return {jQuery}
	 */
	var getDateTimeField = function(form, attribute, value) {
		// convert date
		if (value) {
			value = $.Cmdbuild.utilities.convertDateDB2GUI(value, attribute.type);
		}

		if (attribute.writable === true || attribute.writable === "true") {
			var $input = $("<input></input>").attr("type", "text");
			setFieldCommonAttributes($input, attribute, form);
			$input.attr("isDate", true).attr("date-input-type", attribute.type);

			if (value) {
				value = $.Cmdbuild.utilities.convertDateDB2GUI(text, param.type)
				$input.val(value);
			}
			switch (attribute.type) {
			case "time":
				$input.timepicker({
					timeFormat : "HH:mm:ss"
				});
				break;
			case "date":
				$input.datepicker({
					dateFormat : 'dd/mm/yy'
				});
				break;
			case "dateTime":
				$input.datetimepicker({
					dateFormat : 'dd/mm/yy',
					timeFormat : "HH:mm:ss"
				});
				break;
			}

			return $input;
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			var $text = $("<span></span>").text(value ? value : "");
			return [$text, $input];
		}
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {Decimal|String} value
	 * @return {jQuery}
	 */
	var getDecimalField = function(form, attribute, value) {
		if (attribute.writable === true || attribute.writable === "true") {
			var $input = $("<input></input>").attr("type", "text");
			setFieldCommonAttributes($input, attribute, form);
			if (value) {
				$input.val(value);
			}
			return $input;
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			var $text = $("<span></span>").text(value ? value : "");
			return [$text, $input];
		}
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {Float|String} value
	 * @return {jQuery}
	 */
	var getDoubleField = function(form, attribute, value) {
		if (attribute.writable === true || attribute.writable === "true") {
			var $input = $("<input></input>").attr("type", "text");
			setFieldCommonAttributes($input, attribute, form);
			if (value) {
				$input.val(value);
			}
			return $input;
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			var $text = $("<span></span>").text(value ? value : "");
			return [$text, $input];
		}
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {String} value
	 * @return {jQuery}
	 */
	var getIpAddressField = function(form, attribute, value) {
		if (attribute.writable === true || attribute.writable === "true") {
			var $input = $("<input></input>").attr("type", "text");
			setFieldCommonAttributes($input, attribute, form);
			if (attribute.ipType === "ipv4") {
				$input.attr("maxlength", "15");
			} else if (attribute.ipType === "ipv6") {
				$input.attr("maxlength", "39");
			}
			if (value) {
				$input.val(value);
			}
			return $input;
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			var $text = $("<span></span>").text(value ? value : "");
			return [$text, $input];
		}
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {Integer|String} value
	 * @return {jQuery}
	 */
	var getIntegerField = function(form, attribute, value) {
		if (attribute.writable === true || attribute.writable === "true") {
			var $input = $("<input></input>").attr("type", "text");
			setFieldCommonAttributes($input, attribute, form);
			if (value) {
				$input.val(value);
			}
			return $input;
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			var $text = $("<span></span>").text(value ? value : "");
			return [$text, $input];
		}
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {Integer|String} value
	 * @return {jQuery}
	 */
	var getLookupField = function(form, attribute, value) {
		var $input;
		var writable = attribute.writable === true || attribute.writable === "true";
		if (writable) {
			var $input = $("<select></select>");
			setFieldCommonAttributes($input, attribute, form);
			$("#" + form).on('DOMNodeInserted', function(event) {
				if ($(event.target).find("#" + $input.attr("id")).length) {
					$input.selectmenu({ 
						change : function (event, ui) { 
							var conf = {id : $input.attr("id")};
							if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands.fieldChanged) {
								$.Cmdbuild.custom.commands.fieldChanged(conf);
							}
							else {
								$.Cmdbuild.standard.commands.fieldChanged(conf);
							}
							// TODO: custom on change event
						}
					});
				}
			});
			
		} else {
			var $input = getHiddenField(form, attribute._id, value);
		}
		var id = $input.attr("id");
		new $.Cmdbuild.standard.lookupField({
			form : form,
			id : id,
			readOnly : ! writable,
			lookupType : attribute.lookupType,
			value : value
		});
		return $input;
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {Integer|String} value
	 * @return {jQuery}
	 */
	var getReferenceField = function(form, formContainer, attribute, value) {
		var $input;
		var output = [];
		var writable = attribute.writable === true || attribute.writable === "true";
		if (writable) {
			var $input = $("<select></select>");
			setFieldCommonAttributes($input, attribute, form);

			// create buttons
			var $searchBtn = $("<button></button>").attr("id",
					$input.attr("id") + "_btnsearch")
					.addClass("cmdbuildButton").button({
						icons : {
							primary : "ui-icon-search"
						},
						text : false
					}).click(function(event) {
						$input.trigger("searchreferenceitem");
						return false;
					});
			var $clearBtn = $("<button></button>").attr("id",
					$input.attr("id") + "_btnclear").addClass("cmdbuildButton")
					.button({
						icons : {
							primary : "ui-icon-closethick"
						},
						text : false
					}).click(function(event) {
						$input.trigger("clearreference");
						return false;
					});

			// initialize select
			$("#" + form).on('DOMNodeInserted', function(event) {
				if ($(event.target).find("#" + $input.attr("id")).length) {
					$input.selectmenu({ 
						change : function (event, ui) { 
							var conf = {id : $input.attr("id")};
							if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands.fieldChanged) {
								$.Cmdbuild.custom.commands.fieldChanged(conf);
							}
							else {
								$.Cmdbuild.standard.commands.fieldChanged(conf);
							}
							// TODO: custom on change event
						}
					});
				}
			});

			output.push($input, $searchBtn, $clearBtn);
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			output.push($input);
		}
		var id = $input.attr("id");
		new $.Cmdbuild.standard.referenceField({
			id : id,
			container : formContainer,
			targetClass : attribute.targetClass,
			targetType : attribute.targetType,
			filter : attribute.filter,
			readOnly : !writable,
			optionsLimit : null,
			value : value,
			displayAttribute : null
		});
		return output;
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {String} value
	 * @return {jQuery}
	 */
	var getStringField = function(form, attribute, value) {
		if (attribute.writable === true || attribute.writable === "true") {
			var $input = $("<input></input>").attr("type", "text");
			setFieldCommonAttributes($input, attribute, form);
			if (attribute.length) {
				$input.attr("maxlength", attribute.length);
			}
			if (value) {
				$input.val(value);
			}
			return $input;
		} else {
			var $input = getHiddenField(form, attribute._id, value);
			var $text = $("<span></span>").text(value ? value : "");
			return [$text, $input];
		}
	};

	/**
	 * @param {String} form
	 * @param {Object} attribute
	 * @param {String} value
	 * @return {jQuery}
	 */
	var getTextField = function(form, attribute, value) {
		var $input = $("<textarea></textarea>");
		setFieldCommonAttributes($input, attribute, form);
		if (value) {
			$input.val(value);
		}

		var isHtml = (attribute.editorType === "HTML") ? true : false;
		if (attribute.writable === true || attribute.writable === "true") {
			var rows = 6;
			if (attribute.rows) {
				rows = attribute.rows;
			}
			$input.attr("rows", rows);
			if (attribute.cols) {
				$input.attr("cols", attribute.cols);
			}
			if (attribute.length) {
				$input.attr("maxlength", attribute.lenght);
			}
			if (isHtml) {
				$("#" + form).on('DOMNodeInserted', function(event) {
					if ($(event.target).find("#" + $input.attr("id")).length) {
						$input.cleditor({
							controls : "bold italic underline | size | color highlight | "
								+ "alignleft center alignright justify | link unlink | "
								+ "bullets numbering | pastetext | removeformat | source"
						});
					}
				});
			}
			return $input;
		} else {
			var $text = $("<div></div>").addClass("textareaReadOnly");
			if (isHtml) {
				$text.html(value);
			} else {
				$text.text(value);
			}
			$input.addClass("hiddentextarea").attr("disabled", "disabled");
			return [$text, $input];
		}
	};

	$.Cmdbuild.fieldsManager = fieldsManager;
}(jQuery));