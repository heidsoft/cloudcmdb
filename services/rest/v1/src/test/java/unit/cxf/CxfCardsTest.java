package unit.cxf;

import static org.cmdbuild.service.rest.v1.model.Models.newCard;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.v1.cxf.CxfCards;
import org.cmdbuild.service.rest.v1.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CxfCardsTest {

	@Captor
	private ArgumentCaptor<MultivaluedMap<String, String>> multivaluedMapCaptor;

	private ErrorHandler errorHandler;
	private DataAccessLogic userDataAccessLogic;

	private CxfCards cxfCards;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		userDataAccessLogic = mock(DataAccessLogic.class);
		cxfCards = new CxfCards(errorHandler, userDataAccessLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void createRaisesErrorWhenTypeIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(userDataAccessLogic).findClass(eq("some class"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("some class"));

		// when
		cxfCards.create("some class", newCard() //
				.withType("type ignored") //
				.build());
	}

	@Test(expected = WebApplicationException.class)
	public void createRaisesErrorWhenTypeIsProcess() throws Exception {
		// given
		final CMClass type = clazz("found class");
		doReturn(type) //
				.when(userDataAccessLogic).findClass(eq("some class"));
		doReturn(true) //
				.when(userDataAccessLogic).isProcess(type);
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFoundClassIsProcess(eq("some class"));

		// when
		cxfCards.create("some class", newCard() //
				.withType("type ignored") //
				.build());
	}

	@Test
	public void logicCalledOnCreation() throws Exception {
		// given
		final CMClass type = clazz("found class");
		doReturn(type) //
				.when(userDataAccessLogic).findClass(anyString());
		doReturn(false) //
				.when(userDataAccessLogic).isProcess(any(CMClass.class));
		doReturn(123L) //
				.when(userDataAccessLogic).createCard(any(org.cmdbuild.model.data.Card.class));

		// when
		final ResponseSingle<Long> response = cxfCards.create("some class", newCard() //
				.withType("type ignored") //
				.withValue("some name", "some value") //
				.build());

		// then
		assertThat(response.getElement(), equalTo(123L));
		final ArgumentCaptor<org.cmdbuild.model.data.Card> captor = ArgumentCaptor
				.forClass(org.cmdbuild.model.data.Card.class);
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic);
		inOrder.verify(userDataAccessLogic).findClass(eq("some class"));
		inOrder.verify(userDataAccessLogic).isProcess(eq(type));
		inOrder.verify(userDataAccessLogic).createCard(captor.capture());
		inOrder.verifyNoMoreInteractions();
		final org.cmdbuild.model.data.Card captured = captor.getValue();
		assertThat(captured.getClassName(), equalTo("found class"));
		assertThat(captured.getAttributes(), hasEntry("some name", (Object) "some value"));
	}

	@Test(expected = WebApplicationException.class)
	public void updateRaisesErrorWhenTypeIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(userDataAccessLogic).findClass(eq("some class"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("some class"));

		// when
		cxfCards.update("some class", 123L, newCard() //
				.withType("type ignored") //
				.withId(456L) //
				.build());
	}

	@Test(expected = WebApplicationException.class)
	public void updateRaisesErrorWhenTypeIsProcess() throws Exception {
		// given
		final CMClass type = clazz("found class");
		doReturn(type) //
				.when(userDataAccessLogic).findClass(eq("some class"));
		doReturn(true) //
				.when(userDataAccessLogic).isProcess(type);
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFoundClassIsProcess(eq("some class"));

		// when
		cxfCards.update("some class", 123L, newCard() //
				.withType("type ignored") //
				.withId(456L) //
				.build());
	}

	@Test
	public void logicCalledOnUpdate() throws Exception {
		final CMClass type = clazz("found class");
		doReturn(type) //
				.when(userDataAccessLogic).findClass(anyString());

		// when
		cxfCards.update("some class", 123L, newCard() //
				.withType("type ignored") //
				.withId(456L) //
				.withValue("some name", "some value") //
				.build());

		// then
		final ArgumentCaptor<org.cmdbuild.model.data.Card> cardCaptor = ArgumentCaptor
				.forClass(org.cmdbuild.model.data.Card.class);
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic);
		inOrder.verify(userDataAccessLogic).findClass(eq("some class"));
		inOrder.verify(userDataAccessLogic).isProcess(eq(type));
		inOrder.verify(userDataAccessLogic).updateCard(cardCaptor.capture());
		inOrder.verifyNoMoreInteractions();
		final org.cmdbuild.model.data.Card captured = cardCaptor.getValue();
		assertThat(captured.getClassName(), equalTo("found class"));
		assertThat(captured.getId(), equalTo(123L));
		assertThat(captured.getAttributes(), hasEntry("some name", (Object) "some value"));
	}

	@Test(expected = WebApplicationException.class)
	public void deleteRaisesErrorWhenTypeIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(userDataAccessLogic).findClass(eq("some class"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFound(eq("some class"));

		// when
		cxfCards.delete("some class", 123L);
	}

	@Test(expected = WebApplicationException.class)
	public void deleteRaisesErrorWhenTypeIsProcess() throws Exception {
		// given
		// given
		final CMClass type = clazz("found class");
		doReturn(type) //
				.when(userDataAccessLogic).findClass(eq("some class"));
		doReturn(true) //
				.when(userDataAccessLogic).isProcess(type);
		doThrow(new WebApplicationException()) //
				.when(errorHandler).classNotFoundClassIsProcess(eq("some class"));

		// when
		cxfCards.delete("some class", 123L);
	}

	@Test
	public void logicCalledOnDeletion() throws Exception {
		final CMClass type = clazz("found class");
		doReturn(type) //
				.when(userDataAccessLogic).findClass(anyString());
		doReturn(false) //
				.when(userDataAccessLogic).isProcess(any(CMClass.class));

		// when
		cxfCards.delete("some class", 123L);

		// then
		final InOrder inOrder = inOrder(errorHandler, userDataAccessLogic);
		inOrder.verify(userDataAccessLogic).findClass(eq("some class"));
		inOrder.verify(userDataAccessLogic).isProcess(eq(type));
		inOrder.verify(userDataAccessLogic).deleteCard(eq("found class"), eq(123L));
		inOrder.verifyNoMoreInteractions();
	}

	private CMClass clazz(final String name) {
		final CMClass mock = mock(CMClass.class);
		doReturn(name) //
				.when(mock).getName();
		return mock;
	}

}
