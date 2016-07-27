(function() {

	/**
	 * Fix to strip javascript unsupported UTF characters with a single regex.
	 *
	 * @override
	 */
	Ext.decode = function(json, safe) {
		var rx_dangerous = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g;
		rx_dangerous.lastIndex = 0;

		if (rx_dangerous.test(json))
			json = json.replace(rx_dangerous, function(a) {
				return '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
			});

		return Ext.JSON.decode(json, safe);
	};

})();