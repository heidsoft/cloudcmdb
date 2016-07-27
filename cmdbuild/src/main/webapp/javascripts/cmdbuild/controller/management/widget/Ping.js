(function () {

	Ext.define('CMDBuild.controller.management.widget.Ping', {
		extend: 'CMDBuild.controller.common.abstract.Widget',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.widget.Ping'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.model.CMActivityInstance or Ext.data.Model}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'beforeHideView',
			'getData',
			'getId = widgetPingIdGet',
			'getTemplateResolverServerVars = widgetPingGetTemplateResolverServerVars',
			'isValid',
			'onEditMode',
			'onWidgetPingBeforeActiveView = beforeActiveView',
			'widgetConfigurationGet = widgetPingConfigurationGet'
		],

		/**
		 * @property {CMDBuild.view.management.widget.PingView}
		 */
		view: undefined,

		/**
		 * @cfg {String}
		 */
		widgetConfigurationModelClassName: 'CMDBuild.model.widget.ping.Configuration',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onWidgetPingBeforeActiveView: function () {
			this.beforeActiveView(arguments); // CallParent alias

			this.view.removeAll();

			var params = {};
			params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

			CMDBuild.proxy.widget.Ping.readClass({ // TODO: waiting for refactor (CRUD)
				params: params,
				scope: this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

					if (!Ext.isEmpty(decodedResponse)) {
						this.targetClassObject = Ext.Array.findBy(decodedResponse, function (item, i) {
							return item[CMDBuild.core.constants.Proxy.ID] == this.card.get('IdClass');
						}, this);

						if (!Ext.Object.isEmpty(this.targetClassObject)) {
							this.resolveConfigurationTemplates(this.targetClassObject[CMDBuild.core.constants.Proxy.NAME]);
						} else {
							_error('class "' + this.card.get('IdClass') + '" not found', this);
						}
					}
				}
			});
		},

		/**
		 * @param {String} className
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		resolveConfigurationTemplates: function (className) {
			if (!Ext.isEmpty(className) && Ext.isString(className)) {
				var xaVars = this.cmfg('widgetPingConfigurationGet', CMDBuild.core.constants.Proxy.TEMPLATES);
				xaVars['_address'] = this.cmfg('widgetPingConfigurationGet', CMDBuild.core.constants.Proxy.ADDRESS);

				new CMDBuild.Management.TemplateResolver({
					clientForm: this.clientForm,
					xaVars: xaVars,
					serverVars: this.cmfg('widgetPingGetTemplateResolverServerVars')
				}).resolveTemplates({
					attributes: ['_address'],
					scope: this,
					callback: function (out, ctx) {
						var params = {};
						params[CMDBuild.core.constants.Proxy.ACTION] = 'legacytr';
						params[CMDBuild.core.constants.Proxy.CLASS_NAME] = className;
						params[CMDBuild.core.constants.Proxy.ID] = this.card.get('Id');
						params[CMDBuild.core.constants.Proxy.PARAMS] = Ext.encode({ address: out['_address'] });
						params[CMDBuild.core.constants.Proxy.WIDGET_ID] = this.cmfg('widgetPingIdGet');

						CMDBuild.proxy.widget.Ping.ping({
							params: params,
							loadMask: this.view, // Apply load mask to view
							scope: this,
							success: function (response, options, decodedResponse) {
								decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

								if (!Ext.isEmpty(decodedResponse) && Ext.isString(decodedResponse))
									this.showPingResult(decodedResponse);
							}
						});
					}
				});
			} else {
				_error('wrong or unmanaged className parameter', this);
			}
		},

		/**
		 * @param {String} result
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		showPingResult: function (result) {
			result = Ext.isString(result) ? result : '';

			this.view.add(
				Ext.create('Ext.panel.Panel', {
					border: false,
					frame: false,
					padding: '0 10',
					html: '<pre>' + result + '</pre>'
				})
			);
		}
	});

})();
