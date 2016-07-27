(function () {

	Ext.define('CMDBuild.core.Message', {

		requires: ['Ext.ux.Notification'],

		/**
		 * @cfg {Array}
		 */
		detailBuffer: [],

		singleton: true,

		/**
		 * @param {String} title
		 * @param {Mixed} body
		 * @param {Boolean} popup
		 * @param {String} iconCls
		 */
		alert: function (title, text, popup, iconCls) {
			title = title || '';
			text = text || '';
			popup = Ext.isBoolean(popup) ? popup : false;

			var win = undefined;

			if (popup) {
				win = Ext.MessageBox.show({
					title: title,
					msg: text,
					width: 300,
					buttons: Ext.MessageBox.OK,
					icon: iconCls
				});
			} else {
				win = Ext.create('Ext.ux.Notification', {
					iconCls: iconCls,
					title: title,
					html: text,
					autoDestroy: true,
					hideDelay:  5000,
					shadow: false
				}).show(document);
			}

			return win;
		},

		/**
		 * @param {Number} id
		 * @param {String} stacktrace
		 *
		 * @returns {String}
		 *
		 * @private
		 */
		buildDetailLink: function (id, stacktrace) {
			CMDBuild.core.Message.detailBuffer[id] = stacktrace;

			return '<p class="show_detail_link" id="errorDetails_" onClick="javascript:buildDetaiWindow(' + id + ')">' + CMDBuild.Translation.showDetails + '</p>';
		},

		/**
		 * @param {String} title
		 * @param {Mixed} body
		 * @param {Boolean} popup
		 */
		error: function (title, body, popup) {
			title = title || CMDBuild.Translation.error;
			popup = Ext.isBoolean(popup) ? popup : false;

			var text = body;

			if (Ext.isObject(body) && !Ext.isEmpty(body.text)) {
				text = body.text;

				if (!Ext.isEmpty(body.detail))
					text += CMDBuild.core.Message.buildDetailLink(CMDBuild.core.Message.detailBuffer.length, body.detail);
			}

			CMDBuild.core.Message.alert(title, text, popup, Ext.MessageBox.ERROR);
		},

		/**
		 * @param {String} title
		 * @param {Mixed} body
		 * @param {Boolean} popup
		 */
		info: function (title, text, popup) {
			CMDBuild.core.Message.alert(title, text, popup, Ext.MessageBox.INFO);
		},

		success: function () {
			CMDBuild.core.Message.alert('', CMDBuild.Translation.success, false, Ext.MessageBox.INFO);
		},

		/**
		 * @param {String} title
		 * @param {Mixed} body
		 * @param {Boolean} popup
		 */
		warning: function (title, text, popup) {
			CMDBuild.core.Message.alert(
				title || CMDBuild.Translation.warning,
				text,
				Ext.isBoolean(popup) ? popup : false,
				Ext.MessageBox.WARNING
			);
		}
	});

})();

/**
 * @param {Number} detailBufferIndex
 */
function buildDetaiWindow(detailBufferIndex) {
	var detailsWindow = Ext.create('CMDBuild.core.window.AbstractModal', {
		title: CMDBuild.Translation.details,

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
					Ext.create('CMDBuild.core.buttons.text.Close', {
						scope: this,

						handler: function (button, e) {
							detailsWindow.destroy();
						}
					})
				]
			})
		],

		items: [
			Ext.create('Ext.panel.Panel', {
				border: false,
				autoScroll: true,

				html: '<pre style="padding:5px; font-size: 1.2em">'	+ CMDBuild.core.Message.detailBuffer[detailBufferIndex] + '</pre>'
			})
		]
	}).show();
}
