(function () {

	Ext.define('CMDBuild.view.common.field.filter.cql.Cql', {
		extend: 'Ext.form.FieldContainer',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @property {CMDBuild.controller.common.field.filter.cql.Cql}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		buttonLabel: CMDBuild.Translation.editMetadata,

		/**
		 * @cfg {String}
		 */
		fieldName: CMDBuild.core.constants.Proxy.FILTER,

		/**
		 * @property {CMDBuild.core.buttons.iconized.Modify}
		 */
		metadataButton: undefined,

		/**
		 * @property {Ext.form.field.TextArea}
		 */
		textAreaField: undefined,

		considerAsFieldToDisable: true,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.controller.common.field.filter.cql.Cql', { view: this });

			Ext.apply(this, {
				items: [
					this.textAreaField = Ext.create('Ext.form.field.TextArea', {
						name: this.fieldName,
						vtype: 'comment'
					}),
					this.metadataButton = Ext.create('CMDBuild.core.buttons.iconized.Modify', {
						text: this.buttonLabel,
						maxWidth: this.buttonLabel.length * 10,
						scope: this,

						handler: function (button, e) {
							this.delegate.cmfg('onFieldFilterCqlMetadataButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		disable: function () {
			this.delegate.cmfg('onFieldFilterCqlDisable');

			this.callParent(arguments);
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		enable: function () {
			this.delegate.cmfg('onFieldFilterCqlEnable');

			this.callParent(arguments);
		},

		/**
		 * @returns {Object}
		 */
		getValue: function () {
			return this.delegate.cmfg('onFieldFilterCqlGetValue');
		},

		/**
		 * @param {Boolean} state
		 *
		 * @returns {CMDBuild.view.common.field.filter.cql.Cql}
		 *
		 * @override
		 */
		setDisabled: function (state) {
			this.delegate.cmfg('onFieldFilterCqlSetDisabled', state);

			return this.callParent(arguments);
		},

		/**
		 * @param {Object} value
		 *
		 * @returns {Void}
		 */
		setValue: function (value) {
			this.delegate.cmfg('onFieldFilterCqlSetValue', value);
		}
	});

})();
