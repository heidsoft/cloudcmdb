package org.cmdbuild.services.store.report;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.services.store.report.ReportQuery.QueryConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.google.common.base.Function;

public class JDBCReportStore implements ReportStore {

	public static final String REPORT_CLASS_NAME = "Report";

	private static enum Attributes {
		Id, //
		Code, //
		Description, //
		Status, //
		User, //
		BeginDate, //
		Type, //
		Query, //
		SimpleReport, //
		RichReport, //
		Wizard, //
		Images, //
		ImagesLength, //
		ReportLength, //
		IdClass, //
		ImagesName, //
		Groups, //
	};

	private final JdbcTemplate jdbcTemplate;
	private final Function<ResultSet, Report> function;

	public JDBCReportStore( //
			final DataSource dataSource, //
			final Function<ResultSet, Report> function //
	) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.function = function;
	}

	@Override
	public List<Report> findReportsByType(final ReportType type) throws ORMException {
		final List<Report> reportList = new ArrayList<Report>();
		final QueryConfiguration configuration = ReportQuery.listByType(type);

		jdbcTemplate.query(configuration.query, configuration.arguments, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				reportList.add(fromResultSet(rs));
			}
		});

		return reportList;
	}

	@Override
	public Report findReportByTypeAndCode(final ReportType type, final String code) throws ORMException {
		for (final Report report : findReportsByType(type)) {
			if (report.getCode().equalsIgnoreCase(code)) {
				return report;
			}
		}

		return null;
	}

	private class FindReportByIdCallbackHandler implements RowCallbackHandler {
		private final boolean founded = false;
		private Report report = null;

		@Override
		public void processRow(final ResultSet rs) throws SQLException {
			if (!founded) {
				report = fromResultSet(rs);
			}
		}

		public Report getReport() {
			return report;
		}
	}

	@Override
	public Report findReportById(final int id) throws NotFoundException, ORMException {
		final QueryConfiguration configuration = ReportQuery.selectById(id);
		final FindReportByIdCallbackHandler callBackHandler = new FindReportByIdCallbackHandler();

		jdbcTemplate.query(configuration.query, configuration.arguments, callBackHandler);

		return callBackHandler.getReport();
	}

	@Override
	public void deleteReport(final int id) {
		final QueryConfiguration configuration = ReportQuery.delete(id);
		jdbcTemplate.update(configuration.query, configuration.arguments);
	}

	@Override
	public List<String> getReportTypes() {

		final QueryConfiguration configuration = ReportQuery.findTypes();
		final ArrayList<String> list = new ArrayList<String>();
		jdbcTemplate.query(configuration.query, configuration.arguments, new RowCallbackHandler() {
			@Override
			public void processRow(final ResultSet rs) throws SQLException {
				list.add(rs.getString(Attributes.Type.toString()));
			}
		});

		return list;
	}

	@Override
	public void insertReport(final Report report) throws SQLException, IOException {
		final QueryConfiguration configuration = ReportQuery.insert(report);
		jdbcTemplate.update(configuration.query, configuration.arguments);
	}

	@Override
	public void updateReport(final Report report) throws SQLException, IOException {
		final QueryConfiguration configuration = ReportQuery.update(report);
		jdbcTemplate.update(configuration.query, configuration.arguments);
	}

	private Report fromResultSet(final ResultSet rs) throws SQLException {
		return function.apply(rs);
	}

}
