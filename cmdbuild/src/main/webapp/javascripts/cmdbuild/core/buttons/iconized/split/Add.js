(function() {

	Ext.define('CMDBuild.core.buttons.iconized.split.Add', {
		extend: 'Ext.button.Split',

		/**
		 * @property {Ext.menu.Menu}
		 */
		menu: undefined,

		iconCls: 'add',
		text: CMDBuild.Translation.add,

		initComponent: function() {
			Ext.apply(this, {
				scope: this,
				menu: Ext.create('Ext.menu.Menu'),

				handler: function(button, e) {
					this.showMenu();
				}
			});

			this.callParent(arguments);
		}
	});

})();