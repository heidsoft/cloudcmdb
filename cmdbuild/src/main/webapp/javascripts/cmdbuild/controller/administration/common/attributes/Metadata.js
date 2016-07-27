(function() {

	Ext.define('CMDBuild.controller.administration.common.attributes.Metadata', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onMetadataWindowAbortButtonClick',
			'onMetadataWindowSaveButtonClick'
		],

		/**
		 * @cfg {Object}
		 */
		data: undefined,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		/**
		 * Metadata nameSpace
		 *
		 * @cfg {String}
		 */
		nameSpace: undefined,

		/**
		 * @property {CMDBuild.view.administration.common.attributes.metadata.MetadataWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.common.attributes.metadata.MetadataWindow', {
				delegate: this
			});

			// Shorthands
			this.grid = this.view.grid;

			this.loadData();

			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * @return {Object} data
		 *
		 * 	Example:
		 * 		{
		 * 			key1: value1,
		 * 			key2: value2,
		 * 			...
		 * 		}
		 */
		getData: function() {
			var data = {};

			// To validate and filter grid rows
			this.grid.getStore().each(function(record) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.KEY))
					&& !Ext.isEmpty(record.get(CMDBuild.core.constants.Proxy.VALUE))
				) {
					data[this.nameSpaceAdd(record.get(CMDBuild.core.constants.Proxy.KEY))] = record.get(CMDBuild.core.constants.Proxy.VALUE);
				}
			}, this);

			return data;
		},

		/**
		 * Loads data object in store
		 */
		loadData: function() {
			this.grid.getStore().removeAll();

			if (Ext.isObject(this.data))
				Ext.Object.each(this.data, function(key, value, myself) {
					if (key.indexOf(this.nameSpace) >= 0) { // Filters objects by nameSpace
						var recordConf = {};
						recordConf[CMDBuild.core.constants.Proxy.KEY] = this.nameSpaceStrip(key);
						recordConf[CMDBuild.core.constants.Proxy.VALUE] = value || '';

						this.grid.getStore().add(recordConf);
					}
				}, this);
		},

		/**
		 * @param {String} metadataKey
		 *
		 * @return {String}
		 */
		nameSpaceAdd: function(metadataKey) {
			if (
				!Ext.isEmpty(this.nameSpace)
				&& !Ext.isEmpty(metadataKey)
				&& Ext.isString(metadataKey)
			) {
				return this.nameSpace + metadataKey;
			}

			return metadataKey;
		},

		/**
		 * @param {String} metadataKey
		 *
		 * @return {String}
		 */
		nameSpaceStrip: function(metadataKey) {
			if (
				!Ext.isEmpty(this.nameSpace)
				&& !Ext.isEmpty(metadataKey)
				&& Ext.isString(metadataKey)
				&& metadataKey.indexOf(this.nameSpace) == 0
			) {
				return metadataKey.substr(this.nameSpace.length);
			}

			return metadataKey;
		},

		onMetadataWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onMetadataWindowSaveButtonClick: function() {
			this.parentDelegate.referenceFilterMetadata = this.getData();

			this.onMetadataWindowAbortButtonClick();
		}
	});

})();