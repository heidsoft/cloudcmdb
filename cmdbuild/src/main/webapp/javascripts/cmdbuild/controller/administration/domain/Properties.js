(function() {

	Ext.define('CMDBuild.controller.administration.domain.Properties', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.domain.Domain}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'domainPropertiesDataGet',
			'onDomainPropertiesAbortButtonClick',
			'onDomainPropertiesAddButtonClick',
			'onDomainPropertiesCardinalitySelect',
			'onDomainPropertiesDomainSelected = onDomainSelected',
			'onDomainPropertiesMasterDetailCheckboxChange',
			'onDomainPropertiesModifyButtonClick',
			'onDomainPropertiesNameChange'
		],

		/**
		 * @property {CMDBuild.view.administration.domain.properties.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {CMDBuild.view.administration.domain.properties.PropertiesView}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.domain.Domain} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.domain.properties.PropertiesView', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * Forwarder method
		 *
		 * @returns {Object}
		 */
		domainPropertiesDataGet: function() {
//			return Ext.create('CMDBuild.model.domain.Domain', this.form.getData(true)).getDataForSubmit();
			return this.form.getData(true);
		},

		onDomainPropertiesAbortButtonClick: function() {
			if (this.cmfg('domainSelectedDomainIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true, true, true);
			} else {
				this.onDomainPropertiesDomainSelected();
			}
		},

		onDomainPropertiesAddButtonClick: function() {
			this.cmfg('domainSelectedDomainReset');

			this.form.reset();
			this.form.setDisabledModify(false, true);
			this.form.loadRecord(Ext.create('CMDBuild.model.domain.Domain'));

			this.onDomainPropertiesCardinalitySelect(); // Execute cardinality selection event actions to disable masterDetailCheckbox
		},

		/**
		 * A domain could set MD only if the cardinality is '1:N' or 'N:1'
		 */
		onDomainPropertiesCardinalitySelect: function() {
			if (
				!Ext.isEmpty(this.form.cardinalityCombo.getValue())
				&& (
					this.form.cardinalityCombo.getValue() == '1:N'
					|| this.form.cardinalityCombo.getValue() == 'N:1'
				)
			) {
				this.form.masterDetailCheckbox.enable();
			} else {
				this.form.masterDetailCheckbox.setValue(false);
				this.form.masterDetailCheckbox.disable();
			}
		},

		onDomainPropertiesDomainSelected: function() {
			if (!this.cmfg('domainSelectedDomainIsEmpty')) {
				this.form.reset();
				this.form.setDisabledModify(true);
				this.form.loadRecord(this.cmfg('domainSelectedDomainGet'));
			}
		},

		/**
		 * Show the masterDetailLabel field only when the domain is setted as a masterDetail
		 */
		onDomainPropertiesMasterDetailCheckboxChange: function() {
			if (this.form.masterDetailCheckbox.getValue()) {
				this.form.masterDetailLabel.show();
				this.form.masterDetailLabel.setDisabled(this.form.masterDetailCheckbox.isDisabled());
			} else {
				this.form.masterDetailLabel.hide();
				this.form.masterDetailLabel.disable();
			}
		},

		onDomainPropertiesModifyButtonClick: function() {
			this.form.setDisabledModify(false);

			this.onDomainPropertiesCardinalitySelect(); // Execute cardinality selection event actions to disable masterDetailCheckbox
		},

		/**
		 * Synchronize name and description fields
		 *
		 * @param {Object} parameters
		 * @param {String} parameters.newValue
		 * @param {String} parameters.oldValue
		 */
		onDomainPropertiesNameChange: function(parameters) {
			if (Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)) {
				var actualValue = this.form.domainDescription.getValue();

				if (Ext.isEmpty(actualValue) || actualValue == parameters.oldValue )
					this.form.domainDescription.setValue(parameters.newValue);
			}
		}
	});

})();