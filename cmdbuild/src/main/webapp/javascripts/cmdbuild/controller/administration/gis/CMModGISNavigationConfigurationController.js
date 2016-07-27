(function() {

	Ext.require(['CMDBuild.proxy.gis.TreeNavigation']);

	Ext.define("CMDBuild.controller.administration.gis.CMModGISNavigationConfigurationController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		mixins: {
			gisNavigationConfigurationPanel: "CMDBuild.view.administration.gis.CMModGISNavigationConfigurationDelegate"
		},

		constructor: function() {
			this.callParent(arguments);
			this.loaded = false;
			this.view.addDelegate(this);
		},

		onViewOnFront: function() {
			var me = this;
			if (!this.loaded) {

				me.view.classesMenu.readClasses();

				CMDBuild.core.LoadMask.show();
				CMDBuild.proxy.gis.TreeNavigation.read({
					loadMask: false,
					success: function(operation, config, response) {
						me.loaded = true;
						var root = response.root;

						if (!root) {
							me.view.classesMenu.showMenu();
							return;
						}

						var rootEntryType = _CMCache.getEntryTypeByName(root.targetClassName);
						if (rootEntryType) {
							me.view.classesMenu.setText(root.targetClassDescription);
							me.view.setTreeForEntryType(rootEntryType);
							me.onGISNavigationBaseClassMenuItemSelect(rootEntryType);

							for (var i=0, l=root.childNodes.length; i<l; ++i) {
								synchViewForNodeConf(me, root.childNodes[i]);
							}
						}
					},
					callback: function() {
						CMDBuild.core.LoadMask.hide();
					}
				});
			}
		},

		// as gisNavigationConfigurationPanel

		onGISNavigationSaveButtonClick: function(panel) {
			var structure = panel.getTreeStructure();
			CMDBuild.core.LoadMask.show();
			CMDBuild.proxy.gis.TreeNavigation.update({
				params: {
					structure: Ext.encode(structure)
				},
				loadMask: false,
				callback: function() {
					CMDBuild.core.LoadMask.hide();
				}
			});
		},

		onGISNavigationRemoveButtonClick: function(panel) {
			var me = this;
			Ext.Msg.show({
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == "yes") {
						CMDBuild.core.LoadMask.show();
						CMDBuild.proxy.gis.TreeNavigation.remove({
							success: function() {
								me.view.resetView();;
							},
							loadMask: false,
							callback: function() {
								CMDBuild.core.LoadMask.hide();
							}
						});
					}
				}
			});
		},

		onGISNavigationBaseClassMenuItemSelect: function(selectedEntryType) {
			var domains = retrieveDomainsForEntryType(selectedEntryType);
			this.view.addDomainsAsFirstLevelChildren(domains);
		},

		onGISNavigationTreeItemChecked: function(node, checked) {
			if (checked) {
				synchUpperBranchCheck(node, checked);
			}
			if (node._alreadyExpanded) {
				return;
			} else {
				expandNode(this, node);
			}
		}
	});

	// Take the domains 1:N or N:1 where the selected
	// entryType is the 1 side.
	function retrieveDomainsForEntryType(entryType) {
		var ids =  _CMUtils.getAncestorsId(entryType);
		return _CMCache.getDomainsBy(function(domain) {
			var cardinality = domain.get("cardinality");

			if (cardinality == "1:N"
				&& Ext.Array.contains(ids, domain.getSourceClassId())) {

				return true;
			}

			if (cardinality == "N:1"
				&& Ext.Array.contains(ids, domain.getDestinationClassId())) {

				return true;
			}

			return false;
		});
	}

	function synchViewForNodeConf(me, nodeConf) {
		var node = me.view.getTreeNodeForConf(nodeConf);
		if (!node) {
			return;
		}

		node.set("checked", true);
		node.set("baseNode", nodeConf.baseNode); // I don't know why it does not check it automatically...
		node.commit();

		var l = nodeConf.childNodes.length;
		if (l> 0) {
			expandNode(me, node);
			for (var i=0; i<l; ++i) {
				synchViewForNodeConf(me, nodeConf.childNodes[i]);
			}
		}

	}

	function expandNode(me, node) {
		node._alreadyExpanded = true;
		var id = node.getNSideIdInManyRelation();
		if (!id) {
			return;
		}
		var domains = retrieveDomainsForEntryType(id);
		me.view.addDomainsAsNodeChildren(domains, node);
	}

	function synchUpperBranchCheck(node, checked) {
		while (node.parentNode) {
			node = node.parentNode;
			node.set("checked", checked);
			node.commit();
			synchUpperBranchCheck(node, checked);
		}
	}
})();