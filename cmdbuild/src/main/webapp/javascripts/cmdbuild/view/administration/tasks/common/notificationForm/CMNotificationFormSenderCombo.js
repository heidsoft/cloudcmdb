(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.notificationForm.CMNotificationFormSenderCombo', {
		extend: 'Ext.form.field.ComboBox',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.email.Account'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.notificationForm.CMNotificationFormController}
		 */
		delegate: undefined,

		fieldLabel: CMDBuild.Translation.administration.tasks.notificationForm.account,
		name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT,

		valueField: CMDBuild.core.constants.Proxy.NAME,
		displayField: CMDBuild.core.constants.Proxy.NAME,
		labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
		maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
		forceSelection: true,
		editable: false,
		anchor: '100%',

		initComponent: function() {
			Ext.apply(this, {
				store: CMDBuild.proxy.email.Account.getStore()
			});

			this.callParent(arguments);
		}
	});

})();