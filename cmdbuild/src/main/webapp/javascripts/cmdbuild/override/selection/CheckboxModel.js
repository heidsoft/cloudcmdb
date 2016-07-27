(function() {

	Ext.define('CMDBuild.override.selection.CheckboxModel', {
		override: 'Ext.selection.CheckboxModel',

		/**
		 * @cfg {String}
		 */
		text: '&#160;',

		/**
		 * Implementation of header custom text 18/11/2015
		 *
		 * @returns {Object}
		 */
		getHeaderConfig: function() {
			var showCheck = this.showHeaderCheckbox !== false;

			return {
				isCheckerHd: showCheck,
				text: this.text,
				width: this.headerWidth,
				align: 'center',
				sortable: false,
				draggable: false,
				resizable: false,
				hideable: false,
				menuDisabled: true,
				dataIndex: '',
				cls: showCheck ? Ext.baseCSSPrefix + 'column-header-checkbox ' : '',
				renderer: Ext.Function.bind(this.renderer, this),
				editRenderer: this.editRenderer || this.renderEmpty,
				locked: this.hasLockedHeader()
			};
		}
	});

})();