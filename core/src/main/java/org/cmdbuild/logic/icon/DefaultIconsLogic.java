package org.cmdbuild.logic.icon;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.Objects;
import java.util.Optional;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Converter;

public class DefaultIconsLogic implements IconsLogic {

	private static final Marker MARKER = MarkerFactory.getMarker(DefaultIconsLogic.class.getName());

	private final Store<org.cmdbuild.data.store.icon.Icon> store;
	private final Converter<Icon, org.cmdbuild.data.store.icon.Icon> converter;

	public DefaultIconsLogic(final Store<org.cmdbuild.data.store.icon.Icon> store,
			final Converter<Icon, org.cmdbuild.data.store.icon.Icon> converter) {
		this.store = store;
		this.converter = converter;
	}

	@Override
	public Icon create(final Icon element) {
		logger.info(MARKER, "creating '{}'", element);
		final Storable storable = store.create(converter.convert(requireNonNull(element, "missing " + Icon.class)));
		return converter.reverse().convert(store.read(storable));
	}

	@Override
	public Iterable<Icon> read() {
		logger.info(MARKER, "getting all icons");
		return store.readAll().stream() //
				.map(input -> converter.reverse().convert(input)) //
				.collect(toList());
	}

	@Override
	public Optional<Icon> read(final Icon element) {
		logger.info(MARKER, "getting icon '{}'", element);
		return store.readAll().stream() //
				.filter(input -> Objects.equals(input.getId(), element.getId())) //
				.limit(1) //
				.findFirst() //
				.map(input -> converter.reverse().convert(input));
	}

	@Override
	public void update(final Icon element) {
		logger.info(MARKER, "updating icon '{}'", element);
		store.readAll().stream() //
				.filter(input -> Objects.equals(input.getId(), element.getId())) //
				.limit(1) //
				.findFirst() //
				.ifPresent(input -> store.update(converter.convert(element)));
	}

	@Override
	public void delete(final Icon element) {
		logger.info(MARKER, "deleting icon '{}'", element);
		store.readAll().stream() //
				.filter(input -> Objects.equals(input.getId(), element.getId())) //
				.limit(1) //
				.findFirst() //
				.ifPresent(input -> store.delete(converter.convert(element)));
	}

}
