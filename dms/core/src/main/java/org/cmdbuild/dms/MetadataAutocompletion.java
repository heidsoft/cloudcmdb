package org.cmdbuild.dms;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.Map;

public class MetadataAutocompletion {

	private MetadataAutocompletion() {
		// prevents instantiation
	}

	public static interface AutocompletionRules {

		Iterable<String> getMetadataGroupNames();

		Iterable<String> getMetadataNamesForGroup(String groupName);

		Map<String, String> getRulesForGroupAndMetadata(String groupName, String metadataName);

	}

	public static final AutocompletionRules NULL_AUTOCOMPLETION_RULES = new AutocompletionRules() {

		@Override
		public Iterable<String> getMetadataGroupNames() {
			return emptyList();
		}

		@Override
		public Iterable<String> getMetadataNamesForGroup(final String groupName) {
			return emptyList();
		}

		@Override
		public Map<String, String> getRulesForGroupAndMetadata(final String groupName, final String metadataName) {
			return emptyMap();
		}

	};

	public static interface Reader {

		/**
		 * Reads the auto-completion rules.
		 * 
		 * @throws {@link
		 *             RuntimeException}
		 */
		public AutocompletionRules read();

	}

}
