package org.cmdbuild.data.store.metadata;

import org.cmdbuild.data.store.Storable;

public interface Metadata extends Storable {

	String name();

	String value();

}