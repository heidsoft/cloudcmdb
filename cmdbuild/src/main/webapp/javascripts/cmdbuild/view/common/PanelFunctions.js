(function () {

	/**
	 * Service class to be used as mixin for Ext.form.Panel or some methods are compatible also with Ext.panel.Panel
	 *
	 * Specific properties:
	 * 	- {Boolean} considerAsFieldToDisable: enable setDisable function on processed item also if it's not inherits from Ext.form.Field
	 * 	- {Boolean} disableEnableFunctions: disable enable/setDisabled(false) on processed item (ex. cmImmutable)
	 * 	- {Boolean} disablePanelFunctions: disable PanelFunctions class actions on processed item
	 * 	- {Boolean} forceDisabledState: force item to be disabled
	 */
	Ext.define('CMDBuild.view.common.PanelFunctions', {

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * Keeps in sync two fields, usually name and description. If the master field changes and the slave is empty, or it has the same
		 * value as the old value of the master, its value is updated with the new one.
		 *
		 * These function has to be used with the change listener, example:
		 * 		change: function (field, newValue, oldValue, eOpts) {
		 * 			this.fieldSynch(slaveField, newValue, oldValue);
		 * 		}
		 *
		 * @param {Object} fieldToComplete
		 * @param {Object} newValue
		 * @param {Object} oldValue
		 *
		 * @returns {Void}
		 */
		fieldSynch: function (slaveField, newValue, oldValue) {
			if (this.isManagedField(slaveField)) {
				var actualValue = slaveField.getValue();

				if (Ext.isEmpty(actualValue) || actualValue == oldValue)
					slaveField.setValue(newValue);
			}
		},

		/**
		 * @param {Boolean} withDisabled
		 *
		 * @returns {Array}
		 */
		getData: function (withDisabled) {
			if (withDisabled) {
				var data = {};

				this.cascade(function (item) {
					if (
						!Ext.isEmpty(item)
						&& Ext.isFunction(item.getValue)
						&& this.isManagedField(item)
						&& !item.disablePanelFunctions
					) {
						data[item.name] = item.getValue();
					}
				}, this);

				return data;
			}

			return this.getForm().getValues();
		},

		/**
		 * @returns {Array} nonValidFields
		 */
		getNonValidFields: function () {
			var nonValidFields = [];

			this.cascade(function (item) {
				if (
					!Ext.isEmpty(item)
					&& this.isManagedField(item)
					&& Ext.isFunction(item.isDisabled) && !item.isDisabled()
					&& Ext.isFunction(item.isHidden) && !item.isHidden()
					&& Ext.isFunction(item.isValid) && !item.isValid()
					&& !item.disablePanelFunctions
				) {
					nonValidFields.push(item);
				}
			}, this);

			return nonValidFields;
		},

		/**
		 * @param {Object} field
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isManagedField: function (field) {
			return (
				field instanceof Ext.form.Field
				|| field instanceof Ext.form.field.Base
				|| field instanceof Ext.form.field.HtmlEditor
				|| field instanceof Ext.form.FieldContainer
			);
		},

		/**
		 * @returns {Void}
		 */
		reset: function () {
			// SetValues
			this.cascade(function (item) {
				if (
					!Ext.isEmpty(item)
					&& Ext.isFunction(item.setValue)
					&& this.isManagedField(item)
					&& !item.disablePanelFunctions
				) {
					item.setValue();
				}
			}, this);

			// Reset
			this.cascade(function (item) {
				if (
					!Ext.isEmpty(item)
					&& Ext.isFunction(item.reset)
					&& this.isManagedField(item)
					&& !item.disablePanelFunctions
				) {
					item.reset();
				}
			}, this);
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		setDisabledBottomBar: function (state) {
			var bottomToolbar = this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM);

			if (!Ext.isEmpty(bottomToolbar))
				Ext.Array.forEach(bottomToolbar.items.items, function (button, i, allButtons) {
					if (
						!Ext.isEmpty(button)
						&& Ext.isFunction(button.setDisabled)
						&& !button.disablePanelFunctions
					) {
						button.setDisabled(Ext.isBoolean(button.forceDisabledState) ? button.forceDisabledState : state);
					}
				}, this);
		},

		/**
		 * @param {Boolean} state
		 * @param {Boolean} allFields
		 * @param {Boolean} disableIsVisibleCheck
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		setDisableFields: function (state, allFields, disableIsVisibleCheck) {
			allFields = Ext.isBoolean(allFields) ? allFields : false;
			disableIsVisibleCheck = Ext.isBoolean(disableIsVisibleCheck) ? disableIsVisibleCheck : false;

			// For Ext.form.field.Field objects
			this.getForm().getFields().each(function (item, i, length) {
				if (
					!Ext.isEmpty(item)
					&& Ext.isFunction(item.setDisabled)
					&& !item.disablePanelFunctions
				) {
					if (state) {
						item.setDisabled(state);
					} else {
						if ((allFields || !item.disableEnableFunctions) && item.isVisible())
							item.setDisabled(state);
					}
				}
			}, this);

			// For extra objects (Buttons and objects with considerAsFieldToDisable property)
			this.cascade(function (item) {
				if (
					!Ext.isEmpty(item)
					&& Ext.isFunction(item.setDisabled)
					&& (
						item instanceof Ext.button.Button
						|| item.considerAsFieldToDisable
					)
					&& !item.disablePanelFunctions
				) {
					if (state) {
						item.setDisabled(state);
					} else {
						if (
							(allFields || !item.disableEnableFunctions)
							&& (item.isVisible() || disableIsVisibleCheck)
						) {
							item.setDisabled(state);
						}
					}
				}
			}, this);
		},

		/**
		 * Don't disable FieldSets, but only contained fields
		 *
		 * @param {Ext.form.FieldSet} fieldset
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		setDisabledFieldSet: function (fieldset, state) {
			state = Ext.isBoolean(state) ? state : true;

			if (fieldset instanceof Ext.form.FieldSet)
				fieldset.cascade(function (item) {
					if (
						!Ext.isEmpty(item)
						&& Ext.isFunction(item.setDisabled)
						&& (
							this.isManagedField(item)
							|| item instanceof Ext.button.Button
							|| item.considerAsFieldToDisable
						)
						&& !item.disablePanelFunctions
					) {
						if (state) {
							item.setDisabled(state);
						} else {
							if (
								!item.disableEnableFunctions
								&& (item.isVisible() || disableIsVisibleCheck)
							) {
								item.setDisabled(state);
							}
						}
					}
				}, this);
		},

		/**
		 * @param {Boolean} state
		 * @param {Boolean} allFields
		 * @param {Boolean} tBarState
		 * @param {Boolean} bBarState
		 *
		 * @returns {Void}
		 */
		setDisabledModify: function (state, allFields, tBarState, bBarState) {
			this.setDisableFields(state, allFields);
			this.setDisabledTopBar(Ext.isBoolean(tBarState) ? tBarState : !state);
			this.setDisabledBottomBar(Ext.isBoolean(bBarState) ? bBarState : state);
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		setDisabledTopBar: function (state) {
			var topToolbar = this.getDockedComponent(CMDBuild.core.constants.Proxy.TOOLBAR_TOP);

			if (!Ext.isEmpty(topToolbar))
				Ext.Array.forEach(topToolbar.items.items, function (button, i, allButtons) {
					if (
						!Ext.isEmpty(button)
						&& Ext.isFunction(button.setDisabled)
						&& !button.disablePanelFunctions
					) {
						button.setDisabled(Ext.isBoolean(button.forceDisabledState) ? button.forceDisabledState : state);
					}
				}, this);
		}
	});

})();
