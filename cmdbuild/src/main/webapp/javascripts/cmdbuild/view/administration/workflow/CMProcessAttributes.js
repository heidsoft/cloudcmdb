(function () {

	Ext.define('CMDBuild.view.administration.workflow.CMProcessAttributes', {
		extend: "Ext.panel.Panel",

		constructor: function () {
			this.formPanel = this.buildFormPanel();

			this.gridPanel = Ext.create('CMDBuild.view.administration.workflow.CMAttributeGrid', {
				region: "north",
				split: true,
				height: "40%",
				border: false
			});

			this.callParent(arguments);

			this.formPanel.disableModify();
		},

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				layout: "border",
				items: [this.formPanel, this.gridPanel]
			});

			this.callParent(arguments);
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		onClassSelected: function (idClass, className) {
			this.formPanel.onClassSelected(idClass, className);
			this.gridPanel.onClassSelected(idClass, className);
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		buildFormPanel: function () {
			return Ext.create('CMDBuild.view.administration.workflow.CMProcessAttributesForm', {
				region: 'center'
			});
		}
	});

})();
