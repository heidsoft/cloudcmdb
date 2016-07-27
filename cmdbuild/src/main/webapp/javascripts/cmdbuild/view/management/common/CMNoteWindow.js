(function() {

	Ext.define('CMDBuild.view.management.common.CMNoteWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		withButtons: false,
		withTbar: true,

		initComponent: function() {
			var me = this;

			this.note = Ext.create('CMDBuild.view.management.classes.CMCardNotesPanel', {
				withButtons: this.withButtons,
				withTbar: this.withTbar,

				getExtraButtons: function() {
					return Ext.create('Ext.button.Button', {
						text: CMDBuild.Translation.close,

						handler: function() {
							me.destroy();
						}
					});
				}
			});

			this.CMEVENTS = this.note.CMEVENTS;

			if (this.withButtons)
				this.relayEvents(this.note, [this.note.CMEVENTS.saveNoteButtonClick, this.note.CMEVENTS.cancelNoteButtonClick]);

			Ext.apply(this, {
				items: [this.note],
				buttonAlign: 'center'
			});

			this.callParent(arguments);
		},

		getForm: function() {
			return this.note.getForm();
		},

		reset: function() {
			this.note.reset();
		},

		/**
		 * @param (Object) card - selected card object
		 */
		loadCard: function(card) {
			this.note.loadCard(card);
		},

		/**
		 * @param (Boolean) enableModifyButton
		 */
		disableModify: function(enableModifyButton) {
			this.note.disableModify(enableModifyButton);
		},

		syncForms: function() {
			return this.note.syncForms();
		},

		updateWritePrivileges: function(priv) {
			return this.note.updateWritePrivileges(priv);
		}
	});

})();