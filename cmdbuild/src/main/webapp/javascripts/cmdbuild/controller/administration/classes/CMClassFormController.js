(function() {

	var tr = CMDBuild.Translation.administration.modClass.classProperties;

	Ext.define("CMDBuild.controller.administration.classes.CMClassFormController", {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Classes',
			'CMDBuild.view.common.field.translatable.Utils'
		],

		constructor: function(view) {
			if (Ext.isEmpty(view)) {
				this.view = new CMDBuild.view.administration.classes.CMClassForm({
					title: CMDBuild.Translation.administration.modClass.tabs.properties,
					border: false
				});
			} else {
				this.view = view;
			}

			this.selection = null;

			this.view.abortButton.on("click", this.onAbortClick, this);
			this.view.saveButton.on("click", this.onSaveClick, this);
			this.view.deleteButton.on("click", this.onDeleteClick, this);
			this.view.printClassButton.on("click", this.onPrintClass, this);
		},

		getView: function() {
			return this.view;
		},

		onClassSelected: function(classId) {
			this.selection = _CMCache.getClassById(classId);
			if (this.selection) {
				this.view.onClassSelected(this.selection);

				this.view.iconForm.delegate.cmfg('onClassPropertiesIconClassSelected', this.selection);
			}
		},

		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.onAddClassButtonClick();
		},

		onSaveClick: function() {
			CMDBuild.core.LoadMask.show();

			CMDBuild.proxy.Classes.update({
				params: this.buildSaveParams(),
				loadMask: false,
				callback: callback,
				success: this.saveSuccessCB,
				scope: this
			});

			function callback() {
				CMDBuild.core.LoadMask.hide();
			}
		},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 */
		saveSuccessCB: function(response, options, decodedResponse) {
			decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.TABLE];

			this.view.disableModify(true);

			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', {
				identifier: 'class',
				nodeIdToSelect: decodedResponse[CMDBuild.core.constants.Proxy.ID]
			});

			/**
			 * @deprecated
			 */
			this.selection = _CMCache.onClassSaved(decodedResponse);

			CMDBuild.view.common.field.translatable.Utils.commit(this.view.form);

			CMDBuild.core.Message.success();
		},

		buildSaveParams: function() {
			var withDisabled = true;
			var params = this.view.getData(withDisabled);

			/*
			 * If has no selection, we are adding
			 * a new table. Set the parameter forceCreation
			 * to say to the server that must check that
			 * does not exists another table with that name
			 */
			params.forceCreation = this.selection == null;
			params.isprocess = false;
			params.description = params.text; // adapter: maybe one day everything will be better
			params.inherits = params.parent; // adapter

			return params;
		},

		onDeleteClick: function() {
			Ext.Msg.show({
				title: tr.remove_class,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == "yes") {
						this.deleteCurrentClass();
					}
				}
			});
		},

		deleteCurrentClass: function() {
			CMDBuild.core.LoadMask.hide();

			var params = {};
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.selection.get("name");

			CMDBuild.proxy.Classes.remove({
				params: params,
				loadMask: false,
				callback: callback,
				success: this.deleteSuccessCB,
				scope: this
			});

			function callback() {
				CMDBuild.core.LoadMask.hide();
				this.view.disableModify();
				this.view.reset();
			}

		},

		/**
		 * @param {Object} response
		 * @param {Object} options
		 * @param {Object} decodedResponse
		 */
		deleteSuccessCB: function(response, options, decodedResponse) {
			var removedClassId = this.selection.get(CMDBuild.core.constants.Proxy.ID);

			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionDeselect', 'class');
			CMDBuild.global.controller.MainViewport.cmfg('mainViewportAccordionControllerUpdateStore', { identifier: 'class' });

			/**
			 * @deprecated
			 */
			_CMCache.onClassDeleted(removedClassId);
			this.selection = null;

			CMDBuild.core.Message.success();
		},

		onAbortClick: function() {
			this.view.disableModify();
			this.view.reset();
			if (this.selection != null) {
				this.view.onClassSelected(this.selection);
			}
		},

		/**
		 * @params {String} format
		 */
		onPrintClass: function(format) {
			if (!Ext.isEmpty(format)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.selection.get(CMDBuild.core.constants.Proxy.ID));
				params[CMDBuild.core.constants.Proxy.FORMAT] = format;

				Ext.create('CMDBuild.controller.common.entryTypeGrid.printTool.PrintWindow', {
					parentDelegate: this,
					format: format,
					mode: 'classSchema',
					parameters: params
				});
			}
		}
	});

})();