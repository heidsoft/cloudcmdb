(function () {

	Ext.Loader.setConfig({
		enabled: true,
		disableCaching: false,
		paths: {
			'CMDBuild': './javascripts/cmdbuild',
			'Ext.ux': './javascripts/ext-' + getExtJsVersion({ release: false }) + '-ux',
			'Logger': './javascripts/log'
		}
	});

	/**
	 * Returns string with custom formatted ExtJs version (copy of CMDBuild.core.Utils to avoid dependences problems)
	 *
	 * @param {Object} format
	 * 		Ex. {
	 * 			{String} separator,
	 * 			{Boolean} major,
	 * 			{Boolean} minor,
	 * 			{Boolean} patch,
	 * 			{Boolean} release
	 * 		}
	 *
	 * @returns {String}
	 */
	function getExtJsVersion(format) {
		format = format || {};
		format.separator = format.separator || '.';
		format.major = format.major || true;
		format.minor = format.minor || true;
		format.patch = format.patch || true;
		format.release = format.release || false;

		var extjsVersion = Ext.getVersion('extjs');
		var outputArray = [];

		if (format.major)
			outputArray.push(extjsVersion.getMajor());

		if (format.minor)
			outputArray.push(extjsVersion.getMinor());

		if (format.patch)
			outputArray.push(extjsVersion.getPatch());

		if (format.release)
			outputArray.push(extjsVersion.getRelease());

		return outputArray.join(format.separator);
	};

})();
