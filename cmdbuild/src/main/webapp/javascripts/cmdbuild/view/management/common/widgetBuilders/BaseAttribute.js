(function() {
	/**
	 * @Class CMDBuild.WidgetBuilders.BaseAttribute
	 * Abstract class to define the interface of the CMDBuild attributes
	 **/
	Ext.ns("CMDBuild.WidgetBuilders");
	CMDBuild.WidgetBuilders.BaseAttribute = function () {};

	CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator = {
		EQUAL: "equal",
		NOT_EQUAL: "notequal",
		NULL: "isnull",
		NOT_NULL: "isnotnull",
		GREATER_THAN: "greater",
		LESS_THAN: "less",
		BETWEEN: "between",
		LIKE: "like",
		CONTAIN: "contain",
		NOT_CONTAIN: "notcontain",
		BEGIN: "begin",
		NOT_BEGIN: "notbegin",
		END: "end",
		NOT_END: "notend",

		NET_CONTAINS: "net_contains",
		NET_CONTAINED: "net_contained",
		NET_CONTAINSOREQUAL: "net_containsorequal",
		NET_CONTAINEDOREQUAL: "net_containedorequal",
		NET_RELATION: "net_relation"
	};

	CMDBuild.WidgetBuilders.BaseAttribute.prototype = {
		/**
		 * This template method return a combo-box with the options given
		 * by the getQueryOptions method that must be implemented in the subclasses
		 * @param attribute
		 * @return Ext.form.ComboBox
		 */
		getQueryCombo: function(attribute) {
			var store = new Ext.data.SimpleStore({
				fields: ['id','type'],
				data: this.getQueryOptions()
			});

			return new Ext.form.ComboBox({
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				fieldLabel: attribute.description,
				labelSeparator: "",
				hideLabel: true,
				name: attribute.name + "_ftype",
				queryMode: 'local',
				store: store,
				value: this.getDefaultValueForQueryCombo(),
				valueField: 'id',
				displayField: 'type',
				triggerAction: 'all',
				forceSelection: true,
				allowBlank: true,
				width: 130
			});

		},

		getDefaultValueForQueryCombo: function() {
			return CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator.EQUAL;
		},

		/**
		 * The implementation must return an array to use as data of the store of the query combo
		 * The query combo is the combo-box in the attribute section of the Search window with the
		 * filtering options
		 */
		getQueryOptions: function() {
			throw new Error('not implemented');
		},

		/**
		 * Template method, call the buildAttributeField method that must be implemented in the subclass
		 * @return Ext.form.Field or a subclass in order with the specific attribute
		 */
		buildField: function(attribute, hideLabel) {
			var field = this.buildAttributeField(attribute);
			field.hideLabel = hideLabel;
			return this.markAsRequired(field, attribute);
		},

		buildAttributeField: function() {
			throw new Error('not implemented');
		},

		/**
		 * service function to add an asterisk before the label of a required attribute
		 */
		markAsRequired: function(field, attribute) {
			if (attribute.isnotnull || attribute.fieldmode == "required") {
				field.allowBlank = false;
				if (field.fieldLabel) {
					field.fieldLabel = '* ' + field.fieldLabel;
				}
			}
			return field;
		},

		/**
		 * @return Ext.form.DisplayField
		 */
		buildReadOnlyField: function(attribute) {
			var field = new Ext.form.DisplayField({
				allowBlank: true,
				labelAlign: "right",
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				fieldLabel: attribute.description || attribute.name,
				width: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
				submitValue: false,
				name: attribute.name,
				disabled: false,

				/**
				 * Validate also display field
				 *
				 * @override
				 */
				isValid: function() {
					if (this.allowBlank)
						return true;

					return !Ext.isEmpty(this.getValue());
				}
			});
			return this.markAsRequired(field, attribute);
		},
		/**
		 * The implementation must return a configuration object for the header of a Ext.GridPanel
		 */
		buildGridHeader: function(attribute) {
			throw new Error('not implemented');
		},
		/***
		 *
		 * @param attribute
		 * @return a Ext.form.field.* used for the attribute in the grid
		 */
		buildCellEditor: function(attribute) {
			return CMDBuild.Management.FieldManager.getFieldForAttr(attribute, readOnly = false);
		},

		/**
		 * This method prepare some variable and call the method buildFieldsetForFilter to have the fieldset in the subclass is possible override buildFieldsetForFilter
		 * to build a different fieldset.
		 *
		 * @param {Object} attribute
		 *
		 * @return {CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel}
		 */
		getFieldSetForFilter: function(attribute) {
			var attributeCopy = Ext.apply({}, {
				fieldmode: 'write', // Change the fieldmode because in the filter must write on this field
				name: attribute.name
			}, attribute);

			var field = this.buildField(attributeCopy, true);
			var conditionCombo = this.getQueryCombo(attributeCopy);

			return this.buildFieldsetForFilter(field, conditionCombo, attributeCopy);
		},

		/**
		 * Build a fieldSet with a combo-box and a field to edit a filtering criteria used in the attribute section of the filter.
		 *
		 * @param {Object} field
		 * @param {Ext.form.field.ComboBox} query
		 * @param {Object} attribute
		 *
		 * @return {CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel}
		 */
		buildFieldsetForFilter: function(field, query, attribute) {
			return this.genericBuildFieldsetForFilter([field], query, attribute);
		},

		/**
		 * @param {Array} fields - Array of fields to display
		 * @param {Ext.form.field.ComboBox} query
		 * @param {Object} attribute
		 *
		 * @return {CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel}
		 *
		 * @protected
		 */
		genericBuildFieldsetForFilter: function(fields, query, attribute) {
			return Ext.create('CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel', {
				selectAtRuntimeCheckDisabled: attribute.selectAtRuntimeCheckDisabled,
				attributeName: attribute.name,
				conditionCombo: query,
				valueFields: fields
			});
		}
	};

	Ext.define('CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanelDelegate', {
		/**
		 * The condition panel to remove
		 *
		 * @param {CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel} condition
		 */
		onFilterAttributeConditionPanelRemoveButtonClick: Ext.emptyFn
	});

	Ext.define('CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel', {
		extend: 'Ext.container.Container',

		mixins: {
			delegable: 'CMDBuild.core.CMDelegable'
		},

		defaults: {
			margins:'0 5 0 0'
		},

		layout: {
			type: 'hbox',
			pack: 'start',
			align: 'top'
		},

		hideMode: 'offsets',

		// Configuration
			attributeName: '',
			conditionCombo: '',
			valueFields: [],
		// END: Configuration

		selectAtRuntimeCheckDisabled: true, // Flag to enable/disable selectAtRuntime checkbox

		constructor: function() {
			this.mixins.delegable.constructor.call(this, 'CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanelDelegate');

			this.callParent(arguments);
		},

		initComponent: function() {
			var me = this;

			this.removeFieldButton = Ext.create('Ext.button.Button', {
				iconCls: 'delete',
				handler: function() {
					me.callDelegates('onFilterAttributeConditionPanelRemoveButtonClick', [me]);
				}
			});

			this.selectAtRuntimeCheck = Ext.create('Ext.form.field.Checkbox', {
				boxLabel: CMDBuild.Translation.setLater,
				handler: function(checkbox, setValueAtRuntime) {
					// If the user choose to set the value at runtime, disable the valueFilds to say back to the user that the value fields are not considered
					for (var i = 0; i <  me.valueFields.length; ++i) {
						var field = me.valueFields[i];

						if (field) {
							if (setValueAtRuntime) {
								field.disable();
							} else { // Set the value of the condition combo to enable only the value fields that are needed for the current operator
								me.conditionCombo.setValue(me.conditionCombo.getValue());
							}
						}
					}
				}
			});

			Ext.apply(this, {
				items: [
					this.removeFieldButton,
					this.conditionCombo
				]
			});

			this.callParent(arguments);

			this.add(this.valueFields);

			if (!this.selectAtRuntimeCheckDisabled)
				this.add(this.selectAtRuntimeCheck);

			this.onConditionComboSelectStrategy = buildOnConditionComboSelectStrategy(this.valueFields);

			this.conditionCombo.setValue = Ext.Function.createSequence(this.conditionCombo.setValue, function(value) {
				// If the user wanna select at runtime the values, the fields are disabled, so do nothing
				if (!me.selectAtRuntimeCheck.getValue())
					me.onConditionComboSelectStrategy.run(this.getValue());
			}, this.conditionCombo);
		},

		showOr: function(){
			if (!this.orPanel)
				this.orPanel = new Ext.container.Container({
					html: 'or'
				});

			this.add(this.orPanel);
		},

		hideOr: function() {
			if (this.orPanel)
				this.remove(this.orPanel);
		},

		getData: function() {
			var USE_MY_USER = -1;
			var USE_MY_GROUP = -1;

			var value = [];
			for (var i = 0; i < this.valueFields.length; ++i) {
				var field = this.valueFields[i];

				if (!field.isDisabled())
					value.push(field.getValue());
			}

			var out = {
				simple: {
					attribute: this.attributeName,
					operator: this.conditionCombo.getValue(),
					value: value
				}
			};

			if (this.selectAtRuntimeCheck.getValue()) {
				out.simple.parameterType = 'runtime';
			} else {
				out.simple.parameterType = 'fixed';
			}

			// Manage 'calculated values' for My User and My Group
			var field = this.valueFields[0];
			if (Ext.getClassName(field) == 'CMDBuild.Management.ReferenceField.Field') {
				var cmAttribute = field.CMAttribute;

				if (cmAttribute) {
					if (cmAttribute.referencedClassName == 'User' && field.getValue() == USE_MY_USER) {
						out.simple.parameterType = 'calculated';
						out.simple.value = ['@MY_USER'];
					} else if (cmAttribute.referencedClassName == 'Role' && field.getValue() == USE_MY_GROUP) {
						out.simple.parameterType = 'calculated';
						out.simple.value = ['@MY_GROUP'];
					}
				}
			}

			return out;
		},

		/**
		 * @param {Object} data
		 */
		setData: function(data) {
			if (!Ext.isEmpty(data)) {
				this.conditionCombo.setValue(data.operator);

				for (var i = 0; i < this.valueFields.length; ++i) {
					var field = this.valueFields[i];

					try {
						field.setValue(data.value[i]);
					} catch (e) {
						// Length of the value array on data is less than the number of fields
					}
				}

				if (data.parameterType == 'runtime')
					this.selectAtRuntimeCheck.setValue(true);
			}
		}
	});

	function buildOnConditionComboSelectStrategy(valueFields) {
		if (valueFields.length == 1) {
			return new CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel.SingleStrategy(valueFields);
		} else if (valueFields.length == 2) {
			return new CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel.DoubleStrategy(valueFields);
		} else {
			_debug("There is no Strategy for this value fields", valueFields);
			return new CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel.Strategy();
		}
	}

	Ext.define("CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel.Strategy", {
		constructor: function(valueFields) {
			this.valueFields = valueFields;
		},

		run: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel.SingleStrategy", {
		extend: "CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel.Strategy",

		run: function(operator) {
			var disableValueFields = needsFieldToSetAValue(operator);
			for (var i=0, l=this.valueFields.length; i<l; ++i) {
				var f = this.valueFields[i];
				f.setDisabled(disableValueFields);
			}
		}
	});

	Ext.define("CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel.DoubleStrategy", {
		extend: "CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanel.SingleStrategy",

		constructor: function(valueFields) {
			this.callParent(arguments);
		},

		run: function(operator) {
			this.callParent(arguments);

			// only the between needs two fields
			this.valueFields[1].setDisabled(operator !== "between");
		}
	});

	function needsFieldToSetAValue(operator) {
		var needIt = (operator == CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator.NULL)
			|| (operator == CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator.NOT_NULL);

		return needIt;
	}
})();