package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessStatus;
import static org.cmdbuild.service.rest.v1.model.ProcessStatus.ABORTED;
import static org.cmdbuild.service.rest.v1.model.ProcessStatus.COMPLETED;
import static org.cmdbuild.service.rest.v1.model.ProcessStatus.OPEN;
import static org.cmdbuild.service.rest.v1.model.ProcessStatus.SUSPENDED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_CLOSED_ABORTED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_CLOSED_COMPLETED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_CLOSED_TERMINATED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED;
import static org.enhydra.shark.api.common.SharkConstants.STATE_OPEN_RUNNING;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.cmdbuild.data.store.lookup.LookupImpl;
import org.cmdbuild.service.rest.v1.cxf.DefaultProcessStatusHelper;
import org.cmdbuild.service.rest.v1.model.ProcessStatus;
import org.cmdbuild.workflow.LookupHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.base.Optional;

public class DefaultProcessStatusHelperTest {

	private LookupHelper lookupHelper;
	private DefaultProcessStatusHelper defaultProcessStatusHelper;

	@Before
	public void setUp() throws Exception {
		lookupHelper = mock(LookupHelper.class);
		defaultProcessStatusHelper = new DefaultProcessStatusHelper(lookupHelper);
	}

	@Test
	public void notAllLookupAreTransformedAndReturned() throws Exception {
		// given
		long id = 0;
		final LookupImpl running = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_OPEN_RUNNING) //
				.withDescription("this is the 'open' state") //
				.build();
		final LookupImpl suspended = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_OPEN_NOT_RUNNING_SUSPENDED) //
				.withDescription("this is the 'suspended' state") //
				.build();
		final LookupImpl completed = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_COMPLETED) //
				.withDescription("this is the 'completed' state") //
				.build();
		final LookupImpl aborted = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_ABORTED) //
				.withDescription("this is the 'aborted' state") //
				.build();
		final LookupImpl terminated = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_TERMINATED) //
				.withDescription("this should not be converted") //
				.build();
		doReturn(asList(running, suspended, completed, aborted, terminated)) //
				.when(lookupHelper).allLookups();

		// when
		final Iterable<ProcessStatus> values = defaultProcessStatusHelper.allValues();

		// then
		assertThat(size(values), equalTo(4));
		assertThat(get(values, 0), equalTo(newProcessStatus() //
				.withId(running.getId()) //
				.withValue(OPEN) //
				.build()));
		assertThat(get(values, 1), equalTo(newProcessStatus() //
				.withId(suspended.getId()) //
				.withValue(SUSPENDED) //
				.build()));
		assertThat(get(values, 2), equalTo(newProcessStatus() //
				.withId(completed.getId()) //
				.withValue(COMPLETED) //
				.build()));
		assertThat(get(values, 3), equalTo(newProcessStatus() //
				.withId(aborted.getId()) //
				.withValue(ABORTED) //
				.build()));
		final InOrder inOrder = inOrder(lookupHelper);
		inOrder.verify(lookupHelper).allLookups();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void missingDefaultValue() throws Exception {
		// given
		long id = 0;
		final LookupImpl running = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_OPEN_RUNNING) //
				.withDescription("this is the 'open' state") //
				.build();
		final LookupImpl suspended = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_OPEN_NOT_RUNNING_SUSPENDED) //
				.withDescription("this is the 'suspended' state") //
				.build();
		final LookupImpl completed = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_COMPLETED) //
				.withDescription("this is the 'completed' state") //
				.build();
		final LookupImpl aborted = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_ABORTED) //
				.withDescription("this is the 'aborted' state") //
				.build();
		final LookupImpl terminated = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_TERMINATED) //
				.withDescription("this should not be converted") //
				.build();
		doReturn(asList(running, suspended, completed, aborted, terminated)) //
				.when(lookupHelper).allLookups();

		// when
		final Optional<ProcessStatus> defaultValue = defaultProcessStatusHelper.defaultValue();

		// then
		assertThat(defaultValue.isPresent(), equalTo(false));

		final InOrder inOrder = inOrder(lookupHelper);
		inOrder.verify(lookupHelper).allLookups();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void singleDefaultValue() throws Exception {
		// given
		long id = 0;
		final LookupImpl running = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_OPEN_RUNNING) //
				.withDescription("this is the 'open' state") //
				.withDefaultStatus(true) //
				.build();
		final LookupImpl suspended = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_OPEN_NOT_RUNNING_SUSPENDED) //
				.withDescription("this is the 'suspended' state") //
				.build();
		final LookupImpl completed = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_COMPLETED) //
				.withDescription("this is the 'completed' state") //
				.build();
		final LookupImpl aborted = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_ABORTED) //
				.withDescription("this is the 'aborted' state") //
				.build();
		final LookupImpl terminated = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_TERMINATED) //
				.withDescription("this should not be converted") //
				.build();
		doReturn(asList(running, suspended, completed, aborted, terminated)) //
				.when(lookupHelper).allLookups();

		// when
		final Optional<ProcessStatus> defaultValue = defaultProcessStatusHelper.defaultValue();

		// then
		assertThat(defaultValue.isPresent(), equalTo(true));
		assertThat(defaultValue.get(), equalTo(newProcessStatus() //
				.withId(running.getId()) //
				.withValue(OPEN) //
				.build()));

		final InOrder inOrder = inOrder(lookupHelper);
		inOrder.verify(lookupHelper).allLookups();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void multipleDefaultValuesFirstIsReturned() throws Exception {
		// given
		long id = 0;
		final LookupImpl running = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_OPEN_RUNNING) //
				.withDescription("this is the 'open' state") //
				.build();
		final LookupImpl suspended = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_OPEN_NOT_RUNNING_SUSPENDED) //
				.withDescription("this is the 'suspended' state") //
				.withDefaultStatus(true) //
				.build();
		final LookupImpl completed = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_COMPLETED) //
				.withDescription("this is the 'completed' state") //
				.build();
		final LookupImpl aborted = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_ABORTED) //
				.withDescription("this is the 'aborted' state") //
				.withDefaultStatus(true) //
				.build();
		final LookupImpl terminated = LookupImpl.newInstance() //
				.withId(++id) //
				.withCode(STATE_CLOSED_TERMINATED) //
				.withDescription("this should not be converted") //
				.build();
		doReturn(asList(running, suspended, completed, aborted, terminated)) //
				.when(lookupHelper).allLookups();

		// when
		final Optional<ProcessStatus> defaultValue = defaultProcessStatusHelper.defaultValue();

		// then
		assertThat(defaultValue.isPresent(), equalTo(true));
		assertThat(defaultValue.get(), equalTo(newProcessStatus() //
				.withId(suspended.getId()) //
				.withValue(SUSPENDED) //
				.build()));

		final InOrder inOrder = inOrder(lookupHelper);
		inOrder.verify(lookupHelper).allLookups();
		inOrder.verifyNoMoreInteractions();
	}

}
