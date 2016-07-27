(function() {

	Ext.define('CMDBuild.controller.administration.lookup.Properties', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.lookup.Type',
			'CMDBuild.model.lookup.Type'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.lookup.Lookup}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLookupPropertiesAbortButtonClick',
			'onLookupPropertiesAddButtonClick',
			'onLookupPropertiesLookupSelected = onLookupSelected',
			'onLookupPropertiesModifyButtonClick',
			'onLookupPropertiesSaveButtonClick'
		],

		/**
		 * @property {CMDBuild.view.administration.lookup.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.lookup.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.lookup.Lookup} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.lookup.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		onLookupPropertiesAbortButtonClick: function() {
			if (this.cmfg('lookupSelectedLookupTypeIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onLookupPropertiesLookupSelected();
			}
		},

		onLookupPropertiesAddButtonClick: function() {
			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.lookup.Type'));

			this.form.parentCombobox.getStore().load(); // Force store to be reloaded
		},

		onLookupPropertiesLookupSelected: function() {
			this.view.setDisabled(this.cmfg('lookupSelectedLookupTypeIsEmpty'));

			if (!this.cmfg('lookupSelectedLookupTypeIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true);
				this.form.loadRecord(this.cmfg('lookupSelectedLookupTypeGet'));
			}
		},

		onLookupPropertiesModifyButtonClick: function() {
			this.form.setDisabledModify(false);
		},

		onLookupPropertiesSaveButtonClick: function() {
			if (this.validate(this.form)) {
				var params = this.form.getData(true);
				params['orig_type'] = params[CMDBuild.core.constants.Proxy.ID]; // TODO: wrong server implementation to fix

				if (Ext.isEmpty(params[CMDBuild.core.constants.Proxy.ID])) {
					CMDBuild.proxy.lookup.Type.create({
						params: params,
						scope: this,
						success: this.success
					});
				} else {
					CMDBuild.proxy.lookup.Type.update({
						params: params,
						scope: this,
						success: this.success
					});
				}
			}
		},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 *
		 * TODO: waiting for refactor (crud)
		 */
		success: function(response, options, decodedResponse) {
			if (!Ext.isEmpty(decodedResponse.isNew)) {
				_CMCache.onNewLookupType(decodedResponse[CMDBuild.core.constants.Proxy.LOOKUP]);
			} else {
				_CMCache.onModifyLookupType(decodedResponse[CMDBuild.core.constants.Proxy.LOOKUP]);
			}

			this.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: this.cmfg('identifierGet'),
				nodeIdToSelect: decodedResponse[CMDBuild.core.constants.Proxy.LOOKUP][CMDBuild.core.constants.Proxy.ID]
			});

			this.form.setDisabledModify(true);
		}
	});

})();