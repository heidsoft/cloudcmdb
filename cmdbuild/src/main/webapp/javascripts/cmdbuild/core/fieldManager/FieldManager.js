(function () {

	/**
	 * New implementation of field manager, builds fields starting from CMDBuild.model.common.attributes.Attribute model.
	 */
	Ext.define('CMDBuild.core.fieldManager.FieldManager', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Object}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.model.common.attributes.Attribute}
		 *
		 * @private
		 */
		attributeModel: undefined,

		/**
		 * @cfg {Ext.form.Panel}
		 */
		targetForm: undefined,

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		templateResolverAttributes: [],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		templateList: ['{client', '{cql', '{group', '{js', '{server', '{user', '{xa'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'attributeModelGet',
			'attributeModelIsEmpty',
			'templateResolverBuild',
			'templateResolverGetResolveFunction'
		],

		/**
		 * @property {Array}
		 *
		 * @private
		 */
		managedAttributesTypes: ['boolean', 'char', 'date', 'decimal', 'double', 'foreignkey', 'integer', 'text', 'time', 'timestamp', 'string'],

		// AttributeModel methods
			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Mixed}
			 */
			attributeModelGet: function(attributePath) {
				attributePath = Ext.isArray(attributePath) ? attributePath : [attributePath];

				var requiredAttribute = this.attributeModel;

				if (!Ext.isEmpty(attributePath))
					Ext.Array.forEach(attributePath, function(attributeName, i, allAttributeNames) {
						if (!Ext.isEmpty(attributeName) && Ext.isString(attributeName)) {
							if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
								&& Ext.isFunction(requiredAttribute.get)
							) { // Model management
								requiredAttribute = requiredAttribute.get(attributeName);
							} else if (
								!Ext.isEmpty(requiredAttribute)
								&& Ext.isObject(requiredAttribute)
							) { // Simple object management
								requiredAttribute = requiredAttribute[attributeName];
							}
						}
					}, this);

				return requiredAttribute;
			},

			/**
			 * @param {Array or String} attributePath
			 *
			 * @returns {Boolean}
			 */
			attributeModelIsEmpty: function(attributePath) {
				if (!Ext.isEmpty(attributePath))
					return Ext.isEmpty(this.attributeModelGet(attributePath));

				return Ext.isEmpty(this.attributeModel);
			},

			/**
			 * @param {CMDBuild.model.common.attributes.Attribute} attributeModel
			 */
			attributeModelSet: function(attributeModel) {
				if (
					!Ext.isEmpty(attributeModel)
					&& Ext.getClassName(attributeModel) == 'CMDBuild.model.common.attributes.Attribute'
					&& attributeModel.isValid() // Validate attribute model
				) {
					this.attributeModel = attributeModel;
				} else {
					_error('invalid attribute model', this);
				}
			},

		/**
		 * @returns {Mixed}
		 *
		 * @private
		 */
		buildAttributeController: function() {
			var attributeType = this.attributeModelGet(CMDBuild.core.constants.Proxy.TYPE).toLowerCase();

			switch (attributeType) {
				case 'boolean': return Ext.create('CMDBuild.core.fieldManager.builders.Boolean', { parentDelegate: this });
				case 'char': return Ext.create('CMDBuild.core.fieldManager.builders.Char', { parentDelegate: this });
				case 'date': return Ext.create('CMDBuild.core.fieldManager.builders.Date', { parentDelegate: this });
				case 'decimal': return Ext.create('CMDBuild.core.fieldManager.builders.Decimal', { parentDelegate: this });
				case 'double': return Ext.create('CMDBuild.core.fieldManager.builders.Double', { parentDelegate: this });
				case 'foreignkey': return Ext.create('CMDBuild.core.fieldManager.builders.ForeignKey', { parentDelegate: this });
				case 'integer': return Ext.create('CMDBuild.core.fieldManager.builders.Integer', { parentDelegate: this });
				case 'string': return Ext.create('CMDBuild.core.fieldManager.builders.String', { parentDelegate: this });
				case 'text': return Ext.create('CMDBuild.core.fieldManager.builders.text.Text', { parentDelegate: this });
				case 'time': return Ext.create('CMDBuild.core.fieldManager.builders.Time', { parentDelegate: this });
				case 'timestamp': return Ext.create('CMDBuild.core.fieldManager.builders.TimeStamp', { parentDelegate: this });
			}
		},

		/**
		 * Builds Ext.grid.column.* object
		 *
		 * @param {Boolean} withEditor
		 *
		 * @returns {Object}
		 */
		buildColumn: function(withEditor) {
			withEditor = Ext.isBoolean(withEditor) ? withEditor : false;

			return this.buildAttributeController().buildColumn(withEditor);
		},

		/**
		 * Builds Ext.form.field.* object
		 *
		 * @param {CMDBuild.model.common.attributes.Attribute} attributeModel
		 *
		 * @returns {Object}
		 */
		buildEditor: function() {
			return this.buildAttributeController().buildEditor();
		},

		/**
		 * Builds Ext.form.field.* object
		 *
		 * @returns {Object}
		 */
		buildField: function() {
			return this.buildAttributeController().buildField();
		},

		/**
		 * Builds Ext.data.Store field definition object
		 *
		 * @returns {Object}
		 */
		buildStoreField: function() {
			return this.buildAttributeController().buildStoreField();
		},

		/**
		 * Manage attributeType as case insensitive
		 *
		 * @param {String} attributeType
		 *
		 * @returns {Boolean}
		 */
		isAttributeManaged: function(attributeType) {
			attributeType = attributeType.toLowerCase();

			return Ext.Array.contains(this.managedAttributesTypes, attributeType);
		},

		// Service functions to add generated fields to targets
			/**
			 * Filters empty components
			 *
			 * @param {Object} target
			 * @param {Mixed or Array} components
			 */
			add: function(target, components) {
				components = Ext.isArray(components) ? components : [components];

				if (
					Ext.isObject(target)
					&& Ext.isFunction(target.add)
				) {
					components = Ext.Array.filter(components, function(item, i, array) {
						return !Ext.isEmpty(item) && !Ext.Object.isEmpty(item);
					}, this);

					if (!Ext.isEmpty(components))
						target.add(components);
				} else {
					_error('target not supported object', this);
				}
			},

			/**
			 * Filters empty elements
			 *
			 * @param {Array} target
			 * @param {Mixed or Array} elements
			 */
			push: function(target, elements) {
				elements = Ext.isArray(elements) ? elements : [elements];

				if (Ext.isArray(target)) {
					elements = Ext.Array.filter(elements, function(item, i, array) {
						return !Ext.isEmpty(item) && !Ext.Object.isEmpty(item);
					}, this);

					if (!Ext.isEmpty(elements))
						target = Ext.Array.push(target, elements);
				} else {
					_error('target in not array', this);
				}
			},

		// TemplateResolver property methods
			/**
			 * @returns {Object}
			 *
			 * @private
			 */
			templateResolverAttributesDataGet: function() {
				var attributesData = {};

				Ext.Array.forEach(this.templateResolverAttributesGet(), function(attribute, i, allAttributes) {
					attributesData[attribute] = this.attributeModelGet(attribute);
				}, this);

				return attributesData;
			},

			/**
			 * @returns {Array}
			 *
			 * @private
			 */
			templateResolverAttributesGet: function() {
				return this.templateResolverAttributes;
			},

			/**
			 * @returns {Boolean}
			 *
			 * @private
			 */
			templateResolverAttributesHasTemplates: function() {
				var encodedAttributesModel = Ext.encode(this.attributeModelGet().getData());

				return !Ext.Array.every(this.templateList, function(template, i, allTemplates) {
					return encodedAttributesModel.indexOf(template) < 0; // Stops loop at first template found
				}, this);
			},

			/**
			 * @param {Array} attributes
			 *
			 * @private
			 */
			templateResolverAttributesSet: function(attributes) {
				this.templateResolverAttributes = [];

				if (!Ext.isEmpty(attributes) && Ext.isArray(attributes))
					this.templateResolverAttributes = attributes;
			},

			/**
			 * @param {Array} attributes
			 *
			 * @returns {CMDBuild.Management.TemplateResolver} templateResolver
			 *
			 * @private
			 */
			templateResolverBuild: function(attributes) {
				var templateResolver = undefined;

				if (
					!Ext.isEmpty(this.targetForm)
					&& this.targetForm instanceof Ext.form.Panel
					&& this.templateResolverAttributesHasTemplates()
				) {
					this.templateResolverAttributesSet(attributes);

					templateResolver = new CMDBuild.Management.TemplateResolver({
						clientForm: this.targetForm.getForm(),
						xaVars: this.templateResolverAttributesDataGet()
					});
				}

				return templateResolver;
			},

			/**
			 * Resolve template of filter attribute
			 *
			 * @private
			 */
			templateResolverGetResolveFunction: function() {
				return function() { // This is field object
					if (!Ext.isEmpty(this.templateResolver) && !this.isDisabled()) {
						this.templateResolver.resolveTemplates({
							attributes: [CMDBuild.core.constants.Proxy.FILTER],
							scope: this,
							callback: function(out, ctx) {
								// Filter attribute manage
								var params = {};
								params[CMDBuild.core.constants.Proxy.CLASS_NAME] = this.attributeModel.get(CMDBuild.core.constants.Proxy.TARGET_CLASS);
								params[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode({ CQL: out[CMDBuild.core.constants.Proxy.FILTER] });

								if (!this.getStore().isLoading()) {
									this.getStore().getProxy().extraParams = params; // Set last load params
									this.getStore().load();
								}

								// Add listeners to  fields which depends
								Ext.Object.each(this.templateResolver.getLocalDepsAsField(), function(dependsName, dependsField, myself) {
									if (!Ext.isEmpty(dependsField))
										dependsField.on('change', function(field, newValue, oldValue, eOpts) {
											this.reset();
											this.resolveTemplates();
										}, this);
								}, this);
							}
						});
					}
				};
			}
	});

})();