package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.reflect.Reflection.newProxy;
import static java.util.Objects.requireNonNull;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.cmdbuild.logic.icon.Types.classType;
import static org.cmdbuild.logic.icon.Types.processType;
import static org.cmdbuild.service.rest.v2.model.Models.newIcon;
import static org.cmdbuild.service.rest.v2.model.Models.newImage;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.util.Map;
import java.util.Optional;

import org.cmdbuild.logic.icon.ForwardingIcon;
import org.cmdbuild.logic.icon.IconsLogic;
import org.cmdbuild.logic.icon.Type;
import org.cmdbuild.logic.icon.TypeVisitor;
import org.cmdbuild.logic.icon.Types.ClassType;
import org.cmdbuild.logic.icon.Types.ProcessType;
import org.cmdbuild.service.rest.v2.Icons;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.cmdbuild.service.rest.v2.model.Icon;
import org.cmdbuild.service.rest.v2.model.Image;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

import com.google.common.base.Converter;

public class CxfIcons implements Icons, LoggingSupport {

	private static final String CLASS = "class";
	private static final String PROCESS = "process";

	public static class ConverterImpl extends Converter<Icon, org.cmdbuild.logic.icon.Icon> {

		private final ErrorHandler errorHandler;

		public ConverterImpl(final ErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
		}

		@Override
		protected org.cmdbuild.logic.icon.Icon doForward(final Icon a) {
			final Type type;
			switch (a.getType()) {
			case CLASS: {
				final Map<String, Object> details = requireNonNull(a.getDetails(), "missing details");
				final String name = String.class.cast(requireNonNull(details.get(Icon.id), "missing " + Icon.id));
				type = classType() //
						.withName(name) //
						.build();
				break;
			}
			case PROCESS: {
				final Map<String, Object> details = requireNonNull(a.getDetails(), "missing details");
				final String name = String.class.cast(requireNonNull(details.get(Icon.id), "missing " + Icon.id));
				type = processType() //
						.withName(name) //
						.build();
				break;
			}
			default:
				errorHandler.invalidIconType(a.getType());
				throw new AssertionError("should never come here");
			}
			return new org.cmdbuild.logic.icon.Icon() {

				@Override
				public Long getId() {
					return a.getId();
				}

				@Override
				public Type getType() {
					return type;
				}

				@Override
				public org.cmdbuild.logic.icon.Image getImage() {
					final org.cmdbuild.logic.icon.Image output;
					switch (requireNonNull(a.getImage(), "missing image").getType()) {
					case Image.filestore:
						output = new org.cmdbuild.logic.icon.Image() {

							private final Map<String, Object> details = a.getImage().getDetails();

							@Override
							public String folder() {
								return String.class
										.cast(requireNonNull(details.get(Image.folder), "missing " + Image.folder));
							}

							@Override
							public String file() {
								return String.class
										.cast(requireNonNull(details.get(Image.file), "missing " + Image.file));
							}

						};
						break;

					default:
						output = null;
						break;
					}
					return requireNonNull(output, "missing " + org.cmdbuild.logic.icon.Image.class);
				}

			};
		}

		@Override
		protected Icon doBackward(final org.cmdbuild.logic.icon.Icon b) {
			return new TypeVisitor() {

				private String type;
				private Map<String, Object> details;

				public Icon icon() {
					b.getType().accept(this);
					return newIcon() //
							.withId(b.getId()) //
							.withType(requireNonNull(type, "missing type")) //
							.withDetails(requireNonNull(details, "missing details")) //
							.withImage(imageOf(requireNonNull(b.getImage(), "missing image"))) //
							.build();
				}

				@Override
				public void visit(final ClassType type) {
					this.type = CLASS;
					this.details = newHashMap();
					this.details.put(Icon.id, type.getName());
				}

				@Override
				public void visit(final ProcessType type) {
					this.type = PROCESS;
					this.details = newHashMap();
					this.details.put(Icon.id, type.getName());
				}

				private Image imageOf(final org.cmdbuild.logic.icon.Image image) {
					return newImage() //
							.withType(Image.filestore) //
							.withDetail(Image.folder, image.folder()) //
							.withDetail(Image.file, image.file()) //
							.build();
				}

			}.icon();
		}

	}

	private final ErrorHandler errorHandler;
	private final IconsLogic logic;
	private final Converter<Icon, org.cmdbuild.logic.icon.Icon> converter;

	public CxfIcons(final ErrorHandler errorHandler, final IconsLogic logic,
			final Converter<Icon, org.cmdbuild.logic.icon.Icon> converter) {
		this.errorHandler = errorHandler;
		this.logic = logic;
		this.converter = converter;
	}

	@Override
	public ResponseSingle<Icon> create(final Icon icon) {
		final org.cmdbuild.logic.icon.Icon created = logic
				.create(converter.convert(requireNonNull(icon, "missing icon")));
		return newResponseSingle(Icon.class) //
				.withElement(converter.reverse().convert(created)) //
				.build();
	}

	@Override
	public ResponseMultiple<Icon> read() {
		final Iterable<org.cmdbuild.logic.icon.Icon> elements = logic.read();
		return newResponseMultiple(Icon.class) //
				.withElements(from(elements) //
						.transform(converter.reverse())) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Icon> read(final Long id) {
		final Optional<org.cmdbuild.logic.icon.Icon> read = logic.read(wrapperForId(id));
		final Icon output;
		if (read.isPresent()) {
			output = converter.reverse().convert(read.get());
		} else {
			errorHandler.missingIcon(id);
			output = null;
		}
		return newResponseSingle(Icon.class) //
				.withElement(output) //
				.build();
	}

	@Override
	public void update(final Long id, final Icon icon) {
		final Optional<org.cmdbuild.logic.icon.Icon> read = logic.read(wrapperForId(id));
		if (read.isPresent()) {
			// TODO do it better
			logic.update(new ForwardingIcon() {

				private final org.cmdbuild.logic.icon.Icon delegate = converter
						.convert(requireNonNull(icon, "missing " + Icon.class));

				@Override
				protected org.cmdbuild.logic.icon.Icon delegate() {
					return delegate;
				}

				@Override
				public Long getId() {
					return read.get().getId();
				}

			});
		} else {
			errorHandler.missingIcon(id);
		}
	}

	@Override
	public void delete(final Long id) {
		final Optional<org.cmdbuild.logic.icon.Icon> read = logic.read(wrapperForId(id));
		if (read.isPresent()) {
			logic.delete(read.get());
		} else {
			errorHandler.missingIcon(id);
		}
	}

	private static org.cmdbuild.logic.icon.Icon wrapperForId(final Long id) {
		return new ForwardingIcon() {

			final org.cmdbuild.logic.icon.Icon UNSUPPORTED = newProxy(org.cmdbuild.logic.icon.Icon.class,
					unsupported("not supported"));

			@Override
			protected org.cmdbuild.logic.icon.Icon delegate() {
				return UNSUPPORTED;
			}

			@Override
			public Long getId() {
				return requireNonNull(id, "missing id");
			}

		};
	}

}
