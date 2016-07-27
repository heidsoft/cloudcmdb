(function () {

	Ext.define('CMDBuild.core.window.Credits', {
		extend: 'Ext.window.Window',

		modal: true,
		resizable: false,

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		initComponent: function () {
			Ext.apply(this, {
				contentEl: Ext.create('Ext.Element', {
					html: '<div id="cm-credits-content" class="cm-credits-container">'
							+ '<div class="cm-credits-logo-container">'
								+ '<img src="images/logo.jpg">'
								+ '<p class="cm-credits-release-version">' + CMDBuild.locale.version + ' ' + CMDBuild.Translation.release + '</p>'
							+ '</div>'
							+ '<div class="cm-credits-links-container">'
								+ '<div class="cm-credits-links-left">'
									+ '<h1>' + CMDBuild.locale.needYouHelp + '</h1>'
									+ '<ul>'
										+ '<li>' + CMDBuild.locale.lookAtTheManuals + '</li>'
										+ '<li>' + CMDBuild.locale.goToTheForum + '</li>'
										+ '<li class="cm-credit-last-link">' + CMDBuild.locale.requestTecnicalSupport + '</li>'
									+ '</ul>'
								+ '</div>'
								+ '<div class="cm-credits-links-right">'
									+ '<h1>' + CMDBuild.locale.wouldYouFollowCMDBuild + '</h1>'
									+ '<ul>'
										+ '<li>' + CMDBuild.locale.subscribeToNewsLetter + '</li>'
										+ '<li>' + CMDBuild.locale.folowUsOnTweeter + '</li>'
										+ '<li class="cm-credit-last-link">' + CMDBuild.locale.participatesInTheLinkedInGroup + '</li>'
									+ '</ul>'
								+ '</div>'
								+ '<div class="cm-credits-producer">'
									+ '<h1>Credits</h1>'
									+ '<p>' + CMDBuild.locale.cmdbuildIsASofwareDevelopedByTecnoteca + '</p>'
									+ '<p>' + CMDBuild.locale.cmdbuildIsAtradeMarkRegisterd + '</p>'
								+ '</div>'
							+ '</div>'
						+ '</div>'
				})
			});

			this.callParent(arguments);
		}
	});

})();
