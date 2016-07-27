(function() {

	Ext.define('CMDBuild.core.buttons.iconized.split.Print', {
		extend: 'Ext.button.Split',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		mode: undefined,

		/**
		 * Supported formats
		 *
		 * @cfg {Array}
		 * */
		formatList: [
			CMDBuild.core.constants.Proxy.CSV,
			CMDBuild.core.constants.Proxy.ODT,
			CMDBuild.core.constants.Proxy.PDF,
			CMDBuild.core.constants.Proxy.RTF
		],

		iconCls: 'print',
		text: CMDBuild.Translation.print,

		initComponent: function() {
			Ext.apply(this, {
				menu: Ext.create('Ext.menu.Menu'),

				handler: function(button, e) {
					if (!button.isDisabled())
						button.showMenu();
				}
			});

			this.callParent(arguments);

			switch (this.mode) {
				case 'legacy':
					return this.buildLegacyMenu();

				default:
					return this.buildMenu();
			}
		},

		buildLegacyMenu: function() {
			var me = this;

			Ext.Array.forEach(this.formatList, function(format, i, allFormats) {
				this.menu.add({
					text: CMDBuild.Translation.as + ' ' + format.toUpperCase(),
					iconCls: format,
					format: format,

					handler: function(button, e) {
						me.fireEvent('click', this.format);
					}
				});
			}, this);
		},

		buildMenu: function() {
			Ext.Array.forEach(this.formatList, function(format, i, allFormats) {
				this.menu.add({
					text: CMDBuild.Translation.as + ' ' + format.toUpperCase(),
					iconCls: format,
					format: format,
					scope: this,

					handler: function(button, e) {
						this.delegate.cmfg('onButtonPrintClick', button.format);
					}
				});
			}, this);
		}
	});

})();