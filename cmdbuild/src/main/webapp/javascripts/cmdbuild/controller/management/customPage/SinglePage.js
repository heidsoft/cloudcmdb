(function() {

	Ext.define('CMDBuild.controller.management.customPage.SinglePage', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.configurations.CustomPage'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onCustomPageModuleInit = onModuleInit'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.management.customPage.SinglePagePanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.customPage.SinglePagePanel', { delegate: this });
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @override
		 */
		onCustomPageModuleInit: function (node) {
			if (!Ext.isEmpty(node)) {
				var basePath = window.location.toString().split('/');
				basePath = Ext.Array.slice(basePath, 0, basePath.length - 1).join('/');

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.view.removeAll();
				this.view.add({
					xtype: 'component',

					autoEl: {
						tag: 'iframe',
						src: CMDBuild.core.configurations.CustomPage.getCustomizationsPath()
							+ node.get(CMDBuild.core.constants.Proxy.NAME)
							+ '/?basePath=' + basePath
							+ '&frameworkVersion=' + CMDBuild.core.configurations.CustomPage.getVersion()
							+ '&language=' + CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.LANGUAGE)
					}
				});

				// History record save
				if (!Ext.isEmpty(node))
					CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
						moduleId: this.cmfg('identifierGet'),
						entryType: {
							description: node.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
							id: node.get(CMDBuild.core.constants.Proxy.ID),
							object: node
						}
					});

				this.onModuleInit(node); // Custom callParent() implementation
			}
		}
	});

})();
