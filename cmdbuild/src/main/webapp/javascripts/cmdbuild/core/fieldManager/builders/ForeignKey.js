(function () {

	/**
	 * Specific field attributes:
	 * 		- {String} filter: filter to apply
	 * 		- {String} targetClass: target class name
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.ForeignKey', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.ForeignKey'
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
		 * @returns {Object}
		 */
		buildEditor: function() {
			return this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN) ? {} : Ext.create('CMDBuild.view.common.field.comboBox.Searchable', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				attributeModel: this.cmfg('attributeModelGet'),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				displayField: 'Description',
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				plugins: new CMDBuild.SetValueOnLoadPlugin(),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				valueField: 'Id',

				store: this.buildFieldStore(),
				queryMode: 'local',

				templateResolver: this.cmfg('templateResolverBuild', [CMDBuild.core.constants.Proxy.FILTER]),
				resolveTemplates: this.cmfg('templateResolverGetResolveFunction'),

				listeners: {
					scope: this,
					added: function(field, container, pos, eOpts) {
						field.resolveTemplates();
					}
				}
			});
		},

		/**
		 * @returns {Ext.form.field.Text}
		 */
		buildField: function() {
			return Ext.create('CMDBuild.view.common.field.comboBox.Searchable', {
				allowBlank: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.MANDATORY),
				attributeModel: this.cmfg('attributeModelGet'),
				disabled: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				displayField: 'Description',
				fieldLabel: this.applyMandatoryLabelFlag(
					this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.DESCRIPTION)
					|| this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME)
				),
				hidden: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.HIDDEN),
				labelAlign: 'right',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				maxWidth: CMDBuild.core.constants.FieldWidths.STANDARD_BIG,
				name: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.NAME),
				plugins: new CMDBuild.SetValueOnLoadPlugin(),
				readOnly: !this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.WRITABLE),
				valueField: 'Id',

				store: this.buildFieldStore(),
				queryMode: 'local',

				templateResolver: this.cmfg('templateResolverBuild', [CMDBuild.core.constants.Proxy.FILTER]),
				resolveTemplates: this.cmfg('templateResolverGetResolveFunction'),

				listeners: {
					scope: this,
					added: function(field, container, pos, eOpts) {
						field.resolveTemplates();
					}
				}
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		buildFieldStore: function() {
			var extraParams = {};
			extraParams[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.TARGET_CLASS);

			if (!this.cmfg('attributeModelIsEmpty', CMDBuild.core.constants.Proxy.FILTER))
				extraParams[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode({ CQL: this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.FILTER) });

			return CMDBuild.proxy.common.field.ForeignKey.getStore({ extraParams: extraParams });
		}
	});

})();