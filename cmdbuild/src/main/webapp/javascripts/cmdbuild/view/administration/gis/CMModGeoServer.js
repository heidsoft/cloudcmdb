(function() {

	var tr = CMDBuild.Translation.administration.modcartography.geoserver;

	Ext.define('CMDBuild.view.administration.gis.CMModGeoServer', {
		extend: 'Ext.panel.Panel',

		border: true,
		firstShow: true,
		layout: 'border',
		title: tr.title,

		initComponent : function() {
			this.addLayerButton = Ext.create('Ext.button.Button', {
				iconCls: 'add',
				text: tr.add_layer
			});

			this.layersGrid = Ext.create('CMDBuild.view.administration.gis.GisLayersGrid', {
				region: 'north',
				height: '30%',
				split: true
			});

			this.form = Ext.create('CMDBuild.view.administration.gis.GisLayersForm', {
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: [this.addLayerButton]
					}
				],
				items: [this.layersGrid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();