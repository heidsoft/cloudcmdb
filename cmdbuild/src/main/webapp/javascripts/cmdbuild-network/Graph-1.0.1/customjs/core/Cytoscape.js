(function($) {
	$('#cy').cytoscape({
		container: undefined,
		zoomingEnabled: false,
		userZoomingEnabled: false,
		panningEnabled: false,
		userPanningEnabled: false,
		boxSelectionEnabled: false,
		selectionType: 'single',
		touchTapThreshold: 8,
		desktopTapThreshold: 4,
		autolock: false,
		autoungrabify: false,
		autounselectify: false,

		// rendering options:
		headless: true,
		styleEnabled: false,
		hideEdgesOnViewport: true,
		hideLabelsOnViewport: true,
		textureOnViewport: true,
		motionBlur: false,
		motionBlurOpacity: 0.2,
		wheelSensitivity: 1,
		pixelRatio: 'auto',

		elements: {
			nodes: [],
			edges: [],
		},

		ready: function() {
			window.cy = this;
		}
	});
})(jQuery);
