(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController', {

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {CMDBuild.view.administration.tasks.common.cronForm.CMCronFormAdvanced}
		 */
		advancedField: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.cronForm.CMCronFormBase}
		 */
		baseField: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onChangeAdvancedRadio':
					return this.onChangeAdvancedRadio(param);

				case 'onChangeBaseRadio':
					return this.onChangeBaseRadio(param);

				case 'onSelectBaseCombo':
					return this.setValueAdvancedFields(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {Array} fields
		 *
		 * @return {String} cronExpression
		 */
		buildCronExpression: function(fields) {
			var cronExpression = '';

			for (var i = 0; i < fields.length; i++) {
				cronExpression += fields[i];

				if (i < (fields.length - 1))
					cronExpression += ' ';
			}

			return cronExpression;
		},

		/**
		 * Create CMCronTriggerField
		 *
		 * @param {String} name
		 * @param {String} label
		 *
		 * @return {Object}
		 */
		createCronField: function(name, label) {
			var me = this;

			return Ext.create('CMDBuild.view.common.field.CMCronTriggerField', {
				name: name,
				fieldLabel: label,
				cmImmutable: true,
				disabled: true,

				listeners: {
					change: function(field, newValue, oldValue) {
						me.setValueBase(
							me.buildCronExpression([
								me.advancedField.advancedFields[0].getValue(),
								me.advancedField.advancedFields[1].getValue(),
								me.advancedField.advancedFields[2].getValue(),
								me.advancedField.advancedFields[3].getValue(),
								me.advancedField.advancedFields[4].getValue()
							])
						);
					}
				}
			});
		},

		// GETters functions
			/**
			 * @return {Object} baseCombo
			 */
			getBaseCombo: function() {
				return this.baseField.baseCombo;
			},

			/**
			 * Get cron form formatted values
			 *
			 * @return {String} cronExpression
			 */
			getValue: function() {
				var cronExpression = null;

				if (!this.isEmptyBase())
					cronExpression = this.baseField.baseCombo.getValue();

				if (!this.isEmptyAdvanced())
					cronExpression = this.buildCronExpression([
						this.advancedField.advancedFields[0].getValue(),
						this.advancedField.advancedFields[1].getValue(),
						this.advancedField.advancedFields[2].getValue(),
						this.advancedField.advancedFields[3].getValue(),
						this.advancedField.advancedFields[4].getValue()
					]);

				return cronExpression;
			},

		/**
		 * @return {Boolean}
		 */
		isEmptyAdvanced: function() {
			return (
				Ext.isEmpty(this.advancedField.advancedFields[0].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[1].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[2].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[3].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[4].getValue())
			);
		},

		/**
		 * @return {Boolean}
		 */
		isEmptyBase: function() {
			return Ext.isEmpty(this.baseField.baseCombo.getValue());
		},

		/**
		 * @param {Boolean} state
		 */
		onChangeAdvancedRadio: function(state) {
			this.setDisabledAdvancedFields(!state);
			this.setDisabledBaseCombo(state);
		},

		/**
		 * @param {Boolean} state
		 */
		onChangeBaseRadio: function(state) {
			this.setDisabledAdvancedFields(state);
			this.setDisabledBaseCombo(!state);
		},

		// SETters functions
			/**
			 * Set fields as required/unrequired
			 *
			 * @param {Boolean} state
			 */
			setAllowBlankAdvancedFields: function(state) {
				for(item in this.advancedField.advancedFields)
					this.advancedField.advancedFields[item].allowBlank = state;
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledAdvancedFields: function(state) {
				for (var key in this.advancedField.advancedFields)
					this.advancedField.advancedFields[key].setDisabled(state);
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledBaseCombo: function(state) {
				this.baseField.baseCombo.setDisabled(state);
			},

			/**
			 * @param {String} cronExpression
			 */
			setValueAdvancedFields: function(cronExpression) {
				if (!Ext.isEmpty(cronExpression)) {
					var values = cronExpression.split(' ');
					var fields = this.advancedField.advancedFields;

					for (var i = 0; i < fields.length; i++)
						if (values[i])
							fields[i].setValue(values[i]);
				}
			},

			/**
			 * @param {String} value
			 */
			setValueAdvancedRadio: function(value) {
				this.advancedField.advanceRadio.setValue(value);
			},

			/**
			 * Try to find the correspondence of advanced cronExpression in baseCombo's store
			 *
			 * @param {String} value
			 */
			setValueBase: function(value) {
				var index = this.baseField.baseCombo.store.find(CMDBuild.core.constants.Proxy.VALUE, value);

				if (index > -1) {
					this.baseField.baseCombo.setValue(value);
				} else {
					this.baseField.baseCombo.setValue();
				}
			},

			/**
			 * @param {String} value
			 */
			setValueBaseRadio: function(value) {
				this.baseField.baseRadio.setValue(value);
			},

		/**
		 * Cron form validation
		 *
		 * @param {Boolean} enable
		 */
		validate: function(enable) {
			this.setValueAdvancedRadio(enable);
			this.setAllowBlankAdvancedFields(
				!(this.isEmptyAdvanced() && enable)
			);
		}
	});

})();