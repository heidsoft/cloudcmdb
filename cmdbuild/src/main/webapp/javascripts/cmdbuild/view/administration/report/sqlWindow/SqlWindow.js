(function () {

	Ext.define('CMDBuild.view.administration.report.sqlWindow.SqlWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.report.SqlWindow}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.TextArea}
		 */
		textArea: undefined,

		border: false,
		frame: true,
		layout: 'fit',
		title: CMDBuild.Translation.sql,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Close', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onReportSqlWindowCloseButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.textArea = Ext.create('Ext.form.field.TextArea', {
						border: false,
						frame: false,
						overflowY: 'auto',
						readOnly: true
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();
