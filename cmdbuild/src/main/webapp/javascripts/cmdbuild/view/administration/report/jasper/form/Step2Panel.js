(function () {

	Ext.define('CMDBuild.view.administration.report.jasper.form.Step2Panel', {
		extend: 'Ext.form.Panel',

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		bodyCls: 'cmdb-gray-panel',
		border: false,
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align: 'stretch'
		}
	});

})();
