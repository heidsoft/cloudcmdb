(function() {

	var tr = CMDBuild.Translation.administration.tasks.cronForm;

	Ext.define('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormBase', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.ComboBox}
		 */
		baseCombo: undefined,

		/**
		 * @property {Ext.form.field.Radio}
		 */
		baseRadio: undefined,

		frame: true,
		layout: 'hbox',
		margin: '0 0 5 0',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.baseRadio = Ext.create('Ext.form.field.Radio', {
				name: CMDBuild.core.constants.Proxy.CRON_INPUT_TYPE,
				inputValue: CMDBuild.core.constants.Proxy.BASE,
				boxLabel: tr.basic,
				width: CMDBuild.core.constants.FieldWidths.LABEL,

				listeners: {
					change: function(radio, value) {
						me.delegate.cmOn('onChangeBaseRadio', value);
					}
				}
			});

			this.baseCombo = Ext.create('Ext.form.field.ComboBox', {
				name: 'baseCombo',
				valueField: CMDBuild.core.constants.Proxy.VALUE,
				displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
				forceSelection: true,
				editable: false,
				margins: '0 0 0 ' + (CMDBuild.core.constants.FieldWidths.LABEL - 45),

				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
					data: [
						['0 * * * ?', tr.everyHour],
						['0 0 * * ?', tr.everyDay],
						['0 0 1 * ?', tr.everyMonth],
						['0 0 1 1 ?', tr.everyYear]
					]
				}),
				queryMode: 'local',

				listeners: {
					select: function(combo, record, index) {
						me.delegate.cmOn('onSelectBaseCombo', this.getValue());
					}
				}
			});

			Ext.apply(this, {
				items: [this.baseRadio, this.baseCombo]
			});

			this.callParent(arguments);
		}
	});

})();