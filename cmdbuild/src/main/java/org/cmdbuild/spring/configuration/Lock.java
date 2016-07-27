package org.cmdbuild.spring.configuration;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.logic.data.ConfigurationAwareLockLogic;
import org.cmdbuild.logic.data.DefaultLockLogic;
import org.cmdbuild.logic.data.DummyLockLogic;
import org.cmdbuild.logic.data.LockLogic;
import org.cmdbuild.logic.data.access.lock.CmdbuildConfigurationAdapter;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.DurationExpired;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.Owner;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.OwnerAccepted;
import org.cmdbuild.logic.data.access.lock.DisposingLockableStore;
import org.cmdbuild.logic.data.access.lock.DisposingLockableStore.Disposer;
import org.cmdbuild.logic.data.access.lock.DisposingLockableStore.PredicateBasedDisposer;
import org.cmdbuild.logic.data.access.lock.LockManager;
import org.cmdbuild.logic.data.access.lock.Lockable;
import org.cmdbuild.logic.data.access.lock.LockableStore;
import org.cmdbuild.logic.data.access.lock.MapLockableStore;
import org.cmdbuild.logic.data.access.lock.SessionSupplier;
import org.cmdbuild.logic.data.access.lock.SynchronizedLockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

@Configuration
public class Lock {

	@Autowired
	private Authentication authentication;

	@Autowired
	private Properties properties;

	public static final String USER_LOCK_LOGIC = "UserLockLogic";

	@Bean(name = USER_LOCK_LOGIC)
	public LockLogic configurationAwareLockLogic() {
		return new ConfigurationAwareLockLogic(properties.cmdbuildProperties(), dummyLockLogic(), defaultLockLogic());
	}

	@Bean
	protected LockLogic defaultLockLogic() {
		// TODO wrap properties
		return new DefaultLockLogic(properties.cmdbuildProperties(), synchronizedLockCardManager());
	}

	@Bean
	public LockLogic dummyLockLogic() {
		return new DummyLockLogic();
	}

	@Bean
	protected LockManager synchronizedLockCardManager() {
		return new SynchronizedLockManager(defaultLockManager());
	}

	@Bean
	protected LockManager defaultLockManager() {
		return new DefaultLockManager(disposingLockableStore(), ownerSupplier());
	}

	@Bean
	protected LockableStore<DefaultLockManager.Lock> disposingLockableStore() {
		return new DisposingLockableStore<>(inMemoryLockableStore(), predicateBasedDisposer());
	}

	@Bean
	protected LockableStore<DefaultLockManager.Lock> inMemoryLockableStore() {
		final Map<Lockable, DefaultLockManager.Lock> map = new HashMap<>();
		return new MapLockableStore<DefaultLockManager.Lock>(map);
	}

	@Bean
	protected Disposer<DefaultLockManager.Lock> predicateBasedDisposer() {
		return new PredicateBasedDisposer<>(or(durationExpired(), ownerNotAccepted()));
	}

	@Bean
	protected Predicate<DefaultLockManager.Lock> durationExpired() {
		return new DurationExpired(cmdbuildConfigurationAdapter());
	}

	@Bean
	protected Predicate<DefaultLockManager.Lock> ownerNotAccepted() {
		return not(new OwnerAccepted(new Predicate<Owner>() {

			@Override
			public boolean apply(final Owner input) {
				return sessionLogic().exists(input.getId());
			}

		}));
	}

	@Bean
	protected CmdbuildConfigurationAdapter cmdbuildConfigurationAdapter() {
		return new CmdbuildConfigurationAdapter(properties.cmdbuildProperties());
	}

	@Bean
	protected Supplier<Owner> ownerSupplier() {
		return new SessionSupplier(sessionLogic());
	}

	private SessionLogic sessionLogic() {
		return authentication.standardSessionLogic();
	}

}
