package org.cmdbuild.logic.email;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;

import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.DefaultEmailAccount;
import org.cmdbuild.data.store.email.EmailAccount;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;

public class DefaultEmailAccountLogic implements EmailAccountLogic {

	private static final Marker marker = MarkerFactory.getMarker(DefaultEmailAccountLogic.class.getName());

	private static abstract class ForwardingAccount extends ForwardingObject implements Account {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingAccount() {
		}

		@Override
		protected abstract Account delegate();

		@Override
		public Long getId() {
			return delegate().getId();
		}

		@Override
		public String getName() {
			return delegate().getName();
		}

		@Override
		public boolean isDefault() {
			return delegate().isDefault();
		}

		@Override
		public String getUsername() {
			return delegate().getUsername();
		}

		@Override
		public String getPassword() {
			return delegate().getPassword();
		}

		@Override
		public String getAddress() {
			return delegate().getAddress();
		}

		@Override
		public String getSmtpServer() {
			return delegate().getSmtpServer();
		}

		@Override
		public Integer getSmtpPort() {
			return delegate().getSmtpPort();
		}

		@Override
		public boolean isSmtpSsl() {
			return delegate().isSmtpSsl();
		}

		@Override
		public boolean isSmtpStartTls() {
			return delegate().isSmtpStartTls();
		}

		@Override
		public String getOutputFolder() {
			return delegate().getOutputFolder();
		}

		@Override
		public String getImapServer() {
			return delegate().getImapServer();
		}

		@Override
		public Integer getImapPort() {
			return delegate().getImapPort();
		}

		@Override
		public boolean isImapSsl() {
			return delegate().isImapSsl();
		}

		@Override
		public boolean isImapStartTls() {
			return delegate().isImapStartTls();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class AlwaysDefault extends ForwardingAccount {

		public static AlwaysDefault of(final Account delegate) {
			return new AlwaysDefault(delegate);
		}

		private final Account delegate;

		private AlwaysDefault(final Account delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Account delegate() {
			return delegate;
		}

		@Override
		public boolean isDefault() {
			return true;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class NeverDefault extends ForwardingAccount {

		public static NeverDefault of(final Account delegate) {
			return new NeverDefault(delegate);
		}

		private final Account delegate;

		private NeverDefault(final Account delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Account delegate() {
			return delegate;
		}

		@Override
		public boolean isDefault() {
			return false;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class MaybeDefault extends ForwardingAccount {

		public static MaybeDefault of(final Account account, final boolean isDefault) {
			return new MaybeDefault(account, isDefault);
		}

		private final Account delegate;
		private final boolean isDefault;

		private MaybeDefault(final Account delegate, final boolean isDefault) {
			this.delegate = delegate;
			this.isDefault = isDefault;
		}

		@Override
		protected Account delegate() {
			return delegate;
		}

		@Override
		public boolean isDefault() {
			return isDefault;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class AccountWrapper implements Account {

		private final EmailAccount delegate;

		public AccountWrapper(final EmailAccount delegate) {
			this.delegate = delegate;
		}

		@Override
		public Long getId() {
			return delegate.getId();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public boolean isDefault() {
			return delegate.isDefault();
		}

		@Override
		public String getUsername() {
			return delegate.getUsername();
		}

		@Override
		public String getPassword() {
			return delegate.getPassword();
		}

		@Override
		public String getAddress() {
			return delegate.getAddress();
		}

		@Override
		public String getSmtpServer() {
			return delegate.getSmtpServer();
		}

		@Override
		public Integer getSmtpPort() {
			return delegate.getSmtpPort();
		}

		@Override
		public boolean isSmtpSsl() {
			return delegate.isSmtpSsl();
		}

		@Override
		public boolean isSmtpStartTls() {
			return delegate.isSmtpStartTls();
		}

		@Override
		public String getOutputFolder() {
			return delegate.getOutputFolder();
		}

		@Override
		public String getImapServer() {
			return delegate.getImapServer();
		}

		@Override
		public Integer getImapPort() {
			return delegate.getImapPort();
		}

		@Override
		public boolean isImapSsl() {
			return delegate.isImapSsl();
		}

		@Override
		public boolean isImapStartTls() {
			return delegate.isImapStartTls();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static final Function<EmailAccount, Account> EMAIL_ACCOUNT_TO_ACCOUNT = new Function<EmailAccount, EmailAccountLogic.Account>() {

		@Override
		public Account apply(final EmailAccount input) {
			return new AccountWrapper(input);
		};

	};

	private static final Function<Account, EmailAccount> ACCOUNT_TO_EMAIL_ACCOUNT = new Function<Account, EmailAccount>() {

		@Override
		public EmailAccount apply(final Account input) {
			return DefaultEmailAccount.newInstance() //
					.withDefaultStatus(input.isDefault()) //
					.withName(input.getName()) //
					.withAddress(input.getAddress()) //
					.withUsername(input.getUsername()) //
					.withPassword(input.getPassword()) //
					.withSmtpServer(input.getSmtpServer()) //
					.withSmtpPort(input.getSmtpPort()) //
					.withSmtpSsl(input.isSmtpSsl()) //
					.withSmtpStartTls(input.isSmtpStartTls()) //
					.withOutputFolder(input.getOutputFolder()) //
					.withImapServer(input.getImapServer()) //
					.withImapPort(input.getImapPort()) //
					.withImapSsl(input.isImapSsl()) //
					.withImapStartTls(input.isImapStartTls()) //
					.build();
		};

	};

	private static Function<EmailAccount, String> TO_NAME = new Function<EmailAccount, String>() {

		@Override
		public String apply(final EmailAccount input) {
			return input.getName();
		}

	};

	private static Predicate<EmailAccount> IS_DEFAULT = new Predicate<EmailAccount>() {

		@Override
		public boolean apply(final EmailAccount input) {
			return input.isDefault();
		}

	};

	private final Store<EmailAccount> store;

	public DefaultEmailAccountLogic(final Store<EmailAccount> store) {
		this.store = store;
	}

	@Override
	public Long create(final Account account) {
		logger.info(marker, "creating account '{}'", account);
		final Collection<EmailAccount> elements = store.readAll();
		assureNoOneWithName(account.getName(), elements);
		final Account readyAccount = isEmpty(elements) ? AlwaysDefault.of(account) : NeverDefault.of(account);
		final EmailAccount emailAccount = ACCOUNT_TO_EMAIL_ACCOUNT.apply(readyAccount);
		final Storable created = store.create(emailAccount);
		final EmailAccount readed = store.read(created);
		return readed.getId();
	}

	@Override
	public void update(final Account account) {
		logger.info(marker, "updating account '{}'", account);
		assureOnlyOneWithName(account.getName());
		final EmailAccount emailAccount = ACCOUNT_TO_EMAIL_ACCOUNT.apply(account);
		final EmailAccount readed = store.read(emailAccount);
		final EmailAccount updateable = ACCOUNT_TO_EMAIL_ACCOUNT.apply(MaybeDefault.of(account, readed.isDefault()));
		store.update(updateable);
	}

	@Override
	public Iterable<Account> getAll() {
		logger.info(marker, "getting all accounts");
		return from(store.readAll()) //
				.transform(EMAIL_ACCOUNT_TO_ACCOUNT);
	}

	@Override
	public Account getAccount(final String name) {
		logger.info(marker, "getting account '{}'", name);
		assureOnlyOneWithName(name);
		final EmailAccount account = DefaultEmailAccount.newInstance() //
				.withName(name) //
				.build();
		final EmailAccount readed = store.read(account);
		return EMAIL_ACCOUNT_TO_ACCOUNT.apply(readed);
	}

	@Override
	public void delete(final String name) {
		logger.info(marker, "deleting account '{}'", name);
		assureOnlyOneWithName(name);
		assureNotDefault(name);
		final EmailAccount account = DefaultEmailAccount.newInstance() //
				.withName(name) //
				.build();
		store.delete(account);
	}

	@Override
	public void setDefault(final String name) {
		logger.info(marker, "setting to default '{}'", name);
		final Collection<EmailAccount> elements = store.readAll();
		assureOnlyOneWithName(name, elements);
		boolean alreadyDefault = false;
		for (final EmailAccount element : from(elements).filter(IS_DEFAULT)) {
			if (element.getName().equals(name)) {
				alreadyDefault = true;
				continue;
			}
			final Account account = EMAIL_ACCOUNT_TO_ACCOUNT.apply(element);
			final EmailAccount updated = ACCOUNT_TO_EMAIL_ACCOUNT.apply(NeverDefault.of(account));
			store.update(updated);
		}
		if (!alreadyDefault) {
			final EmailAccount toBeSet = DefaultEmailAccount.newInstance() //
					.withName(name) //
					.build();
			final EmailAccount element = store.read(toBeSet);
			final Account account = EMAIL_ACCOUNT_TO_ACCOUNT.apply(element);
			final EmailAccount updated = ACCOUNT_TO_EMAIL_ACCOUNT.apply(AlwaysDefault.of(account));
			store.update(updated);
		}
	}

	private void assureNoOneWithName(final String name, final Iterable<EmailAccount> elements) {
		final boolean existing = from(elements) //
				.transform(TO_NAME) //
				.contains(name);
		Validate.isTrue(!existing, "already existing element");
	}

	private void assureOnlyOneWithName(final String name) {
		assureOnlyOneWithName(name, store.readAll());
	}

	private void assureOnlyOneWithName(final String name, final Iterable<EmailAccount> elements) {
		final int count = from(elements) //
				.transform(TO_NAME) //
				.filter(equalTo(name)) //
				.size();
		Validate.isTrue(!(count == 0), "element not found");
		Validate.isTrue(!(count > 1), "multiple elements found");
	}

	private void assureNotDefault(final String name) {
		final EmailAccount account = DefaultEmailAccount.newInstance() //
				.withName(name) //
				.build();
		final EmailAccount readed = store.read(account);
		Validate.isTrue(!readed.isDefault(), "element is default");
	}

}
