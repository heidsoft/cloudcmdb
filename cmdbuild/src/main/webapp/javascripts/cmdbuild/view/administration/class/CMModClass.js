(function() {

	Ext.define("CMDBuild.view.administration.classes.CMModClass", {
		extend: "Ext.panel.Panel",

		NAME: "CMModClass",
		cmName:'class',

		constructor: function() {

			this.addClassButton = new Ext.button.Button({
				iconCls : 'add',
				text : CMDBuild.Translation.administration.modClass.add_class
			});

			this.printSchema = Ext.create('CMDBuild.core.buttons.iconized.split.Print', {
				formatList: [
					CMDBuild.core.constants.Proxy.PDF,
					CMDBuild.core.constants.Proxy.ODT
				],
				mode: 'legacy',
				text: CMDBuild.Translation.administration.modClass.print_schema
			});

			this.tabPanel = new Ext.tab.Panel({
				frame: false,
				border: false,
				activeTab: 0
			});

			Ext.apply(this, {
				tbar:[this.addClassButton, this.printSchema],
				title: CMDBuild.Translation.administration.modClass.title,
				basetitle: CMDBuild.Translation.administration.modClass.title + ' - ',
				layout: 'fit',
				items: [this.tabPanel],
				frame: false,
				border: true
			});

			this.callParent(arguments);
		},

		onAddClassButtonClick: function() {
			this.tabPanel.setActiveTab(0);
		},

		onClassDeleted: function() {
			this.attributesPanel.disable();
			this.geoAttributesPanel.disable();
			this.domainGrid.disable();
			this.layerVisibilityGrid.disable();
		},

		onClassSelected: function(selection) {
			if (CMDBuild.configuration.gis.get('enabled') && !_CMUtils.isSimpleTable(selection.id)) { // TODO: use proxy constants
				this.layerVisibilityGrid.enable();
				this.layerVisibilityGrid.onClassSelected(selection);
			} else {
				this.layerVisibilityGrid.disable();
			}

		}
	});

})();