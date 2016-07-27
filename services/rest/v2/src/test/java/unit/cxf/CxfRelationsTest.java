package unit.cxf;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v2.model.Models.newCard;
import static org.cmdbuild.service.rest.v2.model.Models.newRelation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.v2.cxf.CxfRelations;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.base.Optional;

public class CxfRelationsTest {

	private ErrorHandler errorHandler;
	private DataAccessLogic dataAccessLogic;

	private CxfRelations cxfRelations;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		cxfRelations = new CxfRelations(errorHandler, dataAccessLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void typeNotFoundOnCreate() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findDomain(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).domainNotFound(anyString());

		// when
		cxfRelations.create("some domain", newRelation() //
				.withType("should be ignored") //
				.withValue("foo", "FOO") //
				.withValue("bar", "BAR") //
				.withValue("baz", "BAZ") //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq("some domain"));
		inOrder.verify(errorHandler).domainNotFound(eq("some domain"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void logicCalledWhenCreatingRelation() throws Exception {
		// given
		doReturn(domain("found domain")) //
				.when(dataAccessLogic).findDomain(anyString());
		doReturn(asList(789L)) //
				.when(dataAccessLogic).createRelations(any(RelationDTO.class));

		// when
		final ResponseSingle<Long> response = cxfRelations.create("some domain", newRelation() //
				.withSource(newCard() //
						.withId(123L) //
						.withType("source class") //
						.build()) //
				.withDestination(newCard() //
						.withId(456L) //
						.withType("destination class") //
						.build()) //
				.withType("should be ignored") //
				.withValue("foo", "FOO") //
				.withValue("bar", "BAR") //
				.withValue("baz", "BAZ") //
				.build());

		// then
		final ArgumentCaptor<RelationDTO> relationCaptor = ArgumentCaptor.forClass(RelationDTO.class);
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq("some domain"));
		inOrder.verify(dataAccessLogic).createRelations(relationCaptor.capture());
		inOrder.verifyNoMoreInteractions();
		final RelationDTO captured = relationCaptor.getValue();
		assertThat(captured.domainName, equalTo("found domain"));
		assertThat(captured.srcCardIdToClassName, hasEntry(123L, "source class"));
		assertThat(captured.srcCardIdToClassName.size(), equalTo(1));
		assertThat(captured.dstCardIdToClassName, hasEntry(456L, "destination class"));
		assertThat(captured.dstCardIdToClassName.size(), equalTo(1));
		assertThat(captured.relationAttributeToValue, hasEntry("foo", (Object) "FOO"));
		assertThat(captured.relationAttributeToValue, hasEntry("bar", (Object) "BAR"));
		assertThat(captured.relationAttributeToValue, hasEntry("baz", (Object) "BAZ"));
		assertThat(response.getElement(), equalTo(789L));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationsButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		doThrow(WebApplicationException.class) //
				.when(errorHandler).domainNotFound(eq("dummy"));

		// when
		cxfRelations.read("dummy", null, null, null, false);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationsButBusinessLogicThrowsException() throws Exception {
		doReturn(domain("dummy")) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		doReturn(clazz("foo")) //
				.when(dataAccessLogic).findClass(anyString());
		final RuntimeException exception = new RuntimeException();
		doThrow(exception) //
				.when(dataAccessLogic).getRelationListEmptyForWrongId(any(Card.class), any(DomainWithSource.class));
		doThrow(new WebApplicationException(exception)) //
				.when(errorHandler).propagate(any(Exception.class));

		// when
		cxfRelations.read("12", null, null, null, false);
	}

	@Ignore
	@Test
	public void logicCalledWhenReadingRelations() throws Exception {
		fail("cannot mock business logic return value");
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		doThrow(WebApplicationException.class) //
				.when(errorHandler).domainNotFound(eq("dummy"));

		// when
		cxfRelations.read("dummy", 123L);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationButBusinessLogicThrowsException() throws Exception {
		// given
		final CMDomain domain = domain("dummy");
		doReturn(domain) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		final RuntimeException exception = new RuntimeException();
		doThrow(exception) //
				.when(dataAccessLogic).getRelation(eq(domain), eq(123L));
		doThrow(new WebApplicationException(exception)) //
				.when(errorHandler).propagate(any(Exception.class));

		// when
		cxfRelations.read("dummy", 123L);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingRelationButRelationNotFound() throws Exception {
		// given
		final CMDomain domain = domain("dummy");
		doReturn(domain) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		doReturn(Optional.absent()) //
				.when(dataAccessLogic).getRelation(eq(domain), eq(123L));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).relationNotFound(eq(123L));

		// when
		cxfRelations.read("dummy", 123L);
	}

	@Ignore
	@Test
	public void logicCalledWhenReadingRelation() throws Exception {
		fail("cannot mock business logic return value");
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenUpdatingRelationButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		doThrow(WebApplicationException.class) //
				.when(errorHandler).domainNotFound(eq("dummy"));

		// when
		cxfRelations.update("dummy", 123L, newRelation() //
				.withType("should be ignored") //
				.withValue("foo", "FOO") //
				.withValue("bar", "BAR") //
				.withValue("baz", "BAZ") //
				.build());
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenUpdatingRelationButBusinessLogicThrowsException() throws Exception {
		// given
		final CMDomain domain = domain("dummy");
		doReturn(domain) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		final RuntimeException exception = new RuntimeException();
		doThrow(exception) //
				.when(dataAccessLogic).updateRelation(any(RelationDTO.class));
		doThrow(new WebApplicationException(exception)) //
				.when(errorHandler).propagate(any(Exception.class));

		// when
		cxfRelations.update("dummy", 123L, newRelation() //
				.withType("should be ignored") //
				.withValue("foo", "FOO") //
				.withValue("bar", "BAR") //
				.withValue("baz", "BAZ") //
				.build());
	}

	@Test
	public void logicCalledWhenUpdatingRelation() throws Exception {
		// given
		doReturn(domain("the domain")) //
				.when(dataAccessLogic).findDomain(anyString());

		// when
		cxfRelations.update("some domain", 123L, newRelation() //
				.withSource(newCard() //
						.withId(123L) //
						.withType("source class") //
						.build()) //
				.withDestination(newCard() //
						.withId(456L) //
						.withType("destination class") //
						.build()) //
				.withType("should be ignored") //
				.withValue("foo", "FOO") //
				.withValue("bar", "BAR") //
				.withValue("baz", "BAZ") //
				.build());

		// then
		final ArgumentCaptor<RelationDTO> captor = ArgumentCaptor.forClass(RelationDTO.class);
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq("some domain"));
		inOrder.verify(dataAccessLogic).updateRelation(captor.capture());
		inOrder.verifyNoMoreInteractions();
		final RelationDTO captured = captor.getValue();
		assertThat(captured.domainName, equalTo("the domain"));
		assertThat(captured.srcCardIdToClassName, hasEntry(123L, "source class"));
		assertThat(captured.srcCardIdToClassName.size(), equalTo(1));
		assertThat(captured.dstCardIdToClassName, hasEntry(456L, "destination class"));
		assertThat(captured.dstCardIdToClassName.size(), equalTo(1));
		assertThat(captured.relationAttributeToValue, hasEntry("foo", (Object) "FOO"));
		assertThat(captured.relationAttributeToValue, hasEntry("bar", (Object) "BAR"));
		assertThat(captured.relationAttributeToValue, hasEntry("baz", (Object) "BAZ"));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenDeletingRelationButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		doThrow(WebApplicationException.class) //
				.when(errorHandler).domainNotFound(eq("dummy"));

		// when
		cxfRelations.delete("dummy", 123L);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenDeletingRelationButBusinessLogicThrowsException() throws Exception {
		// given
		final CMDomain domain = domain("dummy");
		doReturn(domain) //
				.when(dataAccessLogic).findDomain(eq("dummy"));
		final RuntimeException exception = new RuntimeException();
		doThrow(exception) //
				.when(dataAccessLogic).deleteRelation(eq("dummy"), eq(123L));
		doThrow(new WebApplicationException(exception)) //
				.when(errorHandler).propagate(any(Exception.class));

		// when
		cxfRelations.delete("dummy", 123L);
	}

	@Test
	public void logicCalledWhenDeletingRelation() throws Exception {
		// given
		doReturn(domain("dummy")) //
				.when(dataAccessLogic).findDomain(anyString());

		// when
		cxfRelations.delete("dummy", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, dataAccessLogic);
		inOrder.verify(dataAccessLogic).findDomain(eq("dummy"));
		inOrder.verify(dataAccessLogic).deleteRelation(eq("dummy"), eq(123L));
		inOrder.verifyNoMoreInteractions();
	}

	private CMDomain domain(final String name) {
		final CMDomain domain = mock(CMDomain.class);
		doReturn(name) //
				.when(domain).getName();
		return domain;
	}

	private CMClass clazz(final String name) {
		final CMClass clazz = mock(CMClass.class);
		doReturn(name) //
				.when(clazz).getName();
		return clazz;
	}

}
