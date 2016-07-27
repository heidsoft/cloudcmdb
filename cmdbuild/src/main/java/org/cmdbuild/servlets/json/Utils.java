package org.cmdbuild.servlets.json;

import static java.lang.Long.MAX_VALUE;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.cmdbuild.servlets.json.CommunicationConstants.MODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOT_NEGATIVES;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOT_POSITIVES;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.UUID;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils extends JSONBaseWithSpringContext {

	@JSONExported
	@Unauthorized
	public String getTranslationObject() {
		final String lang = languageStore().getLanguage();
		final String transFile = TranslationService.getInstance().getTranslationObject(lang).toString();
		return "CMDBuild.Translation = " + transFile;
	}

	@JSONExported
	@Unauthorized
	public JSONObject getDefaultLanguage() throws JSONException {
		final JSONObject j = new JSONObject();

		j.put("language", cmdbuildConfiguration().getLanguage());

		return j;
	}

	@JSONExported
	@Unauthorized
	public JSONObject listAvailableTranslations(final JSONObject serializer) throws JSONException {

		final Map<String, String> trs = TranslationService.getInstance().getTranslationList();

		for (final String lang : trs.keySet()) {
			final JSONObject j = new JSONObject();
			j.put("tag", lang);
			j.put("description", trs.get(lang));
			serializer.append("translations", j);
		}

		return serializer;
	}

	@JSONExported
	@Unauthorized
	public void success() throws JSONException {
	}

	/**
	 * @param exceptionType
	 * @param exceptionCodeString
	 */
	@JSONExported
	@Unauthorized
	public void failure(@Parameter("type") final String exceptionType,
			@Parameter(value = "code", required = false) final String exceptionCodeString) {
		try {
			final Class<? extends CMDBException> classDefinition = Class.forName(
					"org.cmdbuild.exception." + exceptionType).asSubclass(CMDBException.class);
			if (exceptionCodeString == null) {
				final Constructor<? extends CMDBException> constructorDefinition = classDefinition
						.getDeclaredConstructor();
				throw constructorDefinition.newInstance();
			} else {
				for (final Class<?> subClass : classDefinition.getClasses()) {
					if (subClass.isEnum()) {
						for (final Object enumConst : subClass.getEnumConstants()) {
							if (exceptionCodeString.equals(enumConst.toString())) {
								final Constructor<? extends CMDBException> constructorDefinition = classDefinition
										.getDeclaredConstructor(enumConst.getClass());
								throw constructorDefinition.newInstance(enumConst);
							}
						}
					}
				}
			}
		} catch (final CMDBException ex) {
			throw ex;
		} catch (final Exception ex) {
			// Returns success if no error can be instantiated
		}
	}

	@JSONExported
	@Admin
	public void clearCache() {
		cachingLogic().clearCache();
	}

	private static enum GenerateIdMode {

		NUMERIC, //
		TEXT, //
		;

		public static GenerateIdMode of(final String s) {
			for (final GenerateIdMode value : values()) {
				if (value.name().equalsIgnoreCase(trim(s))) {
					return value;
				}
			}
			return NUMERIC;
		}

	}

	@JSONExported
	public JsonResponse generateId( //
			@Parameter(value = MODE, required = false) final String mode, //
			@Parameter(value = NOT_POSITIVES, required = false) final boolean notPositives, //
			@Parameter(value = NOT_NEGATIVES, required = false) final boolean notNegatives //
	) {
		final Object generated;
		final GenerateIdMode _mode = GenerateIdMode.of(mode);
		switch (_mode) {
		default:
		case NUMERIC:
			final boolean negative;
			if (notPositives && notNegatives) {
				throw new IllegalArgumentException("positives and negatives both not allowed");
			} else if (notPositives && !notNegatives) {
				negative = true;
			} else if (!notPositives && notNegatives) {
				negative = false;
			} else {
				negative = toBoolean(nextInt(0, 2));
			}
			final long l = nextLong(0, MAX_VALUE) + 1;
			generated = negative ? -l : l;
			break;

		case TEXT:
			generated = UUID.randomUUID().toString();
			break;
		}
		return JsonResponse.success(generated);
	}
}
