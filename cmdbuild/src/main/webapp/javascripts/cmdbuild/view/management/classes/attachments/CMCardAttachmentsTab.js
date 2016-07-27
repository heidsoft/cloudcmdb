(function() {

	Ext.require('CMDBuild.proxy.classes.tabs.Attachment');

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel", {
		extend: "Ext.grid.Panel",
		translation : CMDBuild.Translation.management.modcard,
		eventtype: 'card',
		eventmastertype: 'class',
		hideMode: "offsets",

		frame: false,
		border: false,

		initComponent: function() {
			var col_tr = CMDBuild.Translation.management.modcard.attachment_columns;

			this.addAttachmentButton = Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
				text: CMDBuild.Translation.management.modcard.add_attachment
			});

			Ext.apply(this, {
				loadMask: false,
				tbar:[this.addAttachmentButton],
				features: [{
					groupHeaderTpl: '{name} ({rows.length} {[values.rows.length > 1 ? CMDBuild.Translation.management.modcard.attachment_columns.items : CMDBuild.Translation.management.modcard.attachment_columns.item]})',
					ftype: 'groupingsummary'
				}],
				columns: [
					{header: col_tr.category, dataIndex: 'Category', hidden: true},
					{header: col_tr.creation_date, sortable: true, dataIndex: 'CreationDate', renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'), flex: 2},
					{header: col_tr.modification_date, sortable: true, dataIndex: 'ModificationDate', renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'), flex: 2},
					{header: col_tr.author, sortable: true, dataIndex: 'Author', flex: 2},
					{header: col_tr.version, sortable: true, dataIndex: 'Version', flex: 1},
					{header: col_tr.filename, sortable: true, dataIndex: 'Filename', flex: 4},
					{header: col_tr.description, sortable: true, dataIndex: 'Description', flex: 4},
					{header: '&nbsp;', width: 80, sortable: false, renderer: this.renderAttachmentActions, align: 'center', tdCls: 'grid-button', dataIndex: 'Fake'}
				],
				store: CMDBuild.proxy.classes.tabs.Attachment.getStore()
			});

			this.callParent(arguments);
		},

		reloadCard: function() {
			this.loaded = false;
			if (this.ownerCt.layout.getActiveItem) {
				if (this.ownerCt.layout.getActiveItem().id == this.id) {
					this.loadCardAttachments();
				}
			} else {
				// it is not in a tabPanel
				this.loadCardAttachments();
			}
		},

		loadCardAttachments: function() {
			if (this.loaded) {
				return;
			}

			this.getStore().load();

			this.loaded = true;
		},

		setExtraParams: function(p) {
			this.store.proxy.extraParams = p;
		},

		clearStore: function() {
			this.store.removeAll();
		},

		renderAttachmentActions: function() {
			var tr = CMDBuild.Translation.management.modcard,
				out = '<img style="cursor:pointer" title="'+tr.download_attachment+'" class="action-attachment-download" src="images/icons/bullet_go.png"/>&nbsp;';

				if (this.writePrivileges) {
					out += '<img style="cursor:pointer" title="'+tr.edit_attachment+'" class="action-attachment-edit" src="images/icons/modify.png"/>&nbsp;'
					+ '<img style="cursor:pointer" title="'+tr.delete_attachment+'" class="action-attachment-delete" src="images/icons/delete.png"/>';
				}

				return out;
		},

		/**
		 * @param {Boolean} writePrivilege
		 */
		updateWritePrivileges: function(writePrivilege) {
			this.writePrivileges = writePrivilege;
			this.addAttachmentButton.setDisabled(!writePrivilege);
		},

		/**
		 * @deprecated
		 */
		onAddCardButtonClick: function() {
			_deprecated('onAddCardButtonClick', this);

			this.disable();
		},

		/**
		 * @deprecated
		 */
		onCardSelected: function(card) {
			_deprecated('onCardSelected', this);

			this.updateWritePrivileges(card.raw.priv_write);
		}
	});

})();