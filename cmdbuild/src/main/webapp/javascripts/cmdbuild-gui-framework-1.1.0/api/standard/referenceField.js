(function($) {
	var TARGET_CLASS_BACKEND = "CardList";
	var TARGET_PROCESS_BACKEND = "ReferenceActivityList";

	/**
	 * @param {Object} config
	 * @param {String} config.id
	 * @param {String} config.targetClass
	 * @param {String} config.targetType
	 * @param {Object} config.filter
	 * @param {Boolean} config.readOnly
	 * @param {Integer} config.optionsLimit Max number of options within the select
	 * @param {*} config.value
	 * @param {String} config.displayAttribute
	 */
	var referenceField = function(config) {
		this.config = config;
		this.backend = undefined;
		
		this.init = function() {
			if (!this.config.displayAttribute) {
				this.config.displayAttribute = "Description";
			}
			if (!this.config.optionsLimit) {
				this.config.optionsLimit = $.Cmdbuild.global.getMaxLookupEntries();
			}
			var backendName = getBackendName(this.config.targetType, this.config.targetClass);

			// backend
			var backendFn = $.Cmdbuild.utilities.getBackend(backendName);
			var params = {
				className : this.config.targetClass
			};
			this.setBackend(new backendFn(params, this.initReference, this));
		};

		this.initReference = function() {
			var me = this;

			// add dialog
			var $selectMenu = $("#" + this.config.id);
			addDialog(this.config.id + "_lookupDialog");
			
			// create dialog form
			$.Cmdbuild.standard.elements.lookupDialog(this.config.id, {
				formName : $selectMenu.attr("form"),
				fieldName : $selectMenu.attr("name"),
				className : this.config.targetClass,
				backend : getBackendName(this.config.targetType, this.config.targetClass),
				toExecCommand : this.config.id,
				bReference : "",
				container : this.config.container
			});

			// add event listeners
			$selectMenu.on({
				clearreference : function(event) {
					me.clear();
				},
				searchreferenceitem : function(event) {
					var id = me.config.id;
					$.Cmdbuild.standard.commands.navigate({
						form : id + "_dialog",
						dialog : id + "_lookupDialog",
						// TODO: translate
						title : "Reference values",
						width : "90%"
					});
				},
				itemselectedfromgrid : function(event) {
					var new_value = $.Cmdbuild.dataModel.getValue(me.config.id
							+ "_dialogGrid", "_id");
					me.updateValue(new_value);
				},
				refreshfield : function(event) {
					me.loadReference();
				}
			});

			this.loadReference();
		};

		/**
		 * Init reference
		 */
		this.loadReference = function() {
			var me = this;
			if (this.config.readOnly) {
				this.loadReadOnlyReference();
				return;
			}

			var $selectMenu = $("#" + this.config.id);
			var formName = $selectMenu.attr("form");
			var fieldName = $selectMenu.attr("name");
			$.Cmdbuild.CqlManager.resolve(formName, fieldName, function(filter) {
				/*
				 * if filter is undefined or there is an error or 
				 * a field present in the filter is not valorized
				 */
				if (filter == undefined || $.Cmdbuild.CqlManager.isUndefined(filter)) {
					me.addEmptyOption();
					return;
				}
				if (filter) {
					me.config.filter = {
							CQL: filter
					};
					me.getBackend().filter = this.config.filter;
				}
				me.loadData();
			}, this);
		};

		/**
		 * Load read-only reference
		 */
		this.loadReadOnlyReference = function() {
			var me = this;
			if (this.config.value) {
				var $input = $("#" + this.config.id);
				var $span = $("<span></span>").addClass("referenceLabel");
				$input.after($span);
				// load reference item data
				if (isClass(this.config.targetType, this.config.targetClass)) {
					$.Cmdbuild.utilities.proxy.getCardData(this.config.targetClass, this.config.value, {}, function(data, metadata) {
						$span.text(data[me.config.displayAttribute]);
					}, this);
				} else {
					$.Cmdbuild.utilities.proxy.getCardProcess(this.config.targetClass, this.config.value, {}, function(data, metadata) {
						$span.text(data[me.config.displayAttribute]);
					}, this);
				}
			}
		};

		/**
		 * Add empty option
		 */
		this.addEmptyOption = function() {
			// get select and empty
			var $selectMenu = $("#" + this.config.id);
			$selectMenu.empty();

			// append options
			var $option = $("<option></option>").attr("selected", "selected");
			$selectMenu.append($option);

			// set value
			$selectMenu.val("");
			this.fieldChanged();
		};

		/**
		 * Add options from backend data
		 */
		this.addOptionsFromBackend = function() {
			var me = this;
			// get select and empty
			var $selectMenu = $("#" + this.config.id);
			$selectMenu.empty();

			// append options
			$selectMenu.append($("<option></option>"));
			$.each(this.getBackend().getData(), function(index, item) {
				var $option = $("<option></option>").attr("value", item._id).text(item[me.config.displayAttribute]);
				if (item._id == me.config.value){
					$option.attr("selected", "selected");
					$selectMenu.val(me.config.value);
				}
				$selectMenu.append($option);
			});

			// set value
			this.fieldChanged();
		};

		/**
		 * Add option for current value
		 */
		this.addOptionForCurrentValue = function() {
			var me = this;
			// get select and empty
			var $selectMenu = $("#" + this.config.id);
			$selectMenu.empty();

			if (isClass(this.config.targetType, this.config.targetClass)) {
				$.Cmdbuild.utilities.proxy.getCardData(this.config.targetClass, this.config.value, {}, function(data, metadata) {
					// append options
					$selectMenu.append($("<option></option>").attr("value", data._id).text(data[me.config.displayAttribute]));
					$selectMenu.val(data._id);

					// set value
					me.fieldChanged();
				}, this);
			} else {
				$.Cmdbuild.utilities.proxy.getCardProcess(this.config.targetClass, this.config.value, {}, function(data, metadata) {
					// append options
					$selectMenu.append($("<option></option>").attr("value", data._id).text(data[me.config.displayAttribute]));
					$selectMenu.val(data._id);

					// set value
					me.fieldChanged();
				}, this);
			}
		};

		/**
		 * Propagate field changed event
		 */
		this.fieldChanged = function() {
			if ($.Cmdbuild.custom.commands && $.Cmdbuild.custom.commands.fieldChanged) {
				$.Cmdbuild.custom.commands.fieldChanged(this.config);
			} else {
				$.Cmdbuild.standard.commands.fieldChanged(this.config);
			}
			$("#" + this.config.id).selectmenu("refresh");
		};

		/**
		 * Load reference items
		 */
		this.loadData = function() {
			var me = this;
			var config = {
				page : 0,
				start : 0,
				nRows : me.config.optionsLimit
			};
			this.getBackend().loadData(config, this.showReference, this);
		};

		/**
		 * Show reference options
		 */
		this.showReference = function(data, metadata) {
			var me = this;
			var $selectMenu = $("#" + this.config.id);
			var showOptions = this.getBackend().getTotalRows() < this.config.optionsLimit;
			if (showOptions) {
				this.addOptionsFromBackend();
			} else if (this.config.value) {
				this.addOptionForCurrentValue();
			} else {
				this.addEmptyOption();
			}
			if ( ! showOptions) {
				$selectMenu.selectmenu({
					open: function( event, ui ) {
						$(event.target).trigger("searchreferenceitem");
					}
				});
			}
		};

		this.updateValue = function(new_value) {
			var me = this;
			this.config.value = new_value;
			if (this.getBackend().getTotalRows() < this.config.optionsLimit) {
				$("#" + me.config.id).val(new_value);
				me.fieldChanged();
			} else {
				me.addOptionForCurrentValue()
			}
		};

		/**
		 * Clear reference field
		 */
		this.clear = function() {
			var $selectMenu = $("#" + this.config.id);
			var showOptions = this.getBackend().getTotalRows() < this.config.optionsLimit;
			if (! showOptions) {
				this.addEmptyOption();
			} else {
				$selectMenu.val("");
				this.fieldChanged();
			}
		};

		/**
		 * @return {Backend} 
		 */
		this.getBackend = function() {
			return this.backend;
		}
		/**
		 * @param {Backend} backend 
		 */
		this.setBackend = function(backend) {
			this.backend = backend;
		}
		
		this.init();
	};
	$.Cmdbuild.standard.referenceField = referenceField;

	/**
	 * @param {String} targetType
	 * @param {String} targetClass
	 * @return {Boolean}
	 */
	var isClass = function(targetType, targetClass) {
		return (targetType && targetType === "class") || (!targetType && $.Cmdbuild.dataModel.isAClass(targetClass));
	}
	/**
	 * @param {String} targetType
	 * @param {String} targetClass
	 * @return {Boolean}
	 */
	var isProcess = function(targetType, targetClass) {
		return (targetType && targetType === "process") || (!targetType && $.Cmdbuild.dataModel.isAClass(targetClass));
	}

	/**
	 * @param {String} targetType
	 * @param {String} targetClass
	 * @return {String}
	 */
	var getBackendName = function(targetType, targetClass) {
		if (isClass(targetType, targetClass)) {
			return TARGET_CLASS_BACKEND;
		} else if (isProcess(targetType, targetClass)) {
			return TARGET_PROCESS_BACKEND;
		} 
	};

	var addDialog = function(dialogId) {
		var $div = $("<div></div>").attr("id", dialogId);
		$div.dialog({
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
	}

}) (jQuery);
