(function() {

	Ext.define('CMDBuild.controller.administration.email.Email', {
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
			'onEmailModuleInit = onModuleInit'
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
		 * @cfg {CMDBuild.view.administration.email.EmailView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.email.EmailView', { delegate: this });
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @override
		 */
		onEmailModuleInit: function(node) {
			if (!Ext.Object.isEmpty(node)) {
				this.view.removeAll(true);

				switch (node.get(CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'queue': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.Queue', { parentDelegate: this });
					} break;

					case 'templates': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.template.Template', { parentDelegate: this });
					} break;

					case 'accounts':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.email.Account', { parentDelegate: this });
					}
				}

				this.setViewTitle(node.get(CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.view.add(this.sectionController.getView());

				this.sectionController.getView().fireEvent('show');

				this.onModuleInit(node); // Custom callParent() implementation
			}
		}
	});

})();