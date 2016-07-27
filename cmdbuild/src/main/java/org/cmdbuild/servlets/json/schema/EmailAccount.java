package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.CommunicationConstants.ADDRESS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.IMAP_PORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.IMAP_SERVER;
import static org.cmdbuild.servlets.json.CommunicationConstants.IMAP_SSL;
import static org.cmdbuild.servlets.json.CommunicationConstants.IMAP_STARTTLS;
import static org.cmdbuild.servlets.json.CommunicationConstants.IS_DEFAULT;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.OUTPUT_FOLDER;
import static org.cmdbuild.servlets.json.CommunicationConstants.PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.SMTP_PORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.SMTP_SERVER;
import static org.cmdbuild.servlets.json.CommunicationConstants.SMTP_SSL;
import static org.cmdbuild.servlets.json.CommunicationConstants.SMTP_STARTTLS;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER_NAME;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.email.EmailAccountLogic.Account;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class EmailAccount extends JSONBaseWithSpringContext {

	private static class JsonAccount implements Account {

		private Long id;
		private String name;
		private boolean isDefault;
		private String username;
		private String password;
		private String address;
		private String smtpServer;
		private Integer smtpPort;
		private boolean smtpSsl;
		private boolean smtpStartTls;
		private String outputFolder;
		private String imapServer;
		private Integer imapPort;
		private boolean imapSsl;
		private boolean imapStartTls;

		@Override
		@JsonProperty(ID)
		public Long getId() {
			return id;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		@Override
		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		@JsonProperty(IS_DEFAULT)
		public boolean isDefault() {
			return isDefault;
		}

		public void setDefault(final Boolean isDefault) {
			this.isDefault = (isDefault == null) ? false : isDefault;
		}

		@Override
		@JsonProperty(ADDRESS)
		public String getAddress() {
			return address;
		}

		public void setAddress(final String address) {
			this.address = address;
		}

		@Override
		@JsonProperty(USER_NAME)
		public String getUsername() {
			return username;
		}

		public void setUsername(final String username) {
			this.username = username;
		}

		@Override
		@JsonProperty(PASSWORD)
		public String getPassword() {
			return password;
		}

		public void setPassword(final String password) {
			this.password = password;
		}

		@Override
		@JsonProperty(SMTP_SERVER)
		public String getSmtpServer() {
			return smtpServer;
		}

		public void setSmtpServer(final String smtpServer) {
			this.smtpServer = smtpServer;
		}

		@Override
		@JsonProperty(SMTP_PORT)
		public Integer getSmtpPort() {
			return smtpPort;
		}

		public void setSmtpPort(final Integer smtpPort) {
			this.smtpPort = smtpPort;
		}

		@Override
		@JsonProperty(SMTP_SSL)
		public boolean isSmtpSsl() {
			return smtpSsl;
		}

		public void setSmtpSsl(final Boolean smtpSsl) {
			this.smtpSsl = (smtpSsl == null) ? false : smtpSsl;
		}

		@Override
		@JsonProperty(SMTP_STARTTLS)
		public boolean isSmtpStartTls() {
			return smtpStartTls;
		}

		public void setSmtpStartTls(final Boolean smtpStartTls) {
			this.smtpStartTls = (smtpStartTls == null) ? false : smtpStartTls;
		}

		@Override
		@JsonProperty(OUTPUT_FOLDER)
		public String getOutputFolder() {
			return outputFolder;
		}

		public void setOutputFolder(final String outputFolder) {
			this.outputFolder = outputFolder;
		}

		@Override
		@JsonProperty(IMAP_SERVER)
		public String getImapServer() {
			return imapServer;
		}

		public void setImapServer(final String imapServer) {
			this.imapServer = imapServer;
		}

		@Override
		@JsonProperty(IMAP_PORT)
		public Integer getImapPort() {
			return imapPort;
		}

		public void setImapPort(final Integer imapPort) {
			this.imapPort = imapPort;
		}

		@Override
		@JsonProperty(IMAP_SSL)
		public boolean isImapSsl() {
			return imapSsl;
		}

		public void setImapSsl(final Boolean imapSsl) {
			this.imapSsl = (imapSsl == null) ? false : imapSsl;
		}

		@Override
		@JsonProperty(IMAP_STARTTLS)
		public boolean isImapStartTls() {
			return imapStartTls;
		}

		public void setImapStartTls(final Boolean imapStartTls) {
			this.imapStartTls = (imapStartTls == null) ? false : imapStartTls;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class JsonAccounts {

		private List<? super JsonAccount> elements;

		@JsonProperty(ELEMENTS)
		public List<? super JsonAccount> getElements() {
			return elements;
		}

		public void setElements(final Iterable<? extends JsonAccount> elements) {
			this.elements = Lists.newArrayList(elements);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static Function<Account, JsonAccount> ACCOUNT_TO_ACCOUNT_DETAILS = new Function<Account, JsonAccount>() {

		@Override
		public JsonAccount apply(final Account input) {
			return new JsonAccount() {
				{
					setId(input.getId());
					setName(input.getName());
					setDefault(input.isDefault());
					setUsername(input.getUsername());
					setPassword(input.getPassword());
					setAddress(input.getAddress());
					setSmtpServer(input.getSmtpServer());
					setSmtpPort(input.getSmtpPort());
					setSmtpSsl(input.isSmtpSsl());
					setSmtpStartTls(input.isSmtpStartTls());
					setOutputFolder(input.getOutputFolder());
					setImapServer(input.getImapServer());
					setImapPort(input.getImapPort());
					setImapSsl(input.isImapSsl());
					setImapStartTls(input.isImapStartTls());
				}
			};
		}

	};

	@JSONExported
	@Admin
	public JsonResponse delete( //
			@Parameter(NAME) final String name //
	) {
		emailAccountLogic().delete(name);

		return JsonResponse.success();
	}

	@JSONExported
	@Admin
	public JsonResponse get( //
			@Parameter(NAME) final String name //
	) {
		final Account emailAccount = emailAccountLogic().getAccount(name);

		final JsonAccount element = ACCOUNT_TO_ACCOUNT_DETAILS.apply(emailAccount);

		return JsonResponse.success(element);
	}

	@JSONExported
	@Admin
	public JsonResponse getAll() {
		final Iterable<Account> emailAccounts = emailAccountLogic().getAll();

		final Iterable<JsonAccount> elements = from(emailAccounts) //
				.transform(ACCOUNT_TO_ACCOUNT_DETAILS);
		final JsonAccounts accounts = new JsonAccounts();
		accounts.setElements(elements);

		return JsonResponse.success(accounts);
	}

	@JSONExported
	@Admin
	public JsonResponse post( //
			@Parameter(NAME) final String name, //
			@Parameter(IS_DEFAULT) final Boolean isDefault, //
			@Parameter(USER_NAME) final String username, //
			@Parameter(PASSWORD) final String password, //
			@Parameter(ADDRESS) final String address, //
			@Parameter(SMTP_SERVER) final String smtpServer, //
			@Parameter(SMTP_PORT) final Integer smtpPort, //
			@Parameter(SMTP_SSL) final Boolean smtpSsl, //
			@Parameter(SMTP_STARTTLS) final Boolean smtpStartTls, //
			@Parameter(value = OUTPUT_FOLDER, required = false) final String outputFolder, //
			@Parameter(IMAP_SERVER) final String imapServer, //
			@Parameter(IMAP_PORT) final Integer imapPort, //
			@Parameter(IMAP_SSL) final Boolean imapSsl, //
			@Parameter(IMAP_STARTTLS) final Boolean imapStartTls //
	) {
		final JsonAccount accountDetails = new JsonAccount() {
			{
				setName(name);
				setDefault(isDefault);
				setUsername(username);
				setPassword(password);
				setAddress(address);
				setSmtpServer(smtpServer);
				setSmtpPort(smtpPort);
				setSmtpSsl(smtpSsl);
				setSmtpStartTls(smtpStartTls);
				setOutputFolder(outputFolder);
				setImapServer(imapServer);
				setImapPort(imapPort);
				setImapSsl(imapSsl);
				setImapStartTls(imapStartTls);
			}
		};

		final Long id = emailAccountLogic().create(accountDetails);

		return JsonResponse.success(id);
	}

	@JSONExported
	@Admin
	public JsonResponse put( //
			@Parameter(NAME) final String name, //
			@Parameter(IS_DEFAULT) final Boolean isDefault, //
			@Parameter(USER_NAME) final String username, //
			@Parameter(PASSWORD) final String password, //
			@Parameter(ADDRESS) final String address, //
			@Parameter(SMTP_SERVER) final String smtpServer, //
			@Parameter(SMTP_PORT) final Integer smtpPort, //
			@Parameter(SMTP_SSL) final Boolean smtpSsl, //
			@Parameter(SMTP_STARTTLS) final Boolean smtpStartTls, //
			@Parameter(value = OUTPUT_FOLDER, required = false) final String outputFolder, //
			@Parameter(IMAP_SERVER) final String imapServer, //
			@Parameter(IMAP_PORT) final Integer imapPort, //
			@Parameter(IMAP_SSL) final Boolean imapSsl, //
			@Parameter(IMAP_STARTTLS) final Boolean imapStartTls //
	) {
		final JsonAccount accountDetails = new JsonAccount() {
			{
				setName(name);
				setDefault(isDefault);
				setUsername(username);
				setPassword(password);
				setAddress(address);
				setSmtpServer(smtpServer);
				setSmtpPort(smtpPort);
				setSmtpSsl(smtpSsl);
				setSmtpStartTls(smtpStartTls);
				setOutputFolder(outputFolder);
				setImapServer(imapServer);
				setImapPort(imapPort);
				setImapSsl(imapSsl);
				setImapStartTls(imapStartTls);
			}
		};

		emailAccountLogic().update(accountDetails);

		return JsonResponse.success();
	}

	@JSONExported
	@Admin
	public JsonResponse setDefault( //
			@Parameter(NAME) final String name //
	) {
		emailAccountLogic().setDefault(name);

		return JsonResponse.success();
	}

}