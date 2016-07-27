(function () {

	Ext.define('CMDBuild.view.common.panel.gridAndForm.tools.Minimize', {
		extend: 'Ext.panel.Tool',

		tooltip: CMDBuild.Translation.minimizeGrid,
		type: 'minimize',

		/**
		 * @param {Ext.EventObject} event
		 * @param {Ext.Element} target
		 * @param {Ext.panel.Header} owner
		 * @param {Ext.panel.Tool} tool
		 *
		 * @override
		 */
		handler: function (event, target, owner, tool) {
			_CMUIState.onlyForm();
		}
	});

})();
