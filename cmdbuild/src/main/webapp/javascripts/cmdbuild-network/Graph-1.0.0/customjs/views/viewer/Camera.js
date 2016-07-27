(function($) {
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Camera = function(model) {
		this.observers = [];
		this.model = model;
		this.cameraPosition = {x: 0, y: 0, z: 0};
		this.getData = function() {
			return this.cameraPosition;
		};
		this.observe = function(observer) {
			this.observers.push(observer);
		};
		this.changed = function(params) {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refreshCamera(params);
			}
		};
		this.animateZoomOnPosition = function(position, times, callback, callbackScope) {
			var stepX = (position.x - this.cameraPosition.x) / times;
			var stepY = (position.y - this.cameraPosition.y) / times;
			var stepZ = (position.z - this.cameraPosition.z) / times;
			var me = this;
			function singleStep(index) {
				if (index == 0) {
					callback.apply(callbackScope, []);
					return;
				}
				setTimeout(function() {
					me.cameraPosition.x += stepX;
					me.cameraPosition.y += stepY;
					me.cameraPosition.z += stepZ;
					me.changed({});
					singleStep(index - 1);
				}, 50);
			}
			singleStep(times);
		};
		this.zoomOnPosition = function(position, callback, callbackScope) {
			this.animateZoomOnPosition(position, 10, function() {
				if (callback) {
					callback.apply(callbackScope, []);
				}
			});
		};
		this.zoomOn = function(nodeId) {
			var node = this.model.getNode(nodeId);
			this.zoomOnPosition({
				x: node.glObject.position.x,
				y: node.glObject.position.y,
				z: node.glObject.position.z
			});
		};
	};
	$.Cmdbuild.g3d.Camera = Camera;

})(jQuery);