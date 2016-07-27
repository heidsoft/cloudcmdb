(function() {

	Ext.define('CMDBuild.controller.common.field.filter.advanced.window.panels.ColumnPrivileges', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.tabs.attribute.Attribute'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onFieldFilterAdvancedWindowColumnPrivilegesGetData',
			'onFieldFilterAdvancedWindowColumnPrivilegesSet',
			'onFieldFilterAdvancedWindowColumnPrivilegesSetData = onFieldFilterAdvancedWindowSetData',
			'onFieldFilterAdvancedWindowColumnPrivilegesTabBuild'
		],

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.field.filter.advanced.window.Window} configurationObject.parentDelegate
		 * @param {CMDBuild.view.common.field.filter.advanced.window.panels.columnPrivileges.ColumnPrivilegesView} configurationObject.view
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'columnPrivileges')) {
				// Shorthands
				this.grid = this.view.grid;

				// Delegate forward
				this.view.delegate = this;
				this.grid.delegate = this;
			}
		},

		/**
		 * @param {Array} attributes
		 */
		fillGridStore: function(attributes) {
			var records = [];

			if (Ext.isArray(attributes))
				Ext.Array.forEach(attributes, function(attribute, i, allAttributes) {
					if (attribute[CMDBuild.core.constants.Proxy.NAME] != 'Notes') { // As usual, the notes attribute is managed in a special way
						var recordConfigurationObject = {};
						recordConfigurationObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = attribute[CMDBuild.core.constants.Proxy.DESCRIPTION];
						recordConfigurationObject[CMDBuild.core.constants.Proxy.NAME] = attribute[CMDBuild.core.constants.Proxy.NAME];

						records.push(Ext.create('CMDBuild.model.common.field.filter.advanced.window.ColumnPrivilegesGridRecord', recordConfigurationObject));
					}
				}, this);

			this.grid.getStore().loadRecords(records);
		},

		/**
		 * The convention is to send to server an array of string.
		 * Each string has the template: "attributeName:[none, read, write]"
		 *
		 * @returns {Array} out
		 */
		onFieldFilterAdvancedWindowColumnPrivilegesGetData: function() {
			var out = [];

			this.grid.getStore().each(function(record) {
				out.push(record.get(CMDBuild.core.constants.Proxy.NAME) + ':' + record.getPrivilege());
			}, this);

			return out;
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.dataIndex
		 * @param {Number} parameters.rowIndex
		 */
		onFieldFilterAdvancedWindowColumnPrivilegesSet: function(parameters) {
			if (
				!Ext.Object.isEmpty(parameters) && Ext.isObject(parameters)
				&& !Ext.isEmpty(parameters.dataIndex) && Ext.isString(parameters.dataIndex)
				&& !Ext.isEmpty(parameters.rowIndex) && Ext.isNumeric(parameters.rowIndex)
				&& !Ext.isEmpty(this.grid.getStore().getAt(parameters.rowIndex))
			) {
				this.grid.getStore().getAt(parameters.rowIndex).setPrivilege(parameters.dataIndex);
			}
		},

		/**
		 * @param {Object} filter
		 */
		onFieldFilterAdvancedWindowColumnPrivilegesSetData: function(filter) {
			if (!Ext.isEmpty(filter)) {
				var attributePrivileges = {};

				Ext.Array.forEach(this.cmfg('fieldFilterAdvancedWindowSelectedRecordGet', CMDBuild.core.constants.Proxy.ATTRIBUTES_PRIVILEGES), function(privilege, i, allPrivileges) { // String to object conversion
					var parts = privilege.split(':');

					if (parts.length == 2)
						attributePrivileges[parts[0]] = parts[1];
				}, this);

				this.grid.getStore().each(function(record) {
					if (!Ext.isEmpty(attributePrivileges[record.get(CMDBuild.core.constants.Proxy.NAME)]))
						record.setPrivilege(attributePrivileges[record.get(CMDBuild.core.constants.Proxy.NAME)]);
				}, this);
			}
		},

		/**
		 * Builds tab from filter value (preset values and add)
		 */
		onFieldFilterAdvancedWindowColumnPrivilegesTabBuild: function() {
			if (this.cmfg('fieldFilterAdvancedConfigurationIsPanelEnabled', 'columnPrivileges')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('fieldFilterAdvancedSelectedClassGet', CMDBuild.core.constants.Proxy.NAME);

				CMDBuild.proxy.common.tabs.attribute.Attribute.read({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTES];

						this.fillGridStore(decodedResponse);

						if (!this.cmfg('fieldFilterAdvancedWindowSelectedRecordIsEmpty', CMDBuild.core.constants.Proxy.ATTRIBUTES_PRIVILEGES))
							this.onFieldFilterAdvancedWindowColumnPrivilegesSetData(this.cmfg('fieldFilterAdvancedWindowSelectedRecordGet', CMDBuild.core.constants.Proxy.ATTRIBUTES_PRIVILEGES));
					}
				});
			}
		}
	});

})();