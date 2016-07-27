(function () {

	Ext.define('CMDBuild.controller.management.widget.customForm.Import', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.proxy.widget.customForm.Csv',
			'CMDBuild.proxy.lookup.Lookup',
			'CMDBuild.proxy.widget.customForm.CustomForm'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetCustomFormImportAbortButtonClick',
			'onWidgetCustomFormImportModeChange',
			'onWidgetCustomFormImportUploadButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.widget.customForm.import.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.widget.customForm.import.ImportWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.widget.customForm.import.ImportWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * Complete CSV translation data and forward call to parent delegate:
		 * 	- Lookup: from description to id
		 * 	- Reference: from code to id
		 *
		 * @param {Array} csvData
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		dataManageAndForward: function (csvData) {
			if (
				!Ext.isEmpty(csvData) && Ext.isArray(csvData)
				&& !this.cmfg('widgetCustomFormConfigurationIsEmpty',  CMDBuild.core.constants.Proxy.MODEL)
			) {
				var requestBarrier = Ext.create('CMDBuild.core.RequestBarrier', {
					id: 'widgetCustomFormImportBarrier',
					scope: this,
					callback: function () {
						// Forwards to parent delegate
						this.cmfg('widgetCustomFormLayoutDataSet', this.importDataModeManager(csvData));
						this.cmfg('onWidgetCustomFormImportAbortButtonClick');

						this.view.setLoading(false);
					}
				});

				Ext.Array.forEach(this.cmfg('widgetCustomFormConfigurationGet', CMDBuild.core.constants.Proxy.MODEL), function (attribute, i, allAttributes) {
					switch (attribute.get(CMDBuild.core.constants.Proxy.TYPE)) {
						case 'lookup': {
							this.dataManageLookup(csvData, attribute, requestBarrier);
						} break;

						case 'reference': {
							this.dataManageReference(csvData, attribute, requestBarrier);
						} break;
					}
				}, this);

				requestBarrier.finalize('widgetCustomFormImportBarrier', true);
			}
		},

		/**
		 * @param {Array} csvData
		 * @param {CMDBuild.model.widget.customForm.Attribute} attribute
		 * @param {String} requestBarrier
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		dataManageLookup: function (csvData, attribute, requestBarrier) {
			if (
				!Ext.isEmpty(csvData) && Ext.isArray(csvData)
				&& !Ext.isEmpty(attribute)
				&& !Ext.isEmpty(requestBarrier)
			) {
				var attributeName = attribute.get(CMDBuild.core.constants.Proxy.NAME);

				var params = {};
				params[CMDBuild.core.constants.Proxy.TYPE] = attribute.get(CMDBuild.core.constants.Proxy.LOOKUP_TYPE);
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.lookup.Lookup.readAll({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ROWS];

						Ext.Array.forEach(csvData, function (recordObject, i, allRecordObjects) {
							if (!Ext.isEmpty(recordObject[attributeName])) {
								var selectedLookup = Ext.Array.findBy(decodedResponse, function (lookupObject, i) {
									return lookupObject['Description'] == recordObject[attributeName];
								}, this);

								if (!Ext.isEmpty(selectedLookup))
									csvData[i][attributeName] = selectedLookup['Id'];
							}
						}, this);
					},
					callback: requestBarrier.getCallback('widgetCustomFormImportBarrier')
				});
			} else {
				_error('malformed parameters in Lookup data manage', this, csvData, attribute, requestBarrier);
			}
		},

		/**
		 * @param {Array} csvData
		 * @param {CMDBuild.model.widget.customForm.Attribute} attribute
		 * @param {String} requestBarrier
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		dataManageReference: function (csvData, attribute, requestBarrier) {
			if (
				!Ext.isEmpty(csvData) && Ext.isArray(csvData)
				&& !Ext.isEmpty(attribute)
				&& !Ext.isEmpty(requestBarrier)
			) {
				var attributeName = attribute.get(CMDBuild.core.constants.Proxy.NAME);
				var cardsCodesToManage = [];

				Ext.Array.each(csvData, function (recordObject, i, allRecordObjects) {
					if (!Ext.isEmpty(recordObject[attributeName]))
						cardsCodesToManage.push(recordObject[attributeName]);
				}, this);

				if (!Ext.isEmpty(cardsCodesToManage)) {
					var params = {};
					params[CMDBuild.core.constants.Proxy.ATTRIBUTES] = Ext.encode(['Code', 'Description']);
					params[CMDBuild.core.constants.Proxy.CLASS_NAME] = attribute.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);
					params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode({ // Filters request to get only required cards
						attribute: {
							simple: {
								attribute: 'Code',
								operator: 'in',
								value: cardsCodesToManage,
								parameterType: 'fixed'
							}
						}
					});

					CMDBuild.proxy.widget.customForm.CustomForm.readAllCards({
						params: params,
						loadMask: false,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.ROWS];

							var referencedCardsMap = {};

							if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
								// Build referencedCardsMap
								Ext.Array.each(decodedResponse, function (cardObject, i, allCardObjects) {
									referencedCardsMap[cardObject['Code']] = cardObject;
								}, this);

								Ext.Array.each(csvData, function (recordObject, i, allRecordObjects) {
									if (!Ext.isEmpty(recordObject[attributeName])) {
										var selectedCard = referencedCardsMap[recordObject[attributeName]];

										if (!Ext.isEmpty(selectedCard))
											csvData[i][attributeName] = selectedCard['Id'];
									}
								}, this);
							}
						},
						callback: requestBarrier.getCallback('widgetCustomFormImportBarrier')
					});
				}
			} else {
				_error('malformed parameters in Reference data manage', this, csvData, attribute, requestBarrier);
			}
		},

		/**
		 * @param {Array} csvData
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		importDataModeManager: function (csvData) {
			csvData = Ext.Array.clean(csvData);

			if (!Ext.isEmpty(csvData) && Ext.isArray(csvData))
				switch (this.form.modeCombo.getValue()) {
					case 'add':
						return Ext.Array.push(this.cmfg('widgetCustomFormLayoutDataGet'), csvData);

					case 'merge':
						return this.importDataModeManagerMerge(csvData);

					case 'replace':
					default:
						return csvData;
				}
		},

		/**
		 * @param {Array} csvData
		 *
		 * @returns {Array}
		 *
		 * @private
		 */
		importDataModeManagerMerge: function (csvData) {
			csvData = Ext.Array.clean(csvData);

			var keyAttributes = Ext.Array.clean(this.form.keyAttributesMultiselect.getValue());

			if (
				!Ext.isEmpty(csvData) && Ext.isArray(csvData)
				&& !Ext.isEmpty(keyAttributes) && Ext.isArray(keyAttributes)
				&& this.isValidKeyCsvAttributes(keyAttributes, csvData)
				&& this.isValidGridStoreKeyAttributes(keyAttributes)
			) {
				var outputData = [];

				Ext.Array.forEach(this.cmfg('widgetCustomFormLayoutDataGet'), function (storeRowObject, i, allStoreRowObjects) {
					if (Ext.isObject(storeRowObject) && !Ext.Object.isEmpty(storeRowObject)) {
						var foundCsvRowObject = Ext.Array.findBy(csvData, function (csvRowObject, i, allCsvRowObjects) {
							var isValid = true;

							isValid = Ext.Array.each(keyAttributes, function (name, i, allNames) {
								return String(csvRowObject[name]) == String(storeRowObject[name]);
							}, this);

							return Ext.isBoolean(isValid);
						}, this);

						if (!Ext.Object.isEmpty(foundCsvRowObject)) {
							outputData.push(Ext.Object.merge(storeRowObject, foundCsvRowObject));
						} else {
							outputData.push(storeRowObject);
						}
					}
				}, this);

				return outputData;
			}

			return this.cmfg('widgetCustomFormLayoutDataGet');
		},

		/**
		 * Check key attributes value tuples local store uniqueness
		 *
		 * @param {Array} keyAttributes
		 * @param {Array} csvData
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isValidKeyCsvAttributes: function (keyAttributes, csvData) {
			if (
				!Ext.isEmpty(keyAttributes) && Ext.isArray(keyAttributes)
				&& !Ext.isEmpty(csvData) && Ext.isArray(csvData)
			) {
				var isValid = true;
				var keyAttributeCsvValues = [];

				// Build keyAttributeCsvValues array with append algorithm
				Ext.Array.forEach(csvData, function (csvRowObject, i, allCsvRowObjects) {
					var key = '';

					Ext.Array.forEach(keyAttributes, function (name, i, allNames) {
						key += csvRowObject[name];

						isValid = !Ext.isEmpty(csvRowObject[name]);
					}, this);

					keyAttributeCsvValues.push(key);
				}, this);

				// Check uniqueness of keyAttributes
				if (!Ext.isEmpty(keyAttributeCsvValues) && isValid) {
					isValid = Ext.Array.equals(Ext.Array.unique(keyAttributeCsvValues), keyAttributeCsvValues);
				} else {
					return CMDBuild.core.Message.error(
						CMDBuild.Translation.error,
						CMDBuild.Translation.errors.invalidKeyAttributesInCsvFile,
						false
					);
				}

				return isValid;
			}

			return false;
		},

		/**
		 * Check key attributes value tuples local store uniqueness
		 *
		 * @param {Array} keyAttributes
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isValidGridStoreKeyAttributes: function (keyAttributes) {
			if (!Ext.isEmpty(keyAttributes) && Ext.isArray(keyAttributes)) {
				var isValid = true;
				var keyAttributeCsvValues = [];

				// Build keyAttributeCsvValues array with append algorithm
				Ext.Array.forEach(this.cmfg('widgetCustomFormLayoutDataGet'), function (storeRowObject, i, allStoreRowObjects) {
					var key = '';

					Ext.Array.forEach(keyAttributes, function (name, i, allNames) {
						key += storeRowObject[name];

						isValid = !Ext.isEmpty(storeRowObject[name]);
					}, this);

					keyAttributeCsvValues.push(key);
				}, this);

				// Check uniqueness of keyAttributes
				if (!Ext.isEmpty(keyAttributeCsvValues) && isValid) {
					isValid = Ext.Array.equals(Ext.Array.unique(keyAttributeCsvValues), keyAttributeCsvValues);
				} else {
					return CMDBuild.core.Message.error(
						CMDBuild.Translation.error,
						CMDBuild.Translation.errors.invalidKeyAttributesInLocalStore,
						false
					);
				}

				return isValid;
			}

			return false;
		},

		/**
		 * @returns {Void}
		 */
		onWidgetCustomFormImportAbortButtonClick: function () {
			this.view.destroy();
		},

		/**
		 * @returns {Void}
		 */
		onWidgetCustomFormImportModeChange: function () {
			this.form.keyAttributesMultiselect.setDisabled(
				this.form.modeCombo.getValue() != 'merge'
			);
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 *
		 * @returns {Void}
		 */
		onWidgetCustomFormImportUploadButtonClick: function () {
			if (this.validate(this.form)) {
				this.view.setLoading(true);

				CMDBuild.proxy.widget.customForm.Csv.decode({
					form: this.form.getForm(),
					scope: this,
					failure: function (form, action) {
						this.view.setLoading(false);

						CMDBuild.core.Message.error(
							CMDBuild.Translation.common.failure,
							CMDBuild.Translation.errors.csvUploadOrDecodeFailure,
							false
						);
					},
					success: function (form, action) {
						var decodedRows = [];

						Ext.Array.forEach(action.result.response.elements, function (rowDataObject, i, allRowDataObjects) {
							if (!Ext.isEmpty(rowDataObject) && !Ext.isEmpty(rowDataObject.entries))
								decodedRows.push(rowDataObject.entries);
						}, this);

						this.dataManageAndForward(decodedRows);
					}
				});
			}
		}
	});

})();
