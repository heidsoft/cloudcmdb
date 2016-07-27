(function () {

	Ext.define('CMDBuild.view.login.FormContainer', {
		extend: 'Ext.container.Container',

		/**
		 * @cfg {CMDBuild.controller.login.Login}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.login.FormPanel}
		 */
		form: undefined,

		id: 'login-container',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('CMDBuild.view.login.FormPanel', { delegate: this.delegate }),
					Ext.create('Ext.panel.Panel', {
						border: false,
						contentEl: 'release-box',
						frame: false,
						height: 30
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
