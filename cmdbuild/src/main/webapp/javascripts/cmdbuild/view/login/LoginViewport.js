(function () {

	Ext.define('CMDBuild.view.login.LoginViewport', {
		extend: 'Ext.container.Viewport',

		/**
		 * @cfg {CMDBuild.controller.login.Login}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.login.FormContainer}
		 */
		formContainer: undefined,

		border: false,
		frame: true,
		layout: 'border',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.panel.Panel', {
						region: 'north',
						contentEl: 'header',
						border: false,
						frame: false,
						height: 45
					}),
					Ext.create('Ext.panel.Panel', {
						region: 'center',
						border: false,
						frame: false,
						id: 'login-wrapper',

						items: [
							this.formContainer = Ext.create('CMDBuild.view.login.FormContainer', { delegate: this.delegate })
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
