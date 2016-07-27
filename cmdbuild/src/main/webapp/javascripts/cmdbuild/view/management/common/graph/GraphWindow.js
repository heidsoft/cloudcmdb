(function() {

	Ext.define('CMDBuild.view.management.common.graph.GraphWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		/**
		 * @cfg {CMDBuild.controller.management.common.graph.Graph}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.relationGraph,

		listeners: {
			show: function (window, eOpts) {
				this.delegate.cmfg('onGraphWindowShow');
			}
		}
	});

})();
