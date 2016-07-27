(function() {

	Ext.define('CMDBuild.controller.management.common.graph.Graph', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.Classes'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @property {String}
		 *
		 * @private
		 */
		basePath: undefined,

		/**
		 * @cfg {Number}
		 */
		cardId: undefined,

		/**
		 * @property {String}
		 *
		 * @private
		 */
		className: undefined,

		/**
		 * @cfg {String}
		 */
		classId: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onGraphWindowShow'
		],

		/**
		 * @cfg {CMDBuild.view.management.common.graph.GraphWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			if (
				!Ext.isEmpty(this.cardId) && Ext.isNumber(this.cardId)
				&& !Ext.isEmpty(this.classId) && Ext.isNumber(this.classId)
			) {
				this.basePath = window.location.toString().split('/');
				this.basePath = Ext.Array.slice(this.basePath, 0, this.basePath.length - 1).join('/');

				var params = {};
				params[CMDBuild.core.constants.Proxy.ACTIVE] = true;

				CMDBuild.proxy.Classes.readAll({
					params: params,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.CLASSES];

						if (!Ext.isEmpty(decodedResponse) && Ext.isArray(decodedResponse)) {
							var targetClassObject = Ext.Array.findBy(decodedResponse, function (item, i) {
								return item[CMDBuild.core.constants.Proxy.ID] == this.classId;
							}, this);

							if (!Ext.isEmpty(targetClassObject)) {
								this.className = targetClassObject[CMDBuild.core.constants.Proxy.NAME];

								this.view = Ext.create('CMDBuild.view.management.common.graph.GraphWindow', { delegate: this });
								this.view.show();
							} else {
								_error('class with id ' + this.classId + ' not found', this);
							}
						}
					}
				});
			} else {
				_error('wrong classId or cardId parameters', this);
			}
		},

		onGraphWindowShow: function () {
			this.view.removeAll();
			this.view.add({
				xtype: 'component',

				autoEl: {
					tag: 'iframe',
					src: this.basePath
						+ '/javascripts/cmdbuild-network/?basePath=' + this.basePath
						+ '&classId=' + this.className
						+ '&cardId=' + this.cardId
						+ '&frameworkVersion=' + CMDBuild.core.configurations.CustomPage.getVersion()
						+ '&language=' + CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.LANGUAGE)
				}
			});
		}
	});

})();
