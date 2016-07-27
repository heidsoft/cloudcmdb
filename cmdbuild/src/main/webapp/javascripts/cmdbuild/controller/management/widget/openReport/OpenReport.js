(function () {

	Ext.define('CMDBuild.controller.management.widget.openReport.OpenReport', {
		extend:'CMDBuild.controller.common.abstract.Widget',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.OpenReport'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'beforeHideView',
			'getData',
			'isValid',
			'onBeforeSave',
			'onEditMode',
			'onWidgetOpenReportBeforeActiveView = beforeActiveView',
			'onWidgetOpenReportDownloadButtonClick',
			'onWidgetOpenReportSaveButtonClick',
			'widgetConfigurationGet = widgetOpenReportConfigurationGet',
			'widgetConfigurationIsEmpty = widgetOpenReportConfigurationIsEmpty'
		],

		/**
		 * @property {CMDBuild.view.management.widget.openReport.OpenReportView}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.openReport.Configuration',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onWidgetOpenReportBeforeActiveView: function () {
			this.beforeActiveView(arguments); // CallParent alias

			if (!this.cmfg('widgetOpenReportConfigurationIsEmpty')) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CODE] = this.cmfg('widgetOpenReportConfigurationGet', CMDBuild.core.constants.Proxy.REPORT_CODE);
				params[CMDBuild.core.constants.Proxy.TYPE] = CMDBuild.core.constants.Proxy.CUSTOM;

				CMDBuild.proxy.widget.OpenReport.createFactory({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						if (!decodedResponse['filled'])
							this.configureForm(decodedResponse[CMDBuild.core.constants.Proxy.ATTRIBUTE]);

						new CMDBuild.Management.TemplateResolver({
							clientForm: this.clientForm,
							xaVars: this.cmfg('widgetOpenReportConfigurationGet', CMDBuild.core.constants.Proxy.PRESET),
							serverVars: this.getTemplateResolverServerVars()
						}).resolveTemplates({
							attributes: Ext.Object.getKeys(this.cmfg('widgetOpenReportConfigurationGet', CMDBuild.core.constants.Proxy.PRESET)),
							scope: this,
							callback: function (out, ctx) {
								this.fillFormValues(out);
								this.forceExtension(this.cmfg('widgetOpenReportConfigurationGet', CMDBuild.core.constants.Proxy.FORCE_FORMAT));
							}
						});
					}
				});
			}
		},

		/**
		 * Build server call to configure and create reports
		 *
		 * @returns {Void}
		 */
		onWidgetOpenReportSaveButtonClick: function () {
			if (this.validate(this.view)) {
				var params = Ext.apply(this.view.getData(true), this.view.getValues()); // Cannot use only getData() because of date field format errors
				params['reportExtension'] = this.view.formatCombo.getValue();

				CMDBuild.proxy.widget.OpenReport.update({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) { // Pop-up display mode
						Ext.create('CMDBuild.controller.management.widget.openReport.Modal', {
							parentDelegate: this,
							extension: this.view.formatCombo.getValue()
						});
					}
				});
			}
		},

		/**
		 * Add the required attributes and disable fields if in readOnlyAttributes array
		 *
		 * @param {Array} attributes
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		configureForm: function (attributes) {
			this.view.fieldContainer.removeAll();

			if (!Ext.isEmpty(attributes) && Ext.isArray(attributes)) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', { parentDelegate: this });

				Ext.Array.each(attributes, function (attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute[CMDBuild.core.constants.Proxy.TYPE])) {
						var attributeCustom = Ext.create('CMDBuild.model.common.attributes.Attribute', attribute);
						attributeCustom.setAdaptedData(attribute);

						fieldManager.attributeModelSet(attributeCustom);

						var field = fieldManager.buildField();
					} else { // @deprecated - Old field manager
						var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

						if (!Ext.isEmpty(field)) {
							field.maxWidth = field.width || CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM;

							if (attribute.defaultvalue)
								field.setValue(attribute.defaultvalue);
						}
					}

					if (!Ext.isEmpty(field)) {
						// Disable if field name is contained in widgetConfiguration.readOnlyAttributes
						field.setDisabled(
							Ext.Array.contains(this.cmfg('widgetOpenReportConfigurationGet', CMDBuild.core.constants.Proxy.READ_ONLY_ATTRIBUTES), attribute[CMDBuild.core.constants.Proxy.NAME])
						);

						this.view.fieldContainer.add(field);
					}
				}, this);
			}
		},

		/**
		 * Fixes date format
		 *
		 * @param {Object} parameters - Ex: { input_name: value, ...}
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		fillFormValues: function (parameters) {
			if (!Ext.Object.isEmpty(parameters)) {
				Ext.Object.each(parameters, function (key, value, myself) {
					if (Ext.isDate(value))
						parameters[key] =  new Date(value);
				}, this);

				this.view.loadRecord(Ext.create('CMDBuild.model.common.Generic', parameters));
			}
		},

		/**
		 * @param {String} extension
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		forceExtension: function (extension) {
			if (!Ext.isEmpty(extension)) {
				this.view.formatCombo.setValue(extension);
				this.view.formatCombo.disable();
			} else {
				this.view.formatCombo.enable();
			}
		},

		/**
		 * @param {Boolean} forceDownload
		 *
		 * @returns {Void}
		 */
		onWidgetOpenReportDownloadButtonClick: function () {
			var params = {};
			params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

			CMDBuild.proxy.widget.OpenReport.download({
				buildRuntimeForm: true,
				params: params
			});
		}
	});

})();
