(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.UserAndGroup', {
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
			'onUserAndGroupAccordionSelect',
			'onUserAndGroupModuleInit = onModuleInit',
			'userAndGroupSelectedAccordionGet',
			'userAndGroupSelectedAccordionIsEmpty'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @parameter {CMDBuild.model.userAndGroup.SelectedAccordion}
		 *
		 * @private
		 */
		selectedAccordion: undefined,

		/**
		 * @property {Object}
		 */
		sectionController: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.UserAndGroupView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.UserAndGroupView', { delegate: this });
		},

		/**
		 * Setup view items and controllers on accordion click
		 *
		 * @param {CMDBuild.model.common.Accordion} node
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onUserAndGroupModuleInit: function (node) {
			if (!Ext.Object.isEmpty(node)) {
				var titleParts = [];
				var selectedAccordionData = node.getData();
				selectedAccordionData[CMDBuild.core.constants.Proxy.ID] = selectedAccordionData[CMDBuild.core.constants.Proxy.ENTITY_ID];

				this.userAndGroupSelectedAccordionSet({ value: selectedAccordionData });

				this.view.removeAll(true);

				switch (this.cmfg('userAndGroupSelectedAccordionGet', CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'user': {
						this.sectionController = Ext.create('CMDBuild.controller.administration.userAndGroup.user.User', { parentDelegate: this });

						titleParts = [this.sectionController.getBaseTitle()];
					} break;

					case 'group':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.administration.userAndGroup.group.Group', { parentDelegate: this });

						titleParts = [this.sectionController.getBaseTitle()];

						if (!this.cmfg('userAndGroupSelectedAccordionIsEmpty', CMDBuild.core.constants.Proxy.DESCRIPTION))
							titleParts.push(this.cmfg('userAndGroupSelectedAccordionGet', CMDBuild.core.constants.Proxy.DESCRIPTION));
					}
				}

				this.setViewTitle(titleParts);

				this.view.add(this.sectionController.getView());

				this.sectionController.cmfg('onUserAndGroupAccordionSelect');
				this.sectionController.getView().fireEvent('show'); // Manual show event fire

				this.onModuleInit(node); // Custom callParent() implementation
			}
		},

		// SelectedAccordion property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			userAndGroupSelectedAccordionGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @return {Mixed or undefined}
			 */
			userAndGroupSelectedAccordionIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 *
			 * @private
			 */
			userAndGroupSelectedAccordionSet: function (parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.userAndGroup.SelectedAccordion';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedAccordion';

					this.propertyManageSet(parameters);
				}
			}
	});

})();
