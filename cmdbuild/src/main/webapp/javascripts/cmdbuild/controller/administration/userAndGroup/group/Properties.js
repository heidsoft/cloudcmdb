(function () {

	Ext.define('CMDBuild.controller.administration.userAndGroup.group.Properties', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.userAndGroup.group.Group',
			'CMDBuild.model.userAndGroup.group.Group'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.group.Group}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onUserAndGroupGroupTabPropertiesAbortButtonClick',
			'onUserAndGroupGroupTabPropertiesActiveStateToggleButtonClick',
			'onUserAndGroupGroupTabPropertiesAddButtonClick',
			'onUserAndGroupGroupTabPropertiesGroupSelected = onUserAndGroupGroupSelected',
			'onUserAndGroupGroupTabPropertiesModifyButtonClick',
			'onUserAndGroupGroupTabPropertiesSaveButtonClick',
			'onUserAndGroupGrouptabPropertiesShow'
		],

		/**
		 * @property {CMDBuild.view.administration.userAndGroup.group.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.userAndGroup.group.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.userAndGroup.group.Group} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.userAndGroup.group.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabPropertiesAbortButtonClick: function () {
			if (this.cmfg('userAndGroupGroupSelectedGroupIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.cmfg('onUserAndGroupGrouptabPropertiesShow');
			}
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabPropertiesActiveStateToggleButtonClick: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.GROUP_ID] = this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID);
			params[CMDBuild.core.constants.Proxy.IS_ACTIVE] = !this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ACTIVE);

			CMDBuild.proxy.userAndGroup.group.Group.enableDisable({
				params: params,
				scope: this,
				success: this.success
			});
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabPropertiesAddButtonClick: function () {
			this.cmfg('userAndGroupGroupSelectedGroupReset');
			this.cmfg('onUserAndGroupGroupSetActiveTab');

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.userAndGroup.group.Group'));
		},

		/**
		 * Enable/Disable tab evaluating selected group
		 *
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabPropertiesGroupSelected: function () {
			this.view.setDisabled(this.cmfg('userAndGroupGroupSelectedGroupIsEmpty'));
		},

		/**
		 * @returns {Void}
		 */
		onUserAndGroupGroupTabPropertiesModifyButtonClick: function () {
			this.form.setDisabledModify(false);
		},

		/**
		 * @returns {Void}
		 *
		 * TODO: waiting for a refactor (CRUD)
		 */
		onUserAndGroupGroupTabPropertiesSaveButtonClick: function () {
			if (this.validate(this.form)) { // Validate before save
				var params = this.form.getData(true);

				if (Ext.isEmpty(params[CMDBuild.core.constants.Proxy.ID])) {
					params[CMDBuild.core.constants.Proxy.ID] = -1;

					CMDBuild.proxy.userAndGroup.group.Group.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.userAndGroup.group.Group.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * @returns {Void}
		 *
		 * TODO: waiting for refactor (crud)
		 */
		onUserAndGroupGrouptabPropertiesShow: function () {
			if (!this.cmfg('userAndGroupGroupSelectedGroupIsEmpty'))
				CMDBuild.proxy.userAndGroup.group.Group.read({
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.GROUPS];

						var selectedGroupModel = Ext.Array.findBy(decodedResponse, function (groupObject, i) {
							return this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.ID) == groupObject[CMDBuild.core.constants.Proxy.ID];
						}, this);

						if (!Ext.isEmpty(selectedGroupModel)) {
							this.cmfg('userAndGroupGroupSelectedGroupSet', { value: selectedGroupModel }); // Update selectedGroup data (to delete on refactor)

							var params = {};
							params[CMDBuild.core.constants.Proxy.ACTIVE] = false;

							this.form.startingClassCombo.getStore().load({
								params: params,
								scope: this,
								callback: function (records, operation, success) {
									this.form.loadRecord(this.cmfg('userAndGroupGroupSelectedGroupGet'));
									this.form.activeStateToggleButton.setActiveState(this.cmfg('userAndGroupGroupSelectedGroupGet', CMDBuild.core.constants.Proxy.IS_ACTIVE));
									this.form.setDisabledModify(true, true);
								}
							});
						}
					}
				});
		},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		success: function (response, options, decodedResponse) {
			this.cmfg('mainViewportAccordionDeselect', this.cmfg('identifierGet'));
			this.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: this.cmfg('identifierGet'),
				nodeIdToSelect: decodedResponse[CMDBuild.core.constants.Proxy.GROUP][CMDBuild.core.constants.Proxy.ID]
			});

			this.form.setDisabledModify(true);
		}
	});

})();
