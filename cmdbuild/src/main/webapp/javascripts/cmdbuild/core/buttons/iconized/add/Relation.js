(function() {

	Ext.define('CMDBuild.core.buttons.iconized.add.Relation', {
		extend: 'Ext.button.Split',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.domain.Domain',
			'CMDBuild.core.Utils'
		],

		iconCls: 'add',

		text: CMDBuild.Translation.addRelations,

		initComponent: function() {
			Ext.apply(this, {
				scope: this,
				menu: Ext.create('Ext.menu.Menu'),

				handler: function(button, e) {
					this.showMenu();
				}
			});

			this.callParent(arguments);
		},

		/**
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType
		 */
		setDomainsForEntryType: function(entryType) { // TODO: rename (setDomains)
			if (!Ext.isEmpty(entryType)) {
				this.menu.removeAll();

				var anchestorsId = CMDBuild.core.Utils.getEntryTypeAncestorsId(entryType);

				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = entryType.get(CMDBuild.core.constants.Proxy.NAME);
				params[CMDBuild.core.constants.Proxy.SKIP_DISABLED_CLASSES] = true;

				CMDBuild.proxy.domain.Domain.readAllByClass({
					params: params,
					scope: this,
					success: function(result, options, decodedResult) {
						Ext.Array.forEach(decodedResult[CMDBuild.core.constants.Proxy.DOMAINS], function(domain, i, allDomains) {
							if (_CMCache.isClassById(domain['class1id']) && _CMCache.isClassById(domain['class2id'])) {
								var domainCMObject = {};
								var originClass = _CMCache.getEntryTypeByName(domain['class1']);
								var destinationClass = _CMCache.getEntryTypeByName(domain['class2']);

								if (Ext.Array.contains(anchestorsId, domain['class1id'])) {
									domainCMObject = { // TODO: use domain real object in future
										dom_id: domain['idDomain'],
										description: domain['descrdir'] + ' (' + destinationClass.get(CMDBuild.core.constants.Proxy.TEXT) + ')',
										dst_cid: domain['class2id'],
										src_cid: domain['class1id'],
										src: '_1'
									};

									if (domain['priv_create']) // Add menu item only if i have create privileges
										this.menu.add({
											text: domainCMObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
											domain: domainCMObject,
											scope: this,

											handler: function(item, e){
												this.fireEvent('cmClick', item.domain);
											}
										});
								}

								if (Ext.Array.contains(anchestorsId, domain['class2id'])) {
									domainCMObject = { // TODO: use domain real object in future
										dom_id: domain['idDomain'],
										description: domain['descrinv'] + ' (' + originClass.get(CMDBuild.core.constants.Proxy.TEXT) + ')',
										dst_cid: domain['class1id'],
										src_cid: domain['class2id'],
										src: '_2'
									};

									if (domain['priv_create']) // Add menu item only if i have create privileges
										this.menu.add({
											text: domainCMObject[CMDBuild.core.constants.Proxy.DESCRIPTION],
											domain: domainCMObject,
											scope: this,

											handler: function(item, e){
												this.fireEvent('cmClick', item.domain);
											}
										});
								}
							}
						}, this);

						this.setDisabled(this.menu.items.items.length == 0); // Disable button if there aren't available domains

						// Ascending items sort
						this.menu.items.items.sort(function(a, b) {
							if (a.text < b.text)
								return -1;

							if (a.text > b.text)
								return 1;

							return 0;
						});
					}
				});
			}
		}
	});

})();