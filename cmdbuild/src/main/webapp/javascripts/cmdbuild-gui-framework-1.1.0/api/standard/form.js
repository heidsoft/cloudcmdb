(function($) {
	var NOGROUP = "_NOGROUP_";
	var GROUPOTHERS = "Others";
	
	
	var form = function() {
		this.config = {};
		this.backend = undefined;
		this.customFields = [];
		this.customWidgets = [];
		this.xmlForm = undefined;

		this.init = function(param) {
			this.config = param;

			// fields to show
			this.xmlForm = $.Cmdbuild.elementsManager.getElement(this.config.form);
			this.customFields = $.Cmdbuild.utilities.getFields(this.xmlForm);
			this.customWidgets = $.Cmdbuild.utilities.getWidgetsFromXML(this.xmlForm);

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
			var me = this;

			var formId = this.config.form;
			var attributes = this.getBackendAttributes();
			$.Cmdbuild.dataModel.evaluateJSONAttributes(attributes, this.customFields);

			// compile CQL
			$.Cmdbuild.CqlManager.compile(formId, attributes);

			// generate empty data
			if (this.config.emptyForm && this.config.emptyForm == "true") {
				var newData = getEmptyData(attributes);
				this.setBackendData(newData);
			}

			// update data model
			$.Cmdbuild.dataModel.push({
				form : this.config.form,
				type : "form",
				data : this.getBackendData()
			});

			// get container
			var $container = $("#" + this.config.container);
			$container.empty();
			// create form
			var $form = createForm(this.config.form);
			$container.append($form);

			// create tabs
			var tabs;
			var showTabs = false;
			if (this.config.noGroup && this.config.noGroup == "true") {
				tabs = [NOGROUP];
			} else {
				tabs = getFormTabs(attributes);
				if (tabs.length > 1) {
					showTabs = true;
					$form.append(createTabs(formId, tabs));
				}
			}

			// create fields
			var data = this.getBackendData();
			var formValidator = {
				onsubmit : false,
				ignore : "input:hidden, textarea:hidden",
				rules : {},
				messages : {},
				errorPlacement : function(error, element) {
					error.insertBefore(element);
					element.parents("dl").addClass("cmdbuildGuiFormFieldError");
				},
				success : function(label) {
					label.parents("dl").removeClass("cmdbuildGuiFormFieldError");
				},
			};
			$.each(attributes, function(index, attribute) {
				var value = data[attribute._id];
				// readonly forms
				if (me.config.readonly && me.config.readonly == "true") {
					attribute.writable = false;
				}
				// create field
				var $field = $.Cmdbuild.fieldsManager.generateHTML(attribute, formId, me.config.container, value);
				var validator = $.Cmdbuild.fieldsManager.getFieldValidator(attribute);
				if (validator && validator.rules && !$.isEmptyObject(validator.rules)) {
					formValidator.rules[attribute._id] = validator.rules;
				}
				if (showTabs) {
					var tabId = me.getTabId(formId, attribute.group);
					$("#"+tabId).append($field);
				} else {
					$form.append($field);
				}
			});
			$form.validate(formValidator);

			// get widgets
			this.updateWidgets();
			var widgets = this.getBackendWidgets();
			if (widgets && widgets.length) {
				// prepare widgets
				$.Cmdbuild.widgets.prepareFields(widgets);
				// get widget html
				var htmlStr = $.Cmdbuild.standard.widgetDiv.getWidgetDiv({
					container: this.config.container,
					form: this.config.form,
					readOnly: this.config.readonly,
					widgets: widgets
				});
				// transform input buttons in jQuery UI buttons
				var $htmlStr = $(htmlStr);
				$htmlStr.find("input[type=button]").button();
				// remove dialogs from html
				var dialogs = $htmlStr.filter("div[id$='_widgetDialog']").remove();
				// append html
				$form.append($htmlStr);
				// execute dialogs
				dialogs.dialog({
					autoOpen: false,
					modal: true,
					show: {
						effect: "fade",
						duration: 250
					},
					hide: {
						effect: "explode",
						duration: 500
					}
				});
				// evaluate cql fields
				$.Cmdbuild.widgets.evaluateCqlFields(this.config.form, widgets, true);
				// initialize widgets
				$.Cmdbuild.widgets.initialize(this.config.form, widgets);
			}

			// create form buttons
			var buttonsHtml = $.Cmdbuild.utilities.getButtonsFromXML(this.xmlForm);
			if (buttonsHtml) {
				var $buttonsHtml = $(buttonsHtml);
				$buttonsHtml.find("input[type=button]").button();
				$form.append($buttonsHtml);
			}

			// add children tags
			var childrenHtml = $.Cmdbuild.elementsManager.insertChildren(this.xmlForm);
			if (childrenHtml) {
				$form.append(childrenHtml);
			}

			// execute on init complete
			if (this.config.onInitComplete) {
				window[this.config.onInitComplete]();
			}
		};

		// make public private method
		this.getTabId = getTabId;

		/**
		 * Refresh field
		 * @param {Object} param
		 * @param {String} param.form
		 * @param {String} param.field
		 */
		this.refreshField = function(param) {
			var id = $.Cmdbuild.fieldsManager.getFieldId(param.form, param.field);
			/*
			 * ATTENTION: for now only on referenceFields 
			 * refreshField come from the cql configurator
			 */
			var $selectMenu = $("#" + id);
			if ($selectMenu.length) {
				$selectMenu.trigger("refreshfield");
			}
			else {
				$.Cmdbuild.widgets.refreshCqlField(param.form, this.getBackend().widgets);
			}
		};

		/**
		 * Get field value
		 * @param {String} param.form
		 * @param {String} param.field
		 * @return {*} Field value
		 */
		this.getValue = function(param) {
			var id = $.Cmdbuild.fieldsManager.getFieldId(param.form, param.field);
			var val = $.Cmdbuild.utilities.getHtmlFieldValue("#" + id);
			if (val === null) {
				val = $.Cmdbuild.dataModel.getValue(param.form, param.field);
			}
			return val;
		};

		/**
		 * Flush
		 */
		this.flush = function(param, callback, callbackScope) {
			this.getBackend().updateData(param, callback, callbackScope);
		};

		/**
		 * Update form data and validate
		 */
		this.change = function(param) {
			var errors = [];
			var $form = $("#" + this.config.form);
			var attributes = this.getBackendAttributes();
			// validate form
			if (!$form.valid()) {
				var formErrors = $form.validate().errorList;
				$.each(formErrors, function(index, error) {
					var attributeId = $(error.element).attr("name");
					var attrList = $.grep(attributes, function(a) {
						return a._id == attributeId;
					})
					var attribute = attrList.length > 0 ? attrList[0] : null;
					if (attribute) {
						errors.push({
							field : attribute.description,
							message : error.message
						});
					}
				});
			} else {
				$.each(attributes, function(index, attribute) {
					var id = $.Cmdbuild.fieldsManager.getFieldId(param.form, attribute._id);
					param.data[attribute._id] = $.Cmdbuild.utilities.getHtmlFieldValue("#" + id);
				});
			}
			return errors;
		};

		/**
		 * Reset form
		 */
		this.reset = function(param) {
			for (var key in param.data) {
				var id = $.Cmdbuild.fieldsManager.getFieldId(param.form, key);
				var field = $("#" + id);
				if (field) {
					field.val(param.data[key]);
				}
			}
		};

		/**
		 * This method is as was before refactoring
		 */
		this.getClientDescription = function(param, callback, callbackScope) {
			var attributes = this.getBackendAttributes();
			var value = this.getValue(param);
			this.getDescription(param, attributes, value, function(response) {
				callback.apply(callbackScope, [response]);
			}, this);
		};
		/**
		 * This method is as was before refactoring
		 */
		this.getServerDescription = function(param, callback, callbackScope) {
			if (! this.getBackend().getOriginalAttributes) {
				console.log("Backend without original attributes on form: " + param.form, this.getBackend());
				callback.apply(callbackScope, [""]);
				return;
			}
			var attributes = this.getBackend().getOriginalAttributes();
			var value = $.Cmdbuild.dataModel.getValue(param.form, param.field);
			this.getDescription(param, attributes, value, function(response) {
				callback.apply(callbackScope, [response]);
			}, this);
		};
		/**
		 * This method is as was before refactoring
		 */
		this.getDescription = function(param, attributes, value, callback, callbackScope) {
			var attribute = undefined;
			for (var i = 0; i < attributes.length; i++) {
				if (attributes[i].name == param.field) {
					attribute = attributes[i];
					break;
				}
			}
			if (attribute && value) {
				$.Cmdbuild.utilities.getFieldDescription(attribute, value, function(response) {
					callback.apply(callbackScope, [response]);
				});
			} else {
				callback.apply(callbackScope, [value || ""]);
			}
		};

		/**
		 * Update widgets merge backend widgets with xml widgets
		 */
		this.updateWidgets = function() {
			var widgets = this.getBackendWidgets();
			if (this.customWidgets && this.customWidgets.length) {
				if (widgets === undefined) {
					widgets = [];
				}
				$.each(this.customWidgets, function(index, widget) {
					widgets.push(widget);
				});
				this.setBackendWidgets(widgets);
			}
		};

		/*
		 * Getters and Setters
		 */
		/**
		 * Get the backend object used for this form
		 * @return {Object} The backend object
		 */
		this.getBackend = function() {
			return this.backend;
		};
		/**
		 * Set backend object for this form
		 * @param {Object} backend The backend object
		 */
		this.setBackend = function(backend) {
			this.backend = backend;
		};
		/**
		 * Return data from the backend
		 * @return {Object} Backend data
		 */
		this.getBackendData = function() {
			var backend = this.getBackend();
			if (!backend.getData) {
				console.warn("Missing getData method for backend " + this.config.backend);
				return backend.data;
			}
			return backend.getData();
		};
		/**
		 * Set data into the backend
		 * @param {Object} data New data
		 */
		this.setBackendData = function(data) {
			var backend = this.getBackend();
			if (!backend.setData) {
				console.warn("Missing setData method for backend " + this.config.backend);
				backend.data = data;
			} else {
				backend.setData(data);
			}
		};
		/**
		 * Return attributes from the backend
		 * @return {Array} Backend attributes
		 */
		this.getBackendAttributes = function() {
			var backend = this.getBackend();
			if (!backend.getAttributes) {
				console.warn("Missing getAttributes method for backend " + this.config.backend);
				return backend.attributes;
			}
			return backend.getAttributes();
		};
		/**
		 * Set attributes into the backend
		 * @param {Array} attributes New attributes
		 */
		this.setBackendAttributes = function(attributes) {
			var backend = this.getBackend();
			if (!backend.setAttributes) {
				console.warn("Missing setAttributes method for backend " + this.config.backend);
				backend.attributes = attributes;
			} else {
				backend.setAttributes(attributes);
			}
		};
		/**
		 * Return widgets from the backend
		 * @return {Array} Backend widgets
		 */
		this.getBackendWidgets = function() {
			var backend = this.getBackend();
			if (!backend.getWidgets) {
				console.warn("Missing getWidgets method for backend " + this.config.backend);
				return backend.widgets;
			}
			return backend.getWidgets();
		};
		/**
		 * Set widgets into the backend
		 * @param {Array} data New widgets
		 */
		this.setBackendWidgets = function(widgets) {
			var backend = this.getBackend();
			if (!backend.setWidgets) {
				console.warn("Missing setWidgets method for backend " + this.config.backend);
				backend.widgets = widgets;
			} else {
				backend.setWidgets(widgets);
			}
		};
	};

	/**
	 * @param {Array} attributes Attributes list
	 * @return {Object} Empty data from attributes
	 */
	function getEmptyData (attributes) {
		var newData = {};
		$.each(attributes, function(index, attribute) {
			if (attribute.defaultValue) {
				newData[attribute.name] = attribute.defaultValue;
			} else {
				newData[attribute.name] = undefined;
			}
		});
		return newData;
	};

	/**
	 * @param {Array} attributes Attributes list
	 * @return {Array} list of tabs
	 */
	function getFormTabs (attributes) {
		var tabs = []
		attributes.filter(function(a) {
			var group = a.group;
			if (group && group.trim()) {
				group.trim();
			} else {
				group = NOGROUP;
			}
			return tabs.indexOf(group) == -1 && tabs.push(group)
		});
		// move NOGROUP as last item
		if (tabs.indexOf(NOGROUP) !== -1) {
			var old_index = tabs.indexOf(NOGROUP);
			var new_index = tabs.length - 1;
			tabs = moveArrayItem(tabs, old_index, new_index);
		}
		return tabs;
	};

	/**
	 * @param {String} formName
	 * @return {jQuery} form object
	 */
	function createForm (formName) {
		var form = $("<form></form>").attr("action", "#")
				.attr("name", formName).attr("id", formName);
		return form;
	};

	/**
	 * @param {String} formName
	 * @param {Array} tabs
	 * @return {jQuery} Tabs container
	 */
	function createTabs (formName, tabs) {
		var $container = $("<div></div>").attr("id", formName + "_tabbed");
		// create navigation and append to container
		var $navigation = $("<ul></ul>");
		$container.append($navigation);
		// generate tabs
		$.each(tabs, function(index, tab) {
			var $navItem = $("<li></li>");
			var tabName = tab;
			if (tabName === NOGROUP) {
				// TODO: translations
				tabName = "Other";
			}
			var $navItemLink = $("<a></a>").attr("href", "#" + getTabId(formName, tab)).text(tabName);
			$navigation.append($navItem.append($navItemLink));
			var $tabContent = $("<div></div>").attr("id", getTabId(formName, tab));
			$container.append($tabContent);
		})
		$container.tabs();
		return $container;
	};

	/**
	 * @param {String} formName
	 * @param {String} tabName
	 * @return {String}
	 */
	function getTabId (formName, tabName) {
		tabName = tabName ? tabName : NOGROUP;
		// remove special characters
		tabName = tabName.replace(/[^a-z0-9\s]/gi, '').replace(/[_\s]/g, '-').toLowerCase();
		return formName + "-tab-" + tabName;
	};

	/**
	 * @param {Array} array
	 * @param {Integer} old_index
	 * @param {Integer} new_index
	 * @return {Array} new array
	 */
	function moveArrayItem (array, old_index, new_index) {
		while (old_index < 0) {
			old_index += array.length;
		}
		while (new_index < 0) {
			new_index += array.length;
		}
		if (new_index >= array.length) {
			var k = new_index - array.length;
			while ((k--) + 1) {
				array.push(undefined);
			}
		}
		array.splice(new_index, 0, array.splice(old_index, 1)[0]);
		return array; // for testing purposes
	};

	$.Cmdbuild.standard.form = form;
})(jQuery);
