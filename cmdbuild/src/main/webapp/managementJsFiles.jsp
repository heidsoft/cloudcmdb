<!-- SELECTIONS -->
<script type="text/javascript" src="javascripts/cmdbuild/selection/CMMultiPageSelectionModel.js"></script>

<!-- STATES -->
<script type="text/javascript" src="javascripts/cmdbuild/state/CMWorkflowState.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/state/CMUIState.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/state/CMCardModuleState.js"></script>

<!-- THE OTHERS -->
<script type="text/javascript" src="javascripts/cmdbuild/view/administration/common/CMDomainGrid.js"></script> <!-- TODO move to common files -->

<!-- MODELS -->
<script type="text/javascript" src="javascripts/cmdbuild/model/widget/CMWidgetReaders.js"></script>

<!-- DATASOURCES -->
<script type="text/javascript" src="javascripts/cmdbuild/data/CMMiniCardGridBaseDataSource.js"></script>
<script type="text/javascript" src="javascripts/cmdbuild/data/CMDetailedCardDataSource.js"></script>

<!-- VIEWS -->
	<!-- COMMON -->
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMEditablePanel.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMSideTabPanel.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMTabPanel.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMMiniCardGrid.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMMiniCardGridWindow.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMCardWindow.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMCardListWindow.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMReferenceSearchWindow.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMNoteWindow.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMAttachmentsWindow.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMCardBrowserTree.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMWidgetManager.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/CMCardForm.js"></script>

		<!-- NON COMMON REQUIRED BY THE WIDGETS -->
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/CMCardNotesPanel.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/relations/CMCardRelationsPanel.js"> </script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/attachments/CMCardAttachmentsTab.js"></script>

		<!-- WIDGETS -->
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/linkCards/LinkCards.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMNavigationTree.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMWidgetsWindow.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMWidgetButtonsPanel.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMFormWithWidgetButtons.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMCalendar.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMCreateModifyCard.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMOpenAttachment.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMOpenNote.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMWebService.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMPresetFromCard.js"></script>

	<!-- CLASSES -->
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/CMModCard.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/CMCardPanel.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/CMCardTabPanel.js"></script>

		<!-- RELATIONS -->
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/relations/CMEditRelationWindow.js"> </script>

		<!-- MD -->
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/masterDetails/CMMasterDetailGrid.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/masterDetails/CMCardMasterDetail.js"></script>

		<!-- ATTACHMENTS -->
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/attachments/CMEditAttachmentWindow.js"></script>

	<!-- DASHBOARD -->
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/dashboard/CMModDashbaord.js"></script>

	<!-- WORKFLOW -->
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/workflow/CMModWorkflow.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/workflow/CMActivityPanel.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/workflow/CMActivityTabPanel.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/workflow/CMMultipleActivityRowExpander.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/workflow/CMActivityGrid.js"></script>

	<!-- WIDGET DERIVED -->
	<script type="text/javascript" src="javascripts/cmdbuild/view/management/common/widgets/CMWorkflow.js"></script>

<!-- CONTROLLER -->
	<!-- COMMON -->
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMModCardSubController.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/CMModClassAndWFCommons.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/CMAttachmentsWindowController.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/CMCardBrowserTreeDataSource.js"></script>

		<!-- they are not common but the widget need them -->
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMBaseCardPanelController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMAttachmentController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMRelationsController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMMasterDetailsController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMNoteController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/CMMapPanel.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/CMMapLayerSwitcher.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/view/management/classes/map/MapEditingWindow.js"></script>

		<!-- WIDGETS -->
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/linkCards/LinkCardsController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMNavigationTreeController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMWidgetController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMOpenNoteController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMOpenAttachmentController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMCalendarController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMWebServiceController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMCreateModifyCardController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMPresetFromCardController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMWidgetManagerController.js"></script>

	<!-- DASHBOARD -->
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/dashboard/CMModDashboardController.js"></script>

	<!-- CARD -->
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMModCardController.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/classes/CMCardPanelController.js"></script>

		<!-- here for dependency problems -->
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/CMCardWindowController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/CMDetailWindowController.js"></script>
		<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/CMAddDetailWindowController.js"></script>

	<!-- WORKFLOW -->
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/workflow/CMModWorkflowController.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/workflow/CMActivityPanelController.js"></script>
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/workflow/CMActivityGridController.js"></script>

	<!-- WIDGET DERIVED -->
	<script type="text/javascript" src="javascripts/cmdbuild/controller/management/common/widgets/CMWorkflowController.js"></script>