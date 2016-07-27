(function() {

	Ext.define('CMDBuild.view.administration.configuration.ServerPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Server}
		 */
		delegate: undefined,

		bodyCls: 'cmdb-gray-panel',
		border: false,
		frame: false,
		overflowY: 'auto',

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.cacheManagement,
						padding: '5',

						items: [
							Ext.create('Ext.button.Button', {
								text: CMDBuild.Translation.clearCache,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationServerClearCacheButtonClick');
								}
							})
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.serviceSynchronization,
						padding: '5',

						items: [
							Ext.create('Ext.button.Button', {
								text: CMDBuild.Translation.serviceSynchronization,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationServerServiceSynchButtonClick');
								}
							})
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.lockCardsAndProcessesInEdit,
						padding: '5',

						items: [
							Ext.create('Ext.button.Button', {
								text: CMDBuild.Translation.unlockAllCards,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationServerUnlockCardsButtonClick');
								}
							})
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();