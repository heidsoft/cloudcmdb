(function() {

	var ATTR = {
		INDEX: CMDBuild.core.constants.Proxy.INDEX,
		NAME: CMDBuild.core.constants.Proxy.NAME,
		DESCRIPTION: CMDBuild.core.constants.Proxy.DESCRIPTION,
		TYPE: CMDBuild.core.constants.Proxy.TYPE,
		IS_BASEDSP: 'isbasedsp',
		IS_UNIQUE: 'isunique',
		IS_NOT_NULL: 'isnotnull',
		IS_INHERITED: 'inherited',
		IS_ACTIVE: CMDBuild.core.constants.Proxy.ACTIVE,
		FIELD_MODE: CMDBuild.core.constants.Proxy.FIELD_MODE,
		GROUP: CMDBuild.core.constants.Proxy.GROUP,
		ABSOLUTE_CLASS_ORDER: 'absoluteClassOrder',
		CLASS_ORDER_SIGN: 'classOrderSign',
		EDITOR_TYPE: CMDBuild.core.constants.Proxy.EDITOR_TYPE
	};
	var REQUEST = {
		ROOT: 'attributes'
	};
	var ATTR_TO_SKIP = 'Notes';
	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_attributes = CMDBuild.Translation.administration.modClass.attributeProperties;

	var columns = [{
		header: tr_attributes.type,
		sortable: true,
		dataIndex: 'type',
		flex: 1
	},{
		header: tr_attributes.name,
		sortable: true,
		dataIndex: 'name',
		flex: 1
	},{
		header: tr_attributes.description,
		sortable: true,
		dataIndex: 'description',
		flex: 1
	}];

	Ext.define("CMDBuild.view.administration.classes.CMGeoAttributesGrid", {
		extend: 'Ext.grid.Panel',

		statics: {
			ATTRIBUTES: ATTR
		},

		cls: 'cmdb-border-bottom',
		remoteSort: false,
		includeInherited: true,
		eventtype: 'class',

		hideNotNull: false, // for processes

		hideMode: 'offsets',

		constructor: function() {
			this.addAttributeButton = new Ext.button.Button({
				iconCls: 'add',
				text: translation.add_attribute
			});

			this.orderButton = new Ext.button.Button({
				iconCls: 'order',
				text: translation.set_sorting_criteria
			});

			this.inheriteFlag = new Ext.form.Checkbox({
				boxLabel: CMDBuild.Translation.administration.modClass.include_inherited,
				boxLabelCls: 'cmdb-toolbar-item',
				checked: true,
				scope: this,
				handler: function(obj, checked) {
					this.setIncludeInheritedAndFilter(includeInherited = checked);
				}
			});

			this.buildStore();
			this.buildColumnConf();
			this.buildTBar();

			this.callParent(arguments);
		},

		initComponent: function() {
			Ext.apply(this, {
				viewConfig: {
					loadMask: false,
					plugins: {
						ptype: 'gridviewdragdrop',
						dragGroup: 'dd',
						dropGroup: 'dd'
					},
					listeners: {
						scope: this,
						beforedrop: function() {
							// it is not allowed to reorder the attribute if there are also the inherited attrs
							return this.inheriteFlag.checked;
						},
						drop: function(node, data, dropRec, dropPosition) {
							this.fireEvent('cm_attribute_moved', arguments);
						}
					}
				}
			});

			this.callParent(arguments);

			this.getStore().on('load', function(store, records, opt) {
				this.filterInheritedAndNotes();
			}, this);

			this.on("render", function() {
					if (this.danglingStore) {
						this.reconfigure(this.danglingStore, this.columns);
					}
				}, this, {
					single: true
				});
		},

		buildColumnConf: function() {
			this.columns = columns;
		},

		buildStore: function() {
			this.store = new Ext.data.SimpleStore( {
				model: "GISLayerModel"
			});
		},

		buildTBar: function() {
			this.tbar = [this.addAttributeButton];
		},

		onClassSelected: function(idClass) {
			this.refreshStore(idClass, idAttributeToSelectAfter = null);
		},


		refreshStore: function(idClass, nameOfAttributeToSelect) {
			var et = _CMCache.getEntryTypeById(idClass);
			var me = this;

			_CMCache.getLayersForEntryTypeName(et.get("name"), function(layers) {
				me.store.loadData(layers);
				me.selectAttributeByName(nameOfAttributeToSelect);
			});
		},

		setIncludeInheritedAndFilter: function(includeInherited) {
			this.includeInherited = includeInherited;
			this.filterInheritedAndNotes();
		},

		filterInheritedAndNotes: function() {
			var inh = this.includeInherited;

			this.getStore().filterBy(function(record) {
				return (record.get(ATTR.NAME) != ATTR_TO_SKIP) && (inh || !record.get(ATTR.IS_INHERITED));
			});
		},

		selectFirstRow: function() {
			var _this = this;

			Ext.Function.defer(function() {
				if (_this.store.getCount() > 0 && _this.isVisible()) {
					var sm = _this.getSelectionModel();

					if (!sm.hasSelection())
						sm.select(0);
				}
			}, 200);
		},

		selectRecordAtIndexOrTheFirst: function(indexAttributeToSelectAfter) {
			if (indexAttributeToSelectAfter) {
				var recordIndex = this.store.findRecord(ATTR.INDEX, indexAttributeToSelectAfter);

				if (recordIndex)
					this.getSelectionModel().select(recordIndex);
			} else {
				try {
					if (this.store.count() != 0)
						this.getSelectionModel().select(0);
				} catch (e) {
					// fail if the grid is not rendered
				}
			}
		},

		onAddAttributeClick: function() {
			this.getSelectionModel().deselectAll();
		},

		selectAttributeByName: function(geoAttributeName) {
			var sm = this.getSelectionModel();
			if (geoAttributeName) {
				var r = this.store.findRecord("name", geoAttributeName);
				if (r) {
					sm.select(r);
				}
			} else if (this.store.count() != 0) {
				sm.select(0);
			}
		}
	});


	function renderEditingMode(val) {
		return translation['field_' + val];
	}

})();