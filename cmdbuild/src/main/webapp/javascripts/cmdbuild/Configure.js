(function () {

	/**
	 * Configuration
	 */
	Ext.application({
		extend: 'Ext.app.Application',

		requires: [
			'Ext.tip.QuickTipManager' // Avoid core override
		],

		appFolder: './javascripts/cmdbuild',
		name: 'CMDBuild',

		/**
		 * @returns {Void}
		 */
		launch: function () {
			Ext.WindowManager.getNextZSeed(); // To increase the default zseed. Is needed for the combo on windows probably it fix also the prev problem
			Ext.enableFx = false;
			Ext.tip.QuickTipManager.init();

			// Fix a problem of Ext 4.2 tooltips width
			// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
			delete Ext.tip.Tip.prototype.minWidth;

			Ext.create('CMDBuild.core.LoggerManager'); // Logger configuration
			Ext.create('CMDBuild.core.interfaces.Init'); // Interfaces configuration
			Ext.create('CMDBuild.core.Data'); // Data connections configuration
			Ext.create('CMDBuild.core.cache.Cache');
			Ext.create('CMDBuild.core.configurations.builder.Instance', { enableServerCalls: false }); // CMDBuild instance configuration
			Ext.create('CMDBuild.core.configurations.builder.Localization', { enableServerCalls: false }); // CMDBuild instance configuration

			Ext.create('CMDBuild.controller.configure.Configure');
		}
	});

})();
