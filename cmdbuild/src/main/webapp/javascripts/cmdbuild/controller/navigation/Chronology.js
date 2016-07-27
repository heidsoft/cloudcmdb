(function () {

	Ext.define('CMDBuild.controller.navigation.Chronology', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.configurations.DataFormat',
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Utils'
		],

		mixins: ['CMDBuild.controller.navigation.ButtonHandlers'],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		records: [],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'navigationChronologyItemConfigurationGet',
			'navigationChronologyButtonHandler', // From mixins
			'navigationChronologyRecordSave',
			'onNavigationChronologyMenuBeforeShow'
		],

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		getIconClass: function (record) {
			switch (record.get(CMDBuild.core.constants.Proxy.MODULE_ID)) {
				case 'class': {
					var isSuperClass = record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.OBJECT, 'superclass']);

					return isSuperClass ? 'cmdb-tree-superclass-icon' : 'cmdb-tree-class-icon';
				}

				case CMDBuild.core.constants.ModuleIdentifiers.getCustomPage():
					return 'cmdb-tree-custompage-icon';

				case 'dashboard':
					return 'cmdb-tree-dashboard-icon';

				case CMDBuild.core.constants.ModuleIdentifiers.getWorkflow(): {
					var isSuperClass = record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.OBJECT, 'superclass']);

					return isSuperClass ? 'cmdb-tree-superprocessclass-icon' : 'cmdb-tree-processclass-icon';
				}

				default:
					return 'x-tree-icon-leaf';
			}
		},

		// Label methods
			/**
			 * @param {CMDBuild.model.navigation.chronology.Record} record
			 *
			 * @returns {String}
			 *
			 * @private
			 */
			getLabel: function (record) {
				if (!Ext.isEmpty(record) && Ext.isObject(record)) {
					var itemLabelParts = [];
					itemLabelParts.push(Ext.Date.format(record.get(CMDBuild.core.constants.Proxy.DATE), CMDBuild.core.configurations.DataFormat.getTime()));

					this.getLabelPropertyModuleId(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.MODULE_ID));
					this.getLabelProperty(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.ENTRY_TYPE));
					this.getLabelProperty(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.ITEM));
					this.getLabelProperty(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.SECTION));
					this.getLabelProperty(itemLabelParts, record.get(CMDBuild.core.constants.Proxy.SUB_SECTION));

					return itemLabelParts.join(CMDBuild.core.constants.Global.getTitleSeparator());
				}

				return '';
			},

			/**
			 * @param {Array} targetArray
			 * @param {String} property
			 *
			 * @private
			 */
			getLabelProperty: function (targetArray, property) {
				if (
					Ext.isArray(targetArray)
					&& !Ext.isEmpty(property) && Ext.isObject(property)
				) {
					if (!property.isEmpty(CMDBuild.core.constants.Proxy.DESCRIPTION)) {
						targetArray.push(property.get(CMDBuild.core.constants.Proxy.DESCRIPTION));
					} else if (!property.isEmpty(CMDBuild.core.constants.Proxy.ID)) {
						targetArray.push(property.get(CMDBuild.core.constants.Proxy.ID));
					}
				}
			},

			/**
			 * @param {Array} targetArray
			 * @param {String} property
			 *
			 * @private
			 */
			getLabelPropertyModuleId: function (targetArray, property) {
				if (Ext.isArray(targetArray))
					switch (property) {
						case 'class':
							return targetArray.push(CMDBuild.Translation.classes);

						case CMDBuild.core.constants.ModuleIdentifiers.getCustomPage():
							return targetArray.push(CMDBuild.Translation.customPages);

						case 'dashboard':
							return targetArray.push(CMDBuild.Translation.dashboard);

						case CMDBuild.core.constants.ModuleIdentifiers.getDataView():
							return targetArray.push(CMDBuild.Translation.views);

						case CMDBuild.core.constants.ModuleIdentifiers.getReport():
							return targetArray.push(CMDBuild.Translation.report);

						case CMDBuild.core.constants.ModuleIdentifiers.getWorkflow():
							return targetArray.push(CMDBuild.Translation.processes);
					}
			},

		/**
		 * @returns {Array} menuItems
		 *
		 * @private
		 */
		menuItemsBuild: function () {
			var menuItems = [];

			if (!Ext.isEmpty(this.records) && Ext.isArray(this.records)) {
				CMDBuild.core.Utils.objectArraySort(this.records, CMDBuild.core.constants.Proxy.DATE, 'DESC');

				Ext.Array.forEach(this.records, function (recordObject, i, allRecordObjects) {
					if (!Ext.Object.isEmpty(recordObject))
						menuItems.push({
							delegate: this,

							iconCls: this.getIconClass(recordObject),
							record: recordObject,
							text: this.getLabel(recordObject),

							handler: function (button, e) {
								this.delegate.cmfg('navigationChronologyButtonHandler', this.record);
							}
						});

					if (i == 0)
						menuItems.push('-');
				}, this);
			} else {
				menuItems.push({
					text: '- ' + CMDBuild.Translation.empty + ' -',
					disabled: true
				});
			}

			return menuItems;
		},

		/**
		 * @returns {Object}
		 */
		navigationChronologyItemConfigurationGet: function () {
			return {
				text: CMDBuild.Translation.navigationChronology,
				iconCls: 'navigation-chronology',

				menu: Ext.create('Ext.menu.Menu'),

				listeners: {
					beforeshow: function (tool, eOpts) {
						return CMDBuild.global.navigation.Chronology.cmfg('onNavigationChronologyMenuBeforeShow', this);
					}
				}
			};
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.moduleId - module identifier (class, workflow, dashboard, dataview, ...)
		 * @param {String} parameters.entryType - selected entryType (Class, process, ...)
		 * @param {Object} parameters.item - item selected from grid (card, instance, ...)
		 * @param {Object} parameters.section - usually form tab object
		 * @param {Object} parameters.subSection - usually form tab sub-section
		 */
		navigationChronologyRecordSave: function (parameters) {
			if (
				!Ext.isEmpty(parameters) && Ext.isObject(parameters)
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.MODULE_ID])
				&& !Ext.isEmpty(parameters[CMDBuild.core.constants.Proxy.ENTRY_TYPE])
			) {
				var record = Ext.create('CMDBuild.model.navigation.chronology.Record', parameters);

				// Filter double records save
				if (Ext.isEmpty(this.records) || !this.records[0].equals(record))
					this.records.unshift(record);

				// Resize array to referenceComboStoreLimit configuration parameter
				this.records = Ext.Array.slice(this.records, 0, CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT));
			} else {
				_warning('invalid record field configuration', this, parameters);
			}
		},

		/**
		 * @param {Object} item
		 *
		 * @returns {Boolean}
		 */
		onNavigationChronologyMenuBeforeShow: function (item) {
			if (!Ext.isEmpty(item)) {
				Ext.apply(item, {
					menu: Ext.create('Ext.menu.Menu', {
						overflowX: 'auto',
						maxWidth: CMDBuild.core.constants.FieldWidths.MENU_DROPDOWN,

						items: this.menuItemsBuild()
					})
				});

				return true;
			}

			return false;
		}
	});

})();
