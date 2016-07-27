(function($) {
	var OPTIONS_LABEL_ON_SELECTED = $.Cmdbuild.g3d.constants.LABELS_ON_SELECTED;
	var OPTIONS_NO_LABELS = $.Cmdbuild.g3d.constants.NO_LABELS;
	if (!$.Cmdbuild.g3d) {
		$.Cmdbuild.g3d = {};
	}
	var Viewer = function(idCanvas) {
		var canvasDiv;
		var camera, controls, scene, renderer;
		var plane;
		var thisViewer = undefined;
		var labelsInterval = undefined;
		var labels = [];
		var raycaster = new THREE.Raycaster();
		raycaster.linePrecision = 3;
		var mouse = new THREE.Vector2(), offset = new THREE.Vector3();
		var INTERSECTED, SELECTED, LASTSELECTED;
		var realMouse = new THREE.Vector2();
		this.model = undefined;
		var objects = [];
		var edges = [];
		this.init = function() {
			this.model = $.Cmdbuild.customvariables.model;
			this.selected = $.Cmdbuild.customvariables.selected;
			$.Cmdbuild.g3d.Options.loadConfiguration(function(response) {
				$.Cmdbuild.custom.configuration = response;
				$.Cmdbuild.custom.configuration.camera = {
					position : {
						x : 0,
						y : 2500,
						z : 2500
					}
				};
				this.initCB();
				animate();
			}, this);
		};
		this.initCB = function() {
			THREE.ImageUtils.crossOrigin = '';
			thisViewer = this;
			this.model.observe(this);
			$.Cmdbuild.customvariables.options = new $.Cmdbuild.g3d.Options();
			$.Cmdbuild.customvariables.options.observe(this);
			$.Cmdbuild.g3d.Options.initVariables();
			$.Cmdbuild.g3d.Options.initFields();
			$.Cmdbuild.customvariables.navigationTreesBtnMenu = new $.Cmdbuild.g3d.navigationTreesBtnMenu();
			$.Cmdbuild.customvariables.viewer = this;
			this.camera = new $.Cmdbuild.g3d.Camera(this.model);
			this.camera.observe(this);
			this.selected.observe(this);
			$.Cmdbuild.customvariables.camera = this.camera;
			this.commandsManager = new $.Cmdbuild.g3d.CommandsManager(
					this.model);
			canvasDiv = $("#" + idCanvas)[0];
			$.Cmdbuild.customvariables.commandsManager = this.commandsManager;

			renderer = $.Cmdbuild.g3d.ViewerUtilities.webGlRender(canvasDiv);
			camera = $.Cmdbuild.g3d.ViewerUtilities.camera({
				width : renderer.domElement.width,
				height : renderer.domElement.height
			});
			var cameraHelper = new THREE.CameraHelper(camera);
			scene = new THREE.Scene();

			scene.add(new THREE.AmbientLight(0x909090));
			var light = $.Cmdbuild.g3d.ViewerUtilities.spotLight(camera, 2000);
			scene.add(light);
			var light = $.Cmdbuild.g3d.ViewerUtilities.spotLight(camera, -2000);
			scene.add(light);
			plane = $.Cmdbuild.g3d.ViewerUtilities.spacePlane();
			scene.add(plane);
			canvasDiv.appendChild(renderer.domElement);
			controls = $.Cmdbuild.g3d.ViewerUtilities.trackballControls(camera,
					renderer.domElement);
			controls
					.approxOnY($.Cmdbuild.custom.configuration.viewPointDistance);
			$.Cmdbuild.g3d.ViewerUtilities.declareEvents(this,
					renderer.domElement);
			render();
			var init = new $.Cmdbuild.g3d.commands.init_explode(
					thisViewer.model, $.Cmdbuild.start.httpCallParameters);
			this.commandsManager.execute(init, {}, function(response) {
				this.centerAndSelect();
			}, this);
		};
		this.centerAndSelect = function() {
			var me = this;
			setTimeout(function() {
				var box = me.boundingBox();
				me.zoomAll(box);
				$.Cmdbuild.customvariables.selected.erase();
				$.Cmdbuild.customvariables.selected
						.select($.Cmdbuild.start.httpCallParameters.cardId);
			}, 500);

		};
		this.onWindowResize = function() {
			var canvas = $("#" + idCanvas);
			camera.aspect = canvas.innerWidth() / canvas.innerHeight();
			camera.updateProjectionMatrix();
			renderer.setSize(canvas.innerWidth(), canvas.innerHeight());
		};
		this.refreshCamera = function() {
			var position = this.camera.getData();
			camera.lookAt(position);
			controls.target.set(position.x, position.y, position.z);
		};
		this.getRenderer = function() {
			return renderer;
		};
		this.getCamera = function() {
			return camera;
		};
		this.getScene = function() {
			return scene;
		};
		this.getOpenCompoundCommands = function(node, callback, callbackScope) {
			var compoundData = $.Cmdbuild.g3d.Model.getGraphData(node,
					"compoundData");
			var param = {
				filter : compoundData.filter
			};
			$.Cmdbuild.utilities.proxy
					.getRelations(
							compoundData.domainId,
							param,
							function(elements) {
								var arCommands = [];
								var expandingThreshold = $.Cmdbuild.g3d.constants.EXPANDING_THRESHOLD;
								for (var i = 0; i < elements.length; i += expandingThreshold) {
									arCommands.push({
										command : "openChildren",
										id : node.id(),
										elements : elements.slice(i, i
												+ expandingThreshold)
									});
								}
								callback.apply(callbackScope, [ arCommands ]);
							}, this);
		};
		this.openCompoundNode = function(node, callback, callbackScope) {
			this.getOpenCompoundCommands(node, function(arCommands) {
				var macroCommand = new $.Cmdbuild.g3d.commands.macroCommand(
						thisViewer.model, arCommands);
				$.Cmdbuild.customvariables.commandsManager.execute(
						macroCommand, {}, function() {
							callback.apply(callbackScope, []);
						}, this);
				this.clearSelection();
			}, this);
		};
		this.onDocumentMouseDblClick = function(event) {
			if (!LASTSELECTED) {
				return;
			}
			var node = thisViewer.model.getNode(LASTSELECTED.elementId);
			var classId = $.Cmdbuild.g3d.Model.getGraphData(node, "classId");
			if (classId == $.Cmdbuild.g3d.constants.GUICOMPOUNDNODE) {
				thisViewer.openCompoundNode(node, function() {
					thisViewer.clearSelection();
					thisViewer.model.remove(node.id());
					thisViewer.model.changed(true);
				}, this);
			} else {
				thisViewer.explodeNode({
					id : LASTSELECTED.elementId,
					levels : $.Cmdbuild.customvariables.options.baseLevel
				});
			}
		};
		this.explodeNode = function(params) {
			var explode = new $.Cmdbuild.g3d.commands.explode_levels(
					thisViewer.model, {
						id : params.id,
						levels : params.levels
					});
			thisViewer.commandsManager.execute(explode, {}, function() {
				var nodes = $.Cmdbuild.customvariables.model.getNodes();
				$.Cmdbuild.g3d.Model
						.removeGraphData(nodes, "exploded_children");
				$.Cmdbuild.custom.commands.centerOnViewer();
			}, this);
		};
		this.onDocumentMouseMove = function(event) {
			event.preventDefault();
			// event.stopPropagation();
			realMouse.x = event.clientX;
			realMouse.y = event.clientY;
			mouse.x = ((event.clientX - renderer.domElement.offsetLeft) / renderer.domElement.width) * 2 - 1;
			mouse.y = -((event.clientY - renderer.domElement.offsetTop) / renderer.domElement.height) * 2 + 1;
			raycaster.setFromCamera(mouse, camera);
			if (SELECTED) {
				var intersects = raycaster.intersectObject(plane, true);
				var node = thisViewer.model.getNode(SELECTED.elementId);
				if (intersects.length <= 0) {
					thisViewer.pushNewPosition(thisViewer.model,
							SELECTED.elementId, node.position(),
							SELECTED.position);
					SELECTED = LASTSELECTED = null;
					return;
				}
				var position = intersects[0].point.sub(offset);
				SELECTED.position.copy(position);
				thisViewer.refreshNodeEdges(SELECTED.elementId, position);
				if (node.selectionOnNode) {
					node.selectionOnNode.position.copy(node.glObject.position);
				}
				return;
			}
			var intersects = raycaster.intersectObjects(objects, true);
			if (intersects.length > 0) {
				if (INTERSECTED != intersects[0].object) {
					INTERSECTED = intersects[0].object;
					plane.position.copy(INTERSECTED.position);
					plane.lookAt(camera.position);
					var node = thisViewer.model.getNode(INTERSECTED.elementId);
				}
				canvasDiv.style.cursor = 'pointer';
			} else {
				INTERSECTED = null;
				canvasDiv.style.cursor = 'auto';
			}

			// tooltip && selection
			if (intersects.length > 0 && intersects[0].object.name) {
				try {
					var node = thisViewer.model.getNode(INTERSECTED.elementId);
					$.Cmdbuild.g3d.ViewerUtilities.moveNodeTooltip(
							intersects[0], node, event.clientX, event.clientY);
					if (node.selectionOnNode) {
						node.selectionOnNode.position
								.copy(node.glObject.position);
					}
				} catch (e) {
					console
							.log("Viewer: onDocumentMouseMove error during tooltip show");
				}
			} else {
				$.Cmdbuild.g3d.ViewerUtilities.closeTooltip();
				thisViewer.tooltipLine(event);
			}
		};
		this.tooltipLine = function(event) {
			var vector = new THREE.Vector3(mouse.x, mouse.y, 0.5)
					.unproject(camera);
			var raycaster = new THREE.Raycaster(camera.position, vector.sub(
					camera.position).normalize());
			raycaster.linePrecision = 30;
			var intersects = [];
			try {
				intersects = raycaster.intersectObjects(edges, false);

			} catch (e) {
				console.log(e, edges);
			}
			if (intersects.length > 0) {
				var node = thisViewer.model.getNode(intersects[0].object.id);
				$.Cmdbuild.g3d.ViewerUtilities.moveEdgeTooltip(intersects[0],
						node, event.clientX, event.clientY);
			} else {
				$.Cmdbuild.g3d.ViewerUtilities.closeTooltip();
			}
		};
		this.refreshNodeEdges = function(id, position) {
			var nodes = this.model.connectedEdges(id);
			for (var i = 0; i < nodes.length; i++) {
				var edge = nodes[i];
				var p2 = {};
				if (id == edge.source().id()) {
					p2 = $.Cmdbuild.g3d.ViewerUtilities.getCenterPosition(edge
							.target());
				} else if (id == edge.target().id()) {
					p2 = $.Cmdbuild.g3d.ViewerUtilities.getCenterPosition(edge
							.source());
				}
				$.Cmdbuild.g3d.ViewerUtilities.modifyLine(scene, edge,
						position, p2);
			}
		};
		this.onDocumentMouseDown = function(event) {
			$.Cmdbuild.g3d.ViewerUtilities.closeTooltip();
			event.preventDefault();
			var vector = new THREE.Vector3(mouse.x, mouse.y, 0.5)
					.unproject(camera);
			var raycaster = new THREE.Raycaster(camera.position, vector.sub(
					camera.position).normalize());
			raycaster.linePrecision = 3;
			var intersects = raycaster.intersectObjects(objects);
			// LASTSELECTED = null;
			if (intersects.length > 0) {
				controls.enabled = false;
				SELECTED = intersects[0].object;
				if (SELECTED == LASTSELECTED) {
					return;
				}
				LASTSELECTED = SELECTED;
				var intersects = raycaster.intersectObject(plane, true);
				if (intersects.length <= 0) {
					return;
				}
				offset.copy(intersects[0].point).sub(plane.position);
				if (!event.ctrlKey) {
					thisViewer.clearSelection();
				}
				thisViewer.setSelection(SELECTED.elementId, !event.ctrlKey);
				canvasDiv.style.cursor = 'move';
				// ---->>> controls.set( SELECTED.position);
			}
		};
		this.onDocumentMouseUp = function(event) {
			event.preventDefault();
			controls.enabled = true;
			var node = undefined;
			if (SELECTED) {
				node = thisViewer.model.getNode(SELECTED.elementId);
			}
			if (INTERSECTED && SELECTED) {
				plane.position.copy(INTERSECTED.position);
				if (!$.Cmdbuild.g3d.ViewerUtilities.equals(node.position(),
						INTERSECTED.position)) {
					thisViewer.pushNewPosition(thisViewer.model,
							SELECTED.elementId, node.position(),
							INTERSECTED.position);
				} else {
					if (node.selectionOnNode) {
						node.selectionOnNode.position
								.copy(node.glObject.position);
					}

				}
			} else if (SELECTED) {
				if (!$.Cmdbuild.g3d.ViewerUtilities.equals(node.position(),
						SELECTED.position)) {
					thisViewer.pushNewPosition(thisViewer.model,
							SELECTED.elementId, node.position(),
							SELECTED.position);
				} else {
					if (node.selectionOnNode) {
						node.selectionOnNode.position
								.copy(node.glObject.position);
					}

				}
			}
			canvasDiv.style.cursor = 'auto';
			SELECTED = null;
		};
		this.pushNewPosition = function(model, id, oldPosition, newPosition) {
			var node = thisViewer.model.getNode(id);
			if (node.selectionOnNode) {
				node.selectionOnNode.position.copy(newPosition);
			}
			if (!$.Cmdbuild.g3d.ViewerUtilities
					.equals(oldPosition, newPosition)) {
				var modifyPosition = new $.Cmdbuild.g3d.commands.modifyPosition(
						model, id, newPosition);
				thisViewer.commandsManager.execute(modifyPosition, {});
			}
		};
		this.refresh = function(rough) {
			if (this.duringRefresh) {
				return;
			}
			this.duringRefresh = true;
			this.model.changeLayout({
				name : "guisphere"// "guicircle"//"breadthfirst"//"concentric"//
			});
			if (rough) {
				$.Cmdbuild.g3d.ViewerUtilities.clearScene(scene, this.model,
						objects, edges);
			}
			objects = [];
			edges = [];
			if (rough) {
				this.clearSelection();
			}
			this.refreshNodes(scene, rough);
			this.refreshRelations(scene, rough);
			this.duringRefresh = false;
//			if (this.toRefreshAgain === true) {
//				this.toRefreshAgain = false;
//				this.refresh(false);
//			} else {
				this.refreshLabels();
//			}
		};
		this.refreshNodes = function(scene, rough) {
			var nodes = this.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				if (node.removed()) {
					continue;
				}
				var glObject = scene.getObjectById($.Cmdbuild.g3d.Model
						.getGraphData(node, "glId"));// node.glObject;//
				var me = this;
				if (glObject && !rough) {
					if (!$.Cmdbuild.g3d.ViewerUtilities.equals(
							glObject.position, node.position())) {
						new $.Cmdbuild.g3d.ViewerUtilities.moveObject(me, node);
					}
					objects.push(glObject);
				} else {
					var parentId = $.Cmdbuild.g3d.Model.getGraphData(node,
							"previousPathNode");
					var parentNode = this.model.getNode(parentId);
					var p = (parentNode && parentNode.glObject) ? parentNode.glObject.position
							: {
								x : 0,
								y : 0,
								z : 0
							};
					var object = $.Cmdbuild.g3d.ViewerUtilities.objectFromNode(
							node, p);
					node.glObject = object;
					scene.add(object);
					objects.push(object);
					new $.Cmdbuild.g3d.ViewerUtilities.moveObject(me, node);
				}
			}
		};
		this.refreshRelations = function(scene, rough) {
			var modelEdges = this.model.getEdges();
			for (var i = 0; i < modelEdges.length; i++) {
				var edge = modelEdges[i];
				var source = edge.source();
				var target = edge.target();
				var p1 = $.Cmdbuild.g3d.ViewerUtilities
						.getCenterPosition(source);
				var p2 = $.Cmdbuild.g3d.ViewerUtilities
						.getCenterPosition(target);
				if (edge.glLine && !rough) {
					var glP1 = edge.glLine.geometry.vertices[0];
					var glP2 = edge.glLine.geometry.vertices[1];
					if (!($.Cmdbuild.g3d.ViewerUtilities.equals(glP1, p1) && $.Cmdbuild.g3d.ViewerUtilities
							.equals(glP2, p2))) {
						$.Cmdbuild.g3d.ViewerUtilities.modifyLine(scene, edge,
								p1, p2);
					}
					edges.push(edge.glLine);
				} else {
					var line = $.Cmdbuild.g3d.ViewerUtilities
							.lineFromEdge(edge);
					edge.glLine = line;
					edges.push(line);
					scene.add(line);
					$.Cmdbuild.g3d.ViewerUtilities.modifyLine(scene, edge, p1,
							p2);
				}
			}
		};
		this.refreshSelected = function() {
			this.removeSelectionGlObjects();
			var selected = this.selected.getData();
			for ( var key in selected) {
				this.showSelected(key);
			}
			if ($.Cmdbuild.customvariables.options["displayLabel"] === OPTIONS_LABEL_ON_SELECTED) {
				this.refreshLabels();
			}
		};
		this.removeSelectionGlObjects = function() {
			// this is a viewer operation only for optimization issues
			var nodes = this.model.getNodes();
			for (var i = 0; i < nodes.length; i++) {
				var node = nodes[i];
				if (node && node.selectionOnNode) {
					scene.remove(node.selectionOnNode);
					node.selectionOnNode = undefined;
				}
			}
		};
		this.clearSelection = function() {
			this.removeSelectionGlObjects();
			this.selected.erase();
			this.selected.changed();
		};
		this.showSelected = function(id) {
			var node = thisViewer.model.getNode(id);
			var position = node.position();
			if (!node.selectionOnNode) {
				var object = $.Cmdbuild.g3d.ViewerUtilities
						.selectionOnNode(node);
				scene.add(object);
				node.selectionOnNode = object;
				object.position.set(position.x, position.y, position.z);
			} else {
				var position = node.glObject.position;
				node.selectionOnNode.position.set(position.x, position.y,
						position.z);

			}
		};
		this.setSelection = function(id, select) {
			if (select || !this.selected.isSelect(id)) {
				this.selected.select(id);
				this.showSelected(id);
			} else {
				this.selected.unSelect(id);
			}
		};
		// ZOOM ALL
		this.boundingBox = function() {
			var ZOOM_RANGE = $.Cmdbuild.custom.configuration.stepRadius * 3;
			var ZOOM_BORDER = 2;
			var box = $.Cmdbuild.g3d.ViewerUtilities.boundingBox(objects);
			if (box.w < ZOOM_RANGE) {
				var m = ZOOM_RANGE - box.w;
				box.x -= m / 2;
				box.w += m;
			} else {
				var m = box.w * ZOOM_BORDER / 100;
				box.x -= m;
				box.w += m * 2;

			}
			if (box.h < ZOOM_RANGE) {
				var m = ZOOM_RANGE - box.h;
				box.y -= m / 2;
				box.h += m;
			} else {
				var m = box.h * ZOOM_BORDER / 100;
				box.y -= m;
				box.h += m * 2;

			}
			box.vertices = $.Cmdbuild.g3d.ViewerUtilities
					.boundingBoxVertices(box);
			return box;
		};
		this.zoomAll = function(vertices) {
			this.scaleInView(vertices);
			camera.updateProjectionMatrix();
		};
		this.vector2ScreenPosition = function(vector, camera, widthHalf,
				heightHalf) {
			var v = new THREE.Vector3();
			v.copy(vector);
			vector.project(camera);
			var projectionMatrix = new THREE.Matrix4();
			var matrixWorld = new THREE.Matrix4();
			projectionMatrix.copy(camera.projectionMatrix);
			matrixWorld.copy(camera.matrixWorld);
			for (var i = 0; i < 10; i++) {
				var vApp = new THREE.Vector3();
				vApp.copy(v);
				matrixWorld.makeTranslation(0, 0, i * 10);
				this.projectVector(vApp, projectionMatrix, matrixWorld);
			}
			camera.matrixWorld.copy(matrixWorld);
			vector.x = (vector.x * widthHalf) + widthHalf;
			vector.y = -(vector.y * heightHalf) + heightHalf;
			vector.z = -(vector.z * heightHalf) + heightHalf;

			return new THREE.Vector3(vector.x, vector.y, vector.z);
		};
		this.refreshLabels = function() {
			if (labelsInterval) {
				clearInterval(labelsInterval);
			}
			var canvas = $("#" + idCanvas);
			var wCanvas = canvas.innerWidth();
			var hCanvas = canvas.innerHeight();
			for (var i = 0; i < labels.length; i++) {
				scene.remove(labels[i].label);
				$("#label" + labels[i].id).remove();
			}
			labels = [];
			var showLabels = $.Cmdbuild.customvariables.options.displayLabel;
			if (showLabels !== OPTIONS_NO_LABELS) {
				var nodes = this.model.getNodes();
				for (var i = 0; i < nodes.length; i++) {
					var node = nodes[i];
					var label = $.Cmdbuild.g3d.Model
							.getGraphData(node, "label");
					if (showLabels === OPTIONS_LABEL_ON_SELECTED
							&& !this.selected.isSelect(node.id())) {
						continue;
					}
					labels.push({
						object : node.glObject,
						id : node.id()
					});
					var strEvents = " onmousemove='$.Cmdbuild.customvariables.viewer.onDocumentMouseMove(event)' ";
					strEvents += " onmousedown='$.Cmdbuild.customvariables.viewer.onDocumentMouseDown(event)' ";
					strEvents += " onmouseup='$.Cmdbuild.customvariables.viewer.onDocumentMouseUp(event)' ";
					strEvents += " ondblclick='$.Cmdbuild.customvariables.viewer.onDocumentDblClick(event)' ";
					var strHtml = "<div id='label" + node.id()
							+ "' class='labelText'><span " + strEvents + ">"
							+ label + "</span></div>";
					$("#" + idCanvas).after(strHtml);
				}
			}
			labelsInterval = setInterval(
					function() {
						var showLabels = $.Cmdbuild.customvariables.options["displayLabel"];
						if (showLabels === $.Cmdbuild.g3d.constants.NO_LABELS) {
							clearInterval(labelsInterval);
							return;
						}
						for (var i = 0; i < labels.length; i++) {
							var p = labels[i].object.position.clone();
							p.project(camera);
							var y = parseInt(hCanvas / 2 - p.y * hCanvas / 2);
							var x = parseInt(wCanvas / 2 + p.x * wCanvas / 2);
							if (Math.abs(realMouse.y - y) < 40
									&& (realMouse.x >= x - 40 && realMouse.x < x + 250)) {
								y = realMouse.y - 60;
							}
							if (y < 0 || x < 0 || x > wCanvas
									|| y > hCanvas - 40) {
								$("#label" + labels[i].id).css({
									display : "none"
								});

							} else if (!labels[i].x || !labels[i].y
									|| Math.abs(labels[i].y - y) > 4
									|| Math.abs(labels[i].x - x) > 4) {
								$("#label" + labels[i].id).css({
									top : y,
									left : x,
									display : "block"
								});
							}
							labels[i].x = x;
							labels[i].y = y;
						}
					}, 500);
		};
		this.stepZoom = function(box, w, h) {
			var NORECURSE = 100;
			var me = this;
			function stepIn() {
				setTimeout(function() {
					if ($.Cmdbuild.g3d.ViewerUtilities.onVideo(box, w, h,
							camera.projectionMatrix, camera.matrixWorld)
							&& NORECURSE-- > 0) {
						controls.setY(+1);
						stepIn();
					}
				}, 50);
			}
			function stepOut() {
				setTimeout(function() {
					if (!$.Cmdbuild.g3d.ViewerUtilities.onVideo(box, w, h,
							camera.projectionMatrix, camera.matrixWorld)
							&& NORECURSE-- > 0) {
						controls.setY(-1);
						stepOut();
					}
				}, 50);
			}
			stepIn();
			stepOut();
		};
		this.scaleInView = function(box) {
			NORECURSE = 100;
			var x = box.x + box.w / 2;
			var y = box.y + box.h / 2;
			var z = box.z + box.d / 2;
			var canvas = $("#" + idCanvas);
			var w = canvas.innerWidth();
			var h = canvas.innerHeight();
			var position = {
				x : x,
				y : y,
				z : z
			};
			controls.enabled = true;
			$.Cmdbuild.customvariables.camera.zoomOnPosition(position,
					function() {
						if (box.w > 0 || box.h > 0) {
							this.stepZoom(box, w, h);
						}
					}, this);
		};
		this.refreshOptions = function() {
			this.refresh();
		};
		// initialization
		var animate = function() {
			setTimeout(function() {
				requestAnimationFrame(animate);
				render();
			}, 1000 / 20);
		};

		var render = function() {
			controls.update();
			renderer.render(scene, camera);
		};
		this.init();
	};
	$.Cmdbuild.g3d.Viewer = Viewer;
})(jQuery);
