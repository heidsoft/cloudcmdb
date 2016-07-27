(function() {

	Ext.define('CMDBuild.view.patchManager.PatchManagerViewport', {
		extend: 'Ext.container.Viewport',

		/**
		 * @cfg {CMDBuild.controller.patchManager.PatchManager}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.patchManager.GridContainer}
		 */
		gridContainer: undefined,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.panel.Panel', {
						region: 'north',
						contentEl: 'header',
						border: true,
						frame: false,
						height: 45
					}),
					Ext.create('Ext.panel.Panel', {
						region: 'center',
						border: true,
						frame: false,
						layout: 'fit',
						margin: '5',
						title: CMDBuild.Translation.availablePatchesList,

						items: [
							this.gridContainer = Ext.create('CMDBuild.view.patchManager.GridContainer', { delegate: this.delegate })
						]
					}),
					Ext.create('Ext.panel.Panel', {
						region: 'south',
						contentEl: 'footer',
						border: true,
						frame: false,
						height: 18
					})
				]
			});

			this.callParent(arguments);

			if (!Ext.isEmpty(Ext.get('cmdbuild-credits-link')))
				Ext.get('cmdbuild-credits-link').on('click', function(e, t, eOpts) {
					if (!Ext.isEmpty(this.delegate))
						this.delegate.cmfg('onPatchManagerViewportCreditsClick');
				}, this);
		}
	});

})();