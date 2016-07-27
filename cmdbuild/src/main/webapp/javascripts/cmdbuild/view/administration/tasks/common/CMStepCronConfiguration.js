(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.CMStepCronConfigurationDelegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Object}
		 */
		filterWindow: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.CMStepCronConfiguration}
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
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return {CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController} delegate
			 */
			getCronDelegate: function() {
				return this.view.cronForm.delegate;
			},

		// SETters functions
			/**
			 * @param {String} cronExpression
			 */
			setValueAdvancedFields: function(cronExpression) {
				this.getCronDelegate().setValueAdvancedFields(cronExpression);
			},

			/**
			 * @param {String} value
			 */
			setValueBase: function(value) {
				this.getCronDelegate().setValueBase(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.view.administration.tasks.common.CMStepCronConfigurationDelegate}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.cronForm.CMCronForm}
		 */
		cronForm: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfigurationDelegate', this);
			this.cronForm = Ext.create('CMDBuild.view.administration.tasks.common.cronForm.CMCronForm');

			Ext.apply(this, {
				items: [this.cronForm]
			});

			this.callParent(arguments);
		},

		listeners: {
			// To correctly enable radio fields on item activate
			activate: function(view, eOpts) {
				this.cronForm.fireEvent('show', view, eOpts);
			}
		}
	});

})();