(function() {

	/**
	 * @deprecated (CMDBuild.controller.common.abstract.Widget)
	 */
	Ext.define('CMDBuild.controller.management.common.widgets.CMWidgetController', {

		requires: ['CMDBuild.core.constants.Proxy'],

		statics: {
			WIDGET_NAME: '',
			getTemplateResolverServerVars: getTemplateResolverServerVarsFromModel
		},

		/**
		 * @property {Object}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		ownerController: undefined,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCards}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMWidgetManager} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			if (typeof view != 'object')
				throw 'The view of a WidgetController must be an object';

			if (typeof widgetConf != 'object')
				throw 'The widget configuration is mandatory';

			this.WIDGET_NAME = this.self.WIDGET_NAME;

			this.card = card;
			this.clientForm = clientForm;
			this.ownerController = ownerController;
			this.view = view;
			this.widgetConf = widgetConf;
			this.outputName = this.widgetConf.outputName;
		},

		/**
		 * @abstract
		 */
		beforeActiveView: Ext.emptyFn,

		/**
		 * Executed before window hide perform
		 *
		 * @abstract
		 */
		beforeHideView: Ext.emptyFn,

		/**
		 * @abstract
		 */
		destroy: Ext.emptyFn,

		/**
		 * @return {Mixed}
		 */
		getData: function() {
			return null;
		},

		/**
		 * @param {String}
		 */
		getLabel: function() {
			return this.widgetConf[CMDBuild.core.constants.Proxy.LABEL];
		},

		/**
		 * @param {String} variableName
		 *
		 * @return {Mixed}
		 */
		getVariable: function(variableName) {
			try {
				return this.templateResolver.getVariable(variableName);
			} catch (e) {
				_debug('There is no template resolver');

				return undefined;
			}
		},

		getTemplateResolverServerVars: function() {
			return getTemplateResolverServerVarsFromModel(this.card);
		},

		/**
		 * @return {Number}
		 */
		getWidgetId: function() {
			return this.widgetConf[CMDBuild.core.constants.Proxy.ID];
		},

		/**
		 * @return {Boolean}
		 */
		isValid: function() {
			return true;
		},

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		onBeforeSave: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& Ext.isFunction(parameters.callback)
			) {
				Ext.callback(
					parameters.callback,
					Ext.isEmpty(parameters.scope) ? this : parameters.scope
				);
			} else {
				_error('[' + this.getLabel() + '] onBeforeSave invalid parameters', this);
			}
		},

		/**
		 * @abstract
		 */
		onEditMode: Ext.emptyFn,

		/**
		 * @return {String}
		 */
		toString: function() {
			return Ext.getClassName(this);
		}
	});

	function getTemplateResolverServerVarsFromModel(model) {
		var out = {};

		if (model) {
			var pi = null;

			if (Ext.getClassName(model) == 'CMDBuild.model.CMActivityInstance') {
				// Retrieve the process instance because it stores the data. this.card has only the varibles to show in this step (is the activity instance)
				pi = _CMWFState.getProcessInstance();
			} else if (Ext.getClassName(model) == 'CMDBuild.model.CMProcessInstance') {
				pi = model;
			}

			if (pi != null) { // The processes use a new serialization. Add backward compatibility attributes to the card values
				out = Ext.apply({
					'Id': pi.get('Id'),
					'IdClass': pi.get('IdClass'),
					'IdClass_value': pi.get('IdClass_value')
				}, pi.getValues());
			} else {
				out = model.raw || model.data;
			}
		}

		_debug('Server vars', out);

		return out;
	}

})();