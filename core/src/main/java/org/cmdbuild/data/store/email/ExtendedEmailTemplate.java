package org.cmdbuild.data.store.email;

import java.util.Map;

public interface ExtendedEmailTemplate extends EmailTemplate {

	Map<String, String> getVariables();

}