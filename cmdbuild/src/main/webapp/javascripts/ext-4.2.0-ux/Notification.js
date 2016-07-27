/**
 * Ext.ux.Notification
 *
 * @author  Edouard Fattal
 * @date	March 14, 2008
 *
 * @class Ext.ux.Notification
 * @extends Ext.Window
 *
 * Modified by Tecnoteca
 *
 * - Use close instead of hide
 * - Something else not documented
 */
(function() {

	var BORDER_X_OFFSET = 20;
	var BORDER_Y_OFFSET = 40;

	Ext.define('Ext.ux.NotificationMgr', {

		/**
		 * @cfg {Array}
		 */
		positions: [],

		singleton: true,

		/**
		 * @returns {Number}
		 */
		length: function() {
			return this.positions.length;
		},

		/**
		 * @param {Number} item
		 */
		push: function(item) {
			this.positions.push(item);
		},

		/**
		 * @param {Number} item
		 */
		remove: function(item) {
			this.positions = Ext.Array.remove(this.positions, item);
		}
	});

	Ext.define('Ext.ux.Notification', {
		extend: 'Ext.window.Window',

		/**
		 * @property {Number}
		 */
		pos: undefined,

		/**
		 * @property {}
		 */
		task: undefined,

		bodyStyle: 'text-align: center',
		cls: 'x-notification',
		draggable: false,
		focusOnToFront: false,
		iconCls: 'x-icon-information',
		plain: false,
		width: 200,

		initComponent: function(){
			if (!Ext.isEmpty(this.iconCls))
				Ext.apply(this, {
					iconCls: this.iconCls
				});

			if(this.autoDestroy) {
				this.task = new Ext.util.DelayedTask(this.close, this);
			} else {
				this.closable = true;
			}

			this.callParent(arguments);
		},

		/**
		 * @override
		 */
		afterHide: function() {
			Ext.ux.NotificationMgr.remove(this.pos);

			this.callParent(arguments);
		},

		afterShow: function(){
			this.pos = Ext.ux.NotificationMgr.length();

			Ext.ux.NotificationMgr.push(this.pos);

			this.el.alignTo(document, 'br-br', [-BORDER_X_OFFSET, -BORDER_Y_OFFSET - ((this.getSize().height + 10) * this.pos)]);

			if (!Ext.isEmpty(this.body) && !Ext.isEmpty(this.body.dom))
				Ext.fly(this.body.dom).on('click', this.cancelHiding, this);

			if (this.autoDestroy)
				this.task.delay(this.hideDelay || 5000);

			this.toFront();
		},

		cancelHiding: function(){
			this.addClass('fixed');

			if(this.autoDestroy)
				this.task.cancel();
		},

		/**
		 * @override
		 */
		focus: Ext.emptyFn,

		/**
		 * @param {String} msg
		 */
		setMessage: function(msg){
			this.body.update(msg);
		},

		/**
		 * @param {String} title
		 * @param {String} iconCls
		 *
		 * @override
		 */
		setTitle: function(title, iconCls){
			this.callParent([title, iconCls||this.iconCls]);
		}
	});

})();