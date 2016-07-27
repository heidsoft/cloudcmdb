/**
 * This file is part of Ext JS 4.2
 *
 * Copyright (c) 2011-2013 Sencha Inc
 *
 * Contact:  http://www.sencha.com/contact
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public License version 3.0 as
 * published by the Free Software Foundation and appearing in the file LICENSE included in the
 * packaging of this file.
 *
 * Please review the following information to ensure the GNU General Public License version 3.0
 * requirements will be met: http://www.gnu.org/copyleft/gpl.html.
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department
 * at http://www.sencha.com/contact.
 *
 * Build date: 2013-03-11 22:33:40 (aed16176e68b5e8aa1433452b12805c0ad913836)
 */
(function () {

	Ext.onReady(function () {
		var cm = Ext.ClassManager;
		var exists = Ext.Function.bind(cm.get, cm);

		if (Ext.Updater)
			Ext.Updater.defaults.indicatorText = '<div class="loading-indicator">加载...</div>';

		if (exists('Ext.data.Types'))
			Ext.data.Types.stripRe = /[\$,%]/g;

		if (Ext.Date) {
			Ext.Date.monthNames = ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'];

			Ext.Date.getShortMonthName = function (month) {
				return Ext.Date.monthNames[month].substring(0, 3);
			};

			Ext.Date.monthNumbers = {
				Jan: 0,
				Feb: 1,
				Mar: 2,
				Apr: 3,
				May: 4,
				Jun: 5,
				Jul: 6,
				Aug: 7,
				Sep: 8,
				Oct: 9,
				Nov: 10,
				Dec: 11
			};

			Ext.Date.getMonthNumber = function (name) {
				return Ext.Date.monthNumbers[name.substring(0, 1).toUpperCase() + name.substring(1, 3).toLowerCase()];
			};

			Ext.Date.dayNames = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];

			Ext.Date.getShortDayName = function (day) {
				return Ext.Date.dayNames[day].substring(0, 3);
			};

			Ext.Date.parseCodes.S.s = "(?:st|nd|rd|th)";
		}

		if (Ext.MessageBox)
			Ext.MessageBox.buttonText = {
				ok: 'OK',
				cancel: '取消',
				yes: '是',
				no: '否'
			};

		if (exists('Ext.util.Format'))
			Ext.apply(Ext.util.Format, {
				thousandSeparator: ',',
				decimalSeparator: '.',
				currencySign: '$',
				dateFormat: 'm/d/Y'
			});

		if (exists('Ext.form.field.VTypes')) {
			Ext.apply(Ext.form.field.VTypes, {
				emailText: 'This field should be an e-mail address in the format "user@example.com"',
				urlText: 'This field should be a URL in the format "http:/' + '/www.example.com"',
				alphaText: 'This field should only contain letters and _',
				alphanumText: 'This field should only contain letters, numbers and _'
			});
		}
	});

	Ext.define('Ext.locale.zh_CN.view.View', {
		override: 'Ext.view.View',

		emptyText: ''
	});

	Ext.define('Ext.locale.zh_CN.grid.plugin.DragDrop', {
		override: 'Ext.grid.plugin.DragDrop',

		dragText: '{0} selected row{1}'
	});

	// changing the msg text below will affect the LoadMask
	Ext.define('Ext.locale.zh_CN.view.AbstractView', {
		override: 'Ext.view.AbstractView',

		msg: '加载...'
	});

	Ext.define('Ext.locale.zh_CN.picker.Date', {
		override: 'Ext.picker.Date',

		todayText: '今天',
		minText: '此日期在最小日期前',
		maxText: '此日期在最大日期之后',
		disabledDaysText: '',
		disabledDatesText: '',
		monthNames: Ext.Date.monthNames,
		dayNames: Ext.Date.dayNames,
		nextText: 'Next Month (Control+Right)',
		prevText: 'Previous Month (Control+Left)',
		monthYearText: 'Choose a month (Control+Up/Down to move years)',
		todayTip: '{0} (Spacebar)',
		format: 'm/d/y',
		startDay: 0
	});

	Ext.define('Ext.locale.zh_CN.picker.Month', {
			override: 'Ext.picker.Month',

		okText: '&#160;OK&#160;',
		cancelText: '取消'
	});

	Ext.define('Ext.locale.zh_CN.toolbar.Paging', {
		override: 'Ext.PagingToolbar',

		beforePageText: '页',
		afterPageText: 'of {0}',
		firstText: '第一页',
		prevText: '前一页',
		nextText: '下一页',
		lastText: '最后一页',
		refreshText: '刷新',
		displayMsg: '显示 {0} - {1} of {2}',
		emptyMsg: 'No data to display'
	});

	Ext.define('Ext.locale.zh_CN.form.Basic', {
		override: 'Ext.form.Basic',

		waitTitle: '请稍等...'
	});

	Ext.define('Ext.locale.zh_CN.form.field.Base', {
		override: 'Ext.form.field.Base',

		invalidText: '此字段的值无效'
	});

	Ext.define('Ext.locale.zh_CN.form.field.Text', {
		override: 'Ext.form.field.Text',

		minLengthText: '此字段最小长度是 {0}',
		maxLengthText: '此字段最大长度是{0}',
		blankText: '此字段是必须的',
		regexText: '',
		emptyText: null
	});

	Ext.define('Ext.locale.zh_CN.form.field.Number', {
		override: 'Ext.form.field.Number',

		decimalSeparator: '.',
		decimalPrecision: 2,
		minText: '此字段最小长度是 {0}',
		maxText: '此字段最大长度是 {0}',
		nanText: '{0} 不是一个有效值'
	});

	Ext.define('Ext.locale.zh_CN.form.field.Date', {
		override: 'Ext.form.field.Date',

		disabledDaysText: '不可用',
		disabledDatesText: '不可用',
		minText: '此字段的日期必须在 {0}之后',
		maxText: '此字段的日期必须在{0}这前',
		invalidText: '{0} 是一个无效值 - 它的格式必须是 {1}',
		format: 'm/d/y',
		altFormats: 'm/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d'
	});

	Ext.define('Ext.locale.zh_CN.form.field.ComboBox', {
		override: 'Ext.form.field.ComboBox',

		valueNotFoundText: undefined
	}, function () {
		Ext.apply(Ext.form.field.ComboBox.prototype.defaultListConfig, {
			loadingText: '加载...'
		});
	});

	Ext.define('Ext.locale.zh_CN.form.field.HtmlEditor', {
		override: 'Ext.form.field.HtmlEditor',
		createLinkText: 'Please enter the URL for the link:'
	}, function () {
		Ext.apply(Ext.form.field.HtmlEditor.prototype, {
			buttonTips: {
				bold: {
					title: 'Bold (Ctrl+B)',
					text: 'Make the selected text bold.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				italic: {
					title: 'Italic (Ctrl+I)',
					text: 'Make the selected text italic.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				underline: {
					title: 'Underline (Ctrl+U)',
					text: 'Underline the selected text.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				increasefontsize: {
					title: 'Grow Text',
					text: 'Increase the font size.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				decreasefontsize: {
					title: 'Shrink Text',
					text: 'Decrease the font size.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				backcolor: {
					title: 'Text Highlight Color',
					text: 'Change the background color of the selected text.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				forecolor: {
					title: 'Font Color',
					text: 'Change the color of the selected text.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifyleft: {
					title: 'Align Text Left',
					text: 'Align text to the left.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifycenter: {
					title: 'Center Text',
					text: 'Center text in the editor.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifyright: {
					title: 'Align Text Right',
					text: 'Align text to the right.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				insertunorderedlist: {
					title: 'Bullet List',
					text: 'Start a bulleted list.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				insertorderedlist: {
					title: 'Numbered List',
					text: 'Start a numbered list.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				createlink: {
					title: 'Hyperlink',
					text: 'Make the selected text a hyperlink.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				sourceedit: {
					title: 'Source Edit',
					text: 'Switch to source editing mode.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				}
			}
		});
	});

	Ext.define('Ext.locale.zh_CN.grid.header.Container', {
		override: 'Ext.grid.header.Container',

		sortAscText: '升序排序',
		sortDescText: '降序排序',
		columnsText: '列'
	});

	Ext.define('Ext.locale.zh_CN.grid.GroupingFeature', {
		override: 'Ext.grid.GroupingFeature',

		emptyGroupText: '(None)',
		groupByText: 'Group By This Field',
		showGroupsText: 'Show in Groups'
	});

	Ext.define('Ext.locale.zh_CN.grid.PropertyColumnModel', {
		override: 'Ext.grid.PropertyColumnModel',

		nameText: '名称',
		valueText: '值',
		dateFormat: 'm/j/Y',
		trueText: '正确',
		falseText: '错误'
	});

	Ext.define('Ext.locale.zh_CN.grid.BooleanColumn', {
		override: 'Ext.grid.BooleanColumn',

		trueText: '正确',
		falseText: '错误',
		undefinedText: '&#160;'
	});

	Ext.define('Ext.locale.zh_CN.grid.NumberColumn', {
		override: 'Ext.grid.NumberColumn',

		format: '0,000.00'
	});

	Ext.define('Ext.locale.zh_CN.grid.DateColumn', {
		override: 'Ext.grid.DateColumn',

		format: 'm/d/Y'
	});

	Ext.define('Ext.locale.zh_CN.form.field.Time', {
		override: 'Ext.form.field.Time',

		minText: '此字段的时间必须等于或晚于{0}',
		maxText: '此字段的时间必须等于或早于 {0}',
		invalidText: '{0} 是一个无效时间',
		format: 'g:i A',
		altFormats: 'g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H'
	});

	Ext.define('Ext.locale.zh_CN.form.CheckboxGroup', {
		override: 'Ext.form.CheckboxGroup',

		blankText: '在此组中你必须选择一项'
	});

	Ext.define('Ext.locale.zh_CN.form.RadioGroup', {
		override: 'Ext.form.RadioGroup',

		blankText: '在此组中你必须选择一项'
	});

	// This is needed until we can refactor all of the locales into individual files
	Ext.define('Ext.locale.zh_CN.Component', {
		override: 'Ext.Component'
	});

})();
