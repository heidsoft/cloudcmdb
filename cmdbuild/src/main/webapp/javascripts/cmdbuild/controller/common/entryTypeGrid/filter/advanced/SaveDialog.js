(function () {

	Ext.define('CMDBuild.controller.common.entryTypeGrid.filter.advanced.SaveDialog', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.common.entryTypeGrid.filter.advanced.Manager}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'entryTypeGridFilterAdvancedSaveDialogShow',
			'onEntryTypeGridFilterAdvancedSaveDialogAbortButtonClick',
			'onEntryTypeGridFilterAdvancedSaveDialogSaveButtonClick'
		],

		/**
		 * Parameter to forward to next save call
		 *
		 * @property {Boolean}
		 */
		enableApply: false,

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.saveDialog.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.common.entryTypeGrid.filter.advanced.saveDialog.SaveDialogWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.common.entryTypeGrid.filter.advanced.Manager} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.entryTypeGrid.filter.advanced.saveDialog.SaveDialogWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;
		},

		/**
		 * @param {Boolean} enableApply
		 *
		 * @returns {Void}
		 */
		entryTypeGridFilterAdvancedSaveDialogShow: function (enableApply) {
			if (!this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterIsEmpty')) {
				this.enableApply = Ext.isBoolean(enableApply) ? enableApply : false;

				this.form.loadRecord(this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet'));

				this.view.show();
			} else {
				_error('entryTypeGridFilterAdvancedSaveDialogShow(): cannot manage empty filter', this, this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterGet'));
			}
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedSaveDialogAbortButtonClick: function () {
			this.view.close();
		},

		/**
		 * @returns {Void}
		 */
		onEntryTypeGridFilterAdvancedSaveDialogSaveButtonClick: function () {
			if (this.validate(this.form)) {
				var formData = this.form.getData();

				this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterSet', {
					propertyName: CMDBuild.core.constants.Proxy.DESCRIPTION,
					value: formData[CMDBuild.core.constants.Proxy.DESCRIPTION]
				});
				this.cmfg('entryTypeGridFilterAdvancedManagerSelectedFilterSet', {
					propertyName: CMDBuild.core.constants.Proxy.NAME,
					value: formData[CMDBuild.core.constants.Proxy.NAME]
				});

				this.cmfg('entryTypeGridFilterAdvancedManagerSave', {
					enableApply: this.enableApply,
					enableSaveDialog: false
				});

				this.cmfg('onEntryTypeGridFilterAdvancedSaveDialogAbortButtonClick');
			}
		}
	});

})();
