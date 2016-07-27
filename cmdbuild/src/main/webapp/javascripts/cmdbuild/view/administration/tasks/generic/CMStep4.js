(function () {

	Ext.define('CMDBuild.view.administration.tasks.generic.CMStep4Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormGenericController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.generic.CMStep4}
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
			 * @return {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController} delegate
			 */
			getEmailDelegate: function () {
				return this.view.emailForm.delegate;
			},

		// SETters functions
			/**
			 * @param {String} value
			 */
			setValueEmailAccount: function(value) {
				this.getEmailDelegate().setValue('sender', value);
			},

			/**
			 * @param {String} value
			 */
			setValueEmailTemplate: function (value) {
				this.getEmailDelegate().setValue('template', value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.generic.CMStep4', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.generic.CMStep4Delegate}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm}
		 */
		emailForm: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.generic.CMStep4Delegate', this);

			Ext.apply(this, {
				items: [
					this.emailForm = Ext.create('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationForm', {
						sender: {
							type: 'sender',
							name: CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT,
							disabled: false
						},
						template: {
							type: 'template',
							name: CMDBuild.core.constants.Proxy.EMAIL_TEMPLATE,
							disabled: false
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
