package org.cmdbuild.logic.widget;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.data.store.Storables.storableOf;
import static org.cmdbuild.model.widget.Predicates.active;
import static org.cmdbuild.model.widget.Predicates.sourceClass;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.widget.Widget;

import com.google.common.base.Predicate;

public class WidgetLogic implements Logic {

	private final Store<Widget> store;

	public WidgetLogic(final Store<Widget> store) {
		this.store = store;
	}

	public Iterable<Widget> getAllWidgets(final boolean activeOnly, final Iterable<String> classNames) {
		return from(store.readAll()) //
				.filter(and(activeOnly(activeOnly), classNames(classNames)));
	}

	private Predicate<? super Widget> activeOnly(final boolean value) {
		final Predicate<? super Widget> output;
		if (value) {
			output = active(equalTo(true));
		} else {
			output = alwaysTrue();
		}
		return output;
	}

	private Predicate<? super Widget> classNames(final Iterable<String> values) {
		final Predicate<? super Widget> output;
		if (isEmpty(values)) {
			output = alwaysTrue();
		} else {
			output = sourceClass(in(from(values).toList()));
		}
		return output;
	}

	public Widget getWidget(final Long widgetId) {
		return store.read(storableOf(widgetId));
	}

	public Widget createWidget(final Widget widgetToCreate) {
		return store.read(store.create(widgetToCreate));
	}

	public void updateWidget(final Widget widgetToUpdate) {
		store.update(widgetToUpdate);
	}

	public void deleteWidget(final Long widgetId) {
		final Storable storableToDelete = storableOf((widgetId));
		store.delete(storableToDelete);
	}

}
