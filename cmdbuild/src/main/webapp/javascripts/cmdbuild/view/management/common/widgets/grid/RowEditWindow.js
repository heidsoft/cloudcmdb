(function() {

	Ext.define('CMDBuild.view.management.common.widgets.grid.RowEditWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.RowEdit}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		autoScroll: true,
		border: false,
		defaultSizeW: 0.90,
		defaultSizeH: 0.80,
		title: CMDBuild.Translation.editRow,

		initComponent: function() {
			this.form = Ext.create('Ext.form.Panel', {
				autoScroll: true,
				frame: true,
				border: false
			});

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
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onRowEditWindowSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onRowEditWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [this.form]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;
		}
	});

})();