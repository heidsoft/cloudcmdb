(function() {

	// Here because requires property doesn't work
	Ext.require('CMDBuild.core.constants.Proxy');
	Ext.require('CMDBuild.proxy.email.Template');

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormTemplateCombo', {
		extend: 'Ext.form.field.ComboBox',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController}
		 */
		delegate: undefined,

		fieldLabel: CMDBuild.Translation.administration.tasks.notificationForm.template,
		name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE,

		valueField: CMDBuild.core.constants.Proxy.NAME,
		displayField: CMDBuild.core.constants.Proxy.NAME,
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
		forceSelection: true,
		editable: false,
		anchor: '100%',

		store: CMDBuild.proxy.email.Template.getStore()
	});

})();