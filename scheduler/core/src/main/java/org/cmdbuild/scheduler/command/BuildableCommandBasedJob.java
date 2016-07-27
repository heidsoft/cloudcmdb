package org.cmdbuild.scheduler.command;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.scheduler.command.Commands.nullCommand;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.scheduler.Job;

public class BuildableCommandBasedJob implements Job {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<BuildableCommandBasedJob> {

		private String name;
		private Command command;

		private Builder() {
			// use factory method
		}

		@Override
		public BuildableCommandBasedJob build() {
			validate();
			return new BuildableCommandBasedJob(this);
		}

		private void validate() {
			Validate.notBlank(name);
			command = defaultIfNull(command, nullCommand());
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withCommand(final Command command) {
			this.command = command;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String name;
	private final Command command;

	private BuildableCommandBasedJob(final Builder builder) {
		this.name = builder.name;
		this.command = builder.command;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void execute() {
		command.execute();
	}

}
