package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.dao.ForwardingStorableConverter;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.WidgetConverter;
import org.cmdbuild.model.view.View;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.store.filter.FilterStore.Filter;
import org.cmdbuild.services.store.menu.LocalizedMenuElement;
import org.cmdbuild.services.store.menu.MenuElement;

import com.google.common.base.Function;

public class LocalizedStorableConverter<T extends Storable> extends ForwardingStorableConverter<T> {

	private final StorableConverter<T> delegate;
	private final TranslationFacade facade;
	private final CMDataView dataView;
	private final ReportLogic reportLogic;
	private final Function<Widget, Widget> TRANSLATE_WIDGET_LABEL;

	public LocalizedStorableConverter(final StorableConverter<T> delegate, final TranslationFacade facade,
			final CMDataView dataView, final ReportLogic reportLogic) {
		this.delegate = delegate;
		this.facade = facade;
		this.dataView = dataView;
		this.reportLogic = reportLogic;
		this.TRANSLATE_WIDGET_LABEL = new Function<Widget, Widget>() {

			@Override
			public Widget apply(final Widget input) {
				final TranslationObject translationObject = WidgetConverter.of(WidgetConverter.label()) //
						.withIdentifier(input.getIdentifier()) //
						.create();
				final String translatedDescription = facade.read(translationObject);
				input.setLabel(defaultIfBlank(translatedDescription, input.getLabel()));
				return input;
			}
		};

	}

	@Override
	protected StorableConverter<T> delegate() {
		return delegate;
	}

	@Override
	public T convert(final CMCard card) {
		return proxy(super.convert(card));
	}

	private T proxy(final T input) {
		final T output;
		if (input instanceof LocalizableStorable) {
			final LocalizableStorable localizedStorable = LocalizableStorable.class.cast(input);
			output = new LocalizableStorableVisitor() {

				private T output;

				public T proxy() {
					localizedStorable.accept(this);
					return output;
				}

				@Override
				public void visit(final Lookup storable) {
					output = (T) storable;
					output = new Function<Lookup, T>() {

						@Override
						public T apply(final Lookup input) {
							return (T) ((input == null) ? null : new LocalizedLookup(input, facade));
						}
					}.apply(storable);
				}

				@Override
				public void visit(final MenuElement storable) {
					output = (T) storable;
					output = new Function<MenuElement, T>() {

						@Override
						public T apply(final MenuElement input) {
							return (T) ((input == null) ? null : new LocalizedMenuElement(input, facade, dataView,
									reportLogic));
						}
					}.apply(storable);
				}

				@Override
				public void visit(final View storable) {
					output = (T) storable;
					output = new Function<View, T>() {

						@Override
						public T apply(final View input) {
							return (T) ((input == null) ? null : new LocalizedView(input, facade));
						}
					}.apply(storable);
				}

				@Override
				public void visit(final Widget storable) {
					output = (T) storable;
					output = new Function<Widget, T>() {

						@Override
						public T apply(final Widget input) {
							return (T) ((input == null) ? null : TRANSLATE_WIDGET_LABEL.apply(storable));
						}
					}.apply(storable);

				}

				@Override
				public void visit(final Filter filter) {
					output = (T) filter;
					output = new Function<Filter, T>() {

						@Override
						public T apply(final Filter input) {
							return (T) ((input == null) ? null : new LocalizedFilter(input, facade));
						}
					}.apply(filter);
				}
			}.proxy();
		} else {
			output = input;
		}
		return output;
	}

}
