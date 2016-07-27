package org.cmdbuild.auth;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.joda.time.DateTime.now;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

public class SimpleTokenGenerator implements TokenGenerator {

	private static final int NUMBITS = 130;
	private static final int RADIX = 32;

	private final Random random = new SecureRandom();

	@Override
	public String generate(final String username) {
		return generate0(username);
	}

	private synchronized String generate0(final String username) {
		final int seed = new StringBuilder(defaultString(username)) //
				.append(now().getMillis()) //
				.append(UUID.randomUUID().toString()) //
				.toString() //
				.hashCode();
		random.setSeed(seed);
		return new BigInteger(NUMBITS, random).toString(RADIX);
	}

}
