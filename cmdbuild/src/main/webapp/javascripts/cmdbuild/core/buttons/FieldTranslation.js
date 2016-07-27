(function() {

	Ext.define('CMDBuild.core.buttons.FieldTranslation', {
		extend: 'Ext.button.Button',

		/**
		 * @cfg {Boolean}
		 */
		considerAsFieldToDisable: true,

		/**
		 * @cfg {String}
		 */
		iconCls: 'translate',

		/**
		 * @cfg {String}
		 */
		tooltip: CMDBuild.Translation.translations,

		/**
		 * @cfg {Number}
		 */
		width: 22,

		/**
		 * @param {Ext.button.Button} button
		 * @param {Ext.EventObject} e
		 *
		 * @abstract
		 */
		handler: function(button, e) {
			_warning('unimplemented handler method', this);
		}
	});

})();