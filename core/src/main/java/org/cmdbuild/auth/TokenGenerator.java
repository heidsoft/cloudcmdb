package org.cmdbuild.auth;

public interface TokenGenerator {

	String generate(String username);

}
