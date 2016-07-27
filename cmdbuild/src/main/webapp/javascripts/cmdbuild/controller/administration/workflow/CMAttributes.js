(function() {

	Ext.require([
		'CMDBuild.core.Message',
		'CMDBuild.proxy.common.tabs.attribute.Attribute',
		'CMDBuild.proxy.common.tabs.attribute.Order',
		'CMDBuild.view.common.field.translatable.Utils'
	]);

	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;

	Ext.define("CMDBuild.controller.administration.workflow.CMAttributes", {

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.workflow.Workflow} configurationObject.parentDelegate
		 * @param {CMDBuild.view.administration.workflow.CMProcessAttributes} configurationObject.view
		 */
		constructor: function(configurationObject) {
			Ext.apply(this, configurationObject); // Apply configuration properties

			this.getGrid().on("cm_attribute_moved", this.onAttributeMoved, this);

			this.currentClassId = null;

			this.gridSM = this.view.gridPanel.getSelectionModel();
			this.gridSM.on('selectionchange', onSelectionChanged , this);

            this.view.on("activate", this.onViewActivate, this);

			this.view.formPanel.abortButton.on("click", onAbortClick, this);
			this.view.formPanel.saveButton.on("click", onSaveClick, this);
			this.view.formPanel.deleteButton.on("click", onDeleteClick, this);
			this.view.gridPanel.addAttributeButton.on("click", onAddAttributeClick, this);
			this.view.gridPanel.orderButton.on("click", buildOrderingWindow, this);
            this.view.gridPanel.store.on("load", onAttributesAreLoaded, this);
		},

		anAttributeWasMoved: function(attributeList) {},

		getGrid: function() {
			return this.view.gridPanel;
		},

		getView: function() {
			return this.view;
		},

		getCurrentEntryTypeId: function() {
			return this.currentClassId;
		},

		onAttributeMoved: function() {
			var attributes = [];
			var store = this.getGrid().getStore();

			for (var i=0, l=store.getCount(); i<l; i++) {
				var rec = store.getAt(i);
				var attribute = {};
				attribute[CMDBuild.core.constants.Proxy.NAME] = rec.get("name");
				attribute[CMDBuild.core.constants.Proxy.INDEX] = i+1;
				attributes.push(attribute);
			}

			var me = this;
			var params = {};
			params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.JSON.encode(attributes);
			params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getCurrentEntryTypeId());

			CMDBuild.proxy.common.tabs.attribute.Order.reorder({
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					this.anAttributeWasMoved(attributes);
				}
			});
		},

		onClassSelected: function(classId, className) {
			this.currentClassId = classId;
			this.currentClassName = className;
			this.view.enable();
			if (tabIsActive(this.view)) {
				this.toLoad = false;
				this.view.onClassSelected(this.currentClassId, this.currentClassName);
			} else {
				this.toLoad = true;
			}
		},

		onAddClassButtonClick: function() {
			this.view.disable();
		},

		onViewActivate: function() {
			if (this.toLoad) {
				this.view.onClassSelected(this.currentClassId, this.currentClassName);
			}
		}
	});

	function onAttributesAreLoaded(store, records) {
		this.view.formPanel.fillAttributeGroupsStore(records);
	}

	function onSaveClick() {
		var nonValid = this.view.formPanel.getNonValidFields();
		if (nonValid.length > 0) {
			CMDBuild.core.Message.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			return;
		}

		// External metadata injection
		if (this.view.formPanel.comboType.getValue() == 'REFERENCE') {
			this.view.formPanel.referenceFilterMetadata['system.type.reference.' + CMDBuild.core.constants.Proxy.PRESELECT_IF_UNIQUE] = this.view.formPanel.preselectIfUniqueCheckbox.getValue();
		} else {
			this.view.formPanel.referenceFilterMetadata = {};
		}

		var data = this.view.formPanel.getData(withDisabled = true);
		data[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.currentClassId);
		data[CMDBuild.core.constants.Proxy.META] = Ext.JSON.encode(this.view.formPanel.referenceFilterMetadata);

		var me = this;
		CMDBuild.core.LoadMask.show();
		CMDBuild.proxy.common.tabs.attribute.Attribute.update({
			params : data,
			success : function(form, action, decoded) {
				me.view.gridPanel.refreshStore(me.currentClassId, decoded.attribute.index, me.currentClassName);

				CMDBuild.view.common.field.translatable.Utils.commit(me.view.formPanel);
			},
			callback: function() {
				CMDBuild.core.LoadMask.hide();
			}
		});
	}

	function onAbortClick() {
		if (this.currentAttribute == null) {
			this.view.formPanel.reset();
			this.view.formPanel.disableModify();
		} else {
			onSelectionChanged.call(this, null, [this.currentAttribute]);
		}
	}

	function onDeleteClick() {
		Ext.Msg.show({
			title: tr.delete_attribute,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteCurrentAttribute.call(this);
				}
			}
		});
	}

	function deleteCurrentAttribute() {
		if (this.currentAttribute == null) {
			return; //nothing to delete
		}

		var params = {};
		params[CMDBuild.core.constants.Proxy.NAME] = this.currentAttribute.get("name");
		params[CMDBuild.core.constants.Proxy.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.currentClassId);

		CMDBuild.proxy.common.tabs.attribute.Attribute.remove({
			params: params,
			scope: this,
			callback: function() {
				this.view.formPanel.reset();
				this.view.formPanel.disableModify();
				this.view.gridPanel.refreshStore(this.currentClassId, null, this.currentClassName);
			}
		});
	}

	function onSelectionChanged(sm, selection) {
		if (selection.length > 0) {
			this.currentAttribute = selection[0];
			this.view.formPanel.onAttributeSelected(this.currentAttribute);
		}
	}

	function onAddAttributeClick() {
		this.currentAttribute = null;
		this.view.formPanel.onAddAttributeClick();
		this.view.gridPanel.onAddAttributeClick();
	}

	function buildOrderingWindow() {
		if (this.currentClassId) {
			var win = new CMDBuild.Administration.SetOrderWindow( {
				idClass : this.currentClassId
			}).show();
		}
	}

    function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}
})();