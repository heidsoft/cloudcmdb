package org.cmdbuild.services.store.report;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cmdbuild.report.ReportFactory.ReportType;
import org.postgresql.jdbc.PgArray;

import com.google.common.base.Function;

public class ResultSetToReport implements Function<ResultSet, Report> {

	private static enum Attributes {
		Id, //
		Code, //
		Description, //
		User, //
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
	}

	@Override
	public Report apply(final ResultSet input) {
		try {
			final ReportImpl report = new ReportImpl();
			report.setId(input.getInt(Attributes.Id.toString()));
			report.setCode(input.getString(Attributes.Code.toString()));
			report.setDescription(input.getString(Attributes.Description.toString()));
			report.setUser(input.getString(Attributes.User.toString()));
			final String typeString = input.getString(Attributes.Type.toString());
			report.setType(ReportType.valueOf(typeString.toUpperCase()));
			report.setQuery(input.getString(Attributes.Query.toString()));
			report.setSimpleReport(input.getBytes(Attributes.SimpleReport.toString()));
			report.setRichReport(input.getBytes(Attributes.RichReport.toString()));
			report.setWizard(input.getBytes(Attributes.Wizard.toString()));
			report.setImages(input.getBytes(Attributes.Images.toString()));
			report.setImagesLength(toIntegerArray(input.getObject((Attributes.ImagesLength.toString()))));
			report.setImagesName(toStringArray(input.getObject(Attributes.ImagesName.toString())));
			report.setReportLength(toIntegerArray(input.getObject((Attributes.ReportLength.toString()))));
			report.setGroups(toStringArray(input.getObject((Attributes.Groups.toString()))));
			return report;
		} catch (final SQLException e) {
			// TODO log
			throw new RuntimeException(e);
		}
	}

	private Integer[] toIntegerArray(final Object resultSetOutput) throws SQLException {
		final Integer[] out;
		if (resultSetOutput != null) {
			final PgArray array = (PgArray) resultSetOutput;
			out = (Integer[]) array.getArray();
		} else {
			out = new Integer[0];
		}

		return out;
	}

	private String[] toStringArray(final Object resultSetOutput) throws SQLException {
		final String[] out;

		if (resultSetOutput != null) {
			final PgArray array = (PgArray) resultSetOutput;
			out = (String[]) array.getArray();
		} else {
			out = new String[0];
		}

		return out;
	}

}
