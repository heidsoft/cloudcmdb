(function () {

	Ext.define('CMDBuild.view.common.field.HtmlEditor', {
		extend: 'Ext.ux.form.field.TinyMCE',

		requires: ['CMDBuild.core.Utils'],

		/**
		 * Custom CMDBuild buttons configurations to use
		 *
		 * @cfg {Object} CMDBuilds custom configurations
		 *
		 * @private
		 */
		customConfigurations: {
			common: {
				skin: 'extjs',
				skin_variant: 'silver', // Default color is silver
				schema: 'html5',
				language: 'en',
				theme_advanced_row_height: 27,
				delta_height: 1,
				width: '100%',
				theme_advanced_resizing: false,
				theme_advanced_resize_horizontal: false,
				relative_urls: false,
				convert_urls: false
			},

			full: {
				theme: 'advanced',
				plugins: 'autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,wordcount,advlist',

				// Theme options
				theme_advanced_buttons1: 'save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect',
				theme_advanced_buttons2: 'cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor',
				theme_advanced_buttons3: 'tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen',
				theme_advanced_buttons4: 'insertlayer,moveforward,movebackward,absolute,|,styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,pagebreak,restoredraft',
				theme_advanced_toolbar_location: 'top',
				theme_advanced_toolbar_align: 'left',
				theme_advanced_statusbar_location: 'none'

			},
			standard: {
				theme: 'advanced',
				plugins: 'autolink,paste,fullscreen',

				// Theme options
				theme_advanced_buttons1: 'bold,italic,underline,|,fontsizeselect,|,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,link,unlink,|,bullist,numlist,|,pastetext,pasteword,|,cleanup,removeformat,|,fullscreen,|,code',
				theme_advanced_toolbar_location: 'top',
				theme_advanced_toolbar_align: 'left',
				theme_advanced_statusbar_location: 'none'
			}
		},

		/**
		 * @cfg {Boolean}
		 */
		dirty: false,

		/**
		 * @cfg {Object or String}
		 */
		tinyMCEConfig: 'standard',

		considerAsFieldToDisable: true,
		minHeight: 150,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			var validIdentifiers = ['full', 'standard'];

			// Check and setup TinyMCE configuration from string identifier
			if (Ext.isEmpty(this.tinyMCEConfig) || Ext.isString(this.tinyMCEConfig) || !Ext.Array.contains(validIdentifiers, this.tinyMCEConfig))
				this.tinyMCEConfig = 'standard';

			this.tinyMCEConfig = Ext.Object.merge(this.customConfigurations['common'], this.customConfigurations[this.tinyMCEConfig]);

			// Language setup
			this.tinyMCEConfig.language = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.LANGUAGE);

			// Silver editor color setup for Administration
			if (Ext.isEmpty(CMDBuild.core.Management))
				this.tinyMCEConfig.popup_css = 'javascripts/ext-'
					+ CMDBuild.core.Utils.getExtJsVersion() + '-ux/form/field/tinymce/themes/advanced/skins/extjs/dialog_silver.css';

			// Blue editor color setup for Management
			if (Ext.isEmpty(CMDBuild.core.Administration))
				this.tinyMCEConfig.skin_variant = 'blue';

			this.callParent(arguments);

			this.on('change', function (field, newValue, oldValue, eOpts) {
				this.setDirty(); // Set as dirty
			}, this);
		},

		/**
		 * @returns {Void}
		 */
		initValue: function () {
			this.dirty = false;
		},

		/**
		 * Dirty functionality implementation
		 *
		 * @returns {Boolean}
		 */
		isDirty: function () {
			if (!Ext.isEmpty(this.getEditor()))
				try { // Avoids a getBody of null error
					return this.getEditor().isDirty() || this.dirty;
				} catch (e) {}

			return false;
		},

		/**
		 * @returns {Void}
		 */
		setDirty: function () {
			this.dirty = true;
		},

		/**
		 * Override to avoid event suspend that deny correct editor rendering
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		suspendEvents: Ext.emptyFn
	});

})();
