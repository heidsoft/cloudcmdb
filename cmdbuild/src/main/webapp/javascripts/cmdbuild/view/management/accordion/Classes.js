(function () {

	Ext.define('CMDBuild.view.management.accordion.Classes', {
		extend: 'CMDBuild.view.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.management.accordion.Classes}
		 */
		delegate: undefined,

		title: CMDBuild.Translation.classes,

		listeners: {
			collapse: function (panel, eOpts) {
				this.delegate.cmfg('onAccordionClassesCollapse');
			}
		}
	});

})();
