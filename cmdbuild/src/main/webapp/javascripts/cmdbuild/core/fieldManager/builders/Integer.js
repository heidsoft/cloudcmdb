(function () {

	Ext.define('CMDBuild.core.fieldManager.builders.Integer', {
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
		 *
		 * NOTE: cannot implement Ext.grid.column.Number because don't recognize not anglosaxon number formats
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
				text: this.applyMandatoryLabelFlag(this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)),
				width: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME).length * 9
			});
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : {
				xtype: 'numberfield',
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				hideTrigger: true, // Hides selecting arrows
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				scale: 0,
				vtype: 'numeric'
			};
		},

		/**
		 * @returns {Ext.form.field.Number}
		 */
		buildField: function() {
			return Ext.create('Ext.form.field.Number', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				hidden: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				hideTrigger: true, // Hides selecting arrows
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_SMALL,
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				scale: 0,
				vtype: 'numeric'
			});
		},

		/**
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return { name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME), type: 'int', useNull: true };
		}
	});

})();