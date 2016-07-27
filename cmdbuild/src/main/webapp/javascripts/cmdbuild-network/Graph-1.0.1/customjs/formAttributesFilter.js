(function($) {
	var NOGROUP = "_NOGROUP_";
	var GROUPOTHERS = "Others";

	var formAttributesFilter = function() {
		this.config = {};
		this.backend = undefined;
		this.xmlForm = undefined;
		var defaultTranslations = {
			FILTER_EQUALS : "Equals",
			FILTER_DIFFERENT : "Different",
			FILTER_ISNULL : "Is null",
			FILTER_ISNOTNULL : "Is not null",
			FILTER_BEGINSWITH : "Begins with",
			FILTER_ENDSWITH : "Ends with",
			FILTER_CONTAINS : "Contains",
			FILTER_DOESNOTBEGINWITH : "Does not begin with",
			FILTER_DOESNOTENDWITH : "Does not end with",
			FILTER_DOESNOTCONTAIN : "Does not contain",
			FILTER_BETWEEN : "Between",
			FILTER_GREATERTHAN : "Greater than",
			FILTER_LESSTHAN : "Less than",
			FILTER_CONTAINED : "Contained",
			FILTER_CONTAINSOREQUAL : "Contains or equal",
			FILTER_CONTAINEDOREQUAL : "Contained or equal",
			FILTER_RELATION : "Relation"

		};

		this.init = function(param) {
			this.config = param;
			$.Cmdbuild.custom.formAttributesFilter.initFilters();
			if (!$.Cmdbuild.customvariables.selectObservers) {
				$.Cmdbuild.customvariables.selectObservers = [];
			}
			if ($.inArray(this, $.Cmdbuild.customvariables.selectObservers) === -1) {
				$.Cmdbuild.customvariables.selectObservers.push(this);
			}
			// fields to show
			this.xmlForm = $.Cmdbuild.elementsManager
					.getElement(this.config.form);
			try {
				var backendFn = $.Cmdbuild.utilities.getBackend(param.backend);
				var backend = new backendFn(param, this.show, this);
				this.setBackend(backend);
			} catch (e) {
				console.log("WARNING: No data message " + e.message);
				var htmlContainer = $("#" + this.param.container)[0];
				htmlContainer.innerHTML = "<h1>NO DATA</h1>";
			}
		};

		this.show = function() {
			var formId = this.config.form;
			var $container = $("#" + this.config.container);
			$container.empty();
			// create form
			var $form = createForm(this.config.form);
			$container.append($form);

			// create fields
			this.selectValues = {};
			var data = this.getBackendData();
			var attributes = this.getBackendAttributes();
			this.showAttributes(this.config.classId, attributes, data, formId,
					$form);

			// add children tags
			var childrenHtml = $.Cmdbuild.elementsManager
					.insertChildren(this.xmlForm);
			if (childrenHtml) {
				$form.append(childrenHtml);
			}

			// execute on init complete
			if (this.config.onInitComplete) {
				window[this.config.onInitComplete]();
			}
		};
		this.showAttributes = function(classId, attributes, data, formId, $form) {
			for (var i = 0; i < attributes.length; i++) {
				var attribute = attributes[i];
				// create field
				var value = "";
				var $fieldset = $("<fieldset></fieldset>");
				var $legend = $("<legend>" + attribute.description
						+ "</legend>");
				$fieldset.append($legend);
				this.showOperatorsRows(classId, attribute, $fieldset, formId,
						data);
				$form.append($fieldset);
				$.Cmdbuild.scriptsManager.execute();
			}
		};
		this.showOperatorsRows = function(classId, attribute, $fieldset,
				formId, data) {
			var attributeData = data[attribute._id];
			var base_url = $.Cmdbuild.global.getAppConfigUrl()
					+ $.Cmdbuild.g3d.constants.ICONS_PATH;
			var deleteImage = base_url + $.Cmdbuild.g3d.constants.ICON_DELETE;
			for (var i = 0; i < attributeData.length; i++) {
				var row = attributeData[i];
				var selectId = attribute._id + "_operatorsSelect_" + i + "_" + this.config.idSuffix;
				var $button = $("<input></input>").attr('type', 'image').attr(
						"src", deleteImage).attr("indexData", i).on("click",
						deleteAttributeOnClass);
				$button.attr("classId", classId);
				$button.attr("attributeId", attribute._id);
				$button.attr("indexData", i);
				$button.attr("navigationForm", this.config.navigationForm);
				$button.attr("navigationContainer",
						this.config.navigationContainer);
				var $operatorsSelect = $("<select></select>").attr("id",
						selectId);
				this.appendSpecificOperators($operatorsSelect, attribute);
				$operatorsSelect.val(row.operator);
				$fieldset.append($button);
				$fieldset.append($operatorsSelect);
				this.showOperatorsFields(row, attribute, $fieldset, i, formId);
				var operatorsList = $.Cmdbuild.custom.formAttributesFilter
						.getSpecificOperators(attribute);
				var param = {
					script : "selectFilter",
					id : selectId,
					context : this,
					indexData : row,
					navigationForm : this.config.navigationForm,
					navigationContainer : this.config.navigationContainer,
					operatorsList : operatorsList

				};
				$.Cmdbuild.scriptsManager.push(param);
			}
		};
		function deleteAttributeOnClass(ev) {
			var index = ev.currentTarget.getAttribute("indexData");
			var attributeId = ev.currentTarget.getAttribute("attributeId");
			var classId = ev.currentTarget.getAttribute("classId");
			var navigationContainer = ev.currentTarget.getAttribute("navigationContainer");
			var navigationForm = ev.currentTarget.getAttribute("navigationForm");
			var rows = $.Cmdbuild.custom.configuration.temporaryFilterByAttributes[classId][attributeId].data;
			if (rows.length > 1) {
				rows.splice(index, 1);
			} else if (rows.length == 1) {
				delete $.Cmdbuild.custom.configuration.temporaryFilterByAttributes[classId][attributeId];
			}
			if ($
					.isEmptyObject($.Cmdbuild.custom.configuration.temporaryFilterByAttributes[classId])) {
				delete $.Cmdbuild.custom.configuration.temporaryFilterByAttributes[classId];

			}
			$.Cmdbuild.standard.commands.navigate({
				form : navigationForm,
				container : navigationContainer,
				classId : classId
			});
			return false;
		}
		this.showOperatorsFields = function(row, attribute, $fieldset,
				currentAttributeIndex, formId) {
			for (var i = 0; i < row.operator[2]; i++) {
				var currentAttribute = $.Cmdbuild.utilities.clone(attribute);
				currentAttribute._id += "_" + currentAttributeIndex + "_" + i;
				var $field = $.Cmdbuild.fieldsManager.getFormField(
						currentAttribute, formId, this.config.container,
						row.data.firstParameter);
				this.prepareSelect(attribute, $field, row,
						currentAttributeIndex);
				if ($field.change) {
					this.setChange($field, row);
				}
				$fieldset.append($field);
			}
		};
		this.prepareSelect = function(attribute, $field, row, index) {
			if (attribute.type === "lookup" || attribute.type === "reference") {
				var id = ($.isArray($field)) ? $field[0].attr("id") : $field
						.attr("id");
				this.selectValues[id] = row;
			}
		};
		this.changedSelect = function(configuration) {
			var field = $("#" + configuration.id);
			if (this.selectValues[configuration.id]) { // from form
				this.selectValues[configuration.id].data.firstParameter = field
						.val();
			}
		};
		this.setChange = function($field, row) {
			var closureRow = row;
			$field.change(function() {
				closureRow.data.firstParameter = this.value;
			});
		};
		this.setOnChange = function($field, row) {
			var closureRow = row;
			$field[0].onchange(function() {
				closureRow.data.firstParameter = this.value;
			});
		};
		this.appendSpecificOperators = function(jqueryfield, attribute) {
			var options = $.Cmdbuild.custom.formAttributesFilter
					.getSpecificOperators(attribute);
			for (var i = 0; i < options.length; i++) {
				var t = $.Cmdbuild.translations.getTranslation(options[i][1],
						defaultTranslations[options[i][1]]);
				var $option = $("<option></option>").val(options[i][0]).text(t);
				jqueryfield.append($option);
			}
		};
		this.refreshField = function(param) {
			var id = $.Cmdbuild.fieldsManager.getFieldId(param.form,
					param.field);
			/*
			 * ATTENTION: for now only on referenceFields refreshField come from
			 * the cql configurator
			 */
			var $selectMenu = $("#" + id);
			if ($selectMenu.length) {
				$selectMenu.trigger("refreshfield");
			}
		};
		this.getValue = function(param) {
			var id = $.Cmdbuild.fieldsManager.getFieldId(param.form,
					param.field);
			var val = $.Cmdbuild.utilities.getHtmlFieldValue("#" + id);
			if (val === null) {
				val = $.Cmdbuild.dataModel.getValue(param.form, param.field);
			}
			return val;
		};
		this.getBackend = function() {
			return this.backend;
		};
		this.setBackend = function(backend) {
			this.backend = backend;
		};
		this.getBackendAttributes = function() {
			var backend = this.getBackend();
			if (!backend.getAttributes) {
				console.warn("Missing getData method for backend "
						+ this.config.backend);
				return backend.attributes;
			}
			return backend.getAttributes();
		};
		this.getBackendData = function() {
			var backend = this.getBackend();
			if (!backend.getData) {
				console.warn("Missing getData method for backend "
						+ this.config.backend);
				return backend.data;
			}
			return backend.getData();
		};
	};

	$.Cmdbuild.custom.formAttributesFilter = formAttributesFilter;

	function createForm(formName) {
		var form = $("<form></form>").attr("action", "#")
				.attr("name", formName).attr("id", formName);
		return form;
	}
	$.Cmdbuild.custom.formAttributesFilter.getSpecificOperators = function(
			attribute) {
		var fFilters = $.Cmdbuild.custom.formAttributesFilter;
		switch (attribute.type) {
		case "boolean":
			return fFilters.FILTER_TEXTOPERATORS;
		case "reference":
		case "lookup":
			return fFilters.FILTER_LOOKUPOPERATORS;
		default:
			return fFilters.FILTER_TEXTOPERATORS;
		}
	};
	$.Cmdbuild.custom.formAttributesFilter.initFilters = function() {
		// filter, entry for translations, the arguments number
		$.Cmdbuild.custom.formAttributesFilter.FILTER_RANGEOPERATORS = [
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.EQUAL,
						"FILTER_EQUALS", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NULL,
						"FILTER_ISNULL", 0 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_NULL,
						"FILTER_ISNOTNULL", 0 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_EQUAL,
						"FILTER_DIFFERENT", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.GREATER_THAN,
						"FILTER_GREATERTHAN", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.LESS_THAN,
						"FILTER_LESSTHAN", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.BETWEEN,
						"FILTER_BETWEEN", 2 ] ];
		$.Cmdbuild.custom.formAttributesFilter.FILTER_TEXTOPERATORS = [
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.BEGIN,
						"FILTER_BEGINSWITH", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.CONTAIN,
						"FILTER_CONTAINS", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.END,
						"FILTER_ENDSWITH", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.EQUAL,
						"FILTER_EQUALS", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_BEGIN,
						"FILTER_ENDSWITH", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_CONTAIN,
						"FILTER_DOESNOTCONTAIN", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_END,
						"FILTER_DOESNOTENDWITH", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_EQUAL,
						"FILTER_DIFFERENT", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_NULL,
						"FILTER_ISNOTNULL", 0 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NULL,
						"FILTER_ISNULL", 0 ] ];
		$.Cmdbuild.custom.formAttributesFilter.FILTER_LOOKUPOPERATORS = [
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.EQUAL,
						"FILTER_EQUALS", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NULL,
						"FILTER_ISNULL", 0 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_NULL,
						"FILTER_ISNOTNULL", 0 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_EQUAL,
						"FILTER_DIFFERENT", 1 ] ];
		$.Cmdbuild.custom.formAttributesFilter.FILTER_BOOLEANOPERATORS = [
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.EQUAL,
						"FILTER_EQUALS", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NULL,
						"FILTER_ISNULL", 0 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_NULL,
						"FILTER_ISNOTNULL", 0 ] ];
		$.Cmdbuild.custom.formAttributesFilter.FILTER_INETOPERATORS = [
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.EQUAL,
						"FILTER_EQUALS", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NOT_NULL,
						"FILTER_ISNOTNULL", 0 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NET_CONTAINS,
						"FILTER_CONTAINS", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NET_CONTAINED,
						"FILTER_CONTAINED", 1 ],
				[
						$.Cmdbuild.g3d.constants.FILTER_OPERATORS.NET_CONTAINSOREQUAL,
						"FILTER_CONTAINSOREQUAL", 1 ],
				[
						$.Cmdbuild.g3d.constants.FILTER_OPERATORS.NET_CONTAINEDOREQUAL,
						"FILTER_CONTAINEDOREQUAL", 1 ],
				[ $.Cmdbuild.g3d.constants.FILTER_OPERATORS.NET_RELATION,
						"FILTER_RELATION", 2 ] ];
	};

	$.Cmdbuild.custom.formAttributesFilter.getOrs = function(attributeId, rows) {
		var ors = [];
		for (var i = 0; i < rows.length; i++) {
			var row = rows[i];
			ors.push({
				simple : {
					attribute : attributeId,
					operator : row.operator[0],
					value : row.data.firstParameter,
					parameterType : "fixed"
				}
			});
		}
		return ors;
	};
	$.Cmdbuild.custom.formAttributesFilter.getFilters4Classes = function() {
		var classes = $.Cmdbuild.custom.configuration.filterByAttributes;
		var returnObject = {};
		for ( var keyClass in classes) {
			var ors = [];
			var classFilter = {};
			var data = classes[keyClass];
			for ( var key in data) {
				ors.push(this.getOrs(key, data[key].data));
			}
			if (ors.length > 1) {
				classFilter.and = {};
				classFilter.and.or = [];
				for (var i = 0; i < ors.length; i++) {
					classFilter.and.or.push(ors[i]);
				}
			} else if (ors.length === 1) {
				if (ors[0].length > 1) {
					classFilter.or = ors[0];
				} else {
					classFilter = ors[0];
				}
			}
			returnObject[keyClass] = classFilter;
		}
		return returnObject;
	};
	$.Cmdbuild.custom.formAttributesFilter.inFilter = function(classId, filter) {
		var nodes = $.Cmdbuild.customvariables.model
				.getNodesByClassName(classId);
		var ids = [];
		for (var i = 0; i < nodes.length; i++) {
			var cardId = $.Cmdbuild.g3d.Model.getGraphData(nodes[i], "id");
			ids.push(cardId);
		}

		return ids;
	};

})(jQuery);
