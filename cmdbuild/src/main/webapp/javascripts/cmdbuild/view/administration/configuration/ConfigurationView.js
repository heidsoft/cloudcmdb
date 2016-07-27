(function() {

	Ext.define('CMDBuild.view.administration.configuration.ConfigurationView', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Main}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		baseTitle: CMDBuild.Translation.setup,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: true,
		frame: false,
		layout: 'fit'
	});

})();