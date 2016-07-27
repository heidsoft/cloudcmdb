package unit.cxf;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessStatus;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.cmdbuild.service.rest.v1.cxf.CxfProcessesConfiguration;
import org.cmdbuild.service.rest.v1.cxf.ProcessStatusHelper;
import org.cmdbuild.service.rest.v1.model.ProcessStatus;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfProcessesConfigurationTest {

	private ProcessStatusHelper processStatusHelper;
	private CxfProcessesConfiguration cxfAttachmentsConfiguration;

	@Before
	public void setUp() throws Exception {
		processStatusHelper = mock(ProcessStatusHelper.class);
		cxfAttachmentsConfiguration = new CxfProcessesConfiguration(processStatusHelper);
	}

	@Test
	public void logicCalledWhenCategoriesAreRead() throws Exception {
		// given
		final ProcessStatus foo = newProcessStatus() //
				.withId(1L) //
				.withValue("foo") //
				.build();
		final ProcessStatus bar = newProcessStatus() //
				.withId(2L) //
				.withValue("bar") //
				.build();
		final ProcessStatus baz = newProcessStatus() //
				.withId(2L) //
				.withValue("baz") //
				.build();
		final List<ProcessStatus> asList = asList(foo, bar, baz);
		doReturn(asList) //
				.when(processStatusHelper).allValues();

		// when
		final ResponseMultiple<ProcessStatus> response = cxfAttachmentsConfiguration.readStatuses();

		// then
		assertThat(response.getMetadata().getTotal(), equalTo(3L));
		assertThat(newArrayList(response.getElements()), equalTo(asList));
		final InOrder inOrder = inOrder(processStatusHelper);
		inOrder.verify(processStatusHelper).allValues();
		inOrder.verifyNoMoreInteractions();
	}

}
