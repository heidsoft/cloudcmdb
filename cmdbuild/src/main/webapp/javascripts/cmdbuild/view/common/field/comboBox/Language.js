(function () {

	Ext.define('CMDBuild.view.common.field.comboBox.Language', {
		extend: 'Ext.form.field.ComboBox',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.common.field.comboBox.Language'
		],

		/**
		 * @cfg {CMDBuild.controller.common.field.comboBox.Language}
		 */
		delegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		enableChangeLanguage: true,

		displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
		editable: false,
		fieldCls: 'ux-icon-combo-input ux-icon-combo-item',
		forceSelection: true,
		iconClsField: CMDBuild.core.constants.Proxy.TAG,
		iconClsPrefix: 'ux-flag-',
		valueField: CMDBuild.core.constants.Proxy.TAG,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				delegate: Ext.create('CMDBuild.controller.common.field.comboBox.Language', { view: this }),
				tpl: new Ext.XTemplate(
					'<tpl for=".">',
						'<div class="x-boundlist-item x-combo-list-item ux-icon-combo-item ' + this.iconClsPrefix + '{' + this.iconClsField + '}">{' + this.displayField + '}</div>',
					'</tpl>'
				),
				store: CMDBuild.proxy.common.field.comboBox.Language.getStore(),
				queryMode: 'local'
			});

			this.callParent(arguments);

			this.getStore().on('load', function (store, records, successful, eOpts) {
				this.delegate.cmfg('onFieldComboBoxLanguageStoreLoad');
			}, this);
		},

		listeners: {
			select: function (field, records, eOpts) {
				this.delegate.cmfg('onFieldComboBoxLanguageSelect', records[0]);
			}
		},

		/**
		 * @param {String} value
		 *
		 * @returns {Void}
		 *
		 * @public
		 */
		setValue: function (value) {
			this.delegate.cmfg('onFieldComboBoxLanguageValueSet', value);

			this.callParent(arguments);
		}
	});

})();
