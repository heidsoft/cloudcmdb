(function () {

	/**
	 * This section doen't follow CMDBuild structure standards until refactor of classes administration module
	 */
	Ext.define('CMDBuild.view.administration.class.IconForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.FieldWidths',
			'CMDBuild.core.constants.Proxy'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @property {CMDBuild.controller.administration.classes.Icon}
		 */
		delegate: this,

		/**
		 * @cfg {CMDBuild.view.administration.classes.CMClassForm}
		 */
		parentForm: undefined,

		/**
		 * @property {Ext.Img}
		 */
		imageIconDisplayField: undefined,

		bodyCls: 'cmdb-gray-panel-no-padding',
		border: false,
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		monitorValid: true,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.view.administration.classes.CMClassForm} configurationObject.parentForm
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			Ext.apply(this, configurationObject);

			this.callParent(arguments);
		},

		/**
		 * @override
		 */
		initComponent: function () {
			this.delegate = Ext.create('CMDBuild.controller.administration.classes.Icon', { view: this });

			Ext.apply(this, {
				items: [
					this.fileField = Ext.create('Ext.form.field.File', {
						name: CMDBuild.core.constants.Proxy.FILE,
						fieldLabel: CMDBuild.Translation.file,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
						maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG
					}),
					Ext.create('Ext.form.FieldContainer', {
						fieldLabel: CMDBuild.Translation.current,
						labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,

						items: [
							this.imageIconDisplayField = Ext.create('Ext.Img', {
								considerAsFieldToDisable: true,
								src: '',
								height: 50,
								width: 50
							})
						]
					}),
					Ext.create('Ext.container.Container', {
						padding: '5',

						style: {
							borderTop: '1px solid #d0d0d0'
						},

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Upload', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onClassPropertiesIconUploadButtonClick');
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
