(function () {

	Ext.define('CMDBuild.view.administration.tasks.generic.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormGenericController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		taskType: 'generic',

		/**
		 * @property {CMDBuild.view.administration.tasks.generic.CMStep1}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @overwrite
		 */
		cmOn: function (name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return {String}
			 */
			getValueId: function () {
				return this.view.idField.getValue();
			},

		// SETters functions
			/**
			 * @param {Boolean} state
			 */
			setDisabledTypeField: function (state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param {String} value
			 */
			setValueActive: function (value) {
				this.view.activeField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueDescription: function (value) {
				this.view.descriptionField.setValue(value);
			},

			/**
			 * @param {Int} value
			 */
			setValueId: function (value) {
				this.view.idField.setValue(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.generic.CMStep1', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.generic.CMStep1Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		descriptionField: undefined,

		/**
		 * @property {Ext.form.field.Hidden}
		 */
		idField: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		typeField: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
			anchor: '100%'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.generic.CMStep1Delegate', this);

			Ext.apply(this, {
				items: [
					this.typeField = Ext.create('Ext.form.field.Text', {
						fieldLabel: CMDBuild.Translation.administration.tasks.type,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						name: CMDBuild.core.constants.Proxy.TYPE,
						value: CMDBuild.Translation.others,
						disabled: true,
						cmImmutable: true,
						readOnly: true,
						submitValue: false
					}),
					this.descriptionField = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.description_,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						allowBlank: false
					}),
					this.activeField = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.constants.Proxy.ACTIVE,
						fieldLabel: CMDBuild.Translation.administration.tasks.startOnSave,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
					}),
					this.idField = Ext.create('Ext.form.field.Hidden', { name: CMDBuild.core.constants.Proxy.ID })
				]
			});

			this.callParent(arguments);
		}
	});

})();
