(function () {

	Ext.define('CMDBuild.core.buttons.iconized.state.Map', {
		extend: 'CMDBuild.core.buttons.iconized.state.Double',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {String}
		 */
		state1icon: 'map',

		/**
		 * Identifier of clicked state
		 *
		 * @cfg {String}
		 */
		state1stateId: CMDBuild.core.constants.Proxy.MAP,

		/**
		 * @cfg {String}
		 */
		state1text: CMDBuild.Translation.map,

		/**
		 * @cfg {String}
		 */
		state2icon: 'table',

		/**
		 * Identifier of clicked state
		 *
		 * @cfg {String}
		 */
		state2stateId: CMDBuild.core.constants.Proxy.GRID,

		/**
		 * @cfg {String}
		 */
		state2text: CMDBuild.Translation.list
	});

})();
