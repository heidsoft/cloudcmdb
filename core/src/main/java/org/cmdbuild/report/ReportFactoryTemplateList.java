package org.cmdbuild.report;

import static java.lang.String.format;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.QuerySpecsBuilderFiller;
import org.cmdbuild.services.FilesStore;

public class ReportFactoryTemplateList extends ReportFactoryTemplate {

	private final Iterable<String> attributeNamesSorted;
	private JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final CMEntryType entryType;
	private final static String REPORT_PDF = "CMDBuild_list.jrxml";
	private final static String REPORT_CSV = "CMDBuild_list_csv.jrxml";

	public ReportFactoryTemplateList( //
			final DataSource dataSource, //
			final ReportExtension reportExtension, //
			final QueryOptions queryOptions, //
			final Iterable<String> attributeOrder, //
			final CMEntryType entryType, //
			final CMDataView dataView, //
			final FilesStore filesStore, //
			final CmdbuildConfiguration configuration //
	) throws JRException {
		super(dataSource, configuration, dataView, filesStore);

		this.reportExtension = reportExtension;
		this.attributeNamesSorted = attributeOrder;

		this.entryType = entryType;
		final QuerySpecsBuilderFiller querySpecsBuilderFiller = new QuerySpecsBuilderFiller(dataView, queryOptions,
				entryType);
		final QuerySpecsBuilder querySpecsBuilder = querySpecsBuilderFiller //
				.create();

		final QueryCreator queryCreator = new QueryCreator(querySpecsBuilder.build());
		loadDesign(reportExtension);
		initDesign(queryCreator, querySpecsBuilderFiller.getAlias());
	}

	private void loadDesign(final ReportExtension reportExtension) throws JRException {
		if (reportExtension == ReportExtension.PDF) {
			this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT_PDF);
		} else {
			this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT_CSV);
		}
	}

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}

	private void initDesign(final QueryCreator queryCreator, final Alias alias) throws JRException {
		setNameFromTable();
		setQuery(queryCreator);
		setAllTableFields(alias);
		setTextFieldsInDetailBand(alias);
		setColumnHeadersForNewFields();

		if (reportExtension == ReportExtension.PDF) {
			setTitleFromTable();
			updateImagesPath();
		}

		refreshLayout(alias);
	}

	private void setNameFromTable() {
		jasperDesign.setName(entryType.getIdentifier().getLocalName());
	}

	protected void setQuery(final QueryCreator queryCreator) {
		final String queryString = getQueryString(queryCreator);
		setQuery(queryString);
	}

	private void setTitleFromTable() {
		setTitle(entryType.getIdentifier().getLocalName());
	}

	private void setAllTableFields(final Alias alias) throws JRException {
		final List<CMAttribute> fields = new LinkedList<CMAttribute>();
		for (final String attributeName : attributeNamesSorted) {
			final CMAttribute attribute = entryType.getAttribute(attributeName);
			// If try to take a reserved
			// attribute form the table
			// it returns null
			if (attribute != null) {
				fields.add(attribute);
			}
		}

		setFields(fields, alias);
	}

	private void setTextFieldsInDetailBand(final Alias alias) {
		final JRSection section = jasperDesign.getDetailSection();
		final JRBand band = section.getBands()[0];
		final List<JRChild> graphicVector = new ArrayList<JRChild>();

		for (final JRChild obj : band.getChildren()) {
			if (!(obj instanceof JRDesignTextField)) {
				graphicVector.add(obj);
			}
		}

		final List<JRChild> detailVector = new ArrayList<JRChild>();
		for (final String attributeName : attributeNamesSorted) {
			final CMAttribute attribute = entryType.getAttribute(attributeName);
			if (attribute != null) {
				final String attributeAlias = fieldNameFromCMAttribute(alias, attribute.getName());
				logger.debug(format("setTextFieldsInDetailBand: %s", attributeAlias));
				detailVector.add(createTextFieldForAttribute(attributeAlias, attribute.getType()));
			}
		}

		band.getChildren().clear();
		band.getChildren().addAll(graphicVector);
		band.getChildren().addAll(detailVector);
	}

	private void setColumnHeadersForNewFields() {
		final JRBand columnHeader = jasperDesign.getColumnHeader();
		final JRElement[] elements = columnHeader.getElements();
		final Vector<JRElement> designHeaders = new Vector<JRElement>();
		final Vector<JRElement> designElements = new Vector<JRElement>();

		// backup existing design elements
		for (int i = 0; i < elements.length; i++) {
			if (!(elements[i] instanceof JRDesignStaticText)) {
				designElements.add(elements[i]);
			}
		}

		// create column headers
		for (final String attribute : attributeNamesSorted) {
			final CMAttribute cmAttribute = entryType.getAttribute(attribute);
			if (cmAttribute != null) {
				String description = cmAttribute.getDescription();
				if ("".equals(description) || description == null) {
					description = cmAttribute.getName();
				}
				final JRDesignStaticText dst = new JRDesignStaticText();
				dst.setText(description);
				designHeaders.add(dst);
			}
		}

		// save new list of items
		columnHeader.getChildren().clear();
		columnHeader.getChildren().addAll(designElements);
		columnHeader.getChildren().addAll(designHeaders);
	}

	/*
	 * Update position of report elements
	 */
	private void refreshLayout(final Alias alias) {
		// calculate weight of all elements
		final Map<String, String> weight = new HashMap<String, String>();
		int virtualWidth = 0;
		int size = 0;
		final int height = 17;

		String key = "";
		CMAttribute attribute = null;
		for (final String attributeName : attributeNamesSorted) {
			attribute = entryType.getAttribute(attributeName);
			if (attribute == null) {
				continue;
			}

			size = getSizeFromAttribute(attribute);
			virtualWidth += size;

			key = fieldNameFromCMAttribute(alias, attribute);

			weight.put(attribute.getName(), Integer.toString(size));
			weight.put(key, Integer.toString(size));
			weight.put(attribute.getDescription(), Integer.toString(size));
		}

		final int pageWidth = jasperDesign.getPageWidth();
		final double cx = (pageWidth * 0.95) / virtualWidth;
		logger.debug("cx=" + cx + " pageWidth " + (pageWidth * 0.95) + " / virtualWidth " + virtualWidth);
		double doub = 0;
		final JRSection section = jasperDesign.getDetailSection();
		final JRBand detail = section.getBands()[0];
		JRElement[] elements = detail.getElements();
		JRDesignTextField dtf = null;
		int x = 0;
		final int y = 2;
		logger.debug("RF updateDesign DESIGN");
		JRDesignExpression varExpr = null;
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof JRDesignTextField) {
				dtf = (JRDesignTextField) elements[i];
				varExpr = (JRDesignExpression) dtf.getExpression();
				key = varExpr.getText();
				logger.debug("text=" + key);
				key = key.substring(3, key.length() - 1);
				logger.debug("text=" + key);
				key = weight.get(key);
				logger.debug("kry=" + key);
				try {
					size = Integer.parseInt(key);
				} catch (final NumberFormatException e) {
					size = 0;
				}
				doub = size * cx;
				size = (int) doub;
				dtf.setX(x);
				dtf.setY(y);
				dtf.setWidth(size);
				dtf.setHeight(height);
				dtf.setBlankWhenNull(true);
				dtf.setStretchWithOverflow(true);
				logger.debug("RF updateDesign x=" + dtf.getX() + " Width=" + dtf.getWidth());
				x += size;
			}
		}

		// sizing table headers
		final JRBand columnHeader = jasperDesign.getColumnHeader();
		elements = columnHeader.getElements();
		JRDesignStaticText dst = null;
		x = 0;
		logger.debug("RF updateDesign HEADER");
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof JRDesignStaticText) {
				dst = (JRDesignStaticText) elements[i];
				key = dst.getText();
				logger.debug("text=" + key);
				key = weight.get(key);
				logger.debug("key=" + key);
				size = Integer.parseInt(key);

				doub = size * cx;
				size = (int) doub;
				dst.setForecolor(Color.WHITE);
				dst.setX(x);
				dst.setHeight(height);
				dst.setWidth(size);
				logger.debug("RF updateDesign" + dst.getText() + " x=" + dst.getX() + " Width=" + dst.getWidth());
				x += size;
			}
		}
	}

	protected int getSizeFromAttribute(final CMAttribute attribute) {
		return new ReportAttributeSizeVisitor().getSize(attribute.getType());
	}
}
