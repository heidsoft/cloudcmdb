(function () {

	// External implementation to avoid overrides
	Ext.require(['CMDBuild.core.constants.Proxy']);

	Ext.define('CMDBuild.controller.administration.widget.form.Abstract', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 *
		 * @private
		 */
		definitionModelName: undefined,

		classTabWidgetAdd: function () {
			this.view.reset();
			this.view.setDisabledModify(false, true);
			this.view.loadRecord(Ext.create(this.classTabWidgetDefinitionModelNameGet()));
		},

		// DefinitionModelName property methods
			/**
			 * @returns {String}
			 */
			classTabWidgetDefinitionModelNameGet: function () {
				if (!this.classTabWidgetDefinitionModelNameIsEmpty())
					return this.definitionModelName;

				return '';
			},

			/**
			 * @returns {Boolean}
			 */
			classTabWidgetDefinitionModelNameIsEmpty: function () {
				return (
					Ext.isEmpty(this.definitionModelName)
					|| !Ext.isString(this.definitionModelName)
				);
			},

			/**
			 * @param {String} modelName
			 */
			classTabWidgetDefinitionModelNameSet: function (modelName) {
				if (!Ext.isEmpty(modelName) && Ext.isString(modelName))
					this.definitionModelName = modelName;
			}
	});

})();
