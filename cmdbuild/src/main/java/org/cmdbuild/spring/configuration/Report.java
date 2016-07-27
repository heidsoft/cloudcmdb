package org.cmdbuild.spring.configuration;

import static org.cmdbuild.logic.report.Predicates.currentGroupAllowed;

import javax.sql.DataSource;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.logic.report.DefaultReportLogic;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.services.localization.LocalizedResultSetToReport;
import org.cmdbuild.services.store.report.JDBCReportStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.services.store.report.ResultSetToReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Report {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Properties properties;

	@Autowired
	private Translation translation;

	@Autowired
	private UserStore userStore;

	@Bean
	public ReportLogic reportLogic() {
		return new DefaultReportLogic(reportStore(), dataSource, properties.cmdbuildProperties(),
				currentGroupAllowed(userStore));
	}

	@Bean
	public ReportStore reportStore() {
		return new JDBCReportStore(dataSource, localizedResultSetToReport());
	}

	@Bean
	protected LocalizedResultSetToReport localizedResultSetToReport() {
		return new LocalizedResultSetToReport(resultSetToReport(), translation.translationFacade());
	}

	@Bean
	protected ResultSetToReport resultSetToReport() {
		return new ResultSetToReport();
	}

}
