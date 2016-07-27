(function() {

	/**
	 * @deprecated (CMDBuild.view.common.field.comboBox.Erasable)
	 */
	Ext.define('CMDBuild.view.common.field.CMErasableCombo', {
		extend: 'CMDBuild.view.common.field.CMBaseCombo',
		alias: 'cmerasablecombo',

		trigger1cls: Ext.form.field.ComboBox.triggerCls,
		trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
		hideTrigger1: false,
		hideTrigger2: false,
		onTrigger1Click: Ext.form.field.ComboBox.prototype.onTriggerClick,
		onTrigger2Click: function() {
			// if use clearValue the form does not send the value, so it is not possible delete the value on server side
			if (!this.disabled)
				this.setValue(['']);
		}
	});

})();