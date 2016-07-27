(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.text.HtmlEditor', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : Ext.create('Ext.grid.column.Column', {
				dataIndex: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				editor: withEditor ? this.buildEditor() : null,
				flex: 1,
				hidden: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.SHOW_COLUMN),
				renderer: this.rendererColumn,
				scope: this,
				sortable: true,
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION))
			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.HtmlEditor}
		 */
		buildEditor: function() {
			return this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : Ext.create('CMDBuild.view.common.field.HtmlEditor', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				minWidth: CMDBuild.core.constants.FieldWidths.EDITOR_HTML,
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE)
			});
		},

		/**
		 * @returns {CMDBuild.view.common.field.HtmlEditor}
		 */
		buildField: function() {
			return Ext.create('CMDBuild.view.common.field.HtmlEditor', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				hidden: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.EDITOR_HTML,
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE)
			});
		},

		/**
		 * @param {Object} value
		 * @param {Object} metadata
		 * @param {Ext.data.Model} record
		 * @param {Number} rowIndex
		 * @param {Number} colIndex
		 * @param {Ext.data.Store} store
		 * @param {Ext.view.View} view
		 *
		 * @override
		 */
		rendererColumn: function(value, metadata, record, rowIndex, colIndex, store, view) {
			return Ext.util.Format.stripTags(this.callParent(arguments));
		}
	});

})();
