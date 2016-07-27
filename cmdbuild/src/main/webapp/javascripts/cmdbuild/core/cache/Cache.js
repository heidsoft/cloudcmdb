(function() {

	/**
	 * CMDBuild cache v3
	 */
	Ext.define('CMDBuild.core.cache.Cache', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.interfaces.Rest'
		],

		/**
		 * Object to save all proxy results. All properties are instances of CMDBuild.model.Cache
		 *
		 * @cfg {Object}
		 * 	Structure: {
		 * 		{String} 'type': {
		 * 			{Object} 'groupId': {
		 * 				{Object} 'serviceEndpoint': {
		 * 					{CMDBuild.model.Cache} 'params',
		 * 					...
		 * 				},
		 * 			},
		 * 			...
		 * 		},
		 * 		...
		 * 	}
		 *
		 * @private
		 */
		bufferObject: {
			standard: {},
			store: {}
		},

		/**
		 * Enable/disable cache
		 *
		 * @cfg {Boolean}
		 *
		 * @private
		 */
		enabled: true,

		/**
		 * Managed group ids splits all cached functions in groups. Use 'uncached' to force uncached AJAX calls
		 *
		 * @cfg {Array}
		 *
		 * @private
		 */
		managedGroupsArray: [
			CMDBuild.core.constants.Proxy.GENERIC, // Default
			CMDBuild.core.constants.Proxy.CLASS,
			CMDBuild.core.constants.Proxy.GROUP,
			CMDBuild.core.constants.Proxy.USER,
			CMDBuild.core.constants.Proxy.WORKFLOW
		],

		/**
		 * @param {Object} configurationObject
		 *
		 * @returns {Void}
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject);

			// Setup global reference
			Ext.ns('CMDBuild.global');
			CMDBuild.global.Cache = this;
		},

		/**
		 * @param {String} parameters.type
		 * @param {String} parameters.groupId
		 * @param {String} parameters.serviceEndpoint
		 * @param {Object} parameters.params
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		cachedDataExists: function (parameters) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			return (
				!Ext.isEmpty(this.bufferObject[parameters.type])
				&& !Ext.isEmpty(this.bufferObject[parameters.type][parameters.groupId])
				&& !Ext.isEmpty(this.bufferObject[parameters.type][parameters.groupId][parameters.serviceEndpoint])
				&& !Ext.isEmpty(this.bufferObject[parameters.type][parameters.groupId][parameters.serviceEndpoint][Ext.encode(parameters.params)])
			);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		executeRequest: function (parameters) {
			switch (parameters.mode) {
				case 'ajax':
					return CMDBuild.core.interfaces.Ajax.request(parameters);

				case 'rest':
					return CMDBuild.core.interfaces.Rest.request(parameters);

				default: {
					_error('unmanaged or missing mode parameter', this, parameters);
				}
			}
		},

		/**
		 * @param {String} parameters.type
		 * @param {String} parameters.groupId
		 * @param {String} parameters.serviceEndpoint
		 * @param {Object} parameters.params
		 *
		 * @returns {Object or null} valuesFromCache
		 *
		 * @private
		 */
		get: function (parameters) {
			parameters = this.parametersValidate(parameters, [
				CMDBuild.core.constants.Proxy.TYPE,
				CMDBuild.core.constants.Proxy.GROUP_ID,
				CMDBuild.core.constants.Proxy.SERVICE_ENDPOINT,
				CMDBuild.core.constants.Proxy.PARAMS
			]);

			var valuesFromCache = null;

			if (this.cachedDataExists(parameters) && !this.isExpired(parameters))
				valuesFromCache = this.bufferObject[parameters.type][parameters.groupId][parameters.serviceEndpoint][Ext.encode(parameters.params)].get(CMDBuild.core.constants.Proxy.RESPONSE);

			return valuesFromCache;
		},

		/**
		 * Invalidate cache group (delete object)
		 *
		 * @param {String} parameters.type
		 * @param {String} parameters.groupId
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		invalidate: function (parameters) {
			parameters = this.parametersValidate(parameters, [ CMDBuild.core.constants.Proxy.GROUP_ID ]);

			if (!Ext.isEmpty(parameters.groupId) && this.isCacheable(parameters.groupId))
				Ext.Object.each(this.bufferObject, function (type, typeObject, myself) {
					if (!Ext.isEmpty(this.bufferObject[type]) && this.bufferObject[type][parameters.groupId])
						delete this.bufferObject[type][parameters.groupId];
				}, this);
		},

		/**
		 * @param {String} grupId
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		isCacheable: function (grupId) {
			if (
				!Ext.isEmpty(grupId)
				&& grupId != CMDBuild.core.constants.Proxy.UNCACHED // Force uncached AJAX calls
			) {
				return Ext.Array.contains(this.managedGroupsArray, grupId);
			}

			return false;
		},

		/**
		 * Returns expired state and manage validity invalidate if expired
		 *
		 * @param {String} parameters.type
		 * @param {String} parameters.groupId
		 * @param {String} parameters.serviceEndpoint
		 * @param {Object} parameters.params
		 *
		 * @returns {Boolean} isExpired
		 *
		 * @private
		 */
		isExpired: function (parameters) {
			parameters = this.parametersValidate(parameters, [
				CMDBuild.core.constants.Proxy.TYPE,
				CMDBuild.core.constants.Proxy.GROUP_ID,
				CMDBuild.core.constants.Proxy.SERVICE_ENDPOINT,
				CMDBuild.core.constants.Proxy.PARAMS
			]);

			var isExpired = true;

			if (this.cachedDataExists(parameters)) {
				var cachedObject = this.bufferObject[parameters.type][parameters.groupId][parameters.serviceEndpoint][Ext.encode(parameters.params)];

				isExpired = (
					Ext.isEmpty(cachedObject)
					|| Ext.Object.isEmpty(cachedObject)
					|| cachedObject.get(CMDBuild.core.constants.Proxy.DATE) < (new Date().valueOf() - CMDBuild.core.configurations.Timeout.getCache()) // Compatibility mode with IE older than IE 9 (Date.now())
				);

				if (isExpired)
					delete cachedObject;
			}

			return isExpired;
		},

		/**
		 * @returns {Boolean}
		 *
		 * @public
		 */
		isEnabled: function () {
			return this.enabled;
		},

		/**
		 * @param {Object} parameters
		 * @param {Array} paramsToReturn
		 *
		 * @returns {Object} outputObject
		 *
		 * @private
		 */
		parametersValidate: function (parameters, paramsToReturn) {
			parameters = Ext.isObject(parameters) ? parameters : {};

			var outputObject = {};

			// Apply defaults
			Ext.applyIf(parameters, {
				type: CMDBuild.core.constants.Proxy.STANDARD,
				groupId: CMDBuild.core.constants.Proxy.GENERIC,
				params: CMDBuild.core.constants.Proxy.EMPTY,
				values: null
			});

			parameters.groupId = Ext.isString(parameters.groupId) ? parameters.groupId : CMDBuild.core.constants.Proxy.GENERIC;
			parameters.serviceEndpoint = Ext.isString(parameters.serviceEndpoint) ? parameters.serviceEndpoint : null;
			parameters.params = Ext.isEmpty(parameters.params) || Ext.Object.isEmpty(parameters.params) ? CMDBuild.core.constants.Proxy.EMPTY : parameters.params;

			if (!Ext.isEmpty(paramsToReturn) && Ext.isArray(paramsToReturn)) {
				Ext.Array.forEach(paramsToReturn, function (param, i, allParams) {
					if (!Ext.isEmpty(parameters[param]))
						outputObject[param] = parameters[param];
				}, this);
			} else {
				outputObject = parameters;
			}

			return outputObject;
		},

		/**
		 * @param {String} cacheGroupIdentifier
		 * @param {Object} parameters
		 * @param {String} parameters.mode [ajax, rest]
		 * @param {String} parameters.method [DELETE, GET, POST, PUT]
		 * @param {String} parameters.url
		 * @param {Object} parameters.params
		 * @param {Object} parameters.restUrlParams
		 * @param {Object} parameters.headers
		 * @param {Object} parameters.jsonData
		 * @param {Object} parameters.loadMask
		 * @param {Object} parameters.scope
		 * @param {Function} parameters.callback
		 * @param {Function} parameters.failure
		 * @param {Function} parameters.success
		 * @param {Boolean} invalidateOnSuccess
		 *
		 * @returns {Void}
		 *
		 * @public
		 */
		request: function (groupId, parameters, invalidateOnSuccess) {
			groupId = Ext.isString(groupId) ? groupId : CMDBuild.core.constants.Proxy.GENERIC;
			invalidateOnSuccess = Ext.isBoolean(invalidateOnSuccess) ? invalidateOnSuccess : false;

			if (
				!Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.url)
			) {
				// Apply defaults
				Ext.applyIf(parameters, {
					mode: 'ajax',
					method: 'POST',
					loadMask: true,
					scope: this,
					callback: Ext.emptyFn,
					failure: Ext.emptyFn,
					success: Ext.emptyFn
				});

				if (
					this.isEnabled()
					&& this.isCacheable(groupId)
				) { // Cacheable endpoints manage
					if (
						!this.isExpired({
							groupId: groupId,
							serviceEndpoint: parameters.url,
							params: parameters.params
						})
						&& !invalidateOnSuccess
					) { // Emulation of success and callback execution
						var cachedValues = this.get({
							groupId: groupId,
							serviceEndpoint: parameters.url,
							params: parameters.params
						});

						Ext.Function.createSequence(
							Ext.bind(parameters.success, parameters.scope, [
								cachedValues.response,
								cachedValues.options,
								cachedValues.decodedResponse
							]),
							Ext.bind(parameters.callback, parameters.scope, [
								cachedValues.options,
								true,
								cachedValues.response
							]),
							parameters.scope
						)();
					} else { // Execute real AJAX call
						parameters.success = Ext.Function.createSequence(function (response, options, decodedResponse) {
							if (invalidateOnSuccess) { // Don't cache if want to invalidate
								CMDBuild.global.Cache.invalidate({ groupId: groupId });
							} else {
								CMDBuild.global.Cache.set({
									groupId: groupId,
									serviceEndpoint: parameters.url,
									params: parameters.params,
									values: {
										response: response,
										options: options,
										decodedResponse: decodedResponse
									}
								});
							}
						}, parameters.success);

						this.executeRequest(parameters);
					}
				} else { // Uncacheable endpoints manage
					this.executeRequest(parameters);
				}
			} else {
				_error('invalid request parameters', this);
			}
		},

		/**
		 * @param {String} parameters.groupId
		 * @param {Object} storeParameters - Store configuration object
		 *
		 * @returns {CMDBuild.core.cache.Store}
		 *
		 * @public
		 */
		requestAsStore: function (groupId, storeParameters) {
			parameters = this.parametersValidate({ groupId: groupId }, [CMDBuild.core.constants.Proxy.GROUP_ID]);

			Ext.apply(storeParameters, parameters);

			return Ext.create('CMDBuild.core.cache.Store', storeParameters);
		},

		/**
		 * @param {String} parameters.type
		 * @param {String} parameters.groupId
		 * @param {String} parameters.serviceEndpoint
		 * @param {Object} parameters.params
		 * @param {Object} parameters.values
		 *
		 * @returns {Object or null}
		 *
		 * @private
		 */
		set: function (parameters) {
			parameters = this.parametersValidate(parameters, [
				CMDBuild.core.constants.Proxy.TYPE,
				CMDBuild.core.constants.Proxy.GROUP_ID,
				CMDBuild.core.constants.Proxy.SERVICE_ENDPOINT,
				CMDBuild.core.constants.Proxy.PARAMS,
				CMDBuild.core.constants.Proxy.VALUES
			]);

			if (
				!Ext.isEmpty(parameters.serviceEndpoint) && !Ext.isEmpty(parameters.values)
				&& this.isCacheable(parameters.groupId)
			) {
				var cacheObject = {};
				cacheObject[CMDBuild.core.constants.Proxy.DATE] = new Date().valueOf(); // Compatibility mode with IE older than IE 9 (Date.now())
				cacheObject[CMDBuild.core.constants.Proxy.PARAMETERS] = parameters.params;
				cacheObject[CMDBuild.core.constants.Proxy.RESPONSE] = parameters.values;

				// Creates cache groupId object if not exists
				if (Ext.isEmpty(this.bufferObject[parameters.type][parameters.groupId]))
					this.bufferObject[parameters.type][parameters.groupId] = {};

				// Creates cache serviceEndpoint object if not exists
				if (Ext.isEmpty(this.bufferObject[parameters.type][parameters.groupId][parameters.serviceEndpoint]))
					this.bufferObject[parameters.type][parameters.groupId][parameters.serviceEndpoint] = {};

				this.bufferObject[parameters.type][parameters.groupId][parameters.serviceEndpoint][Ext.encode(parameters.params)] = Ext.create('CMDBuild.model.Cache', cacheObject);

				return this.bufferObject[parameters.type][parameters.groupId][parameters.serviceEndpoint][Ext.encode(parameters.params)];
			}

			return null;
		}
	});

})();
