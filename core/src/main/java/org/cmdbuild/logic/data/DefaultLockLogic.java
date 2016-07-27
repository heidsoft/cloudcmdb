package org.cmdbuild.logic.data;

import static org.cmdbuild.logic.data.access.lock.Lockables.card;
import static org.cmdbuild.logic.data.access.lock.Lockables.instanceActivity;

import java.util.Date;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.logic.data.access.lock.LockManager;
import org.cmdbuild.logic.data.access.lock.LockManager.ExpectedLocked;
import org.cmdbuild.logic.data.access.lock.LockManager.LockedByAnother;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultLockLogic implements LockLogic {

	private static final Marker MARKER = MarkerFactory.getMarker(LockLogic.class.getName());

	// TODO use specific interface
	private final CmdbuildConfiguration configuration;
	private final LockManager lockManager;

	public DefaultLockLogic(final CmdbuildConfiguration configuration, final LockManager lockManager) {
		this.configuration = configuration;
		this.lockManager = lockManager;
	}

	@Override
	public void lockCard(final Long cardId) {
		try {
			logger.debug(MARKER, "locking card '{}'", cardId);
			lockManager.lock(card(cardId));
		} catch (final LockedByAnother e) {
			logger.error(MARKER, "error locking card", e);
			throw forward(e);
		}
	}

	@Override
	public void unlockCard(final Long cardId) {
		try {
			logger.debug(MARKER, "unlocking card '{}'", cardId);
			lockManager.unlock(card(cardId));
		} catch (final LockedByAnother e) {
			logger.error(MARKER, "error unlocking card", e);
			throw forward(e);
		}
	}

	@Override
	public void checkNotLockedCard(final Long cardId) {
		try {
			logger.debug(MARKER, "checking if card '{}' is unlocked", cardId);
			lockManager.checkNotLocked(card(cardId));
		} catch (final LockedByAnother e) {
			logger.error(MARKER, "card is locked", e);
			throw forward(e);
		}
	}

	@Override
	public void checkCardLockedbyUser(final Long cardId) {
		try {
			logger.debug(MARKER, "checking if card '{}' is locked by current user '{}'", cardId);
			lockManager.checkLockedByCurrent(card(cardId));
		} catch (final ExpectedLocked e) {
			logger.error(MARKER, "card should be locked", e);
			throw forward(e);
		} catch (final LockedByAnother e) {
			logger.error(MARKER, "card is locked by another user", e);
			throw forward(e);
		}
	}

	@Override
	public void lockActivity(final Long instanceId, final String activityId) {
		try {
			logger.debug(MARKER, "locking activity '{}' for instance '{}'", activityId, instanceId);
			lockManager.lock(instanceActivity(instanceId, activityId));
		} catch (final LockedByAnother e) {
			logger.error(MARKER, "error locking activity", e);
			throw forward(e);
		}
	}

	@Override
	public void unlockActivity(final Long instanceId, final String activityId) {
		try {
			logger.debug(MARKER, "unlocking activity '{}' for instance '{}'", activityId, instanceId);
			lockManager.unlock(instanceActivity(instanceId, activityId));
		} catch (final LockedByAnother e) {
			logger.error(MARKER, "error unlocking activity", e);
			throw forward(e);
		}
	}

	@Override
	public void checkActivityLockedbyUser(final Long instanceId, final String activityId) {
		try {
			logger.debug(MARKER, "checking if activity '{}' of instance '{}' is locked by current user '{}'",
					activityId, instanceId);
			lockManager.checkLockedByCurrent(instanceActivity(instanceId, activityId));
		} catch (final ExpectedLocked e) {
			logger.error(MARKER, "activity should be locked", e);
			throw forward(e);
		} catch (final LockedByAnother e) {
			logger.error(MARKER, "activity is locked by another user", e);
			throw forward(e);
		}
	}

	@Override
	public void checkNotLockedInstance(final Long instanceId) {
		try {
			logger.debug(MARKER, "checking if instance '{}' is unlocked", instanceId);
			lockManager.checkNotLocked(card(instanceId));
		} catch (final LockedByAnother e) {
			logger.error(MARKER, "instance is locked", e);
			throw forward(e);
		}
	}

	@Override
	public void unlockAll() {
		logger.debug(MARKER, "unlocking all cards");
		lockManager.unlockAll();
	}

	private RuntimeException forward(final ExpectedLocked e) {
		return ConsistencyExceptionType.LOCKED_MISSING //
				.createException();
	}

	private RuntimeException forward(final LockedByAnother e) {
		final long currentTimestamp = new Date().getTime();
		final long differenceInMilliseconds = currentTimestamp - e.getTime().getTime();
		final long differenceInSeconds = differenceInMilliseconds / 1000;
		return ConsistencyExceptionType.LOCKED_CARD //
				.createException(configuration.getLockCardUserVisible() ? e.getOwner() : "undefined",
						"" + differenceInSeconds);
	}

}
