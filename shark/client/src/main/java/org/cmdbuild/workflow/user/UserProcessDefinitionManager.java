package org.cmdbuild.workflow.user;

import static com.google.common.collect.FluentIterable.from;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.ForwardingActivity;
import org.cmdbuild.workflow.ForwardingProcessDefinitionManager;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

import com.google.common.base.Function;

public class UserProcessDefinitionManager extends ForwardingProcessDefinitionManager {

	private static class ReduceWritability implements
			Function<CMActivityVariableToProcess, CMActivityVariableToProcess> {

		private final CMDataView dataView;
		private final CMEntryType type;

		public ReduceWritability(final CMDataView dataView, final CMEntryType type) {
			this.dataView = dataView;
			this.type = type;
		}

		@Override
		public CMActivityVariableToProcess apply(final CMActivityVariableToProcess input) {
			final CMActivityVariableToProcess output;
			if (input.isWritable()) {
				final CMAttribute attribute = type.getAttribute(input.getName());
				if (attribute == null) {
					output = input;
				} else {
					output = new ForwardingAttributeTypeVisitor() {

						private final CMAttributeTypeVisitor delegate = NullAttributeTypeVisitor.getInstance();

						private CMActivityVariableToProcess output;

						@Override
						protected CMAttributeTypeVisitor delegate() {
							return delegate;
						}

						public CMActivityVariableToProcess output() {
							output = input;
							attribute.getType().accept(this);
							return output;
						}

						@Override
						public void visit(final ReferenceAttributeType attributeType) {
							final String domainName = attributeType.getDomainName();
							final CMDomain domain = dataView.findDomain(domainName);
							if (domain == null) {
								output = new CMActivityVariableToProcess(output.getName(), false, output.isMandatory());
							}
						}

					}.output();
				}
			} else {
				output = input;
			}
			return output;
		}

	}

	private static class UserActivity extends ForwardingActivity {

		private final CMActivity delegate;
		private final Function<? super CMActivityVariableToProcess, CMActivityVariableToProcess> variables;

		private UserActivity(final CMActivity delegate,
				final Function<? super CMActivityVariableToProcess, CMActivityVariableToProcess> variables) {
			this.delegate = delegate;
			this.variables = variables;
		}

		@Override
		protected CMActivity delegate() {
			return delegate;
		}

		@Override
		public List<CMActivityVariableToProcess> getVariables() {
			return from(super.getVariables()) //
					.transform(variables) //
					.toList();
		}

	}

	private final ProcessDefinitionManager delegate;
	private final CMDataView dataView;

	public UserProcessDefinitionManager(final ProcessDefinitionManager delegate, final CMDataView dataView) {
		this.delegate = delegate;
		this.dataView = dataView;
	}

	@Override
	protected ProcessDefinitionManager delegate() {
		return delegate;
	}

	@Override
	public CMActivity getManualStartActivity(final CMProcessClass process, final String groupName)
			throws CMWorkflowException {
		return wrap(process, super.getManualStartActivity(process, groupName));
	}

	@Override
	public CMActivity getActivity(final CMProcessInstance processInstance, final String activityDefinitionId)
			throws CMWorkflowException {
		final CMProcessClass type = processInstance.getType();
		return wrap(type, super.getActivity(processInstance, activityDefinitionId));
	}

	private CMActivity wrap(final CMProcessClass type, final CMActivity delegate) {
		return (delegate == null) ? null : new UserActivity(delegate, new ReduceWritability(dataView, type));
	}

}
