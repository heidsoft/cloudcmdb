(function() {

	Ext.define('CMDBuild.controller.administration.dataView.DataView', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onDataViewModuleInit = onModuleInit'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {Mixed}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.dataView.DataViewView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.dataView.DataViewView', { delegate: this });
		},

		/**
		 * @param {String} sectionIdentifier
		 *
		 * @returns {Mixed}
		 */
		buildSectionController: function(sectionIdentifier) {
			switch (sectionIdentifier) {
				case 'sql':
					return Ext.create('CMDBuild.controller.administration.dataView.Sql', { parentDelegate: this });

				case 'filter':
				default:
					return Ext.create('CMDBuild.controller.administration.dataView.Filter', { parentDelegate: this });
			}
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @override
		 */
		onDataViewModuleInit: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.sectionController = this.buildSectionController(node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]);

				this.view.removeAll(true);
				this.view.add(this.sectionController.getView());

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.onModuleInit(node); // Custom callParent() implementation
			}
		}
	});

})();