(function() {

	/**
	 * PanelFunction should be used (new implementation)
	 *
	 * @deprecated (CMDBuild.view.common.PanelFunctions)
	 */
	Ext.define("CMDBUild.view.common.CMFormFunctions", {

		enableCMButtons: function() {
			this.iterateOverCMButtons(function(item) {
				if (item && item.enable)
					item.enable();
			});
		},

		disableCMButtons: function() {
			this.iterateOverCMButtons(function(item) {
				if (item && item.disable)
					item.disable();
			});
		},

		disableCMTbar: function() {
			this.iterateOverCMTBar(function(item) {
				if (item && item.disable)
					item.disable();
			});
		},

		disableFields: function() {
			this.cascade(function(item) {
				if (
					item
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.FieldSet
						|| item.considerAsFieldToDisable
					)
				) {
					item.disable();
				}
			});
		},

		disableModify: function(enableCMTBar) {
			this._cmEditMode = false;
			this.disableFields();
			this.disableCMButtons();

			if (enableCMTBar) {
				this.enableCMTbar();
			} else {
				this.disableCMTbar();
			}
		},

		enableCMTbar: function() {
			this.iterateOverCMTBar(function(item) {
				if (item && item.enable && !item.disabledForGroup)
					item.enable();
			});
		},

		enableModify: function(all) {
			this._cmEditMode = true;
			this.enableFields(all);
			this.disableCMTbar();
			this.enableCMButtons();
		},

		enableTabbedModify: function(all) {
			this._cmEditMode = true;
			this.enableTabbedFields(all);
			this.disableCMTbar();
			this.enableCMButtons();
		},

		enableFields: function(enableAll) {
			this.cascade(function(item) {
				if (
					item
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.FieldSet
						|| item.considerAsFieldToDisable
					)
				) {
					var name = item._name || item.name; // for compatibility I can not change the name of old attrs
					var toBeEnabled = (enableAll || !item.cmImmutable) && item.isVisible();

					if (toBeEnabled)
						item.enable();
				}
			});
		},

		enableTabbedFields: function(enableAll) {
			this.cascade(function(item) {
				if (
					item
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.FieldSet
						|| item.considerAsFieldToDisable
					)
				) {
					var name = item._name || item.name; // for compatibility I can not change the name of old attrs
					var toBeEnabled = (enableAll || !item.cmImmutable);

					if (toBeEnabled)
						item.enable();
				}
			});
		},

		focusOnFirstEnabled: function() {
			var cathced = false;
			this.cascade(function(item) {
				if (item && (item instanceof Ext.form.Field)) {
					if (!item.disabled && !cathced) {
						item.focus();
						cathced = true;
					}
				}
			});
		},

		getData: function(withDisabled) {
			if (withDisabled) {
				var data = {};
				this.cascade(function(item) {
					if (
						item
						&& item.submitValue
						&& (
							(item instanceof Ext.form.Field)
							|| (item instanceof Ext.form.field.Base)
							|| (item instanceof Ext.form.field.HtmlEditor)
						)
					) {
						data[item.name] = item.getValue();
					}
				});

				return data;
			} else {
				return this.getForm().getValues();
			}
		},

		getNonValidFields: function() {
			var data = [];

			this.cascade(function(item) {
				if (item
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.field.Field
					)
					&& !item.disabled
				) {
					if (!item.isValid()) {
						data.push(item);
					}
				}
			});

			return data;
		},

		/**
		 * Clears form fields dirty state
		 */
		initValues: function() {
			if (this.getForm())
				Ext.each(this.getForm().getFields().getRange(), function(field) {
					if (field.isDirty())
						field.initValue();
				});
		},

		iterateOverCMButtons: function(fn) {
			this.iterateOverArray(this.cmButtons, fn);
		},

		iterateOverCMTBar: function(fn) {
			this.iterateOverArray(this.cmTBar, fn);
		},

		iterateOverArray: function(array, fn) {
			array = array || [];

			for (var i = 0, l = array.length; i < l; ++i)
				fn(array[i]);
		},

		reset: function() {
			try {
				this.getForm().setValues();
				this.getForm().reset();
			} catch (e) {}
		}
	});

})();