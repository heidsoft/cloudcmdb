(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm', {
		extend: 'Ext.form.FieldContainer',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormButton}
		 */
		button: undefined,

		/**
		 * @property {Object}
		 */
		buttonConfig: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormTextarea}
		 */
		textarea: undefined,

		/**
		 * @property {Object}
		 */
		textareaConfig: undefined,

		border: false,
		considerAsFieldToDisable: true,
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		layout: 'hbox',
		width: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,

		/**
		 * To acquire informations to setup fields before creation
		 *
		 * @param {Object} configuration
		 * @param {Object} configuration.textarea
		 * @param {Object} configuration.button
		 */
		constructor: function(configuration) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController', this);

			if (!Ext.isEmpty(configuration) || !Ext.isEmpty(configuration.fieldContainer)) {
				Ext.apply(this, configuration.fieldContainer);
			}

			if (Ext.isEmpty(configuration) || Ext.isEmpty(configuration.textarea)) {
				this.textareaConfig = { delegate: this.delegate };
			} else {
				this.textareaConfig = configuration.textarea;
				this.textareaConfig.delegate = this.delegate;
			}

			if (Ext.isEmpty(configuration) || Ext.isEmpty(configuration.button)) {
				this.buttonConfig = { delegate: this.delegate };
			} else {
				this.buttonConfig = configuration.button;
				this.buttonConfig.delegate = this.delegate;
			}

			this.callParent(arguments);
		},

		initComponent: function() {
			this.textarea = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormTextarea', this.textareaConfig);
			this.delegate.textareaField = this.textarea;

			this.button = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormButton', this.buttonConfig);
			this.delegate.buttonField = this.button;

			Ext.apply(this, {
				items: [this.textarea, this.button]
			});

			this.callParent(arguments);
		}
	});

})();