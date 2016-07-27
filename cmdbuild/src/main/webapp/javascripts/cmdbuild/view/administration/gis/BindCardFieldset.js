(function() {

	var tr = CMDBuild.Translation.administration.modClass.geo_attributes;

	Ext.require(['CMDBuild.proxy.gis.GeoServer']);

	Ext.define('CMDBuild.view.administration.gis.BindCardFieldset', {
		extend: 'Ext.form.FieldSet',

		border: false,
		cls: 'cmdb-border-top',
		style: { 'border-color': '#d0d0d0' },
		margin: '5 0 0 0',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		defaults: {
			padding: '5 0 0 0'
		},

		initComponent: function() {
			this.baseItem = Ext.create('CMDBuild.view.administration.gis.BindCardFieldsetItem', {
				delegate: this,
				first: true
			});

			Ext.apply(this, {
				items: [this.baseItem]
			});

			this.items = [this.baseItem];
			this.callParent(arguments);
		},

		bindCardPanelPlusButtonClick: function() {
			return this.add(
				Ext.create('CMDBuild.view.administration.gis.BindCardFieldsetItem', {
					delegate: this,
					isFirst: false
				})
			);
		},

		/**
		 * @param {CMDBuild.view.administration.gis.BindCardFieldsetItem} item
		 */
		bindCardPanelRemoveButtonClick: function(item) {
			this.remove(item);
		},

		/**
		 * @return {Array} out
		 */
		getValue: function() {
			var out = [];

			this.items.each(function(item) {
				var value = item.getValue();

				if (value != null)
					out.push(value);
			});

			return out;
		},

		/**
		 * @param {Object} cardBinding
		 */
		setValue: function(cardBinding) {
			this.removeItems();

			var values = [].concat(cardBinding);
			var v = values.pop();
			var first = true;

			while (v) {
				if (first) {
					this.baseItem.setValue(v);
					first = false;
				} else {
					var item = this.bindCardPanelPlusButtonClick();
					item.setValue(v);
				}

				v = values.pop();
			}
		},

		removeItems: function() {
			var me = this;

			this.items.each(function(item) {
				if (!item.first) {
					me.remove(item);
				}
			});
		}
	});

	Ext.define('CMDBuild.view.administration.gis.BindCardFieldsetItem', {
		extend: 'Ext.container.Container',

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.comboBox.Erasable}
		 */
		classCombo: undefined,

		isFirst: true,

		layout: {
			type: 'hbox'
		},

		initComponent: function() {
			var me = this;

			this.classCombo =  Ext.create('CMDBuild.view.common.field.comboBox.Erasable', {
				fieldLabel: me.isFirst ? tr.card_binding : ' ',
				labelSeparator: me.isFirst ? ':' : '',
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				width: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				displayField: CMDBuild.core.constants.Proxy.TEXT,
				editable: false,
				margin: '0 5 0 0',

				store: CMDBuild.proxy.gis.GeoServer.getStoreCardBind(),
				queryMode: 'local',

				listeners: {
					change: function(combo, newValue) {
						me.remove(me.cardCombo);
						delete me.cardCombo;

						if (newValue) {
							me.cardCombo = CMDBuild.Management.ReferenceField.build(
								{
									referencedClassName: newValue,
									isnotnull: true
								},
								null,
								{
									margin: '0 5 0 0',
									gridExtraConfig: {
										cmAdvancedFilter: false,
										cmAddGraphColumn: false,
										cmAddPrintButton: false
									},
									searchWindowReadOnly: true
								}
							);

							me.insert(1, me.cardCombo);
						}
					},

					enable: function() {
						me.items.each(function(item) {
							if (Ext.getClassName(item) == 'Ext.button.Button') {
								item.enable();
								item.show();
							}
						});
					},

					disable: function() {
						me.items.each(function(item) {
							if (Ext.getClassName(item) == 'Ext.button.Button') {
								item.disable();
								item.hide();
							}
						});
					}
				}
			});

			this.plusButton = Ext.create('Ext.button.Button', {
				iconCls: 'add',
				scope: this,

				handler: function() {
					this.delegate.bindCardPanelPlusButtonClick();
				}
			});

			Ext.apply(this, {
				items: [this.classCombo, this.cardCombo, this.plusButton]
			});

			if (!this.isFirst) {
				this.items.push(
					Ext.create('Ext.button.Button', {
						iconCls: 'delete',

						handler: function() {
							me.delegate.bindCardPanelRemoveButtonClick(me);
						}
					})
				);
			}

			this.callParent(arguments);
		},

		/**
		 * @return {Object} value
		 */
		getValue: function() {
			var value = null;

			if (this.classCombo && this.cardCombo) {
				value =  {
					className: this.classCombo.getValue(),
					idCard: this.cardCombo.getValue()
				};
			}

			return value;
		},

		/**
		 * @param {Object} value
		 */
		setValue: function(value) {
			this.classCombo.setValue(value.className);
			this.cardCombo.setValue(parseInt(value.idCard));
		}
	});

})();