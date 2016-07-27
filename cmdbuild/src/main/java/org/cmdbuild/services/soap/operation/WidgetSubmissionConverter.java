package org.cmdbuild.services.soap.operation;

import static java.util.Arrays.asList;

import java.util.List;

import org.cmdbuild.model.widget.CreateModifyCard;
import org.cmdbuild.model.widget.ForwardingWidgetVisitor;
import org.cmdbuild.model.widget.LinkCards;
import org.cmdbuild.model.widget.NullWidgetVisitor;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.WidgetVisitor;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmission;
import org.cmdbuild.services.soap.structure.WorkflowWidgetSubmissionParameter;

class WidgetSubmissionConverter extends ForwardingWidgetVisitor {

	private static final int ONE_PARAMETER_ONLY_EXPECTED = 0;
	private static final int SINGLE_VALUE_EXPECTED = 0;

	private static final WidgetVisitor NOTHING_TO_DO = NullWidgetVisitor.getInstance();

	private final Widget widget;

	private List<WorkflowWidgetSubmissionParameter> parameters;
	private Object submissionOutput;

	public WidgetSubmissionConverter(final Widget widget) {
		this.widget = widget;
	}

	@Override
	protected WidgetVisitor delegate() {
		return NOTHING_TO_DO;
	}

	public Object convertFrom(final WorkflowWidgetSubmission submission) {
		parameters = asList(submission.getParameters());
		widget.accept(this);
		return submissionOutput;
	}

	@Override
	public void visit(final CreateModifyCard createModifyCard) {
		final CreateModifyCard.Submission submission = new CreateModifyCard.Submission();
		if (!parameters.isEmpty()) {
			final List<String> values = asList(parameters.get(ONE_PARAMETER_ONLY_EXPECTED).getValues());
			submission.setOutput(values.get(SINGLE_VALUE_EXPECTED));
			submissionOutput = submission;
		}
	}

	@Override
	public void visit(final LinkCards linkCards) {
		final LinkCards.Submission submission = new LinkCards.Submission();
		if (!parameters.isEmpty()) {
			parameters.get(ONE_PARAMETER_ONLY_EXPECTED).getValues();
			final List<Object> values = asList(toObject(parameters.get(ONE_PARAMETER_ONLY_EXPECTED).getValues()));
			submission.setOutput(values);
			submissionOutput = submission;
		}
	}

	private Object toObject(final String[] values) {
		return asList(values).toArray();
	}

}
