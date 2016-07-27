(function () {

	Ext.define('CMDBuild.view.administration.widget.form.CalendarPanel', {
		extend: 'CMDBuild.view.administration.widget.form.AbstractWidgetDefinitionPanel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.Calendar'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.form.Calendar}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Erasable}
		 */
		defaultDate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Erasable}
		 */
		endDate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Erasable}
		 */
		eventTitle: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Erasable}
		 */
		startDate: undefined,

		/**
		 * @returns {Array}
		 *
		 * @override
		 */
		widgetDefinitionFormAdditionalPropertiesGet: function () {
			return [
				Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.additionalProperties,
					flex: 1,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [
						Ext.create('Ext.form.field.ComboBox', {
							name: CMDBuild.core.constants.Proxy.EVENT_CLASS,
							fieldLabel: CMDBuild.Translation.targetClass,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							valueField: CMDBuild.core.constants.Proxy.NAME,
							displayField: CMDBuild.core.constants.Proxy.TEXT, // TODO: waiting for refactor (rename description)
							editable: false,
							forceSelection: true,

							store: CMDBuild.proxy.widget.Calendar.getStoreTargetClass(),
							queryMode: 'local',

							listeners: {
								scope: this,
								change: function (combo, newValue, oldValue, eOpts) {
									this.delegate.cmfg('onClassTabWidgetCalendarTargetClassChange', newValue);
								}
							}
						}),
						this.startDate = Ext.create('CMDBuild.view.common.field.comboBox.Erasable', {
							name: CMDBuild.core.constants.Proxy.START_DATE,
							fieldLabel: CMDBuild.Translation.startDate,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							valueField: CMDBuild.core.constants.Proxy.NAME,
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							editable: false,
							forceSelection: true,

							store: CMDBuild.proxy.widget.Calendar.getStoreAttributesDate(),
							queryMode: 'local'
						}),
						this.endDate = Ext.create('CMDBuild.view.common.field.comboBox.Erasable', {
							name: CMDBuild.core.constants.Proxy.END_DATE,
							fieldLabel: CMDBuild.Translation.endDate,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							valueField: CMDBuild.core.constants.Proxy.NAME,
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							editable: false,
							forceSelection: true,

							store: CMDBuild.proxy.widget.Calendar.getStoreAttributesDate(),
							queryMode: 'local'
						}),
						this.defaultDate = Ext.create('CMDBuild.view.common.field.comboBox.Erasable', {
							name: CMDBuild.core.constants.Proxy.DEFAULT_DATE,
							fieldLabel: CMDBuild.Translation.defaultDateToOpen,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							valueField: CMDBuild.core.constants.Proxy.NAME,
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							editable: false,
							forceSelection: true,

							store: CMDBuild.proxy.widget.Calendar.getStoreAttributesDate(),
							queryMode: 'local'
						}),
						this.eventTitle = Ext.create('CMDBuild.view.common.field.comboBox.Erasable', {
							name: CMDBuild.core.constants.Proxy.EVENT_TITLE,
							fieldLabel: CMDBuild.Translation.eventTitle,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
							valueField: CMDBuild.core.constants.Proxy.NAME,
							displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
							editable: false,
							forceSelection: true,

							store: CMDBuild.proxy.widget.Calendar.getStoreAttributesString(),
							queryMode: 'local'
						}),
						Ext.create('Ext.form.field.TextArea', {
							name: CMDBuild.core.constants.Proxy.FILTER,
							fieldLabel: CMDBuild.Translation.cqlFilter,
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
						})
					]
				})
			];
		}
	});

})();
