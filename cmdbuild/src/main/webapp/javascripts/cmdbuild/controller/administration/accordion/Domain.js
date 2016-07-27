(function () {

	Ext.define('CMDBuild.controller.administration.accordion.Domain', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.domain.Domain'
		],

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'accordionBuildId',
			'accordionDeselect',
			'accordionExpand',
			'accordionFirstSelectableNodeSelect',
			'accordionFirtsSelectableNodeGet',
			'accordionIdentifierGet',
			'accordionNodeByIdExists',
			'accordionNodeByIdGet',
			'accordionNodeByIdSelect',
			'accordionUpdateStore',
			'onAccordionBeforeSelect',
			'onAccordionExpand',
			'onAccordionSelectionChange'
		],

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.administration.accordion.Domain}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.accordion.Domain', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			nodeIdToSelect = Ext.isEmpty(nodeIdToSelect) ? null : nodeIdToSelect;

			CMDBuild.proxy.domain.Domain.readAll({
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DOMAINS];

					// Removes all system domains
					decodedResponse = Ext.Array.filter(decodedResponse, function (item, i, array) {
						return !item[CMDBuild.core.constants.Proxy.SYSTEM]; // Discard system domains
					}, this);

					this.view.getStore().getRootNode().removeAll();

					if (!Ext.isEmpty(decodedResponse)) {
						var nodes = [];

						Ext.Array.forEach(decodedResponse, function (domainObject, i, allDomainObjects) {
							var nodeObject = {};
							nodeObject['cmName'] = this.cmfg('accordionIdentifierGet');
							nodeObject['iconCls'] = 'cmdb-tree-domain-icon';
							nodeObject[CMDBuild.core.constants.Proxy.TEXT] = domainObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.DESCRIPTION] = domainObject[CMDBuild.core.constants.Proxy.DESCRIPTION];
							nodeObject[CMDBuild.core.constants.Proxy.ENTITY_ID] = domainObject[CMDBuild.core.constants.Proxy.ID_DOMAIN];
							nodeObject[CMDBuild.core.constants.Proxy.ID] = this.cmfg('accordionBuildId', domainObject[CMDBuild.core.constants.Proxy.ID_DOMAIN]);
							nodeObject[CMDBuild.core.constants.Proxy.LEAF] = true;

							nodes.push(nodeObject);
						}, this);

						if (!Ext.isEmpty(nodes)) {
							this.view.getStore().getRootNode().appendChild(nodes);
							this.view.getStore().sort();
						}
					}

					// Alias of this.callParent(arguments), inside proxy function doesn't work
					this.updateStoreCommonEndpoint(nodeIdToSelect);
				}
			});

			this.callParent(arguments);
		}
	});

})();
