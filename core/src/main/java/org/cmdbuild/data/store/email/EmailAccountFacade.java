package org.cmdbuild.data.store.email;

import org.cmdbuild.data.store.Store;

import com.google.common.base.Optional;

public interface EmailAccountFacade extends Store<EmailAccount> {

	Optional<EmailAccount> defaultAccount();

	Optional<EmailAccount> firstOf(Iterable<String> names);

	Optional<EmailAccount> firstOfOrDefault(Iterable<String> names);

	Optional<EmailAccount> fromId(Long id);

}
