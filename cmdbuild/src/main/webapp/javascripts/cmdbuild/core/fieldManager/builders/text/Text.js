(function () {

	/**
	 * Specific field attributes:
	 * 		- {String} editorType: PLAIN or HTML
	 */
	Ext.define('CMDBuild.core.fieldManager.builders.text.Text', {
		extend: 'CMDBuild.core.fieldManager.builders.Abstract',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.core.fieldManager.FieldManager}
		 */
		parentDelegate: undefined,

		/**
		 * @param {Boolean} withEditor
		 *
		 * @returns {Ext.grid.column.Column or Object}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return this.buildSubFieldClass().buildColumn(withEditor);
		},

		/**
		 * @returns {Object}
		 */
		buildEditor: function() {
			return this.buildSubFieldClass().buildEditor();
		},

		/**
		 * @returns {Mixed}
		 */
		buildField: function() {
			return this.buildSubFieldClass().buildField();
		},

		/**
		 * @returns {Mixed}
		 */
		buildSubFieldClass: function() {
			switch (this.cmfg('attributeModelGet', CMDBuild.core.constants.Proxy.EDITOR_TYPE)) {
				case 'HTML':
					return Ext.create('CMDBuild.core.fieldManager.builders.text.HtmlEditor', { parentDelegate: this });

				case 'PLAIN':
				default:
					return Ext.create('CMDBuild.core.fieldManager.builders.text.Plain', { parentDelegate: this });
			}
		}
	});

})();