package org.cmdbuild.logic.data.access.filter.model;

import com.google.common.base.Optional;

public interface Filter {

	Optional<Element> attribute();

	Optional<String> fullTextQuery();

}
