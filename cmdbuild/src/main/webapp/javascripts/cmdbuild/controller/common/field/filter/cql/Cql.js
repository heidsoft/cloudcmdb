(function () {

	/**
	 * Field to configure a CQL filter with an expression and extra parameters.
	 *
	 * Managed CMDBuild filter format:
	 * 	{
	 *		{String} expression
	 * 		{Object} context
	 * 	}
	 */
	Ext.define('CMDBuild.controller.common.field.filter.cql.Cql', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'fieldFilterCqlFilterGet',
			'fieldFilterCqlFilterIsAttributeEmpty',
			'fieldFilterCqlFilterSet',
			'onFieldFilterCqlDisable',
			'onFieldFilterCqlEnable',
			'onFieldFilterCqlGetValue',
			'onFieldFilterCqlMetadataButtonClick',
			'onFieldFilterCqlSetDisabled',
			'onFieldFilterCqlSetValue'
		],

		/**
		 * @property {CMDBuild.controller.common.field.filter.cql.Metadata}
		 */
		controllerMetadata: undefined,

		/**
		 * @property {CMDBuild.model.common.field.filter.cql.Cql}
		 *
		 * @private
		 */
		filterModel: undefined,

		/**
		 * @cfg {CMDBuild.view.common.field.filter.cql.Cql}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.common.field.filter.cql.Cql} configurationObject.view
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.controllerMetadata = Ext.create('CMDBuild.controller.common.field.filter.cql.Metadata', { parentDelegate: this });
		},

		// Filter methods
			/**
			 * @param {String} attributeName
			 *
			 * @returns {Mixed}
			 */
			fieldFilterCqlFilterGet: function (attributeName) {
				if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName))
					return this.filterModel.get(attributeName);

				return this.filterModel;
			},

			/**
			 * @param {String} attributeName
			 *
			 * @returns {Boolean}
			 */
			fieldFilterCqlFilterIsAttributeEmpty: function (attributeName) {
				if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName))
					if (
						!Ext.isEmpty(this.filterModel)
						&& Ext.isObject(this.filterModel)
						&& Ext.isFunction(this.filterModel.get)
					) {
						return Ext.isEmpty(this.filterModel.get(attributeName));
					}

				return true;
			},

			/**
			 * @param {Object} parameters
			 * @param {Object} parameters.filterObject
			 * @param {String} parameters.propertyName
			 *
			 * @returns {Void}
			 */
			fieldFilterCqlFilterSet: function (parameters) {
				if (Ext.isEmpty(parameters)) {
					this.filterModel = Ext.create('CMDBuild.model.common.field.filter.cql.Cql');
				} else {
					var filterObject = parameters.filterObject;
					var propertyName = parameters.propertyName;

					if (Ext.isEmpty(this.filterModel))
						this.filterModel = Ext.create('CMDBuild.model.common.field.filter.cql.Cql');

					if (!Ext.isEmpty(filterObject))
						if (!Ext.isEmpty(propertyName) && Ext.isString(propertyName)) { // Property model setup
							this.filterModel.set(propertyName, filterObject);
						} else { // Full model setup
							this.filterModel = Ext.create('CMDBuild.model.common.field.filter.cql.Cql', filterObject);
						}
				}
			},

		/**
		 * @returns {Void}
		 */
		onFieldFilterCqlDisable: function () {
			this.view.metadataButton.disable();
			this.view.textAreaField.disable();
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterCqlEnable: function () {
			this.view.metadataButton.enable();
			this.view.textAreaField.enable();
		},

		/**
		 * @returns {Object}
		 */
		onFieldFilterCqlGetValue: function () {
			this.fieldFilterCqlFilterSet({
				filterObject: this.view.textAreaField.getValue(),
				propertyName: CMDBuild.core.constants.Proxy.EXPRESSION
			});

			return this.fieldFilterCqlFilterGet().getData();
		},

		/**
		 * @returns {Void}
		 */
		onFieldFilterCqlMetadataButtonClick: function () {
			this.controllerMetadata.show();
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {Void}
		 */
		onFieldFilterCqlSetDisabled: function (state) {
			this.view.metadataButton.setDisabled(state);
			this.view.textAreaField.setDisabled(state);
		},

		/**
		 * @param {Object} filterObjectValue
		 *
		 * @returns {Void}
		 */
		onFieldFilterCqlSetValue: function (filterObjectValue) {
			if (
				Ext.isObject(filterObjectValue)
				&& filterObjectValue.hasOwnProperty(CMDBuild.core.constants.Proxy.EXPRESSION)
				&& filterObjectValue.hasOwnProperty(CMDBuild.core.constants.Proxy.CONTEXT)
			) {
				this.fieldFilterCqlFilterSet({ filterObject: filterObjectValue });

				this.view.textAreaField.setValue(this.fieldFilterCqlFilterGet(CMDBuild.core.constants.Proxy.EXPRESSION));
			}
		}
	});

})();
