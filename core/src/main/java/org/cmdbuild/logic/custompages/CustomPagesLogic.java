package org.cmdbuild.logic.custompages;

import org.cmdbuild.logic.Logic;

public interface CustomPagesLogic extends Logic {

	Iterable<CustomPage> read();

	Iterable<CustomPage> readForCurrentUser();

	CustomPage read(Long id);

}
