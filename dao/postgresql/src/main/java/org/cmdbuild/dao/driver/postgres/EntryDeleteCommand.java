package org.cmdbuild.dao.driver.postgres;

import org.cmdbuild.dao.entry.CMEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

public class EntryDeleteCommand {

	private final SimpleJdbcCall call;
	private final SqlParameterSource in;

	public EntryDeleteCommand(final JdbcTemplate jdbcTemplate, final CMEntry entry) {
		this.call = new SimpleJdbcCall(jdbcTemplate) //
				.withProcedureName("cm_delete_card");
		this.in = new MapSqlParameterSource() //
				.addValue("cardid", entry.getId()) //
				.addValue("tableid", entry.getType().getId()) //
				.addValue("username", entry.getUser());
	}

	public void execute() {
		call.execute(in);
	}

}
