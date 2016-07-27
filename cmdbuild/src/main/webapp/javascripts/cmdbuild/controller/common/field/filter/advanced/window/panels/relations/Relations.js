(function() {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.window.panels.relations.Relations', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldFilterAdvancedWindowRelationsBeforeEdit',
			'onFieldFilterAdvancedWindowRelationsCardSelectionChange',
			'onFieldFilterAdvancedWindowRelationsCardGridLoad',
			'onFieldFilterAdvancedWindowRelationsDomainCheckchange',
			'onFieldFilterAdvancedWindowRelationsDomainSelect',
			'onFieldFilterAdvancedWindowRelationsGetData',
			'onFieldFilterAdvancedWindowRelationsSetData = onFieldFilterAdvancedWindowSetData',
			'onFieldFilterAdvancedWindowRelationsShow',
			'onFieldFilterAdvancedWindowRelationsTabBuild'
		],

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.panels.relations.CardGridAdapter}
		 */
		controllerGridCardAdapter: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.relations.CardGridPanel}
		 */
		gridCard: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.relations.DomainGridPanel}
		 */
		gridDomain: undefined,

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid}
		 */
		selectedDomain: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.relations.RelationsView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.window.Window} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.buildView();
		},

		/**
		 * @returns {CMDBuild.view.common.field.filter.advanced.window.panels.relations.RelationsView}
		 */
		buildView: function() {
			this.view = Ext.create('CMDBuild.view.common.field.filter.advanced.window.panels.relations.RelationsView', { delegate: this });

			// Shorthands
			this.gridCard = this.view.gridCard;
			this.gridDomain = this.view.gridDomain;

			this.controllerGridCardAdapter = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.panels.relations.CardGridAdapter', {
				parentDelegate: this,
				view: this.gridCard
			});

			return this.view;
		},

		/**
		 * @param {Object} filterConfigurationObject
		 *
		 * @returns {Mixed}
		 */
		decodeFilterConfigurationObject: function(filterConfigurationObject) {
			if (!Ext.Object.isEmpty(filterConfigurationObject)) {
				Ext.Array.forEach(filterConfigurationObject, function(configurationObject, i, allConfigurationObjects) {
					var domainRecord = null;

					var recordIndex = this.gridDomain.getStore().findBy(function(record) {
						return (
							record.get(CMDBuild.core.constants.Proxy.DOMAIN).getName() == configurationObject[CMDBuild.core.constants.Proxy.DOMAIN]
							&& record.get(CMDBuild.core.constants.Proxy.DIRECTION) == configurationObject[CMDBuild.core.constants.Proxy.DIRECTION]
						);
					});

					if (recordIndex >= 0)
						domainRecord = this.gridDomain.getStore().getAt(recordIndex);

					if (!Ext.isEmpty(domainRecord)) {
						domainRecord.setType(configurationObject[CMDBuild.core.constants.Proxy.TYPE]);

						if (!Ext.isEmpty(configurationObject[CMDBuild.core.constants.Proxy.CARDS]))
							domainRecord.set(CMDBuild.core.constants.Proxy.CHECKED_CARDS, configurationObject[CMDBuild.core.constants.Proxy.CARDS]);
					}
				}, this);
			}
		},

		fillDomainGridStore: function() {
			var domains = [];

			this.gridDomain.getStore().removeAll();

			if (!this.cmfg('fieldFilterAdvancedSelectedClassIsEmpty')) {
				if (_CMCache.isEntryTypeByName(this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME)))
					domains = _CMCache.getDirectedDomainsByEntryType(_CMCache.getEntryTypeByName(this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME)));

				Ext.Array.forEach(domains, function(domainObject, i, allDomainObjects) {
					var domain = _CMCache.getDomainById(domainObject['dom_id']);

					this.gridDomain.getStore().add(
						Ext.create('CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid', {
							destination: _CMCache.getEntryTypeById(domainObject['dst_cid']),
							direction: domainObject[CMDBuild.core.constants.Proxy.SRC],
							domain: domain,
							orientedDescription: domainObject[CMDBuild.core.constants.Proxy.SRC] == '_1' ? domain.get('descr_1') : domain.get('descr_2'),
							source: _CMCache.getEntryTypeById(domainObject['src_cid'])
						})
					);
				}, this);
			}
		},

		/**
		 * Apply filter to classes store to display only related items (no sinple classes)
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Boolean}
		 */
		onFieldFilterAdvancedWindowRelationsBeforeEdit: function(parameters) {
			var colIdx = parameters.colIdx;
			var column = parameters.column;
			var record = parameters.record;

			if (colIdx == 2) { // Avoid to go in edit of unwanted columns
				column.getEditor().getStore().clearFilter();
				column.getEditor().getStore().filterBy(function(storeRecord, id) {
					return (
						storeRecord.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) != CMDBuild.core.constants.Global.getTableTypeSimpleTable()
						&& (
							storeRecord.get(CMDBuild.core.constants.Proxy.PARENT) == record.get(CMDBuild.core.constants.Proxy.DESTINATION).getId()
							|| storeRecord.get(CMDBuild.core.constants.Proxy.ID) == record.get(CMDBuild.core.constants.Proxy.DESTINATION).getId()
						)
					);
				}, this);

				return true;
			}

			return false;
		},

		onFieldFilterAdvancedWindowRelationsCardSelectionChange: function() {
			if (this.gridCard.getSelectionModel().hasSelection()) {
				var checkedCards = this.selectedDomainGet(CMDBuild.core.constants.Proxy.CHECKED_CARDS);

				Ext.Array.forEach(this.gridCard.getSelectionModel().getSelection(), function(record, i, allRecords) {
					if (!Ext.isEmpty(record)) {
						var checkedCardObject = {};
						checkedCardObject[CMDBuild.core.constants.Proxy.CLASS_NAME] = record.get('IdClass_value');
						checkedCardObject[CMDBuild.core.constants.Proxy.ID] = record.get('Id');

						checkedCards.push(checkedCardObject);
					}
				}, this);
			}
		},

		/**
		 * Select configuration object cards
		 */
		onFieldFilterAdvancedWindowRelationsCardGridLoad: function() {
			if (!Ext.isEmpty(this.gridCard.getSelectionModel()))
				this.gridCard.getSelectionModel().clearSelections();

			if (!this.selectedDomainIsEmpty())
				Ext.Array.forEach(this.selectedDomainGet(CMDBuild.core.constants.Proxy.CHECKED_CARDS), function(selectedCardObject, i, allSelectedCardObject) {
					this.gridCard.getSelectionModel().select(
						this.gridCard.getStore().findBy(function(storeRecord) {
							return (
								selectedCardObject[CMDBuild.core.constants.Proxy.CLASS_NAME] == storeRecord.get('IdClass_value')
								&& selectedCardObject[CMDBuild.core.constants.Proxy.ID] == storeRecord.get('Id')
							);
						}),
						true
					);
				}, this);
		},

		/**
		 * @param {Boolean} parameters.checked
		 * @param {String} parameters.propertyName
		 * @param {Object} parameters.record
		 */
		onFieldFilterAdvancedWindowRelationsDomainCheckchange: function(parameters) {
			var checked = Ext.isBoolean(parameters.checked) ? parameters.checked : false;
			var propertyName = parameters.propertyName;
			var record = parameters.record;

			if (!Ext.isEmpty(propertyName) && !Ext.isEmpty(record)) {
				this.gridDomain.getSelectionModel().select(record); // Autoselect on checkchange

				// Makes properties mutual exclusive only on check action
				if (checked)
					record.setType(propertyName);

				if (
					!this.selectedDomainIsEmpty()
					&& this.selectedDomainGet(CMDBuild.core.constants.Proxy.DOMAIN).getName() == record.get(CMDBuild.core.constants.Proxy.DOMAIN).getName()
				) {
					this.gridCard.setDisabled(!(propertyName == 'oneof' && checked));
				}
			}
		},

		/**
		 * @param {CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid} record
		 */
		onFieldFilterAdvancedWindowRelationsDomainSelect: function(record) {
			if (!Ext.isEmpty(record)) {
				this.selectedDomainSet(record);

				if (!Ext.isEmpty(this.gridCard.getSelectionModel()))
					this.gridCard.getSelectionModel().clearSelections();

				this.gridCard.updateStoreForClassId(record.get(CMDBuild.core.constants.Proxy.DESTINATION).getId());
				this.gridCard.setDisabled(!record.get('oneof'));
			}
		},

		/**
		 * @returns {Object} out
		 */
		onFieldFilterAdvancedWindowRelationsGetData: function() {
			var out = {};

			if (this.gridDomain.getSelectionModel().hasSelection()) {
				var data = [];

				this.gridDomain.getStore().each(function(domain) {
					var type = domain.getType();

					if (!Ext.isEmpty(type)) {
						var domainFilterConfiguration = {};
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DESTINATION] = domain.get(CMDBuild.core.constants.Proxy.DESTINATION).getName();
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DIRECTION] = domain.get(CMDBuild.core.constants.Proxy.DIRECTION);
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.DOMAIN] = domain.get(CMDBuild.core.constants.Proxy.DOMAIN).getName();
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.SOURCE] = domain.get(CMDBuild.core.constants.Proxy.SOURCE).getName();
						domainFilterConfiguration[CMDBuild.core.constants.Proxy.TYPE] = type;

						if (
							type == 'oneof'
							&& this.gridCard.getSelectionModel().hasSelection()
						) {
							var checkedCards = [];

							Ext.Array.forEach(this.gridCard.getSelectionModel().getSelection(), function(record, i, allRecords) {
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
		 * Apply data only if filter entryType equals to form's selectedClass name
		 *
		 * @param {CMDBuild.model.common.field.filter.advanced.Filter} filter
		 */
		onFieldFilterAdvancedWindowRelationsSetData: function(filter) {
			if (filter.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE) == this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME)) {
				var filterConfigurationObject = filter.get(CMDBuild.core.constants.Proxy.CONFIGURATION);

				this.viewReset();
				this.fillDomainGridStore();

				if (
					!Ext.isEmpty(filterConfigurationObject)
					&& !Ext.Object.isEmpty(filterConfigurationObject[CMDBuild.core.constants.Proxy.RELATION])
				) {
					this.decodeFilterConfigurationObject(filterConfigurationObject[CMDBuild.core.constants.Proxy.RELATION]);
				}
			}
		},

		onFieldFilterAdvancedWindowRelationsShow: function() {
			if (!this.cmfg('fieldFilterAdvancedFilterIsEmpty'))
				this.onFieldFilterAdvancedWindowRelationsSetData(this.cmfg('fieldFilterAdvancedFilterGet'));
		},

		/**
		 * Builds tab from filter value (preset values and add)
		 */
		onFieldFilterAdvancedWindowRelationsTabBuild: function() {
			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'relation'))
				this.cmfg('fieldFilterAdvancedWindowAddTab', this.buildView());
		},

		// SelectedDomain property methods
			/**
			 * @param {String} parameterName
			 *
			 * @returns {Mixed}
			 */
			selectedDomainGet: function(parameterName) {
				if (!Ext.isEmpty(parameterName) && Ext.isObject(this.selectedDomain))
					return this.selectedDomain.get(parameterName);

				return this.selectedDomain;
			},

			/**
			 * @returns {Boolean}
			 */
			selectedDomainIsEmpty: function() {
				return Ext.isEmpty(this.selectedDomain);
			},

			/**
			 * @param {CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid} domain
			 */
			selectedDomainSet: function(domain) {
				this.selectedDomain = undefined;

				if (!Ext.isEmpty(domain) && Ext.isObject(domain)) {
					if (Ext.getClassName(domain) == 'CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid') {
						this.selectedDomain = domain;
					} else {
						this.selectedDomain = Ext.create('CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid', domain);
					}
				}
			},

		viewReset: function() {
			this.gridDomain.getStore().removeAll();
			this.gridCard.getStore().removeAll();
		}
	});

})();