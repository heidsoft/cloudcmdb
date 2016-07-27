(function() {

	Ext.define('CMDBuild.view.administration.bim.CMBIMPanel', {
		extend: 'CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel',

		title: CMDBuild.Translation.bim + ' ' + CMDBuild.Translation.projects,

		addButtonText: CMDBuild.Translation.addProject,
		modifyButtonText: CMDBuild.Translation.modifyProject,
		removeButtonText: CMDBuild.Translation.removeProject,
		withEnableDisableButton: true,
		withRemoveButton: false,

		//override
		buildGrid: function() {
			var gridConfig = {
				region: 'center',
				border: false,
				frame: false,
				withPagingBar: false//this.withPagingBar
			};

			if (this.withPagingBar)
				gridConfig.cls = 'cmdb-border-bottom';

			return new CMDBuild.view.administration.bim.CMBimGrid(gridConfig);
		}
	});

})();