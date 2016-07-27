(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksFormEventController}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {String}
		 */
		taskType: 'event_asynchronous',

		/**
		 * @property {CMDBuild.view.administration.tasks.event.asynchronous.CMStep1}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @override
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		// GETters functions
			/**
			 * @return {String}
			 */
			getValueId: function() {
				return this.view.idField.getValue();
			},

		/**
		 * @return {Boolean}
		 */
		isEmptyClass: function() {
			return Ext.isEmpty(this.view.classNameCombo.getValue());
		},

		// SETters functions
			/**
			 * @param {Boolean} state
			 */
			setDisabledButtonNext: function(state) {
				this.parentDelegate.setDisabledButtonNext(state);
			},

			/**
			 * @param {Boolean} state
			 */
			setDisabledTypeField: function(state) {
				this.view.typeField.setDisabled(state);
			},

			/**
			 * @param {Boolean} state
			 */
			setValueActive: function(state) {
				this.view.activeField.setValue(state);
			},

			/**
			 * @param {String} value
			 */
			setValueClassName: function(value) {
				this.view.classNameCombo.setValue(value);

				// Manually select event fire
				this.cmOn('onClassSelected', { className: value });
			},

			/**
			 * @param {String} value
			 */
			setValueDescription: function(value) {
				this.view.descriptionField.setValue(value);
			},

			/**
			 * @param {String} value
			 */
			setValueId: function(value) {
				this.view.idField.setValue(value);
			}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.view.administration.tasks.event.asynchronous.CMStep1Delegate}
		 */
		delegate: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			maxWidth: CMDBuild.core.constants.FieldWidths.CONFIGURATION_BIG,
			anchor: '100%'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				name: CMDBuild.core.constants.Proxy.TYPE,
				value: tr.tasksTypes.event + ' ' + tr.tasksTypes.eventTypes.asynchronous.toLowerCase(),
				disabled: true,
				cmImmutable: true,
				readOnly: true,
				submitValue: false
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.core.constants.Proxy.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.constants.Proxy.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.core.constants.Proxy.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL
			});

			this.classNameCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.constants.Proxy.CLASS_NAME,
				fieldLabel: CMDBuild.Translation.classLabel,
				labelWidth: CMDBuild.core.constants.FieldWidths.LABEL,
				valueField: CMDBuild.core.constants.Proxy.NAME,
				displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
				maxWidth: CMDBuild.core.constants.FieldWidths.ADMINISTRATION_BIG,
				allowBlank: false,
				forceSelection: true,
				editable: false,

				store: _CMCache.getClassesAndProcessesStore(),
				queryMode: 'local',

				listeners: {
					select: function(combo, records, options) {
						me.delegate.cmOn('onClassSelected', { className: this.getValue() });
					}
				}
			});

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.classNameCombo
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			// Disable next button only if class is not selected
			activate: function(view, eOpts) {
				if (this.delegate.isEmptyClass())
					this.delegate.setDisabledButtonNext(true);
			}
		}
	});

})();