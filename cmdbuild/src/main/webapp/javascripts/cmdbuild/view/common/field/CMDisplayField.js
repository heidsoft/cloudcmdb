(function() {
	var MAX_HEIGHT = 100;

	Ext.form.field.Display.override({

		/**
		 * @cfg {Boolean}
		 */
		allowBlank: true,

		setValue : function(value) {
			// for the attributes like lookup and reference
			// that has as value an object like {id:"", description:""}
			if (value != null
					&& typeof value == "object") {

				value = value.description;
			}

			this.callOverridden([value]);
			this._addTargetToLinks();
		},

		_addTargetToLinks: function() {
			var ct = this.getContentTarget();
			if (ct) {
				var links = Ext.DomQuery.select("a", ct.dom);
				if (links) {
					for (var i=0, l=links.length; i<l; ++i) {
						links[i].target = "_blank";
					}
				}
			}
		},

		/**
		 * Validate also display field
		 *
		 * @override
		 */
		isValid: function() {
			if (this.allowBlank)
				return true;

			return !Ext.isEmpty(this.getValue());
		}
	});

	Ext.define("CMDBuild.view.common.field.CMDisplayField", {
		extend : "Ext.form.field.Display",

		/**
		 * @cfg {Boolean}
		 */
		allowBlank: true,

		constructor : function() {
			this.callParent(arguments);
			this.expandButtonMarkup = Ext.DomHelper.markup({tag: "div", cls: "cmdisplayfield-expandbutton"});
		},

		setValue : function() {
			this.hideExpandButton();

			if (this.rendered) {
				if (this.inputEl) {
					this.inputEl.setHeight("auto");
				}
			}

			this.callParent(arguments);

			if (this.rendered) {
				if (this.getHeight() > MAX_HEIGHT) {
					this.showExpandButton();
					if (this.inputEl) {
						this.inputEl.setHeight(MAX_HEIGHT);
					}
				}
			}
		},

		showExpandButton: function() {
			var el = this.inputEl;
			if (el && !this.expandButtonEl) {
				this.expandButtonEl = Ext.DomHelper.insertBefore(el, this.expandButtonMarkup, returnElement = true);
				addClickListener(this, this.expandButtonEl);
			}

			this.expandButtonEl.show();
		},

		hideExpandButton: function() {
			if (this.expandButtonEl) {
				this.expandButtonEl.hide();
			}
		},

		/**
		 * Validate also display field
		 *
		 * @override
		 */
		isValid: function() {
			if (this.allowBlank)
				return true;

			return !Ext.isEmpty(this.getValue());
		}
	});

	function addClickListener(field, button) {
		field.mon(button, "click", function() {
			var displayField = new Ext.form.field.Display({
				xtype: "displayfield",
				hideLabel: true,
				margin: "8 10"
			});

			var	popup = Ext.create('CMDBuild.core.window.AbstractModal', {
				title: field.fieldLabel,
				items:[{
					xtype: "container",
					region: "center",
					items: [displayField]
				}],
				buttonAlign: "center",
				autoScroll: true,
				buttons: [{
					text: CMDBuild.Translation.close,
					handler: function() {
						popup.destroy();
					}
				}]
			}).show();

			// set value after show to have the targetElement for the
			// _addTargetToLinks of DisplayField
			displayField.setValue(field.getValue());
		});
	}
})();