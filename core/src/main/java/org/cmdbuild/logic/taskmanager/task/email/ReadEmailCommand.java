package org.cmdbuild.logic.taskmanager.task.email;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.data.store.email.EmailConstants.ADDRESSES_SEPARATOR;
import static org.cmdbuild.data.store.email.EmailStatus.RECEIVED;

import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logger.Log;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.email.Email;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailService.Folders;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

class ReadEmailCommand implements Command {

	private static final Logger logger = Log.EMAIL;
	private static Marker marker = MarkerFactory.getMarker(ReadEmailCommand.class.getName());

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ReadEmailCommand> {

		private EmailService emailService;
		private String incomingFolder;
		private String processedFolder;
		private String rejectedFolder;
		private boolean rejectNotMatching;
		private Store<org.cmdbuild.data.store.email.Email> emailStore;
		private final Collection<Action> actions = newArrayList();

		private Builder() {
			// use factory method
		}

		@Override
		public ReadEmailCommand build() {
			validate();
			return new ReadEmailCommand(this);
		}

		private void validate() {
			Validate.notNull(emailService, "invalid email service");
			Validate.notNull(incomingFolder, "invalid incoming folder");
			Validate.notNull(processedFolder, "invalid processed folder");
			if (rejectNotMatching) {
				Validate.notNull(rejectedFolder, "invalid rejected folder");
			}
			Validate.notNull(emailService, "invalid email service");
			Validate.notNull(emailStore, "invalid email store");
		}

		public Builder withEmailService(final EmailService emailService) {
			this.emailService = emailService;
			return this;
		}

		public Builder withIncomingFolder(final String incomingFolder) {
			this.incomingFolder = incomingFolder;
			return this;
		}

		public Builder withProcessedFolder(final String processedFolder) {
			this.processedFolder = processedFolder;
			return this;
		}

		public Builder withRejectedFolder(final String rejectedFolder) {
			this.rejectedFolder = rejectedFolder;
			return this;
		}

		public Builder withRejectNotMatching(final boolean rejectNotMatching) {
			this.rejectNotMatching = rejectNotMatching;
			return this;
		}

		public Builder withEmailStore(final Store<org.cmdbuild.data.store.email.Email> emailStore) {
			this.emailStore = emailStore;
			return this;
		}

		public Builder withAction(final Action action) {
			this.actions.add(action);
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final EmailService service;
	private final String incomingFolder;
	private final String processedFolder;
	private final String rejectedFolder;
	private final boolean rejectNotMatching;
	private final Store<org.cmdbuild.data.store.email.Email> emailStore;
	private final Iterable<Action> actions;

	private ReadEmailCommand(final Builder builder) {
		this.service = builder.emailService;
		this.incomingFolder = builder.incomingFolder;
		this.processedFolder = builder.processedFolder;
		this.rejectedFolder = builder.rejectedFolder;
		this.rejectNotMatching = builder.rejectNotMatching;
		this.emailStore = builder.emailStore;
		this.actions = builder.actions;
	}

	@Override
	public void execute() {
		logger.info(marker, "starting synchronization job");
		execute0();
		logger.info(marker, "finishing synchronization job");
	}

	private void execute0() {
		logger.debug(marker, "reading e-mails");
		for (final Email email : service.receive(new Folders() {

			@Override
			public String incoming() {
				return incomingFolder;
			}

			@Override
			public String processed() {
				return processedFolder;
			}

			@Override
			public boolean rejectNotMatching() {
				return rejectNotMatching;
			}

			@Override
			public String rejected() {
				return rejectedFolder;
			}

		})) {
			logger.debug(marker, "checking e-mail '{}'", email);

			logger.debug(marker, "storing e-mail for the first time");
			final Storable storable = emailStore.create(forStore(email));

			for (final Action action : actions) {
				logger.debug(marker, "taking action '{}'", action);

				logger.info(marker, "executing action");
				action.execute(email, storable);
			}
		}
	}

	private org.cmdbuild.data.store.email.Email forStore(final Email input) {
		final org.cmdbuild.data.store.email.Email output = new org.cmdbuild.data.store.email.Email();
		output.setDate(input.getDate());
		output.setFromAddress(input.getFromAddress());
		output.setToAddresses(on(ADDRESSES_SEPARATOR) //
				.skipNulls() //
				.join(input.getToAddresses()));
		output.setCcAddresses(on(ADDRESSES_SEPARATOR) //
				.skipNulls() //
				.join(input.getCcAddresses()));
		output.setBccAddresses(on(ADDRESSES_SEPARATOR) //
				.skipNulls() //
				.join(input.getBccAddresses()));
		output.setSubject(input.getSubject());
		output.setContent(input.getContent());
		output.setAccount(input.getAccount());
		output.setDelay(input.getDelay());
		output.setStatus(RECEIVED);
		return output;
	}

}