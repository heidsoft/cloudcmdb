(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormTextarea', {
		extend: 'Ext.form.field.TextArea',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController}
		 */
		delegate: undefined,

		/**
		 * @property {String}
		 *
		 * @required
		 */
		name: undefined,

		/**
		 * @property {Int}
		 *
		 * @required
		 */
		id: undefined,

		readOnly: true,
		flex: 1
	});

})();