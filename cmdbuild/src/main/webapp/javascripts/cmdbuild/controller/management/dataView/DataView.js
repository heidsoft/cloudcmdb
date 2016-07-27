(function() {

	Ext.define('CMDBuild.controller.management.dataView.DataView', {
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
			'dataViewSelectedGet',
			'onDataViewModuleInit = onModuleInit',
			'onDataViewViewSelected -> sectionController'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {Object}
		 */
		sectionController: undefined,

		/**
		 * @property {CMDBuild.model.dataView.SqlView}
		 *
		 * @private
		 */
		selectedView: undefined,

		/**
		 * @cfg {CMDBuild.view.management.dataView.DataViewView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.MainViewport} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.dataView.DataViewView', { delegate: this });
		},

		// SelectedView property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			dataViewSelectedGet: function(attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedView';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Object} parameters
			 *
			 * @private
			 */
			dataViewSelectedSet: function(parameters) {
				if (!Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.dataView.SqlView';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedView';

					this.propertyManageSet(parameters);
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
			if (!Ext.isEmpty(node)) {
				var selectedDataView = node.getData();
				selectedDataView[CMDBuild.core.constants.Proxy.OUTPUT] = _CMCache.getDataSourceOutput(node.get(CMDBuild.core.constants.Proxy.SOURCE_FUNCTION));

				this.dataViewSelectedSet({ value: selectedDataView });

				this.view.removeAll(true);

				switch(this.cmfg('dataViewSelectedGet', CMDBuild.core.constants.Proxy.SECTION_HIERARCHY)[0]) {
					case 'sql':
					default: {
						this.sectionController = Ext.create('CMDBuild.controller.management.dataView.Sql', { parentDelegate: this });
					}
				}

				this.view.add(this.sectionController.getView());

				this.sectionController.getView().fireEvent('show');

				this.setViewTitle(this.cmfg('dataViewSelectedGet', CMDBuild.core.constants.Proxy.TEXT));

				// History record save
				CMDBuild.global.navigation.Chronology.cmfg('navigationChronologyRecordSave', {
					moduleId: this.cmfg('identifierGet'),
					entryType: {
						description: this.cmfg('dataViewSelectedGet', CMDBuild.core.constants.Proxy.TEXT),
						id: this.cmfg('dataViewSelectedGet', CMDBuild.core.constants.Proxy.ID),
						object: this.cmfg('dataViewSelectedGet')
					}
				});

				this.onModuleInit(node); // Custom callParent() implementation
			}
		}
	});

})();