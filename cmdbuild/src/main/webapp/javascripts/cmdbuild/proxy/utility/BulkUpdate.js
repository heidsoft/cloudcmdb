(function () {

	Ext.define('CMDBuild.proxy.utility.BulkUpdate', {

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.utility.bulkUpdate.ClassesTree',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		bulkUpdate: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.card.bulkUpdate });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CARD, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		bulkUpdateFromFilter: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.card.bulkUpdateFromFilter });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CARD, parameters, true);
		},

		/**
		 * @returns {Ext.data.TreeStore} store
		 */
		getStoreClassesTree: function () {
			var store = Ext.create('Ext.data.TreeStore', {
				autoLoad: false,
				model: 'CMDBuild.model.utility.bulkUpdate.ClassesTree',
				root: {
					expanded: true,
					children: []
				},
				sorters: [
					{ property: 'cmIndex', direction: 'ASC' },
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.proxy.Classes.readAll({
				params: params,
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES] || [];

					var nodes = [];
					var standardNodesMap = {};

					// Removes all processes and root class from response
					decodedResponse = Ext.Array.filter(decodedResponse, function (item, i, array) {
						return (
							item[CMDBuild.core.constants.Proxy.TYPE] != CMDBuild.core.constants.Global.getTableTypeProcessClass() // Discard processes
							&& item[CMDBuild.core.constants.Proxy.TYPE] != CMDBuild.core.constants.Global.getTableTypeSimpleTable() // Discard simple classes
							&& item[CMDBuild.core.constants.Proxy.NAME] != CMDBuild.core.constants.Global.getRootNameClasses() // Discard root class of all classes
							&& !item[CMDBuild.core.constants.Proxy.SYSTEM] // Discard system classes
						);
					}, this);

					if (!Ext.isEmpty(decodedResponse)) {
						Ext.Array.each(decodedResponse, function (classObject, i, allClassObjects) {
							var nodeObject = {};
							nodeObject['iconCls'] = classObject['superclass'] ? 'cmdb-tree-superclass-icon' : 'cmdb-tree-class-icon';
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = classObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = classObject[CMDBuild.core.constants.Proxy.TEXT];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = classObject[CMDBuild.core.constants.Proxy.ID];
							nodeObject[CMDBuild.core.constants.Proxy.NAME] = classObject[CMDBuild.core.constants.Proxy.NAME];
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;
							nodeObject[CMDBuild.core.constants.Proxy.PARENT] = classObject[CMDBuild.core.constants.Proxy.PARENT];

							standardNodesMap[nodeObject[CMDBuild.core.constants.Proxy.ID]] = nodeObject;
						}, this);

						// Builds full standard/simple classes trees
						for (var id in standardNodesMap) {
							var node = standardNodesMap[id];

							if (
								!Ext.isEmpty(node[CMDBuild.core.constants.Proxy.PARENT])
								&& !Ext.isEmpty(standardNodesMap[node[CMDBuild.core.constants.Proxy.PARENT]])
							) {
								var parentNode = standardNodesMap[node[CMDBuild.core.constants.Proxy.PARENT]];
								parentNode.children = parentNode.children || [];
								parentNode.children.push(node);
								parentNode[CMDBuild.core.constants.Proxy.LEAF] = false;
							} else {
								nodes.push(node);
							}
						}

						// Manually sorting to avoid main classes group sorting
						CMDBuild.core.Utils.objectArraySort(nodes, CMDBuild.core.constants.Proxy.TEXT);

						store.getRootNode().removeAll();

						if (!Ext.isEmpty(nodes))
							store.getRootNode().appendChild(nodes);
					}
				}
			});

			return store;
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAttributes: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attribute.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTRIBUTE, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readClass: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.classes.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CLASS, parameters);
		}
	});

})();
