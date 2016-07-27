(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.cronForm.CMCronForm', {
		extend: 'Ext.container.Container',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.cronForm.CMCronFormAdvanced}
		 */
		advanced: undefined,

		/**
		 * @property {Object}
		 */
		advancedConfig: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.cronForm.CMCronFormBase}
		 */
		base: undefined,

		/**
		 * @property {Object}
		 */
		baseConfig: undefined,

		border: false,
		considerAsFieldToDisable: true,

		/**
		 * To acquire informations to setup fields before creation
		 *
		 * @param (Object) configuration
		 * @param (Object) configuration.advanced
		 * @param (Object) configuration.base
		 */
		constructor: function(configuration) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController', this);

			if (Ext.isEmpty(configuration) || Ext.isEmpty(configuration.advanced)) {
				this.advancedConfig = { delegate: this.delegate };
			} else {
				this.advancedConfig = configuration.advanced;
				this.advancedConfig.delegate = this.delegate;
			}

			if (Ext.isEmpty(configuration) || Ext.isEmpty(configuration.base)) {
				this.baseConfig = { delegate: this.delegate };
			} else {
				this.baseConfig = configuration.base;
				this.baseConfig.delegate = this.delegate;
			}

			this.callParent(arguments);
		},

		initComponent: function() {
			this.advanced = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormAdvanced', this.advancedConfig);
			this.base = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormBase', this.baseConfig);

			this.delegate.advancedField = this.advanced;
			this.delegate.baseField = this.base;

			Ext.apply(this, {
				items: [this.base, this.advanced]
			});

			this.callParent(arguments);
		},

		listeners: {
			// To correctly enable radio fields on tab show
			show: function(view, eOpts) {
				if (this.delegate.isEmptyBase())
					this.delegate.setValueAdvancedRadio(true);

				if (this.delegate.isEmptyAdvanced())
					this.delegate.setValueBaseRadio(true);

				if (!this.delegate.isEmptyBase() && !this.delegate.isEmptyAdvanced())
					this.delegate.setValueBaseRadio(true);
			}
		}
	});

})();