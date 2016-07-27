package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.util.Comparator;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v2.Processes;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToFullProcessDetail;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToSimpleProcessDetail;
import org.cmdbuild.service.rest.v2.model.ProcessWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.ProcessWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.workflow.user.UserProcessClass;

import com.google.common.collect.Ordering;

public class CxfProcesses implements Processes {

	private static final ToSimpleProcessDetail TO_SIMPLE_DETAIL = ToSimpleProcessDetail.newInstance().build();

	private static final Comparator<CMClass> NAME_ASC = new Comparator<CMClass>() {

		@Override
		public int compare(final CMClass o1, final CMClass o2) {
			return o1.getName().compareTo(o2.getName());
		}

	};

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;
	private final ToFullProcessDetail toFullDetail;
	private final IdGenerator idGenerator;

	public CxfProcesses(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic,
			final ProcessStatusHelper processStatusHelper, final IdGenerator idGenerator) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
		this.toFullDetail = ToFullProcessDetail.newInstance() //
				.withLookupHelper(processStatusHelper) //
				.build();
		this.idGenerator = idGenerator;
	}

	@Override
	public ResponseMultiple<ProcessWithBasicDetails> readAll(final boolean activeOnly, final Integer limit,
			final Integer offset) {
		final Iterable<? extends UserProcessClass> all = workflowLogic.findProcessClasses(activeOnly);
		final Iterable<? extends UserProcessClass> ordered = Ordering.from(NAME_ASC) //
				.sortedCopy(all);
		final Iterable<ProcessWithBasicDetails> elements = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(TO_SIMPLE_DETAIL);
		return newResponseMultiple(ProcessWithBasicDetails.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(ordered))) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<ProcessWithFullDetails> read(final String processId) {
		final CMClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		final ProcessWithFullDetails element = toFullDetail.apply(found);
		return newResponseSingle(ProcessWithFullDetails.class) //
				.withElement(element) //
				.build();
	}

	@Override
	public ResponseSingle<Long> generateId(final String processId) {
		final CMClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		return newResponseSingle(Long.class) //
				.withElement(idGenerator.generate()) //
				.build();
	}

}
