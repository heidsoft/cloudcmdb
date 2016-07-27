package org.cmdbuild.logic.icon;

import java.util.Optional;

import org.cmdbuild.logic.Logic;

public interface IconsLogic extends Logic {

	Icon create(Icon element);

	Iterable<Icon> read();

	Optional<Icon> read(Icon element);

	void update(Icon element);

	void delete(Icon element);

}
