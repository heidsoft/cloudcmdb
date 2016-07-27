(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMDomainTabController", {

		requires: ['CMDBuild.proxy.domain.Domain'],

		constructor: function(view) {
			if (Ext.isEmpty(view)) {
				this.view = new CMDBuild.Administration.DomainGrid({
					title : CMDBuild.Translation.administration.modClass.tabs.domains,
					border: false,
					disabled: true
				});
			} else {
				this.view = view;
			}

			this.selection = null;

			this.view.on("itemdblclick", onItemDoubleClick, this);
			this.view.getSelectionModel().on("selectionchange", onSelectionChange, this);
			this.view.addDomainButton.on("click", onAddDomainButton, this);
			this.view.modifyButton.on("click", onModifyDomainButton, this);
			this.view.deleteButton.on("click", onDeleteDomainButton, this);
		},

		getView: function() {
			return this.view;
		},

		onClassSelected: function(classId) {
			this.selection = classId;
			var entryTypeData = _CMCache.getEntryTypeById(classId).data;
			if (entryTypeData.tableType == "simpletable") {
				this.view.disable();
				return;
			}

			var view = this.view;
			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(classId);

			CMDBuild.core.LoadMask.show();
			view.store.load({
				params: params,
				callback: function() {
					CMDBuild.core.LoadMask.hide();
					view.filterInherited(view.filtering);
				}
			});

			view.enable();
			view.modifyButton.disable();
			view.deleteButton.disable();
		},

		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.disable();
		}

	});

	function onSelectionChange(sm, selection) {
		if (selection.length > 0) {
			this.currentDomain = selection[0];
			this.view.modifyButton.enable();
			this.view.deleteButton.enable();
		}
	}

	function onItemDoubleClick(grid, record) {
		if (!Ext.isEmpty(record)) {
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionDeselect', 'domain');
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', 'domain').disableStoreLoad = true;
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerExpand', 'domain');
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: 'domain',
				nodeIdToSelect: record.get('idDomain')
			});
		}
	}

	function onModifyDomainButton() {
		if (this.currentDomain) {
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionDeselect', 'domain');
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', 'domain').disableStoreLoad = true;
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerExpand', 'domain');

			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', 'domain').getView().on('storeload', function(accordion, eOpts) {
				Ext.Function.createDelayed(function() {
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', 'domain').cmfg('onDomainModifyButtonClick');
				}, 100, this)();
			}, this, { single: true });

			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: 'domain',
				nodeIdToSelect: this.currentDomain.get('idDomain')
			});
		}
	}

	function onDeleteDomainButton() {
		Ext.Msg.show({
			title: CMDBuild.Translation.administration.modClass.domainProperties.delete_domain,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteDomain.call(this);
				}
			}
		});
	}

	function deleteDomain() {
		if (this.currentDomain == null) {
			// nothing to delete
			return;
		}

		var params = {};
		params[CMDBuild.core.constants.Proxy.DOMAIN_NAME] = this.currentDomain.get("name");

		CMDBuild.proxy.domain.Domain.remove({
			params: params,
			scope: this,
			success: function(response, options, decodedResponse) {
				this.onClassSelected(this.selection);

				_CMCache.onDomainDeleted(this.currentDomain.get("idDomain"));

				this.currentDomain = null;
			}
		});
	}

	function onAddDomainButton() {
		CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionDeselect', 'domain');

		CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', 'domain').getView().on('storeload', function(accordion, eOpts) {
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', 'domain').cmfg('onDomainAddButtonClick');
		}, this, { single: true });

		CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerGet', 'domain').disableSelection = true;
		CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerExpand', 'domain');
	}
})();