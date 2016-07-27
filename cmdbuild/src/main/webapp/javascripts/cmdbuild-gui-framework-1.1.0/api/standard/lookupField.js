(function($) {
	var lookupField = function(param) {
		this.param = param;

		this.lookupTree = {};
		this.lookupValue = {};

		this.init = function() {
			if (!this.param.lookupType && this.param.lookupName) {
				// @deprecated
				this.param.lookupType = this.param.lookupName;
				console.warn("lookupName parameter is deprecated. Use lookupType.");
			} else if (!this.param.lookupType && !this.param.lookupName) {
				$.Cmdbuild.errorsManager.error("No lookup type specified");
				return;
			}

			if (this.param.value) {
				this.lookupValue[this.param.lookupType] = this.param.value;
			}
			this.generateLookupTypeTree(this.param.lookupType);
		};

		this.generateLookupTypeTree = function(type, childType) {
			var me = this;
			$.Cmdbuild.utilities.proxy.getLookupType(type, {}, function(tData, tMetadata) {
				var tree = {type: tData.name, values: []};
				if (tData.parent) {
					tree.parent = tData.parent;
				}
				if (childType) {
					tree.child = childType;
				}

				// load values
				$.Cmdbuild.utilities.proxy.getLookupData(type, {active: true}, function(vData, vMetadata){
					tree.values = vData;

					// update tree
					me.lookupTree[tData.name] = tree;

					// update values tree
					if (tData.parent && me.lookupValue[tData.name]) {
						var selectedValue = me.lookupValue[tData.name];
						// get selected lookup value
						var lookupvalue = $.grep(vData, function(e) {
							return e._id == selectedValue;
						});
						if (lookupvalue.length) {
							me.lookupValue[lookupvalue[0].parent_type] = lookupvalue[0].parent_id;
						}
					}

					// iterate
					if (tData.parent) {
						me.generateLookupTypeTree(tData.parent, tData.name);
					} else {
						me.generateHTML();
					}
				}, this);

			}, this);
		};

		this.generateHTML = function() {
			var selectfield = $("#" + this.param.id);
			var lookupType = this.param.lookupType;

			if (this.param.readOnly) {
				this.showReadOnlyLookup(lookupType);
				return;
			}
			if (! this.lookupTree[lookupType]) {
				console.log("NOT FOUND : this.lookupTree[" + lookupType + "]");
				return;
			}
			if (! this.lookupTree[lookupType].parent) {
				this.addOptions(selectfield, this.lookupTree[lookupType].values);
			} else {
				// add attributes to selection
				selectfield.attr("rel", this._getRelValueByType(lookupType));
				selectfield.selectmenu("widget").addClass("selectmenu-newrow");

				if (this.lookupValue[lookupType]) {
					var parent_type = this.lookupTree[lookupType].parent;
					var parent_id = this.lookupValue[parent_type];
					this.updateChildSelect(lookupType, parent_id);
				}

				this.addParentSelect(this.lookupTree[this.param.lookupType].parent, selectfield);
			}
		};

		this.addOptions = function(selectfield, values) {
			// empty select
			selectfield.empty();
			// add options
			var me = this;

			// add empty option
			var emptyOpt = $("<option>");
			selectfield.append(emptyOpt);

			// add options
			$.each(values, function(index, value) {
				var opt = $("<option>").attr("value", value._id);
				opt.text(value.description);
				if (me.lookupValue[value._type] == value._id) {
					opt.attr("selected", "selected");
				}
				selectfield.append(opt);
			});

			// refresh select menu
			selectfield.selectmenu("refresh");
		};

		this.addParentSelect = function(type, $childSelect) {
			var me = this;
			var lookup = this.lookupTree[type];

			// create select
			var $select = $("<select>");
			$select.attr("rel", this._getRelValueByType(type));

			// add select to DOM and activate selectmenu 
			$childSelect.before($select);
			$childSelect.before("<div></div>");
			$select.selectmenu({
				change: function(event, data) {
					me.updateChildSelect(lookup.child, data.item.value);
				}
			});

			if (!lookup.parent) {
				this.addOptions($select, lookup.values);
			} else {
				selectfield.selectmenu().addClass("selectmenu-newrow");
			}
		};
		this.updateChildSelect = function(child, parentId) {
			var lookup = this.lookupTree[child];
			var values = [];
			var $child = $("select[rel='" + this._getRelValueByType(child) + "']");
			$.each(lookup.values, function(index, value){
				if (value.parent_id == parentId) {
					values.push(value);
				}
			});
			this.addOptions($child, values);
		};
		this.showReadOnlyLookup = function(type) {
			if (!$.isEmptyObject(this.lookupValue)) {
				var values = this.lookupTree[type].values;
				var selectedId = this.lookupValue[type];
				var lookup = this.getSelectedItem(values, selectedId);
				// add html
				var $input = $("#" + this.param.id);
				if (lookup) {
					$input.after(lookup.description);

					// parent description
					if (lookup.parent_type) {
						$input.after(" / ");
						this.showReadOnlyLookup(lookup.parent_type);
					}
				}
			}
		};
		this.getSelectedItem = function(values, id) {
			var selectedItem;
			$.each(values, function(index, value) {
				if (value._id == id) {
					selectedItem = value;
				}
			});
			return selectedItem;
		};

		/**
		 * Private Methods
		 */
		this._getRelValueByType = function(type) {
			return this.param.id + type;
		};

		this.init();
	};
	$.Cmdbuild.standard.lookupField = lookupField;
	// statics
	$.Cmdbuild.standard.lookupField.setValue = function(id, value) {
		var selectMenu = $("#" + id);
		selectMenu.val("");
		selectMenu.selectmenu("refresh");
	};
	$.Cmdbuild.standard.lookupField.clearValue = function(id) {
		var selectMenu = $("#" + id);
		selectMenu.val("");
		selectMenu.selectmenu("refresh");
	};
}) (jQuery);
