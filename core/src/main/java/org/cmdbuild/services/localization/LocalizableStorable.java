package org.cmdbuild.services.localization;

import org.cmdbuild.data.store.Storable;

public interface LocalizableStorable extends Storable {

	void accept(LocalizableStorableVisitor visitor);

}
