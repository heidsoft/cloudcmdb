(function() {
	var view;
	var selectionModel;
	var controller;

	describe("CMDBuild.controller.accordion.CMBaseAccordionController", function() {
		beforeEach(function() {
			CMDBuild.global.controller.MainViewport = { // have to be global
				bringTofrontPanelByCmName: function() {}
			};

			selectionModel = new Ext.util.Observable();
			view = {
				updateStore: function() {},
				on: function() {},
				selectNodeById: function() {},
				getSelectionModel: function() {
					return selectionModel;
				}
			};

			controller = new CMDBuild.controller.accordion.CMBaseAccordionController(view);
		});

		afterEach(function () {
			delete CMDBuild.global.controller.MainViewport;
			delete view;
			delete selectionModel;
			delete controller;
		});

		it("Notify to the main-viewport-controller that a node was selected", function() {
			var bringTofrontPanelByCmName = spyOn(CMDBuild.global.controller.MainViewport, 'bringTofrontPanelByCmName'),
				panelName = "Purple Haze";
				s = {
					get: function() {
						return panelName;
					}
				};

			selectionModel.fireEvent("selectionchange", selectionModel, [s]);
			expect(bringTofrontPanelByCmName).toHaveBeenCalled();

			var args = bringTofrontPanelByCmName.argsForCall[0];
			expect(args[0]).toEqual(panelName);
		});

		it("is able to select a given node after update the store", function() {
			var updateStore = spyOn(view, 'updateStore'),
				selectNodeById = spyOn(view, 'selectNodeById');

			controller.updateStoreToSelectNodeWithId(1);

			expect(updateStore).toHaveBeenCalled();
			expect(selectNodeById).toHaveBeenCalledWith(1);
		})
	});
})();
