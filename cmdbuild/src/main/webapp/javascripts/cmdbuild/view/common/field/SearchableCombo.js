(function() {

	Ext.require('CMDBuild.proxy.Card');

	/**
	 * @deprecated (CMDBuild.view.common.field.comboBox.Searchable)
	 */
	Ext.define("CMDBuild.view.common.field.SearchableCombo", {
		extend: "CMDBuild.view.common.field.CMBaseCombo",

		trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
		trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
		trigger3Cls: Ext.baseCSSPrefix + 'form-search-trigger',

		hideTrigger1: false,
		hideTrigger2: false,
		hideTrigger3: false,

		/*
		 * use it to pass some
		 * configuration to the grid
		 * window
		 */
		gridExtraConfig: {},

		/*
		 * if read only, there isn't the
		 * add card button on the window
		 */
		searchWindowReadOnly: false,

		initComponent: function() {
			this.labelAlign = "right";
			this.callParent(arguments);
		},

		searchWindow: null,

		onTrigger1Click: function() {
			/*
			 * business rule: if the store has more record than
			 * the configuration limit
			 * we want open the search window
			 */
			if (this.store.isLoading()) {
				this.store.on('load', manageTrigger, this, {
					single: true
				});
			} else {
				manageTrigger.call(this);
			}

			function manageTrigger() {
				if (this.storeIsLargerThenLimit()) {
					this.onTrigger3Click();
				} else {
					this.onTriggerClick();
				}
			};
		},

		onTrigger2Click: function() {
			if (!this.disabled) {
				reset.call(this);
			}
		},

		onTrigger3Click: function(value) {
			if (typeof value != "string") {
				value = "";
			}

			// the value is passed to the
			// window to put it in the quick filter
			// above the grid
			this.createSearchWindow(value);
		},

		storeIsLargerThenLimit: function() {
			if (this.store !== null) {
				return this.store.getTotalCount() > CMDBuild.configuration.instance.get('referenceComboStoreLimit'); // TODO: use proxy constants
			}
			return false;
		},

		createSearchWindow: function(value) {
			if (!this.disabled
				&& !this.searchWindow) {

				var callback = Ext.Function.bind(this.buildSearchWindow, this, [this.store.baseParams, value], true);
				var idClass = this.store.baseParams.IdClass;
				if (!idClass) {
					var className = this.store.baseParams.className;
					if (className) {
						var entryType = _CMCache.getEntryTypeByName(className);
						if (entryType) {
							idClass = entryType.get("id");
						}
					}
				}

				if (idClass) {
					CMDBuild.Management.FieldManager.loadAttributes(idClass, callback);
				}
			}
		},

		buildSearchWindow: function(attributeList, storeParams, value) {
			var extraParams = Ext.apply({}, storeParams);
			delete extraParams.NoFilter;

			this.searchWindow = new CMDBuild.Management.ReferenceSearchWindow({
				ClassName: this.store.baseParams.className,
				selModel: new CMDBuild.selection.CMMultiPageSelectionModel({
					mode: "SINGLE",
					idProperty: "Id" // required to identify the records for the data and not the id of ext
				}),
				extraParams: extraParams,
				gridConfig: this.gridExtraConfig || {},
				readOnly: this.searchWindowReadOnly,
				searchFieldValue: value
			});

			this.searchWindow.on('cmdbuild-referencewindow-selected', function(record) {
				this.addToStoreIfNotInIt(record);
				this.focus(); // to allow the "change" event that occurs on blur
				this.setValue(record.get("Id"));
				this.fireEvent('cmdbuild-reference-selected', record, this);
			}, this);

			this.searchWindow.on("close", function removeWindowReference() {
				if (this.searchWindow) {
					this.searchWindow.destroy();
					delete this.searchWindow;
				}
			}, this);

			this.searchWindow.on("destroy", function removeWindowReference() {
				if (this.searchWindow) {
					delete this.searchWindow;
				}
			}, this);

			this.searchWindow.show();
		},

		/**
		 * @param {CMDBuild.Management.ReferenceSearchWindow row model} record
		 */
		addToStoreIfNotInIt: function(record) {
			var id = record.get('Id');

			if (this.getStore() && (this.getStore().find('Id', id) == -1)) {
				var params = Ext.apply({ cardId: id }, this.getStore().baseParams);

				CMDBuild.proxy.Card.read({
					params: params,
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						if (this.getStore().find('Id', id) == -1)
							this.getStore().add({
								Id: id,
								Description: decodedResponse.card['Description']
							});

						this.setValue(id);
						this.validate();
					}
				});
			}
		},

		recordDescriptionFixedForCarriageReturnBugOnComboBoxes: function(record) {
			try {
				return record.get("Description").replace(/\n/g," ");
			} catch (e) {
				_error('CMDBuild.view.common.field.SearchableCombo recordDescriptionFixedForCarriageReturnBugOnComboBoxes error', e);
			}
		},

		/*
		 * If the store has many items
		 * open the search window when the user try
		 * to filter the combo items typing on it
		 */
		// override
		onKeyUp: function() {
			if (this.storeIsLargerThenLimit()) {
				this.onTrigger3Click(this.getRawValue());
			} else {
				this.callParent(arguments);
			}
		},

		reset: reset
	});

	function reset() {
		this.setValue([""]); // if use clearValue the form does not send the value, so it is not possible delete the value on server side
	}

})();