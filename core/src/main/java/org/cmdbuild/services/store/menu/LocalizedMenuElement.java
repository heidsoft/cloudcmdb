package org.cmdbuild.services.store.menu;

import static com.google.common.base.Optional.absent;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.model.view.ViewConverter.VIEW_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuItemType.isClassOrProcess;
import static org.cmdbuild.services.store.menu.MenuItemType.isDashboard;
import static org.cmdbuild.services.store.menu.MenuItemType.isReport;
import static org.cmdbuild.services.store.menu.MenuItemType.isView;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.report.ReportLogic.Report;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.ReportConverter;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.services.localization.LocalizableStorableVisitor;

import com.google.common.base.Optional;

public class LocalizedMenuElement extends ForwardingMenuElement {

	private static final Optional<String> NO_REPORT = absent();

	private final MenuElement delegate;
	private final TranslationFacade facade;
	private final CMDataView dataView;
	private final ReportLogic reportLogic;

	public LocalizedMenuElement(final MenuElement delegate, final TranslationFacade facade, final CMDataView dataView,
			final ReportLogic reportLogic) {
		this.delegate = delegate;
		this.facade = facade;
		this.dataView = dataView;
		this.reportLogic = reportLogic;
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected MenuElement delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {
		return defaultIfBlank(searchTranslation(), super.getDescription());
	}

	private String searchTranslation() {

		final String uuid = getUuid();
		final String output;
		if (!isBlank(uuid)) {
			final TranslationObject translationObject = org.cmdbuild.logic.translation.converter.MenuItemConverter //
					.of(org.cmdbuild.logic.translation.converter.MenuItemConverter.description()) //
					.withIdentifier(uuid).create();
			String translatedDescription = facade.read(translationObject);

			if (isBlank(translatedDescription)) {
				final MenuItemType type = getType();
				if (isClassOrProcess(type)) {
					final String className = getElementClassName();
					final ClassConverter converter = ClassConverter.of(ClassConverter.description());
					final TranslationObject classTranslation = converter //
							.withIdentifier(className).create();
					translatedDescription = facade.read(classTranslation);

				} else if (isReport(type)) {
					final Optional<String> _reportName = fetchReportName();
					if (_reportName.isPresent()) {
						final ReportConverter converter = ReportConverter.of(ReportConverter.description());
						Validate.isTrue(converter.isValid());
						final TranslationObject reportTranslationObject = converter //
								.withIdentifier(_reportName.get()) //
								.create();
						translatedDescription = facade.read(reportTranslationObject);
					}
				} else if (isView(type)) {
					final Optional<String> _viewName = fetchViewName();
					if (_viewName.isPresent()) {
						final ViewConverter converter = ViewConverter.of(ViewConverter.description());
						Validate.isTrue(converter.isValid());
						final TranslationObject viewTranslationObject = converter //
								.withIdentifier(_viewName.get()) //
								.create();
						translatedDescription = facade.read(viewTranslationObject);
					}
				} else if (isDashboard(type)) {
					// dashboards localization not supported
				}
			}
			output = translatedDescription;
		} else {
			output = super.getDescription();
		}
		return output;
	}

	private Optional<String> fetchReportName() {
		final Number reportId = getElementId();
		final Optional<Report> report = reportLogic.read(reportId.intValue());
		return report.isPresent() ? Optional.of(report.get().getTitle()) : NO_REPORT;
	}

	private Optional<String> fetchViewName() {
		final Number viewId = getElementId();
		final CMClass viewClass = dataView.findClass(VIEW_CLASS_NAME);
		return selectNameFromIdAndClass(viewId, viewClass);
	}

	private Optional<String> selectNameFromIdAndClass(final Number id, final CMClass cmClass) {
		final Optional<CMCard> _reportCard = fetchCardFromIdAndClass(id, cmClass);
		Optional<String> _name;
		if (_reportCard.isPresent()) {
			final CMCard reportCard = _reportCard.get();
			final String name = String.class.cast(reportCard.get("Name"));
			_name = Optional.of(name);
		} else {
			_name = Optional.absent();
		}
		return _name;
	}

	private Optional<CMCard> fetchCardFromIdAndClass(final Number id, final CMClass cmClass) {
		final CMQueryResult queryResult = dataView.select(anyAttribute(cmClass)) //
				.from(cmClass) //
				.where(condition(attribute(cmClass, ID_ATTRIBUTE), eq(id))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run();
		Optional<CMCard> _card;
		if (!queryResult.isEmpty()) {
			final CMCard card = queryResult.getOnlyRow().getCard(cmClass);
			_card = Optional.of(card);
		} else {
			_card = Optional.absent();
		}
		return _card;
	}

}
