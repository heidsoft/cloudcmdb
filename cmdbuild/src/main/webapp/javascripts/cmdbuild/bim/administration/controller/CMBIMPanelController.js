(function() {

	Ext.require([
		'CMDBuild.bim.proxy.Bim',
		'CMDBuild.bim.proxy.Ifc',
		'CMDBuild.bim.proxy.Layer'
	]);

	Ext.define('CMDBuild.controller.administration.filter.CMBIMPanelController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		mixins: {
			gridFormPanelDelegate: 'CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate'
		},

		constructor: function(view) {
			this.callParent(arguments);
			this.mixins.gridFormPanelDelegate.constructor.call(this, view);
			this.fieldManager = null;
			this.gridConfigurator = null;
			this.record = null;

			this.view.delegate = this;
		},

		onViewOnFront: function(group) {
			var me = this;

			if (this.fieldManager == null) {
				this.fieldManager = new CMDBuild.delegate.administration.bim.CMBIMFormFieldsManager();
				this.view.buildFields(this.fieldManager);
				this.bimCardBinding();

				this.fieldManager.getImportIfcButton().handler = function() {
					me.onImportIfc();
				};
			}
			this.view.disableModify();

			if (this.gridConfigurator == null) {
				this.gridConfigurator = new CMDBuild.delegate.administration.bim.CMBIMGridConfigurator();
				this.view.configureGrid(this.gridConfigurator);
			}

			this.gridConfigurator.getStore().load();
			this.selectFirstRow();
		},

		// as gridFormPanelDelegate

		/**
		 * Called after the save button click
		 *
		 * @param (CMDBuild.view.administration.common.basepanel.CMForm) form
		 */
		// override
		onGridAndFormPanelSaveButtonClick: function(form) {
			var me = this;
			var params = me.fieldManager.getValues() || {};
			var proxyFunction = CMDBuild.bim.proxy.Bim.create;

			if (this.record != null) {
				proxyFunction = CMDBuild.bim.proxy.Bim.update;
				params['id'] = this.record.getId();
			}

			if (form != null) {
				CMDBuild.core.LoadMask.show();

				this.view.enableModify();

				proxyFunction(form, params,
					function onSuccess() {
						me.fieldManager.enableFileField();
						CMDBuild.core.LoadMask.hide();
						me.gridConfigurator.getStore().load();
						me.view.disableModify(me.enableCMTBar = false);
						form.reset();
						me.view.grid.getSelectionModel().deselectAll();
					},
					function onFailure() {
						me.view.disableModify(me.enableCMTBar = false);
						form.reset();
						me.view.grid.getSelectionModel().deselectAll();

						CMDBuild.core.LoadMask.hide();
					}
				);
			}
		},

		onImportIfc: function() {
			if (!Ext.isEmpty(this.record))
				CMDBuild.bim.proxy.Ifc.import({
					scope: this,
					params: {
						projectId: this.record.getId()
					}
				});
		},

		/**
		 * @param (CMDBuild.view.administration.common.basepanel.CMForm) form the form that call the function
		 * @param (String) action - a string that say if the button is clicked when configured to activate or deactivate something ['disable' | 'enable']
		 */
		// override
		onEnableDisableButtonClick: function(form, action) {
			var me = this;

			if (!me.record)
				return;

			var proxyFunction = CMDBuild.bim.proxy.Bim.disable;
			if (action == 'enable') {
				proxyFunction = CMDBuild.bim.proxy.Bim.enable;
				this.view.updateEnableDisableButton(false);
			}
			else {
				this.view.updateEnableDisableButton(true);
			}

			CMDBuild.core.LoadMask.show();
			proxyFunction({
				params: {
					id: me.record.getId()
				},
				callback: function() {
					CMDBuild.core.LoadMask.hide();
					me.gridConfigurator.getStore().load();
				}
			});
		},

		// as form delegate
		bimCardBinding: function() {
			var bindingReference = this.view.query('#bimCardBinding')[0];

			CMDBuild.bim.proxy.Layer.readRootName({
				success: function(operation, config, response) {
					bindingReference.initializeItems(response.root);
				},
				callback: Ext.emptyFn
			});
		},

		// as grid delegate
		// override
		onCMGridSelect: function(grid, record) {
			this.mixins.gridFormPanelDelegate.onCMGridSelect.apply(this, arguments);

			if (record)
				this.view.updateEnableDisableButton(!record.get('active'));
		}
	});

})();