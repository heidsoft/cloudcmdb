package unit.logic.email;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.email.DefaultSubjectHandler;
import org.cmdbuild.logic.email.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic.Email;
import org.cmdbuild.logic.email.SubjectHandler.CompiledSubject;
import org.cmdbuild.logic.email.SubjectHandler.ParsedSubject;
import org.junit.Test;

public class DefaultSubjectHandlerTest {

	/*
	 * parsing
	 */

	@Test
	public void parsedSubjectHasExpectedFormat() {
		assertThat(parse("[42]").hasExpectedFormat(), is(true));
		assertThat(parse("[ 42]").hasExpectedFormat(), is(true));
		assertThat(parse("[42 ]").hasExpectedFormat(), is(true));
		assertThat(parse(" [42]").hasExpectedFormat(), is(true));
		assertThat(parse("[42] ").hasExpectedFormat(), is(true));
		assertThat(parse("[42] foo").hasExpectedFormat(), is(true));
	}

	@Test
	public void parsedSubjectDoesNotHaveExpectedFormat() {
		assertThat(parse("[foo]").hasExpectedFormat(), is(false));
		assertThat(parse("[ foo]").hasExpectedFormat(), is(false));
		assertThat(parse("[foo ]").hasExpectedFormat(), is(false));
		assertThat(parse(" [foo]").hasExpectedFormat(), is(false));
		assertThat(parse("[foo] ").hasExpectedFormat(), is(false));
		assertThat(parse("[foo] bar").hasExpectedFormat(), is(false));
	}

	@Test
	public void emailIdExtractedFromParsedSubject() throws Exception {
		assertThat(parse("[42]").getEmailId(), equalTo(42L));
		assertThat(parse("[ 42]").getEmailId(), equalTo(42L));
		assertThat(parse("[42 ]").getEmailId(), equalTo(42L));
		assertThat(parse(" [42]").getEmailId(), equalTo(42L));
		assertThat(parse("[42] ").getEmailId(), equalTo(42L));
		assertThat(parse("[42] foo").getEmailId(), equalTo(42L));
	}

	@Test
	public void realSubjectFromParsedSubject() throws Exception {
		assertThat(parse("[42]").getRealSubject(), equalTo(""));
		assertThat(parse("[42] ").getRealSubject(), equalTo(""));
		assertThat(parse("[42]foo").getRealSubject(), equalTo("foo"));
		assertThat(parse("[42] foo").getRealSubject(), equalTo("foo"));
		assertThat(parse("[42] foo ").getRealSubject(), equalTo("foo "));
	}

	/*
	 * compiling
	 */

	@Test
	public void compiledSubjectIfEmailHasIdHasExpectedFormat() {
		// given
		final Email email = EmailImpl.newInstance() //
				.withId(42L) //
				.withSubject("foo") //
				.build();

		assertThat(compile(email).getSubject(), equalTo("[42] foo"));
	}

	@Test
	public void compiledSubjectIfEmailHasNoIdHasExpectedFormat() {
		// given
		final Email email = EmailImpl.newInstance() //
				.withSubject("foo") //
				.build();

		assertThat(compile(email).getSubject(), equalTo("foo"));
	}

	/*
	 * round-trip
	 */

	@Test
	public void roundTrip() {
		// given
		final Email email = EmailImpl.newInstance() //
				.withId(42L) //
				.withSubject(" foo ") //
				.build();

		// when
		final ParsedSubject parsedSubject = parse(compile(email).getSubject());

		// then
		assertThat(parsedSubject.hasExpectedFormat(), is(true));
		assertThat(parsedSubject.getEmailId(), equalTo(42L));
		assertThat(parsedSubject.getRealSubject(), equalTo("foo "));
	}

	/*
	 * utils
	 */

	private ParsedSubject parse(final String subject) {
		return new DefaultSubjectHandler().parse(subject);
	}

	private CompiledSubject compile(final Email email) {
		return new DefaultSubjectHandler().compile(email);
	}

}
