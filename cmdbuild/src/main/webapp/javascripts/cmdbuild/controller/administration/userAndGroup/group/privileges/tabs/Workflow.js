(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.privileges.tabs.Workflow', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.userAndGroup.group.privileges.Workflow'
		],

		mixins: ['CMDBuild.controller.common.field.filter.advanced.Advanced'], // Import fieldConfiguration, filter, selectedClass property methods

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.common.field.filter.advanced.window.Window}
		 */
		controllerFilterWindow: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterAdvancedConfigurationIsPanelEnabled',
			'fieldFilterAdvancedFilterGet',
			'fieldFilterAdvancedFilterIsEmpty',
			'fieldFilterAdvancedSelectedClassGet',
			'fieldFilterAdvancedSelectedClassIsEmpty',
			'onUserAndGroupGroupTabPrivilegesTabWorkflowFieldFilterAdvancedWindowgetEndpoint = onFieldFilterAdvancedWindowgetEndpoint',
			'onUserAndGroupGroupTabPrivilegesTabWorkflowRemoveFilterClick',
			'onUserAndGroupGroupTabPrivilegesTabWorkflowSetFilterClick',
			'onUserAndGroupGroupTabPrivilegesTabWorkflowSetPrivilege',
			'onUserAndGroupGroupTabPrivilegesTabWorkflowShow'
		],

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.FieldConfiguration}
		 *
		 * @private
		 */
		fieldConfiguration: undefined,

		/**
		 * @property {CMDBuild.model.common.field.filter.advanced.Filter}
		 *
		 * @private
		 */
		filter: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.privileges.tabs.Workflow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.group.privileges.Privileges} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.privileges.tabs.Workflow', { delegate: this });

			// Filter advanced window configuration
			this.fieldFilterAdvancedConfigurationSet({ enabledPanels: ['attribute', 'function', 'columnPrivileges'] });

			// Build sub controller
			this.controllerFilterWindow = Ext.create('CMDBuild.controller.common.field.filter.advanced.window.Window', {
				parentDelegate: this,
				configuration: {
					mode: 'grid',
					tabs: {
						attributes: {
							selectAtRuntimeCheckDisabled: true // BUSINNESS RULE: user couldn't create privilege's filter with runtime parameters
						}
					}
				}
			});
		},

		/**
		 * @param {Object} resultObject
		 * @param {Object} resultObject.columnPrivileges
		 * @param {Object} resultObject.filter
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		onUserAndGroupGroupTabPrivilegesTabWorkflowFieldFilterAdvancedWindowgetEndpoint: function (resultObject) {
			if (Ext.encode(resultObject.filter).indexOf('"parameterType":"calculated"') < 0) {
				var params = {};
				params['privilegedObjectId'] = this.fieldFilterAdvancedSelectedClassGet(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(resultObject.columnPrivileges);
				params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(resultObject.filter);
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.userAndGroup.group.privileges.Workflow.setRowAndColumn({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowShow');
					}
				});
			} else {
				CMDBuild.core.Message.error(
					CMDBuild.Translation.error,
					CMDBuild.Translation.warnings.itIsNotAllowedFilterWithCalculatedParams,
					false
				);
			}
		},

		/**
		 * @param {CMDBuild.model.userAndGroup.group.privileges.GridRecord} record
		 *
		 * @returns {Void}
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onUserAndGroupGroupTabPrivilegesTabWorkflowRemoveFilterClick: function (record) {
			Ext.Msg.show({
				title: CMDBuild.Translation.attention,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				buttons: Ext.Msg.YESNO,
				scope: this,

				fn: function (buttonId, text, opt) {
					if (buttonId == 'yes') {
						var params = {};
						params['privilegedObjectId'] = record.get(CMDBuild.core.constants.Proxy.ID);
						params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

						// Set empty filter to clear value
						CMDBuild.proxy.userAndGroup.group.privileges.Workflow.setRowAndColumn({
							params: params,
							scope: this,
							success: function (response, options, decodedResponse) {
								this.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowShow');
							}
						});
					}
				}
			});
		},

		/**
		 * @param {CMDBuild.model.userAndGroup.group.privileges.GridRecord} record
		 *
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabPrivilegesTabWorkflowSetFilterClick: function (record) {
			// Filter advanced window configuration
			this.filter = Ext.create('CMDBuild.model.common.field.filter.advanced.Filter', { // Manual set to avoid label setup
				configuration: Ext.decode(record.get(CMDBuild.core.constants.Proxy.FILTER) || '{}'),
				entryType: record.get(CMDBuild.core.constants.Proxy.NAME)
			});
			this.selectedClass = _CMCache.getEntryTypeByName(record.get(CMDBuild.core.constants.Proxy.NAME)); // Manual setup to avoid filter setup

			this.controllerFilterWindow.fieldFilterAdvancedWindowSelectedRecordSet({ value: record });
			this.controllerFilterWindow.show();
		},

		/**
		 * @param {Object} parameters
		 * @param {Number} parameters.rowIndex
		 * @param {String} parameters.privilege
		 *
		 * @returns {Void}
		 *
		 * TODO: waiting for refactor (attributes names)
		 */
		onUserAndGroupGroupTabPrivilegesTabWorkflowSetPrivilege: function (parameters) {
			if (!Ext.isEmpty(parameters) && Ext.isObject(parameters)) {
				var params = {};
				params['privilege_mode'] = parameters.privilege;
				params['privilegedObjectId'] = this.view.store.getAt(parameters.rowIndex).get(CMDBuild.core.constants.Proxy.ID);
				params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

				CMDBuild.proxy.userAndGroup.group.privileges.Workflow.update({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						this.cmfg('onUserAndGroupGroupTabPrivilegesTabWorkflowShow');
					}
				});
			} else {
				_error('wrong or empty parameters in onUserAndGroupGroupTabPrivilegesTabWorkflowSetPrivilege()', this);
			}
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabPrivilegesTabWorkflowShow: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);

			this.view.getStore().load({ params: params });
		}
	});

})();
