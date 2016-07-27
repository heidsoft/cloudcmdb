package integration.api.mail.javax.mail;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.common.api.mail.FetchedMail;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.api.mail.javax.mail.JavaxMailBasedMailApiFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

public class MoveMailTest extends AbstractMailTest {

	private static final String UNEXISTING = "unexisting";

	@Rule
	public GreenMailServer greenMailServer = GreenMailServer.newInstance() //
			.withConfiguration(inputServerSetup(), outputServerSetup()) //
			.withUser(FOO_AT_EXAMPLE_DOT_COM, FOO, PASSWORD) //
			.build();

	private MailApi mailApi;

	@Override
	protected ServerSetup inputServerSetup() {
		return ServerSetupTest.IMAP;
	}

	@Override
	protected ServerSetup outputServerSetup() {
		return ServerSetupTest.SMTP;
	}

	@Override
	protected GreenMail greenMail() {
		return greenMailServer.getServer();
	}

	@Before
	public void setUpMailApi() throws Exception {
		final MailApiFactory mailApiFactory = new JavaxMailBasedMailApiFactory();
		mailApi = mailApiFactory.create(configurationFrom(FOO, PASSWORD));
	}

	@Test
	public void moveMailInUnexistingFolder() throws Exception {
		// given
		newMail(FOO, PASSWORD) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.send();
		final FetchedMail fetchedMail = mailApi.selectFolder(INBOX) //
				.fetch() //
				.iterator().next();

		// when
		mailApi.selectMail(fetchedMail) //
				.selectTargetFolder(UNEXISTING) //
				.move();

		// then
		assertThat(size(mailApi.selectFolder(INBOX).fetch()), equalTo(0));
		assertThat(size(mailApi.selectFolder(UNEXISTING).fetch()), equalTo(1));
	}

}
