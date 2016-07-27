package unit.cxf;

import static java.util.Collections.emptyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfAttachmentsConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CxfAttachmentsConfigurationTest {

	private DmsLogic dmsLogic;

	private CxfAttachmentsConfiguration cxfAttachmentsConfiguration;

	@Before
	public void setUp() throws Exception {
		dmsLogic = mock(DmsLogic.class);
		cxfAttachmentsConfiguration = new CxfAttachmentsConfiguration(dmsLogic);
	}

	@Test
	public void logicCalledWhenCategoriesAreRead() throws Exception {
		// given
		doReturn(emptyList()) //
				.when(dmsLogic).getConfiguredCategoryDefinitions();

		// when
		cxfAttachmentsConfiguration.readCategories();

		// then
		final InOrder inOrder = inOrder(dmsLogic);
		inOrder.verify(dmsLogic).getConfiguredCategoryDefinitions();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledWhenCategoryAttributesAreRead() throws Exception {
		// given
		final Iterable<MetadataGroupDefinition> groupDefinition = emptyList();
		final DocumentTypeDefinition definition = mock(DocumentTypeDefinition.class);
		doReturn(groupDefinition) //
				.when(definition).getMetadataGroupDefinitions();
		doReturn(definition) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		cxfAttachmentsConfiguration.readCategoryAttributes("foo");

		// then
		final InOrder inOrder = inOrder(dmsLogic);
		inOrder.verify(dmsLogic).getCategoryDefinition(eq("foo"));
		inOrder.verifyNoMoreInteractions();
	}

}
