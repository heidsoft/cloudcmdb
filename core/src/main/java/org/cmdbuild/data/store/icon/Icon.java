package org.cmdbuild.data.store.icon;

import org.cmdbuild.data.store.Storable;

public interface Icon extends Storable {

	Long getId();

	String getElement();

	String getPath();

}
