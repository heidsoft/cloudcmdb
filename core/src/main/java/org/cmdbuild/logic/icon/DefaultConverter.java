package org.cmdbuild.logic.icon;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.cmdbuild.logic.icon.Types.classType;
import static org.cmdbuild.logic.icon.Types.processType;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.files.Element;
import org.cmdbuild.logic.files.FileStore;
import org.cmdbuild.logic.icon.Types.ClassType;
import org.cmdbuild.logic.icon.Types.ProcessType;

import com.google.common.base.Converter;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class DefaultConverter extends Converter<Icon, org.cmdbuild.data.store.icon.Icon> {

	private final FileStore fileStore;

	public DefaultConverter(final FileStore fileStore) {
		this.fileStore = fileStore;
	}

	@Override
	protected org.cmdbuild.data.store.icon.Icon doForward(final Icon a) {
		requireNonNull(a, "missing " + Icon.class);
		return new org.cmdbuild.data.store.icon.Icon() {

			private final Long id = a.getId();
			private final String element = elementOf(a);
			private final String path = pathOf(a);

			@Override
			public String getIdentifier() {
				return getId().toString();
			}

			@Override
			public Long getId() {
				return id;
			}

			@Override
			public String getElement() {
				return element;
			}

			@Override
			public String getPath() {
				return path;
			}

		};
	}

	private String elementOf(final Icon a) {
		requireNonNull(a, "missing " + Icon.class);
		return new TypeVisitor() {

			private String output;

			public String elementOf(final Icon a) {
				requireNonNull(a.getType(), "missing " + Type.class).accept(this);
				return requireNonNull(output, "missing output");
			}

			@Override
			public void visit(final ClassType type) {
				output = Joiner.on(".") //
						.join( //
								"class", //
								requireNonNull(type.getName(), "missing name"));
			}

			@Override
			public void visit(final ProcessType type) {
				output = Joiner.on(".") //
						.join( //
								"process", //
								requireNonNull(type.getName(), "missing name"));
			}

		}.elementOf(a);
	}

	private String pathOf(final Icon a) {
		requireNonNull(a, "missing " + Icon.class);
		final Image image = requireNonNull(a.getImage(), "missing " + Image.class);
		final Optional<Element> file = stream(fileStore.files(image.folder()).spliterator(), false) //
				.filter(input -> input.getId().equals(image.file())) //
				.limit(1) //
				.findFirst();
		Validate.isTrue(file.isPresent(), "missing file");
		return file.get().getPath();
	}

	@Override
	protected Icon doBackward(final org.cmdbuild.data.store.icon.Icon b) {
		requireNonNull(b, "missing " + Icon.class);
		return new Icon() {

			private final Long id = b.getId();
			private final Type type = typeOf(b);
			private final Image image = imageOf(b);

			@Override
			public Long getId() {
				return id;
			}

			@Override
			public Type getType() {
				return type;
			}

			@Override
			public Image getImage() {
				return image;
			}

		};
	}

	private Type typeOf(final org.cmdbuild.data.store.icon.Icon b) {
		final Type output;
		final List<String> ss = Splitter.on(".") //
				.splitToList(b.getElement());
		switch (ss.get(0)) {
		case "class":
			output = classType() //
					.withName(ss.get(1)) //
					.build();
			break;
		case "process":
			output = processType() //
					.withName(ss.get(1)) //
					.build();
			break;
		default:
			output = null;
			break;
		}
		return requireNonNull(output, "missing " + Type.class);
	}

	private Image imageOf(final org.cmdbuild.data.store.icon.Icon b) {
		final File fileForPath = new File(b.getPath());
		final Optional<Element> folder = stream(fileStore.folders().spliterator(), false) //
				.filter(input -> input.getPath().equals(fileForPath.getParentFile().getPath())) //
				.limit(1) //
				.findFirst();
		Validate.isTrue(folder.isPresent(), "missing folder");
		final Optional<Element> file = stream(fileStore.files(folder.get().getId()).spliterator(), false) //
				.filter(input -> input.getName().equals(fileForPath.getName())) //
				.limit(1) //
				.findFirst();
		Validate.isTrue(file.isPresent(), "missing file");
		return new Image() {

			private final String folderId = folder.get().getId();
			private final String fileId = file.get().getId();

			@Override
			public String folder() {
				return folderId;
			}

			@Override
			public String file() {
				return fileId;
			}

		};
	}

}
