(function() {

	Ext.require([
		'CMDBuild.core.constants.Proxy',
		'CMDBuild.proxy.index.Json'
	]);

	/**
	 * @deprecated new class (CMDBuild.view.common.field.filter.advanced.Advanced)
	 */

	Ext.define('Functions', {
		extend: 'Ext.data.Model',
		fields: [
			{ name: 'name', type: 'string' }
		]
	});

	Ext.define('CMDBuild.view.management.common.filter.CMFunctions', {
		extend: 'Ext.panel.Panel',

		title: CMDBuild.Translation.functionLabel,
		bodyCls: 'x-panel-body-default-framed cmdb-border-top',
		bodyStyle: {
			padding: '5px 5px 0px 5px'
		},
		cls: 'x-panel-body-default-framed',

		// configuration
			className: undefined,
		// configuration

		initComponent: function() {
			Ext.apply(this, {
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
			});

			this.functionsCombo = Ext.create('Ext.form.ComboBox', {
				fieldLabel: CMDBuild.Translation.functionLabel,
				store: CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
					autoLoad: true,
					model: 'Functions',
					proxy: {
						type: 'ajax',
						url: CMDBuild.proxy.index.Json.functions.readAll,
						reader: {
							type: 'json',
							root: CMDBuild.core.constants.Proxy.RESPONSE
						}
					},
					sorters: [
						{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
					]
				}),
				name: CMDBuild.core.constants.Proxy.FUNCTION,
				displayField: CMDBuild.core.constants.Proxy.NAME,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
				trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
				hideTrigger1: false,
				hideTrigger2: false,

				onTrigger2Click: function() {
					this.setValue('');
				}
			});
			this.items = [this.functionsCombo];

			this.callParent(arguments);
		},

		setData: function(data) {
			if (data && data.length > 0) {
				this.functionsCombo.setValue(data[0].name);
			} else {
				this.functionsCombo.setValue('');
			}
		},

		getData: function() {
			var functionName = this.functionsCombo.getValue();

			return (!functionName) ? [] : [{ 'name': functionName }];
		}
	});

})();