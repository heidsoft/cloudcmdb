package org.cmdbuild.report;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.FilesStore;

public class ReportFactoryTemplateSchema extends ReportFactoryTemplate {

	private final JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final static String REPORT = "CMDBuild_dbschema.jrxml";

	public ReportFactoryTemplateSchema( //
			final DataSource dataSource, //
			final ReportExtension reportExtension, //
			final CmdbuildConfiguration configuration, //
			final CMDataView dataView, //
			final FilesStore filesStore //
	) throws JRException {
		this(dataSource, reportExtension, null, configuration, dataView, filesStore);
	}

	public ReportFactoryTemplateSchema( //
			final DataSource dataSource, //
			final ReportExtension reportExtension, //
			final String className, //
			final CmdbuildConfiguration configuration, //
			final CMDataView dataView, //
			final FilesStore filesStore //
	) throws JRException {
		super(dataSource, configuration, dataView, filesStore);
		this.reportExtension = reportExtension;
		jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);
		if (className != null) {
			jasperDesign.setName(className);
			addFillParameter("class", className);
		}
		updateImagesPath();
		updateSubreportsPath();
	}

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}
}
