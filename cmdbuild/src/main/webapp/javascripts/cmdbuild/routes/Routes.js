(function() {

	Ext.define('CMDBuild.routes.Routes', {

		singleton: true,

		/**
		 * @cfg {String}
		 */
		route: undefined,

		exec: function() {
			if(!Ext.isEmpty(this.route)) {
				var route = this.route;
				delete this.route;

				Ext.Router.parse('exec/' + route);
			}
		},

		/**
		 * @return {Mixed} route or null
		 */
		getRoutePath: function() {
			if(!Ext.isEmpty(this.route)) {
				var route = this.route;
				delete this.route;

				return route;
			}

			return null;
		},

		/**
		 * @param {String} path
		 */
		setRoutePath: function(path) {
			if(!Ext.isEmpty(path))
				this.route = path;
		}
	});

})();