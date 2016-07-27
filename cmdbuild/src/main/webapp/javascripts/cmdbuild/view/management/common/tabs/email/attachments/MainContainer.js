(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.attachments.MainContainer', {
		extend: 'Ext.container.Container',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.attachments.Attachments}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.ButtonsContainer}
		 */
		attachmentButtonsContainer: undefined,

		/**
		 * @property {Ext.container.Container}
		 */
		attachmentPanelsContainer: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: false,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function () {
			if (CMDBuild.configuration.dms.get(CMDBuild.core.constants.Proxy.ENABLED)) {
				Ext.apply(this, {
					items: [
						this.attachmentButtonsContainer = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.ButtonsContainer', {
							delegate: this.delegate,
							readOnly: this.readOnly
						}),
						this.attachmentPanelsContainer = Ext.create('Ext.container.Container', {
							overflowX: 'hidden',
							flex: 1
						})
					]
				});
			}

			this.callParent(arguments);
		},

		/**
		 * Forward method
		 *
		 * @param {Object} component
		 *
		 * @return {Ext.Component}
		 */
		addPanel: function (component) {
			return this.attachmentPanelsContainer.add(component);
		}
	});

})();
