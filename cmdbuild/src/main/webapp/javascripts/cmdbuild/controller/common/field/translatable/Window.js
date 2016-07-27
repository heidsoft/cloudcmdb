(function() {

	Ext.define('CMDBuild.controller.common.field.translatable.Window', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.localization.Localization'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onTranslatableWindowAbortButtonClick',
			'onTranslatableWindowBeforeShow',
			'onTranslatableWindowConfirmButtonClick'
		],

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {Mixed}
		 */
		ownerField: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.window.Window}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			if (!Ext.Object.isEmpty(this.ownerField.configurationGet())) {
				this.view = Ext.create('CMDBuild.view.common.field.translatable.window.Window', {
					delegate: this
				});

				// Shorthands
				this.form = this.view.form;

				this.buildTranslationsFields();

				// Show window
				if (!Ext.isEmpty(this.view))
					this.view.show();
			} else {
				_warning('no field configuration on "' + this.ownerField.field.getName() + '"', this);
			}
		},

		buildTranslationsFields: function() {
			var enabledLanguagesObjects = Ext.Object.getValues(CMDBuild.configuration.localization.getEnabledLanguages());

			// Sort languages with description alphabetical order
			CMDBuild.core.Utils.objectArraySort(enabledLanguagesObjects);

			Ext.Array.forEach(enabledLanguagesObjects, function(language, i, allLanguages) {
				if (!Ext.isEmpty(this.form)) {
					this.form.add(
						Ext.create('Ext.form.field.Text', {
							name: language.get(CMDBuild.core.constants.Proxy.TAG),
							fieldLabel: language.get(CMDBuild.core.constants.Proxy.DESCRIPTION),
							labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
							padding: '3 5',
							labelClsExtra: 'ux-flag-' + language.get(CMDBuild.core.constants.Proxy.TAG),
							labelStyle: 'background-repeat: no-repeat; background-position: left; padding-left: 22px;'
						})
					);
				}
			}, this);

			this.view.center(); // AutoHeight windows won't be at the center of viewport on show, manually do it
		},

		onTranslatableWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Build fields with translations refreshing all data
		 */
		onTranslatableWindowBeforeShow: function() {
			this.setViewTitle(this.ownerField.getFieldLabel());

			if (this.ownerField.isConfigurationValid()) {
				this.form.reset();

				if (this.ownerField.translationsGet().isEmpty()) {
					CMDBuild.proxy.localization.Localization.read({
						params: this.ownerField.configurationGet(),
						scope: this,
						success: function(response, options, decodedResponse) {
							this.ownerField.translationsSet(decodedResponse.response);

							this.form.loadRecord(this.ownerField.translationsGet());
						}
					});
				} else {
					this.form.loadRecord(this.ownerField.translationsGet());
				}

			}
		},

		/**
		 * Bufferize translations to save on card save
		 */
		onTranslatableWindowConfirmButtonClick: function() {
			this.ownerField.translationsSet(this.form.getValues());

			this.onTranslatableWindowAbortButtonClick();
		}
	});

})();