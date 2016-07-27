(function () {

	Ext.define('CMDBuild.controller.management.widget.customForm.RowEdit', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.management.widget.customForm.layout.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetCustomFormRowEditWindowAbortButtonClick',
			'onWidgetCustomFormRowEditWindowSaveButtonClick'
		],

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @cfg {Ext.data.Model}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.view.management.widget.customForm.RowEditWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.widget.customForm.layout.Grid} configurationObject.parentDelegate
		 * @param {Object} configurationObject.record
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.widget.customForm.RowEditWindow', { delegate: this });

			// Shorthand
			this.form = this.view.form;

			this.form.add(this.buildFields());

			this.fieldsInitialization();

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * @returns {Array} itemsArray
		 *
		 * @private
		 */
		buildFields: function () {
			var itemsArray = [];

			if (!this.cmfg('widgetCustomFormConfigurationIsEmpty',  CMDBuild.core.constants.Proxy.MODEL)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function (attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute.get(CMDBuild.core.constants.Proxy.TYPE))) {
						fieldManager.attributeModelSet(Ext.create('CMDBuild.model.common.attributes.Attribute', attribute.getData()));
						fieldManager.push(itemsArray, fieldManager.buildField());
					} else { // @deprecated - Old field manager
						var attribute = attribute.getAdaptedData();
						var item = undefined;

						if (attribute.type == 'REFERENCE') { // TODO: hack to force a templateResolver build for editor that haven't a form associated like other fields types
							var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, 'system.template.');
							xaVars['_SystemFieldFilter'] = attribute.filter;

							var templateResolver = new CMDBuild.Management.TemplateResolver({
								clientForm: this.cmfg('widgetCustomFormControllerPropertyGet', 'clientForm'),
								xaVars: xaVars,
								serverVars: this.cmfg('widgetCustomFormGetTemplateResolverServerVars')
							});

							// Required label fix
							if (attribute[CMDBuild.core.constants.Proxy.MANDATORY] || attribute['isnotnull']) {
								attribute[CMDBuild.core.constants.Proxy.DESCRIPTION] = (!Ext.isEmpty(attribute['isnotnull']) && attribute['isnotnull'] ? '* ' : '')
								+ attribute.description || attribute.name;
							}

							item = CMDBuild.Management.ReferenceField.buildEditor(attribute, templateResolver);

							// Force execution of template resolver
							if (!Ext.isEmpty(item) && Ext.isFunction(item.resolveTemplate))
								item.resolveTemplate();
						} else {
							item = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);
						}

						if (attribute[CMDBuild.core.constants.Proxy.FIELD_MODE] == 'read')
							item.setDisabled(true);

						// Force execution of template resolver
						if (!Ext.isEmpty(item) && Ext.isFunction(item.resolveTemplate))
							item.resolveTemplate();

						itemsArray.push(item);
					}
				}, this);
			}

			return itemsArray;
		},

		/**
		 * Calls field template resolver, store load and loads record only at the end of all store loads
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		fieldsInitialization: function () {
			this.view.setLoading(true);

			var requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
				id: 'widgetCustomFormRowEditBarrier',
				scope: this,
				callback: function () {
					this.form.loadRecord(this.record);

					this.view.setLoading(false);
				}
			});

			Ext.Array.forEach(this.form.getForm().getFields().getRange(), function (field, i, allFields) {
				if (!Ext.Object.isEmpty(field) && !Ext.isEmpty(field.resolveTemplate))
					field.resolveTemplate();

				// Force editor fields store load (must be done because FieldManager field don't works properly)
				// TODO: waiting for full FiledManager v2 implementation
				if (!Ext.Object.isEmpty(field) && Ext.isFunction(field.getStore) && field.getStore().count() == 0)
					field.getStore().load({
						scope: this,
						callback: requestBarrier.getCallback('widgetCustomFormRowEditBarrier')
					});
			}, this);

			requestBarrier.finalize('widgetCustomFormRowEditBarrier', true);
		},

		/**
		 * @returns {Void}
		 */
		onWidgetCustomFormRowEditWindowAbortButtonClick: function () {
			this.view.destroy();
		},

		/**
		 * Saves data to widget's grid
		 *
		 * @returns {Void}
		 */
		onWidgetCustomFormRowEditWindowSaveButtonClick: function () {
			Ext.Object.each(this.form.getValues(), function (key, value, myself) {
				this.record.set(key, value);
			}, this);

			this.record.commit();

			this.cmfg('onWidgetCustomFormRowEditWindowAbortButtonClick');
		}
	});

})();
