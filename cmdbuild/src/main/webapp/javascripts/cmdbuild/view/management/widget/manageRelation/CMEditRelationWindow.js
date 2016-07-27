(function () {

	Ext.define('CMDBuild.view.management.widget.manageRelation.CMEditRelationWindow', {
		extend: 'CMDBuild.core.window.AbstractModal',

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.Message'
		],

		successCb: Ext.emptyFn,

		// configuration
			ClassName: undefined, // passed at instantiation
			extraParams: {},
			filterType: undefined, // passed at instantiation
			gridConfig: {}, // passed at instantiation
			idClass: undefined, // passed at instantiation
			multiSelect: false,
			readOnly: undefined, // passed at instantiation
			relation: undefined, // {dst_id: '', dst_cid: '', dom_id: '', rel_id: '', masterSide: '_1', slaveSide: '_2', rel_attr: []}
			selModel: undefined, // if undefined is used the default selType
			selType: 'rowmodel', // to allow the opportunity to pass a selection model to the grid
			sourceCard: undefined, // the source of the relation
		// configuration

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			if (this.relation == undefined) {
				throw 'You must pass a relation to the CMEditRelationWindow';
			} else {
				this.idClass = this.relation.dst_cid;
			}

			this.saveButton = Ext.create('CMDBuild.core.buttons.text.Save', {
				scope: this,
				handler: onSaveButtonClick
			});

			this.abortButton = Ext.create('CMDBuild.core.buttons.text.Abort', {
				scope: this,
				handler: function () {
					this.close();
				}
			});

			this.buttonAlign = 'center';
			this.buttons = [this.saveButton, this.abortButton];

			if (typeof this.idClass == 'undefined' && typeof this.ClassName == 'undefined') {
				throw 'There are no Class Id or Class Name to load';
			}

			this.title = CMDBuild.Translation.management.modcard.title + getClassDescription(this);
			this.grid = new CMDBuild.view.management.common.CMCardGrid(this.buildGrdiConfiguration());
			this.setItems();

			this.callParent(arguments);
		},

		buildAddButton: function () {
			var addCardButton = new CMDBuild.AddCardMenuButton();
			var entry = _CMCache.getEntryTypeById(this.getIdClass());

			addCardButton.updateForEntry(entry);
			this.mon(addCardButton, 'cmClick', function buildTheAddWindow(p) {
				var w = new CMDBuild.view.management.common.CMCardWindow({
					withButtons: true,
					title: p.className
				});

				new CMDBuild.controller.management.common.CMCardWindowController(w, {
					cmEditMode: true,
					card: null,
					entryType: p.classId
				});
				w.show();

				this.mon(w, 'destroy', function () {
					this.grid.reload();
				}, this);

			}, this);

			return addCardButton;
		},

		buildGrdiConfiguration: function () {
			var gridConfig = Ext.apply(this.gridConfig, {
				cmAdvancedFilter: false,
				columns: [],
				CQL: this.extraParams,
				frame: false,
				border: false,
				selType: this.selType,
				multiSelect: this.multiSelect
			});

			if (typeof this.selModel == 'undefined') {
				gridConfig['selType'] = this.selType;
			} else {
				gridConfig['selModel'] = this.selModel;
			}

			return gridConfig;
		},

		getIdClass: function () {
			if (this.idClass) {
				return this.idClass;
			} else {
				var et = _CMCache.getEntryTypeByName(this.ClassName);
				if (et) {
					return et.getId();
				}
			}

			throw 'No class info for ' + Ext.getClassName(this);
		},

		setItems: function () {
			var attributes = _CMCache.getDomainById(this.relation.dom_id).get('attributes');

			this.attributesPanel = CMDBuild.Management.EditablePanel.build({
				autoScroll: true,
				region: 'south',
				height: '30%',
				attributes: attributes,
				split: true,
				frame: false,
				border: false,
				bodyCls: 'x-panel-body-default-framed',
				bodyStyle: {
					padding: '5px'
				}
			});

			this.items = [this.grid];

			if (
				!this.readOnly
				&& _CMCache.getEntryTypeById(this.getIdClass()).get('type') == 'class' // Create add button and topBar only for classes (no for processes)
			) {
				this.tbar = [
					this.addCardButton = this.buildAddButton()
				];
			}

			if (this.attributesPanel != null) {
				this.layout = 'border';
				this.grid.region = 'center';
				this.grid.addCls('cmdb-border-bottom');
				this.items.push(this.attributesPanel);
			} else {
				this.attributesPanel = {
					editMode: Ext.emptyFn,
					getFields: function () {
						return {};
					}
				};
			}
		},

		/**
		 * @override
		 */
		show: function () {
			this.callParent(arguments);
			this.grid.updateStoreForClassId(this.getIdClass());

			this.attributesPanel.editMode();

			var fields = this.attributesPanel.getFields();
			var rel_attrs = this.relation.rel_attr || {};

			for (var i = 0; i < fields.length; ++i) {
				var f = fields[i];
				var name;

				if (f.CMAttribute) {
					name = f.CMAttribute['name'];
				} else {
					name = f['name'];
				}

				var val = rel_attrs[name];

				if (val) {
					f.setValue(val['id'] || val);

					if (f.CMAttribute.type == 'LOOKUP') {
						var store = _CMCache.getLookupStore(f.CMAttribute.lookup);
						store.load({
							value: val,
							field: f,
							callback: function (records, operation, success) {
								Ext.Array.each(records, function (item, index, allItems) {
									if (operation['value'] == item.raw['Description']) {
										operation['field'].setValue(item.raw['Id']);

										return;
									}
								});
							}
						});
					}
				}
			}
		}
	});

	function onSaveButtonClick() {
		var p = buildSaveParams(this);

		if (p) {
			if (p[CMDBuild.core.constants.Proxy.RELATION_ID ] == -1) { // creation
				delete p[CMDBuild.core.constants.Proxy.RELATION_ID];

				CMDBuild.proxy.Relation.create({
					params: p,
					loadMask: false,
					scope: this,
					success: function () {
						this.successCb();
						this.close();
					}
				});
			} else { // modify
				CMDBuild.proxy.Relation.update({
					params: p,
					loadMask: false,
					scope: this,
					success: function () {
						this.successCb();
						this.close();
					}
				});
			}
		}
	}

	/**
	 * @return {
	 * 	domainName: string,
	 * 	relationId: int,
	 *  master: string, '_1' | '_2' the side of the domain to consider as master
	 *
	 *  // assuming the master is '_1'
	 *  attributes: {
	 *  	_1: [{
	 *  		className: string,
	 *  		cardId: int
	 *  	}],
	 *  	_2: [{
	 *  		className: string,
	 *  		cardId: int
	 *  	}, {
	 *  		eventually other card objects
	 *  	}],
	 *
	 *  	// the attribute defined for the domain as key/value pairs
	 *  	...
	 *  	attributeName1: value,
	 *  	attributeName2: value
	 *  	...
	 *  }
	 * }
	 */
	function buildSaveParams(me) {
		var domain = _CMCache.getDomainById(me.relation.dom_id);
		var params = {};
		var attributes = {};

		params[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = domain.getName();
		params[CMDBuild.core.constants.Proxy.RELATION_ID] = me.relation.rel_id;

		params['master'] = me.relation.masterSide;
		attributes[me.relation.masterSide] = [getCardAsParameter(me.sourceCard)];

		try {
			attributes[me.relation.slaveSide] = getSelections(me);
		} catch (e) {
			if (e == 'No selection') {
				var msg = Ext.String.format('<p class=\'{0}\'>{1}</p>', CMDBuild.core.constants.Global.getErrorMsgCss(), CMDBuild.Translation.errors.noSelectedCardToUpdate);

				CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, msg, false);
			}

			return;
		}

		try {
			attributes = Ext.apply(attributes, getData(me.attributesPanel));
		} catch (e) {
			var msg = Ext.String.format('<p class=\'{0}\'>{1}</p>', CMDBuild.core.constants.Global.getErrorMsgCss(), CMDBuild.Translation.errors.invalid_attributes);

			CMDBuild.core.Message.error(null, msg + e, false);

			return;
		}

		params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(attributes);

		return params;
	}

	function getSelections(me) {
		var selection = me.grid.getSelectionModel().getSelection();
		var l = selection.length;
		var selectedCards = [];

		if (l>0) {
			for (var i=0; i<l; ++i) {
				var cardAsParameter = getCardAsParameter(selection[i]);

				selectedCards.push(cardAsParameter);
			}
		} else {
			if (me.relation.rel_id == -1) {
				// we are add a new relation, the selection is mandatory
				throw 'No selection';
			} else {
				// is editing a relations and there are relations selected it could be that are updating only the attributes.
				// Retrieve the already related card
				var relatedCardData = {
					Id: me.relation.dst_id,
					IdClass: me.relation.dst_cid
				};

				selectedCards.push(
						// mock a card to use the same function to have the parameters
						getCardAsParameter({
							get: function (key) {
								return relatedCardData[key];
							}
						})
					);
			}
		}

		return selectedCards;
	}

	function getCardAsParameter(card) {
		var parameter = {};

		parameter[CMDBuild.core.constants.Proxy.CARD_ID] = card.get('Id');
		parameter[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(card.get('IdClass'));

		return parameter;
	}

	function getData(attributesPanel) {
		var data = {};
		var nonValid = '';
		var ff = attributesPanel.getFields();
		var f;

		for (var i = 0; i < ff.length; ++i) {
			f = ff[i];

			if (f.isValid()) {
				data[f.name] = f.getValue();
			} else {
				nonValid += '<p><b>' + f.fieldLabel + '</b></p>';
			}
		}

		if (nonValid) {
			throw nonValid;
		} else {
			return data;
		}
	}

	function getClassDescription(me) {
		var entryType = _CMCache.getEntryTypeById(me.getIdClass());
		var description = '';
		if (entryType) {
			description = entryType.getDescription();
		}

		return description;
	}

})();
