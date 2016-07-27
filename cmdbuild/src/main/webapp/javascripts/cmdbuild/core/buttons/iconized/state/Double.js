(function () {

	Ext.define('CMDBuild.core.buttons.iconized.state.Double', {
		extend: 'Ext.button.Cycle',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {String}
		 */
		state1icon: 'delete',

		/**
		 * Identifier of clicked state
		 *
		 * @cfg {String}
		 */
		state1stateId: CMDBuild.core.constants.Proxy.ENABLE,

		/**
		 * @cfg {String}
		 */
		state1text: CMDBuild.Translation.disable,

		/**
		 * @cfg {String}
		 */
		state2icon: 'ok',

		/**
		 * Identifier of clicked state
		 *
		 * @cfg {String}
		 */
		state2stateId: CMDBuild.core.constants.Proxy.DISABLE,

		/**
		 * @cfg {String}
		 */
		state2text: CMDBuild.Translation.enable,

		arrowCls: '', // Disable menu arrow
		showText: true,

		initComponent: function () {
			var me = this;

			Ext.apply(this, {
				menu: {
					items: [
						{
							text: me.state1text,
							iconCls: me.state1icon,
							clickedStateIdentifier: me.state1stateId
						},
						{
							text: me.state2text,
							iconCls: me.state2icon,
							clickedStateIdentifier: me.state2stateId
						}
					]
				}
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {String} clickedStateIdentifier
		 */
		getActiveState: function () {
			return this.getActiveItem().clickedStateIdentifier;
		},

		/**
		 * @param {Boolean} currentState
		 */
		setActiveState: function (currentState) {
			if (Ext.isBoolean(currentState))
				this.setActiveItem(currentState ? this.menu.items.items[0] : this.menu.items.items[1]);
		}
	});

})();
