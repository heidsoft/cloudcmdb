package org.cmdbuild.services;

import static java.util.Arrays.stream;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class TranslationService {

	private static TranslationService instance;

	private Map<String, JSONObject> map;

	private TranslationService() {
		init();
	}

	public void reload() {
		init();
	}

	private void init() {
		map = new HashMap<String, JSONObject>();
	}

	public static TranslationService getInstance() {
		if (instance == null) {
			instance = new TranslationService();
		}

		return instance;
	}

	public void loadTraslation(final String lang) {
		final String path = Settings.getInstance().getRootPath();
		final String file = path + "translations" + File.separator + lang + ".json";
		try {
			final JSONObject tr = new JSONObject(FileUtils.getContents(file));
			map.put(lang, tr);
		} catch (final JSONException ex) {
			Log.CMDBUILD.error("Can't read translation", ex);
			map.put(lang, new JSONObject());
		}
	}

	public Map<String, String> getTranslationList() {
		final String path = Settings.getInstance().getRootPath();
		final File dir = new File(path + "translations");

		final Map<String, String> list = new Hashtable<String, String>();
		stream(dir.listFiles()) //
				.filter(input -> {
					final boolean isFile = input.isFile();
					if (!isFile) {
						Log.CMDBUILD.warn("'{}' is not a file", input);
					}
					return isFile;
				}) //
				.filter(input -> {
					final boolean maybeJson = input.getName().endsWith(".json");
					if (!maybeJson) {
						Log.CMDBUILD.warn("'{}' misses 'json' extension", input);
					}
					return maybeJson;
				}) //
				.forEach(input -> {
					final int pos = input.getName().indexOf(".json");
					final String lang = input.getName().substring(0, pos);
					final String description = getTranslation(lang, "description");
					list.put(lang, description);
				});
		return list;
	}

	public JSONObject getTranslationObject(final String lang) {
		if (!map.containsKey(lang)) {
			loadTraslation(lang);
		}
		return map.get(lang);
	}

	public String getTranslation(final String lang, final String key) {
		try {
			JSONObject json = getTranslationObject(lang);
			final StringTokenizer tokenizer = new StringTokenizer(key, ".");
			while (tokenizer.hasMoreTokens()) {
				final String token = tokenizer.nextToken();
				if (tokenizer.hasMoreTokens()) {
					json = json.getJSONObject(token);
				} else {
					return json.getString(token);
				}
			}
		} catch (final Exception e) {
			Log.CMDBUILD.error("Error translating: " + key, e);
		}
		// translation not found
		return "[" + lang + "." + key + "]";
	}

}
