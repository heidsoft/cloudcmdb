(function () {

	Ext.define('CMDBuild.controller.administration.navigationTree.Properties', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.navigationTree.NavigationTree}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'navigationTreeTabPropertiesFormGet',
			'onNavigationTreeTabPropertiesAbortButtonClick',
			'onNavigationTreeTabPropertiesAddButtonClick',
			'onNavigationTreeTabPropertiesModifyButtonClick',
			'onNavigationTreeTabPropertiesTreeSelected = onNavigationTreeSelected'
		],

		/**
		 * @property {CMDBuild.view.administration.navigationTree.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.navigationTree.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.navigationTree.NavigationTree} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.navigationTree.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @returns {CMDBuild.view.administration.navigationTree.properties.PropertiesView or null}
		 */
		navigationTreeTabPropertiesFormGet: function () {
			if (!Ext.isEmpty(this.form))
				return this.form;

			return null;
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeTabPropertiesAbortButtonClick: function () {
			if (this.cmfg('navigationTreeSelectedTreeIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.cmfg('onNavigationTreeTabPropertiesTreeSelected');
			}
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeTabPropertiesAddButtonClick: function () {
			this.view.setDisabled(false);

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.navigationTree.NavigationTree'));
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeTabPropertiesModifyButtonClick: function () {
			this.form.setDisabledModify(false);
		},

		/**
		 * @returns {Void}
		 */
		onNavigationTreeTabPropertiesTreeSelected: function () {
			this.view.setDisabled(this.cmfg('navigationTreeSelectedTreeIsEmpty'));

			this.form.reset();

			if (this.cmfg('navigationTreeSelectedTreeIsEmpty')) {
				this.form.setDisabledModify(true, true, true, true);
			} else {
				this.form.setDisabledModify(true);
				this.form.loadRecord(this.cmfg('navigationTreeSelectedTreeGet'));
			}
		}
	});

})();
