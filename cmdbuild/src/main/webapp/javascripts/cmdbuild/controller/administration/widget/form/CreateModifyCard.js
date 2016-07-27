(function () {

	Ext.define('CMDBuild.controller.administration.widget.form.CreateModifyCard', {
		extend: 'CMDBuild.controller.administration.widget.form.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.model.widget.createModifyCard.Definition'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'classTabWidgetAdd',
			'classTabWidgetCreateModifyCardDefinitionGet = classTabWidgetDefinitionGet',
			'classTabWidgetCreateModifyCardLoadRecord = classTabWidgetLoadRecord',
			'classTabWidgetCreateModifyCardValidateForm = classTabWidgetValidateForm',
			'classTabWidgetDefinitionModelNameGet'
		],

		/**
		 * @cfg {String}
		 *
		 * @private
		 */
		definitionModelName: 'CMDBuild.model.widget.createModifyCard.Definition',

		/**
		 * @cfg {CMDBuild.view.administration.widget.form.CreateModifyCardPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.widget.Widget} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.widget.form.CreateModifyCardPanel', { delegate: this });
		},

		/**
		 * @return {Object}
		 */
		classTabWidgetCreateModifyCardDefinitionGet: function () {
			return CMDBuild.model.widget.createModifyCard.Definition.convertToLegacy(
				Ext.create(this.classTabWidgetDefinitionModelNameGet(), this.view.getData(true)).getData()
			);
		},

		/**
		 * Fills form with widget data
		 *
		 * @param {CMDBuild.model.widget.createModifyCard.Definition} record
		 */
		classTabWidgetCreateModifyCardLoadRecord: function (record) {
			this.view.loadRecord(record);
		},

		/**
		 * @param {CMDBuild.view.administration.widget.form.CreateModifyCardPanel} form
		 *
		 * @returns {Boolean}
		 */
		classTabWidgetCreateModifyCardValidateForm: function (form) {
			var formValues = this.view.getData(true);

			if (Ext.isEmpty(formValues[CMDBuild.core.constants.Proxy.TARGET_CLASS]) && Ext.isEmpty(formValues[CMDBuild.core.constants.Proxy.FILTER])) {
				this.view.filter.markInvalid(CMDBuild.Translation.errors.requiredFieldMessage);
				this.view.targetClass.markInvalid(CMDBuild.Translation.errors.requiredFieldMessage);

				CMDBuild.core.Message.error(
					CMDBuild.Translation.common.failure,
					CMDBuild.Translation.errors.targetClassOrCqlFilterRequired,
					false
				);

				return false;
			}

			return this.validate(form);
		}
	});

})();
