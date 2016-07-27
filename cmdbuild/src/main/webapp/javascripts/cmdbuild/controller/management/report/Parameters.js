(function () {

	Ext.define('CMDBuild.controller.management.report.Parameters', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.proxy.report.Report'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		attributeList: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportParametersWindowAbortButtonClick',
			'onReportParametersWindowPrintButtonClick'
		],

		/**
		 * @cfg {Boolean}
		 */
		forceDownload: false,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.report.ParametersWindow} emailWindows
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.report.ParametersWindow', { delegate: this });

			// ShortHands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view)) {
				this.buildFields();

				this.setViewTitle(this.cmfg('selectedReportRecordGet', CMDBuild.core.constants.Proxy.DESCRIPTION));

				this.view.show();
			}
		},

		/**
		 * @private
		 */
		buildFields: function () {
			if (this.attributeList.length > 0) {
				var fieldManager = Ext.create('CMDBuild.core.fieldManager.FieldManager', {
					parentDelegate: this,
					targetForm: this.form
				});

				Ext.Array.each(this.attributeList, function (attribute, i, allAttributes) {
					if (fieldManager.isAttributeManaged(attribute[CMDBuild.core.constants.Proxy.TYPE])) {
						var attributeCustom = Ext.create('CMDBuild.model.common.attributes.Attribute', attribute);
						attributeCustom.setAdaptedData(attribute);

						fieldManager.attributeModelSet(attributeCustom);
						fieldManager.add(this.form, fieldManager.buildField());
					} else { // @deprecated - Old field manager
						var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

						if (!Ext.isEmpty(field)) {
							field.maxWidth = field.width || CMDBuild.core.constants.FieldWidths.STANDARD_MEDIUM;

							if (attribute.defaultvalue)
								field.setValue(attribute.defaultvalue);

							this.form.add(field);
						}
					}
				}, this);
			}
		},

		onReportParametersWindowAbortButtonClick: function () {
			this.view.destroy();
		},

		onReportParametersWindowPrintButtonClick: function () {
			if (this.view.form.getForm().isValid()) {
				this.cmfg('selectedReportParametersSet', {
					callIdentifier: 'update',
					params: this.form.getValues()
				});

				this.cmfg('updateReport', this.forceDownload);

				this.onReportParametersWindowAbortButtonClick();
			}
		}
	});

})();
