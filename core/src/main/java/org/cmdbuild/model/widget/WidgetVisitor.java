package org.cmdbuild.model.widget;

import org.cmdbuild.model.widget.customform.CustomForm;
import org.cmdbuild.workflow.CMActivityWidget;

/**
 * This visitor should really visit {@link CMActivityWidget} class instead of
 * this. At the moment it's not possible because {@link CMActivityWidget} is
 * defined in the workflow module that doesn't know which
 * {@link CMActivityWidget} implementations have been created.
 * 
 * All in all it's not a so bad solution compared to other...
 */
public interface WidgetVisitor {

	interface WidgetVisitable {

		void accept(WidgetVisitor visitor);

	}

	void visit(Calendar widget);

	void visit(CreateModifyCard widget);

	void visit(CustomForm widget);

	void visit(Grid widget);

	void visit(LinkCards widget);

	void visit(ManageEmail widget);

	void visit(ManageRelation widget);

	void visit(NavigationTree widget);

	void visit(OpenAttachment widget);

	void visit(OpenNote widget);

	void visit(OpenReport widget);

	void visit(Ping widget);

	void visit(PresetFromCard widget);

	void visit(WebService widget);

	void visit(Workflow widget);

}
