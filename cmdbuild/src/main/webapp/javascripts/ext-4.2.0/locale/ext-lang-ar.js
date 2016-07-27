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
(function() {

	Ext.onReady(function() {
		var cm = Ext.ClassManager;
		var exists = Ext.Function.bind(cm.get, cm);

		if (Ext.Updater)
			Ext.Updater.defaults.indicatorText = '<div class="loading-indicator">جاري التحميل ...</div>';

		if (exists('Ext.data.Types'))
			Ext.data.Types.stripRe = /[\$,%]/g;

		if (Ext.Date) {
			Ext.Date.monthNames = ['يناير', 'فبراير', 'مارس', 'إبريل', 'مايو', 'يونيو', 'يوليو', 'أغسطس', 'سمبتمبر', 'أكتوبر', 'نوفمبر', 'ديسمبر'];

			Ext.Date.getShortMonthName = function(month) {
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

			Ext.Date.getMonthNumber = function(name) {
				return Ext.Date.monthNumbers[name.substring(0, 1).toUpperCase() + name.substring(1, 3).toLowerCase()];
			};

			Ext.Date.dayNames = ['الأحد', 'الإثنين', 'الثلاثاء', 'الأربعاء', 'الخميس', 'الجمعة', 'السبت'];

			Ext.Date.getShortDayName = function(day) {
				return Ext.Date.dayNames[day].substring(0, 3);
			};

			Ext.Date.parseCodes.S.s = "(?:st|nd|rd|th)";
		}

		if (Ext.MessageBox) {
			Ext.MessageBox.buttonText = {
				ok: 'حسنًا',
				cancel: 'إلغاء',
				yes: 'نعم',
				no: 'لا'
			};
		}

		if (exists('Ext.util.Format'))
			Ext.apply(Ext.util.Format, {
				thousandSeparator: ',',
				decimalSeparator: '.',
				currencySign: '$',
				dateFormat: 'm/d/Y'
			});

		if (exists('Ext.form.field.VTypes'))
			Ext.apply(Ext.form.field.VTypes, {
				emailText: 'يجب أن يكون هذا الحقل بريد إلكتروني كهذا "user@example.com"',
				urlText: 'يجب أن يكون هذا الحقل رابط كهذا "http:/' + '/www.example.com"',
				alphaText: 'هذا الحقل يمكن أن يحتوي فقط على حروف و _',
				alphanumText: 'هذا الحقل يمكن أن يحتوي فقط على حروف وأرقام و _'
			});
	});

	Ext.define('Ext.locale.ar.view.View', {
		override: 'Ext.view.View',

		emptyText: ''
	});

	Ext.define('Ext.locale.ar.grid.plugin.DragDrop', {
		override: 'Ext.grid.plugin.DragDrop',

		dragText: '{0} {صف مختار{1'
	});

	// changing the msg text below will affect the LoadMask

	Ext.define('Ext.locale.ar.view.AbstractView', {
		override: 'Ext.view.AbstractView',

		msg: 'جاري التحميل ...'
	});

	Ext.define('Ext.locale.ar.picker.Date', {
		override: 'Ext.picker.Date',

		todayText: 'اليوم',
		minText: 'هذا التاريخ قبل التاريخ الأدنى',
		maxText: 'هذا التاريخ قبل التاريخ الأقصى',
		disabledDaysText: '',
		disabledDatesText: '',
		monthNames: Ext.Date.monthNames,
		dayNames: Ext.Date.dayNames,
		nextText: 'الشهر التالي (Control+Right)',
		prevText: 'الشهر السابق (Control+Left)',
		monthYearText: 'اختر شهر (Control+Up/Down لتحريك السنوات)',
		todayTip: '{0} (Spacebar)',
		format: 'm/d/y',
		startDay: 0

	});

	Ext.define('Ext.locale.ar.picker.Month', {
		override: 'Ext.picker.Month',

		okText: 'حسنًا',
		cancelText: 'إلغاء'
	});

	Ext.define('Ext.locale.ar.toolbar.Paging', {
		override: 'Ext.PagingToolbar',

		beforePageText: 'صفحة',
		afterPageText: '{من {0',
		firstText: 'الصفحة الأولى',
		prevText: 'الصفحة السابقة',
		nextText: 'الصفحة التالية',
		lastText: 'الصفحة الأخيرة',
		refreshText: 'تحديث',
		displayMsg: '{عرض {0} - {1} من {2',
		emptyMsg: 'ليس هناك تاريخ لعرضه'
	});

	Ext.define('Ext.locale.ar.form.Basic', {
		override: 'Ext.form.Basic',

		waitTitle: 'الرجاء الإنتظار...'
	});



	Ext.define('Ext.locale.ar.form.field.Base', {
		override: 'Ext.form.field.Base',

		invalidText: 'قيمة هذا الحقل غير صالحة'
	});



	Ext.define('Ext.locale.ar.form.field.Text', {
		override: 'Ext.form.field.Text',

		minLengthText: 'أدنى طول لهذا الحقل {0} حرف',
		maxLengthText: 'أقصى طول لهذا الحقل {0} حرف',
		blankText: 'هذا الحقل مطلوب',
		regexText: '',
		emptyText: null
	});

	Ext.define('Ext.locale.ar.form.field.Number', {
		override: 'Ext.form.field.Number',

		decimalSeparator: '.',
		decimalPrecision: 2,
		minText: '{أدنى قيمة لهذا الحقل {0',
		maxText: '{أقصى قيمة لهذا الحقل {0',
		nanText: '{0} ليس برقم'
	});

	Ext.define('Ext.locale.ar.form.field.Date', {
		override: 'Ext.form.field.Date',

		disabledDaysText: 'معطل',
		disabledDatesText: 'معطل',
		minText: '{تاريخ هذا الحقل يجب أن يكون بعد {0',
		maxText: '{تاريخ هذا الحقل يجب أن يكون قبل {0',
		invalidText: '{0} {1} ليس بتاريخ - يجب أن يكون كـ',
		format: 'm/d/y',
		altFormats: 'm/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d'
	});

	Ext.define('Ext.locale.ar.form.field.ComboBox', {
		override: 'Ext.form.field.ComboBox',

		valueNotFoundText: undefined
	}, function() {
		Ext.apply(Ext.form.field.ComboBox.prototype.defaultListConfig, {
			loadingText: 'جاري التحميل...'
		});
	});

	Ext.define('Ext.locale.ar.form.field.HtmlEditor', {
		override: 'Ext.form.field.HtmlEditor',

		createLinkText: 'رجاءً أدخل الرابط:'
	}, function() {
		Ext.apply(Ext.form.field.HtmlEditor.prototype, {
			buttonTips: {
				bold: {
					title: 'ثخين (Ctrl+B)',
					text: 'يجعل النص المختار ثخينًا.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				italic: {
					title: 'مائل (Ctrl+I)',
					text: 'يجعل النص المختار مائلًا.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				underline: {
					title: 'تحته خط (Ctrl+U)',
					text: 'يضع خط تحت النص المختار.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				increasefontsize: {
					title: 'تضخيم النص',
					text: 'يزيد من حجم الخط.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				decreasefontsize: {
					title: 'إكماش النص',
					text: 'يقلل من حجم الخط.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				backcolor: {
					title: 'لون التسليط',
					text: 'يغيير لون خلفية النص المختار.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				forecolor: {
					title: 'لون الخط',
					text: 'يغيير من لون النص المختار.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifyleft: {
					title: 'محاذاة لليسار',
					text: 'محاذاة النص لليسار.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifycenter: {
					title: 'توسيط',
					text: 'توسيط النص في المحرر.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifyright: {
					title: 'محاذاة لليمين',
					text: 'محاذاة النص لليمين.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				insertunorderedlist: {
					title: 'قائمة',
					text: 'إنشاء قائمة.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				insertorderedlist: {
					title: 'قائمة مرقمة',
					text: 'إنشاء قائمة مرقمة.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				createlink: {
					title: 'رابط',
					text: 'جعل النص المختار رابط.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				sourceedit: {
					title: 'تحرير المصدر',
					text: 'التبديل إلى وضع تحرير المصدر',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				}
			}
		});
	});

	Ext.define('Ext.locale.ar.grid.header.Container', {
		override: 'Ext.grid.header.Container',

		sortAscText: 'رتب تصاعديًا',
		sortDescText: 'رتب تنازليًا',
		columnsText: 'الأعمدة'
	});

	Ext.define('Ext.locale.ar.grid.GroupingFeature', {
		override: 'Ext.grid.GroupingFeature',

		emptyGroupText: '(بدون)',
		groupByText: 'جمع حسب هذا الحقل',
		showGroupsText: 'أظهر في مجموعات'
	});

	Ext.define('Ext.locale.ar.grid.PropertyColumnModel', {
		override: 'Ext.grid.PropertyColumnModel',

		nameText: 'الاسم',
		valueText: 'القيمة',
		dateFormat: 'm/j/Y',
		trueText: 'صواب',
		falseText: 'خطأ'
	});

	Ext.define('Ext.locale.ar.grid.BooleanColumn', {
		override: 'Ext.grid.BooleanColumn',

		trueText: 'صواب',
		falseText: 'خطأ',
		undefinedText: '&#160;'
	});

	Ext.define('Ext.locale.ar.grid.NumberColumn', {
		override: 'Ext.grid.NumberColumn',

		format: '0,000.00'
	});

	Ext.define('Ext.locale.ar.grid.DateColumn', {
		override: 'Ext.grid.DateColumn',

		format: 'm/d/Y'
	});

	Ext.define('Ext.locale.ar.form.field.Time', {
		override: 'Ext.form.field.Time',

		minText: '{الوقت في هذا الحقل يجب أن يكون بعد أو مساوٍ لـ {0',
		maxText: '{الوقت في هذا الحقل يجب أن يكون قبل أو مساوٍ لـ {0',
		invalidText: '{0} ليس بوقت',
		format: 'g:i A',
		altFormats: 'g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H'
	});

	Ext.define('Ext.locale.ar.form.CheckboxGroup', {
		override: 'Ext.form.CheckboxGroup',

		blankText: 'يجب أن تختار عنصرًا واحدًا على الأقل من هذه المجموعة'
	});

	Ext.define('Ext.locale.ar.form.RadioGroup', {
		override: 'Ext.form.RadioGroup',

		blankText: 'يجب أن تختار عنصرًا واحدًا فقط من هذه المجموعة'
	});

	// This is needed until we can refactor all of the locales into individual files
	Ext.define('Ext.locale.ar.Component', {
		override: 'Ext.Component'
	});

})();