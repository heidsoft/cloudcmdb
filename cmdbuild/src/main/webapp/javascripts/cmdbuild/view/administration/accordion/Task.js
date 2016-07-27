(function () {

	Ext.define('CMDBuild.view.administration.accordion.Task', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.administration.accordion.Task}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.administration.tasks.title,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				store: Ext.create('Ext.data.TreeStore', {
					autoLoad: true,
					model: this.storeModelName,
					root: {
						expanded: true,
						children: []
					}
				})
			});

			this.callParent(arguments);
		}
	});

})();
