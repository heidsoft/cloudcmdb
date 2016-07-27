(function () {

	Ext.define('CMDBuild.controller.administration.widget.form.Calendar', {
		extend: 'CMDBuild.controller.administration.widget.form.Abstract',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.widget.calendar.Definition'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.widget.Widget}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'classTabWidgetAdd',
			'classTabWidgetCalendarDefinitionGet = classTabWidgetDefinitionGet',
			'classTabWidgetCalendarLoadRecord = classTabWidgetLoadRecord',
			'classTabWidgetDefinitionModelNameGet',
			'onClassTabWidgetCalendarTargetClassChange'
		],

		/**
		 * @cfg {String}
		 *
		 * @private
		 */
		definitionModelName: 'CMDBuild.model.widget.calendar.Definition',

		/**
		 * @cfg {CMDBuild.view.administration.widget.form.CalendarPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.administration.widget.Widget} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.administration.widget.form.CalendarPanel', { delegate: this });
		},

		/**
		 * @param {String} selectedClassName
		 */
		onClassTabWidgetCalendarTargetClassChange: function (selectedClassName) {
			if (!Ext.isEmpty(selectedClassName)) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;
				params[CMDBuild.core.constants.Proxy.CLASS_NAME] = selectedClassName;

				this.view.startDate.getStore().load({ params: params });
				this.view.endDate.getStore().load({ params: params });
				this.view.defaultDate.getStore().load({ params: params });
				this.view.eventTitle.getStore().load({ params: params });
			}
		},

		/**
		 * @return {Object}
		 */
		classTabWidgetCalendarDefinitionGet: function () {
			return CMDBuild.model.widget.calendar.Definition.convertToLegacy(
				Ext.create(this.classTabWidgetDefinitionModelNameGet(), this.view.getData(true)).getData()
			);
		},

		/**
		 * Fills form with widget data
		 *
		 * @param {CMDBuild.model.widget.calendar.Definition} record
		 */
		classTabWidgetCalendarLoadRecord: function (record) {
			this.view.loadRecord(record);
		}
	});

})();
