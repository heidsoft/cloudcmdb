(function () {

	Ext.define('CMDBuild.view.management.widget.manageRelation.CMManageRelation', {
		extend: 'Ext.tree.Panel',

		requires: ['CMDBuild.model.widget.manageRelation.CMRelationPanelModel'],

		CHECK_NAME: 'manage_relation_check', // Used by the controller to identify the selected rows

		constructor: function (c) {
			this.widgetConf = c.widget;

			this.callParent(arguments);
		},

		initComponent: function () {
			this.CHECK_NAME += this.widgetConf['id'];
			this.border= false;
			this.frame = false;
			this.cls = 'x-panel-body-default-framed';

			this.buildTBar();

			this.attrsColumn = new Ext.grid.column.Column({
				header: CMDBuild.Translation.administration.modClass.tabs.attributes,
				hideMode: 'visibility', // Otherwise it fails calling twice hide() on it
				flex: 3,
				sortable: false,
				dataIndex: 'rel_attr'
			});

			Ext.apply(this, {
				loadMask: false,
				hideMode: 'offsets',
				store: new Ext.data.TreeStore({
					model: 'CMDBuild.model.widget.manageRelation.CMRelationPanelModel',
					root : {
						expanded : true,
						children : []
					},
					autoLoad: false
				}),
				rootVisible: false,
				columns: [
					{
						header: CMDBuild.Translation.management.modcard.relation_columns.domain,
						sortable: false,
						dataIndex: 'dom_id',
						hidden: true
					},
					{
						header: CMDBuild.Translation.management.modcard.relation_columns.destclass,
						flex: 2,
						sortable: false,
						dataIndex: 'label',
						xtype: 'treecolumn'
					},
					{
						header: CMDBuild.Translation.management.modcard.relation_columns.begin_date,
						flex: 1,
						sortable: false,
						dataIndex: 'rel_date'
					},
					{
						header: CMDBuild.Translation.management.modcard.relation_columns.code,
						flex: 1,
						sortable: false,
						dataIndex: 'dst_code'
					},
					{
						header: CMDBuild.Translation.management.modcard.relation_columns.description,
						flex: 2,
						sortable: false,
						dataIndex: 'dst_desc'
					},
					this.attrsColumn,
					{
						header: '&nbsp',
						fixed: true,
						sortable: false,
						renderer: Ext.bind(this.renderRelationActions, this),
						align: 'center',
						tdCls: 'grid-button',
						dataIndex: 'Fake',
						menuDisabled: true,
						hideable: false
					}
				]
			});

			this.callParent(arguments);

			this.CMEVENTS = {
				addButtonClick: 'cm-add-relation-click',
				domainNodeAppended: 'cm-domain-node-appended',
				openGraphClick: 'cm-open-graph'
			};

			this.addEvents(this.CMEVENTS.addButtonClick);
			this.addEvents(this.CMEVENTS.domainNodeAppended);
			this.addEvents(this.CMEVENTS.openGraphClick);
		},

		buildTBar: function () {
			var me = this;

			this.tbar = [];

			this.addRelationButton = Ext.create('CMDBuild.view.management.widget.manageRelation.AddRelationMenuButton', {
				text: CMDBuild.Translation.management.modcard.add_relations
			});

			this.mon(this.addRelationButton, 'cmClick', function (d) {
				me.fireEvent(me.CMEVENTS.addButtonClick, d);
			});

			if (this.widgetConf['canCreateAndLinkCard'] || this.widgetConf['canCreateRelation'])
				this.tbar.push(this.addRelationButton);

			if (CMDBuild.configuration.graph.get(CMDBuild.core.constants.Proxy.ENABLED)) {
				this.graphButton = Ext.create('CMDBuild.core.buttons.iconized.RelationGraph', {
					scope: this,

					handler: function (button, e) {
						this.fireEvent(this.CMEVENTS.openGraphClick);
					}
				});

				this.tbar.push(this.graphButton);
			} else {
				this.graphButton = {
					enable: function () {},
					disable: function () {},
					on: function () {}
				};
			}
		},

		clearStore: function () {
			this.store.getRootNode().removeAll();
		},

		fillWithData: function (domains) {
			this.showAttributesColumn = false;

			domains = domains || [];
			var nodes = [],
				r = this.store.getRootNode();

			for (var i=0, l=domains.length; i<l; ++i) {
				var domainResponseObj = domains[i],
					domainCachedData = _CMCache.getDomainById(domainResponseObj.id);

				if (domainCachedData) {
					nodes.push(buildNodeForDomain.call(this, domainResponseObj, domainCachedData));
				} else {
					_error('I have not cached data for domain', domainResponseObj.id);
				}
			}

			r.removeAll();
			if (nodes.length > 0) {
				r.appendChild(nodes);
			}

			if (this.showAttributesColumn) {
				this.attrsColumn.show();
			} else {
				this.attrsColumn.hide();
			}
		},

		convertRelationInNodes: convertRelationInNodes,

		onAddCardButtonClick: function () {
			_deprecated();

			this.disable();
		},

		onClassSelected: function () {
			_deprecated();

			this.disable();
		},

		renderRelationActions: function (value, metadata, record) {
			if (record.get('depth') > 1) {
				var actionsHtml = '';

				if (this.widgetConf['singleSelection'] || this.widgetConf['multiSelection']) {
					actionsHtml += '<input type="' + (this.widgetConf['singleSelection'] ? 'radio' : 'checkbox') + '"'
						+ ' name="' + this.CHECK_NAME + '"'
						+ ' value="' + record.get('dst_id') + '"';

					if (
						!Ext.isEmpty(this.widgetConf['currentValue'])
						&& this.widgetConf['currentValue'].indexOf(parseInt(record.get('CardId'))) >= 0
					) {
						actionsHtml += ' checked="true"';
					}

					actionsHtml += ' />';
				}

				if (this.widgetConf['canModifyARelation'])
					actionsHtml += getImgTag('edit', 'link_edit.png');

				if (this.widgetConf['canRemoveARelation'])
					actionsHtml += getImgTag('delete', 'link_delete.png');

				if (this.widgetConf['canModifyALinkedCard'])
					actionsHtml += getImgTag('editcard', 'modify.png');

				if (this.widgetConf['canDeleteALinkedCard'])
					actionsHtml += getImgTag('deletecard', 'delete.png');

				return actionsHtml;
			}

			return '';
		}
	});

	function buildDescriptionForDomainNode(domainResponseObj, domainCachedData) {
		var prefix = domainCachedData.get('descr'+domainResponseObj.src),
			s = domainResponseObj.relations_size,
			postfix = s  > 1 ? CMDBuild.Translation.management.modcard.relation_columns.items : CMDBuild.Translation.management.modcard.relation_columns.item;

		return '<span class="cm-bold">' + prefix + ' (' + s + ' ' + postfix + ')</span>';
	}

	function buildNodeForDomain(domainResponseObj, domainCachedData) {
		var children = [],
			attributes = domainCachedData.data.attributes || [],
			attributesToString = '<span class="cm-bold">',
			oversize = domainResponseObj.relations_size > CMDBuild.configuration.instance.get('relationLimit'),
			src = domainResponseObj.src,
			domId = domainCachedData.get('id'),
			node = {
				dom_id: domId,
				label: buildDescriptionForDomainNode(domainResponseObj, domainCachedData),

				src: src,
				relations_size: domainResponseObj.relations_size,

				expanded: !oversize,
				leaf: false,
				children: [],
				rel_attr_keys: []
			};

		if (attributes.length > 0) {
			this.showAttributesColumn = true;

			for (var i=0, l=attributes.length; i<l; i++) {
				var attr = attributes[i];
				if (attr.fieldmode == 'hidden') {
					continue;
				}

				node.rel_attr_keys.push(attr.name);

				attributesToString += i==0 ? '' : ' | ';
				attributesToString += attr.description || attr.name;
			}

			node.rel_attr = attributesToString + '</span>';
		}

		if (oversize) {
			// it is asynchronous, add an empty obj to get the possibility to expand the tree widget
			node.children.push({});
		} else {
			node.children = convertRelationInNodes(domainResponseObj.relations, domId, src, node);
		}

		return node;
	}

	function convertRelationInNodes(relations, dom_id, src, node, nodeUI) {
		relations = relations || [];
		var r,c,i=0,
			l=relations.length,
			nodes = [],
			attributesToString = '',
			key,
			val;

		for (; i<l; ++i) {
			r = relations[i];
			c = _CMCache.getEntryTypeById(r.dst_cid);

			if (!c) {
				continue;
			}

			r.leaf = true;
			r.label = c.get('text');
			r.dom_id = dom_id;
			r.src = src;

			attributesToString = '';
			node.rel_attr_keys = node.rel_attr_keys || nodeUI.raw.rel_attr_keys || [];
			for (var j=0; j<node.rel_attr_keys.length; ++j) {
				key = node.rel_attr_keys[j];
				val = r.rel_attr[key];
				if (typeof val == 'undefined') {
					val = ' - '; // is not used the || operator because 0 and false are valid values for val
				}

				attributesToString += j==0 ? '' : ' | ';
				attributesToString += val.dsc || val;
			}
			r.attr_as_obj = r.rel_attr; // used in modify window
			r.rel_attr = attributesToString;
			nodes.push(r);
			if (nodeUI) {
				nodeUI.appendChild(r);
			}
		}

		return nodes;
	}

	function getImgTag(action, icon) {
		return '<img style="cursor:pointer" class="action-relation-'+ action +'" src="images/icons/' + icon + '"/>';
	}

})();
