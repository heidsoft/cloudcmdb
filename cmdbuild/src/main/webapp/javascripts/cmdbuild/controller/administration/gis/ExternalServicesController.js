(function() {

	Ext.define('CMDBuild.controller.administration.gis.ExternalServicesController', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.proxy.configuration.Gis'
		],

		view: undefined,

		/**
		 * @param {CMDBuild.view.administration.gis.ExternalServices} view
		 *
		 * @overwrite
		 */
		constructor: function(view) {
			this.callParent(arguments);

			this.view.delegate = this;
		},

		/**
		 * @param {Object} parameters - AccordionStoreModel
		 *
		 * @overwrite
		 */
		onViewOnFront: function(parameters) {
			this.callParent(arguments);

			this.getConfigFromServer();
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @overwrite
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onFieldsetExpand':
					return this.onFieldsetExpand(param);

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

//				case 'onShow':
//					return this.getConfigFromServer();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {Object} data - CMDBuild.proxy.configuration.Gis.read response
		 */
		collapseFieldsets: function(data) {
			Ext.Array.each(this.view.services, function(itemService, indexService, allItemsService) {
				if (data[itemService] == 'on') {
					Ext.Array.each(this.view.wrapper.items.items, function(itemWrapper, indexWrapper, allItemsWrapper) {
						if (itemWrapper.serviceName == itemService)
							itemWrapper.expand();
					}, this);
				}
			}, this);
		},

		/**
		 * @param {Object} data - CMDBuild.proxy.configuration.Gis.read response
		 */
		fillForm: function(data) {
			for (var name in data) {
				var field = this.view.getForm().findField(name);

				if (field != null)
					field.setValue(data[name]);
			}
		},

		/**
		 * Reads configuration from server call, fill form with response and expands all active fieldset
		 */
		getConfigFromServer: function() {
			CMDBuild.proxy.configuration.Gis.read({
				scope: this,
				success: function(result, options, decodedResult) {
					this.fillForm(decodedResult.data);
					this.collapseFieldsets(decodedResult.data);
				}
			});
		},

		/**
		 * @return {Object} values
		 */
		getValues: function() {
			var values = {};

			Ext.Array.each(this.view.wrapper.items.items, function(item, index, allItems) {
				if (typeof item.serviceName == 'string') {
					if (item.collapsed) {
						values[item.serviceName] = 'off';
					} else {
						values[item.serviceName] = 'on';

						item.cascade(function(subItem) {
							if (typeof subItem.getValue == 'function')
								values[subItem.name] = subItem.getValue();
						});
					}
				}
			}, this);

			return values;
		},

		/**
		 * @param {String} expandedName
		 */
		onFieldsetExpand: function(expandedName) {
			Ext.Array.each(this.view.wrapper.items.items, function(item, index, allItems) {
				if (item.serviceName != expandedName && item.serviceName != 'geoserver')
					item.collapse();
			}, this);
		},

		onSaveButtonClick: function() {
			var values = this.getValues();

			CMDBuild.proxy.configuration.Gis.update({
				params: values,
				success: function() {
					CMDBuild.core.Message.success();

					Ext.create('CMDBuild.core.configurations.builder.Gis'); // Rebuild configuration model
				}
			});
		}
	});

})();