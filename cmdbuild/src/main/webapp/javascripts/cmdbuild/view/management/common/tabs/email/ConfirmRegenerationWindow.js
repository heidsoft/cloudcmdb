(function () {

	Ext.define('CMDBuild.view.management.common.tabs.email.ConfirmRegenerationWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.tabs.email.Email'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.ConfirmRegenerationWindow}
		 */
		delegate: undefined,

		/**
		 * @cfg {Number}
		 */
		defaultSizeW: 0.80,

		/**
		 * @cfg {Number}
		 */
		defaultSizeH: 0.50,

		/**
		 * @property {Ext.grid.Panel}
		 */
		grid: undefined,

		closeAction: 'hide',
		title: CMDBuild.Translation.confirmRegeneration,

		layout: 'border',

		initComponent: function () {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Confirm', {
								scope: this,

								handler: function (button, e) {
									this.delegate.cmfg('onConfirmRegenerationWindowConfirmButtonClick');
								}
							})
						]
					})
				],
				items: [
					{
						xtype: 'component',
						region: 'north',
						style: 'padding: 10px;',
						html: CMDBuild.Translation.confirmRegenerationWindowText
					},
					this.grid = Ext.create('Ext.grid.Panel', {
						region: 'center',
						border: false,
						collapsible: false,
						frame: false,

						selModel: Ext.create('Ext.selection.CheckboxModel', { injectCheckbox: 'last' }),

						columns: [
							{
								text: CMDBuild.Translation.archivingDate,
								sortable: true,
								dataIndex: CMDBuild.core.constants.Proxy.DATE,
								flex: 1
							},
							{
								text: CMDBuild.Translation.address,
								sortable: false,
								scope: this,
								renderer: this.addressRenderer,
								flex: 1
							},
							{
								text: CMDBuild.Translation.subject,
								sortable: false,
								dataIndex: CMDBuild.core.constants.Proxy.SUBJECT,
								flex: 1
							},
							{
								sortable: false,
								scope: this,
								dataIndex: CMDBuild.core.constants.Proxy.BODY,
								menuDisabled: true,
								hideable: false,
								renderer: 'stripTags',
								flex: 2
							}
						],

						plugins: [
							{
								ptype: 'rowexpander',
								rowBodyTpl: new Ext.XTemplate(
									'<p><b>Subject:</b> {subject}</p>',
									'<p><b>Content:</b> {body}</p>'
								)
							}
						],

						store: Ext.create('Ext.data.Store', {
							model: 'CMDBuild.model.common.tabs.email.Email',
							data: []
						})
					})
				]
			});

			this.callParent(arguments);

			// Resize window, smaller than default size
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;
		},

		listeners: {
			hide: function (window, eOpts) {
				this.delegate.cmfg('onConfirmRegenerationWindowClearStore');
			},
			show: function (window, eOpts) {
				this.delegate.cmfg('onConfirmRegenerationWindowShow');
			}
		},

		// Column renderers
			/**
			 * @param {Mixed} value
			 * @param {Object} metaData
			 * @param {Mixed} record
			 *
			 * @return {String}
			 */
			addressRenderer: function (value, metadata, record) {
				if (this.delegate.gridDelegate.recordIsReceived(record)) {
					return record.get(CMDBuild.core.constants.Proxy.FROM);
				} else {
					return record.get(CMDBuild.core.constants.Proxy.TO);
				}
			}
	});

})();
