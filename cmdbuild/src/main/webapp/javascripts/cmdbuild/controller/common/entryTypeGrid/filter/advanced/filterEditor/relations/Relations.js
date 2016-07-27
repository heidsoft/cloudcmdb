(function () {

	Ext.define('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.Relations', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditor}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'entryTypeGridFilterAdvancedFilterEditorRelationsDataGet',
			'entryTypeGridFilterAdvancedFilterEditorRelationsSelectedDomainGet',
			'entryTypeGridFilterAdvancedFilterEditorRelationsSelectedDomainIsEmpty',
			'entryTypeGridFilterAdvancedFilterEditorRelationsSelectedDomainSet',
			'entryTypeGridFilterAdvancedFilterEditorRelationsSelectionManage',
			'onEntryTypeGridFilterAdvancedFilterEditorRelationsCheckchange',
			'onEntryTypeGridFilterAdvancedFilterEditorRelationsDomainSelect',
			'onEntryTypeGridFilterAdvancedFilterEditorRelationsViewShow'
		],

		/**
		 * @property {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.GridDomain}
		 */
		controllerGridDomain: undefined,

		/**
		 * @property {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.GridCard}
		 */
		controllerGridCard: undefined,

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid}
		 *
		 * @private
		 */
		selectedDomain: undefined,

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.relations.RelationsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.FilterEditor} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.entryTypeGrid.filter.advanced.filterEditor.relations.RelationsView', { delegate: this });

			// Sub-controllers
			this.controllerGridCard = Ext.create('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.GridCard', { parentDelegate: this });
			this.controllerGridDomain = Ext.create('CMDBuild.controller.common.entryTypeGrid.filter.advanced.filterEditor.relations.GridDomain', { parentDelegate: this });

			this.view.add([
				this.controllerGridCard.getView(),
				this.controllerGridDomain.getView()
			]);
		},

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.checked
		 * @param {String} parameters.propertyName
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid} parameters.record
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorRelationsCheckchange: function (parameters) {
			this.controllerGridDomain.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsGridDomainCheckchange', parameters);
			this.controllerGridCard.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsGridCardCheckchange');
		},

		/**
		 * @param {CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid} record
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorRelationsDomainSelect: function (record) {
			if (Ext.isObject(record) && !Ext.Object.isEmpty(record)) {
				this.cmfg('entryTypeGridFilterAdvancedFilterEditorRelationsSelectedDomainSet', { value: record });

				this.controllerGridCard.cmfg('onEntryTypeGridFilterAdvancedFilterEditorRelationsGridCardDomainSelect');
			}
		},

		/**
		 * Decodes filter object and launch creation of form items
		 *
		 * @param {Object} filterConfigurationObject
		 *
		 * @returns {Mixed}
		 *
		 * @private
		 */
		decodeFilterConfigurationObject: function (filterConfigurationObject) {
			filterConfigurationObject = Ext.isArray(filterConfigurationObject) && !Ext.isEmpty(filterConfigurationObject) ? filterConfigurationObject[0] : filterConfigurationObject;

			if (Ext.isObject(filterConfigurationObject) && !Ext.Object.isEmpty(filterConfigurationObject)) {
				Ext.Array.each(filterConfigurationObject, function (configurationObject, i, allConfigurationObjects) {
					var domainRecord = null;

					var recordIndex = this.controllerGridDomain.getView().getStore().findBy(function (record) {
						return (
							record.get([CMDBuild.core.constants.Proxy.DOMAIN, CMDBuild.core.constants.Proxy.NAME]) == configurationObject[CMDBuild.core.constants.Proxy.DOMAIN]
							&& record.get(CMDBuild.core.constants.Proxy.DIRECTION) == configurationObject[CMDBuild.core.constants.Proxy.DIRECTION]
						);
					});

					if (recordIndex >= 0)
						domainRecord = this.controllerGridDomain.getView().getStore().getAt(recordIndex);

					if (!Ext.isEmpty(domainRecord)) {
						domainRecord.setType(configurationObject[CMDBuild.core.constants.Proxy.TYPE]);

						if (Ext.isArray(configurationObject[CMDBuild.core.constants.Proxy.CARDS]) && !Ext.isEmpty(configurationObject[CMDBuild.core.constants.Proxy.CARDS]))
							domainRecord.set(CMDBuild.core.constants.Proxy.CHECKED_CARDS, configurationObject[CMDBuild.core.constants.Proxy.CARDS]);
					}
				}, this);
			}
		},

		/**
		 * Manages view's filter configuration
		 *
		 * @returns {Void}
		 */
		entryTypeGridFilterAdvancedFilterEditorRelationsSelectionManage: function () {
			if (!this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty')) {
				var filterConfigurationObject = this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet', CMDBuild.core.constants.Proxy.CONFIGURATION);

				if (
					!this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty', CMDBuild.core.constants.Proxy.CONFIGURATION)
					&& !this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty', [CMDBuild.core.constants.Proxy.CONFIGURATION, CMDBuild.core.constants.Proxy.RELATION])
				) {
					this.decodeFilterConfigurationObject(
						this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet', [CMDBuild.core.constants.Proxy.CONFIGURATION, CMDBuild.core.constants.Proxy.RELATION])
					);
				}
			} else {
				_error('entryTypeGridFilterAdvancedFilterEditorRelationsSelectionManage(): selected filter is empty', this, this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet'));
			}
		},

		/**
		 * @returns {Object} out
		 */
		entryTypeGridFilterAdvancedFilterEditorRelationsDataGet: function () {
			var out = {};

			if (this.controllerGridDomain.getView().getSelectionModel().hasSelection()) {
				var data = [];

				this.controllerGridDomain.getView().getStore().each(function (domain) {
					var type = domain.getType();

					if (!Ext.isEmpty(type)) {
						var domainFilterConfiguration = {};
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DESTINATION] = domain.get([CMDBuild.core.constants.Proxy.DESTINATION, CMDBuild.core.constants.Proxy.NAME]);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DIRECTION] = domain.get(CMDBuild.core.constants.Proxy.DIRECTION);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DOMAIN] = domain.get([CMDBuild.core.constants.Proxy.DOMAIN, CMDBuild.core.constants.Proxy.NAME]);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.SOURCE] = domain.get([CMDBuild.core.constants.Proxy.SOURCE, CMDBuild.core.constants.Proxy.NAME]);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.TYPE] = type;

						if (
							type == 'oneof'
							&& this.controllerGridCard.getView().getSelectionModel().hasSelection()
						) {
							var checkedCards = [];

							Ext.Array.each(this.controllerGridCard.getView().getSelectionModel().getSelection(), function (record, i, allRecords) {
								if (!Ext.isEmpty(record)) {
									var checkedCardObject = {};
									checkedCardObject[CMDBuild.core.constants.Proxy.CLASS_NAME] = record.get('IdClass_value');
									checkedCardObject[CMDBuild.core.constants.Proxy.ID] = record.get('Id');

									checkedCards.push(checkedCardObject);
								}
							}, this);

							domainFilterConfiguration[CMDBuild.core.constants.Proxy.CARDS] = checkedCards;
						}

						data.push(domainFilterConfiguration);
					}
				}, this);

				out[CMDBuild.core.constants.Proxy.RELATION] = data;
			}

			return out;
		},

		/**
		 * Forwarder function
		 *
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedFilterEditorRelationsViewShow: function () {
			if (!this.cmfg('entryTypeGridFilterAdvancedEntryTypeIsEmpty')) {
				this.selectedDomainReset();

				this.controllerGridCard.getView().fireEvent('show');
				this.controllerGridDomain.getView().fireEvent('show');
			}
		},

		// SelectedDomain property methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed or undefined}
			 */
			entryTypeGridFilterAdvancedFilterEditorRelationsSelectedDomainGet: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageGet(parameters);
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			entryTypeGridFilterAdvancedFilterEditorRelationsSelectedDomainIsEmpty: function (attributePath) {
				var parameters = {};
				parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';
				parameters[CMDBuild.core.constants.Proxy.ATTRIBUTE_PATH] = attributePath;

				return this.propertyManageIsEmpty(parameters);
			},

			/**
			 * @private
			 */
			selectedDomainReset: function () {
				this.propertyManageReset('selectedDomain');
			},

			/**
			 * @param {Object} parameters
			 *
			 * @returns {Void}
			 */
			entryTypeGridFilterAdvancedFilterEditorRelationsSelectedDomainSet: function (parameters) {
				if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
					parameters[CMDBuild.core.constants.Proxy.MODEL_NAME] = 'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid';
					parameters[CMDBuild.core.constants.Proxy.TARGET_VARIABLE_NAME] = 'selectedDomain';

					this.propertyManageSet(parameters);
				}
			}
	});

})();
