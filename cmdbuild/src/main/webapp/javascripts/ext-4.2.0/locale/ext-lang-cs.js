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
/**
 * Czech Translations
 * Translated by Tomáš Korčák (72)
 * 2008/02/08 18:02, Ext-2.0.1
 */
(function () {

	Ext.onReady(function () {
		var cm = Ext.ClassManager;
		var exists = Ext.Function.bind(cm.get, cm);

		if (Ext.Updater)
			Ext.Updater.defaults.indicatorText = '<div class="loading-indicator">Prosím čekejte...</div>';

		if (exists('Ext.data.Types'))
			Ext.data.Types.stripRe = /[\$,%]/g;

		if (Ext.Date) {
			Ext.Date.monthNames = ['Leden', 'Únor', 'Březen', 'Duben', 'Květen', 'Červen', 'Červenec', 'Srpen', 'Září', 'Říjen', 'Listopad', 'Prosinec'];

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

			Ext.Date.dayNames = ['Neděle', 'Pondělí', 'Úterý', 'Středa', 'Čtvrtek', 'Pátek', 'Sobota'];

			Ext.Date.getShortDayName = function (day) {
				return Ext.Date.dayNames[day].substring(0, 3);
			};

			Ext.Date.parseCodes.S.s = "(?:st|nd|rd|th)";
		}

		if (Ext.MessageBox) {
			Ext.MessageBox.buttonText = {
				ok: 'OK',
				cancel: 'Storno',
				yes: 'Ano',
				no: 'Ne'
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
				emailText: 'V tomto poli může být vyplněna pouze emailová adresa ve formátu "uživatel@doména.cz"',
				urlText: 'V tomto poli může být vyplněna pouze URL (adresa internetové stránky) ve formátu "http:/' + '/www.doména.cz"',
				alphaText: 'Toto pole může obsahovat pouze písmena abecedy a znak _',
				alphanumText: 'Toto pole může obsahovat pouze písmena abecedy, čísla a znak _'
			});
	});

	Ext.define('Ext.locale.en.view.View', {
		override: 'Ext.view.View',

		emptyText: ''
	});

	Ext.define('Ext.locale.en.grid.plugin.DragDrop', {
		override: 'Ext.grid.plugin.DragDrop',

		dragText: '{0} vybraných řádků{1}'
	});

	// changing the msg text below will affect the LoadMask
	Ext.define('Ext.locale.en.view.AbstractView', {
		override: 'Ext.view.AbstractView',

		msg: 'Prosím čekejte...'
	});

	Ext.define('Ext.locale.en.picker.Date', {
		override: 'Ext.picker.Date',

		todayText: 'Dnes',
		minText: 'Datum nesmí být starší než je minimální',
		maxText: 'Datum nesmí být dřívější než je maximální',
		disabledDaysText: '',
		disabledDatesText: '',
		monthNames: Ext.Date.monthNames,
		dayNames: Ext.Date.dayNames,
		nextText: 'Následující měsíc (Control+Right)',
		prevText: 'Předcházející měsíc (Control+Left)',
		monthYearText: 'Zvolte měsíc (ke změně let použijte Control+Up/Down)',
		todayTip: '{0} (Spacebar)',
		format: 'm/d/y',
		startDay: 0
	});

	Ext.define('Ext.locale.en.picker.Month', {
		override: 'Ext.picker.Month',

			okText: '&#160;OK&#160;',
			cancelText: 'Storno'
	});

	Ext.define('Ext.locale.en.toolbar.Paging', {
		override: 'Ext.PagingToolbar',

		beforePageText: 'Strana',
		afterPageText: 'z {0}',
		firstText: 'První strana',
		prevText: 'Přecházející strana',
		nextText: 'Následující strana',
		lastText: 'Poslední strana',
		refreshText: 'Aktualizovat',
		displayMsg: 'Zobrazeno {0} - {1} z celkových {2}',
		emptyMsg: 'Žádné záznamy nebyly nalezeny'
	});

	Ext.define('Ext.locale.en.form.Basic', {
		override: 'Ext.form.Basic',

		waitTitle: 'Prosím čekejte...'
	});

	Ext.define('Ext.locale.en.form.field.Base', {
		override: 'Ext.form.field.Base',

		invalidText: 'Hodnota v tomto poli je neplatná'
	});

	Ext.define('Ext.locale.en.form.field.Text', {
		override: 'Ext.form.field.Text',

		minLengthText: 'Pole nesmí mít méně {0} znaků',
		maxLengthText: 'Pole nesmí být delší než {0} znaků',
		blankText: 'Povinné pole',
		regexText: '',
		emptyText: null
	});

	Ext.define('Ext.locale.en.form.field.Number', {
		override: 'Ext.form.field.Number',

		decimalSeparator: '.',
		decimalPrecision: 2,
		minText: 'Hodnota v tomto poli nesmí být menší než {0}',
		maxText: 'Hodnota v tomto poli nesmí být větší než {0}',
		nanText: '{0} není platné číslo'
	});

	Ext.define('Ext.locale.en.form.field.Date', {
		override: 'Ext.form.field.Date',

		disabledDaysText: 'Neaktivní',
		disabledDatesText: 'Neaktivní',
		minText: 'Datum v tomto poli nesmí být starší než {0}',
		maxText: 'Datum v tomto poli nesmí být novější než {0}',
		invalidText: '{0} není platným datem - zkontrolujte zda-li je ve formátu {1}',
		format: 'd.m.Y',
		altFormats: 'd/m/Y|d-m-y|d-m-Y|d/m|d-m|dm|dmy|dmY|d|Y-m-d'
	});

	Ext.define('Ext.locale.en.form.field.ComboBox', {
		override: 'Ext.form.field.ComboBox',

		valueNotFoundText: undefined
	}, function () {
		Ext.apply(Ext.form.field.ComboBox.prototype.defaultListConfig, {
			loadingText: 'Prosím čekejte...'
		});
	});

	Ext.define('Ext.locale.en.form.field.HtmlEditor', {
		override: 'Ext.form.field.HtmlEditor',

		createLinkText: 'Zadejte URL adresu odkazu:'
	}, function () {
		Ext.apply(Ext.form.field.HtmlEditor.prototype, {
			buttonTips: {
				bold: {
					title: 'Tučné (Ctrl+B)',
					text: 'Označí vybraný text tučně.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				italic: {
					title: 'Kurzíva (Ctrl+I)',
					text: 'Označí vybraný text kurzívou.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				underline: {
					title: 'Podtržení (Ctrl+U)',
					text: 'Podtrhne vybraný text.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				increasefontsize: {
					title: 'Zvětšit písmo',
					text: 'Zvětší velikost písma.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				decreasefontsize: {
					title: 'Zúžit písmo',
					text: 'Zmenší velikost písma.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				backcolor: {
					title: 'Barva zvýraznění textu',
					text: 'Označí vybraný text tak, aby vypadal jako označený zvýrazňovačem.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				forecolor: {
					title: 'Barva písma',
					text: 'Změní barvu textu.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifyleft: {
					title: 'Zarovnat text vlevo',
					text: 'Zarovná text doleva.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifycenter: {
					title: 'Zarovnat na střed',
					text: 'Zarovná text na střed.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				justifyright: {
					title: 'Zarovnat text vpravo',
					text: 'Zarovná text doprava.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				insertunorderedlist: {
					title: 'Odrážky',
					text: 'Začne seznam s odrážkami.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				insertorderedlist: {
					title: 'Číslování',
					text: 'Začne číslovaný seznam.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				createlink: {
					title: 'Internetový odkaz',
					text: 'Z vybraného textu vytvoří internetový odkaz.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				},
				sourceedit: {
					title: 'Zdrojový kód',
					text: 'Přepne do módu úpravy zdrojového kódu.',
					cls: Ext.baseCSSPrefix + 'html-editor-tip'
				}
			}
		});
	});

	Ext.define('Ext.locale.en.grid.header.Container', {
		override: 'Ext.grid.header.Container',

		sortAscText: 'Řadit vzestupně',
		sortDescText: 'Řadit sestupně',
		columnsText: 'Sloupce'
	});

	Ext.define('Ext.locale.en.grid.GroupingFeature', {
		override: 'Ext.grid.GroupingFeature',

	    emptyGroupText: '(Žádná data)',
	    groupByText: 'Seskupit dle tohoto pole',
	    showGroupsText: 'Zobrazit ve skupině'
	});

	Ext.define('Ext.locale.en.grid.PropertyColumnModel', {
		override: 'Ext.grid.PropertyColumnModel',

		nameText: 'Název',
		valueText: 'Hodnota',
		dateFormat: 'm/j/Y',
		trueText: 'true',
		falseText: 'false'
	});

	Ext.define('Ext.locale.en.grid.BooleanColumn', {
		override: 'Ext.grid.BooleanColumn',

		trueText: 'true',
		falseText: 'false',
		undefinedText: '&#160;'
	});

	Ext.define('Ext.locale.en.grid.NumberColumn', {
		override: 'Ext.grid.NumberColumn',

		format: '0,000.00'
	});

	Ext.define('Ext.locale.en.grid.DateColumn', {
		override: 'Ext.grid.DateColumn',

		format: 'm/d/Y'
	});

	Ext.define('Ext.locale.en.form.field.Time', {
		override: 'Ext.form.field.Time',

		minText: 'The time in this field must be equal to or after {0}',
		maxText: 'The time in this field must be equal to or before {0}',
		invalidText: '{0} is not a valid time',
		format: 'g:i A',
		altFormats: 'g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H'
	});

	Ext.define('Ext.locale.en.form.CheckboxGroup', {
		override: 'Ext.form.CheckboxGroup',

		blankText: 'You must select at least one item in this group'
	});

	Ext.define('Ext.locale.en.form.RadioGroup', {
		override: 'Ext.form.RadioGroup',

		blankText: 'You must select one item in this group'
	});

	// This is needed until we can refactor all of the locales into individual files
	Ext.define('Ext.locale.en.Component', {
		override: 'Ext.Component'
	});

})();
