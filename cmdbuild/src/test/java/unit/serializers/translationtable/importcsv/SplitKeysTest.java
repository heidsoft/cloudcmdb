package unit.serializers.translationtable.importcsv;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.bouncycastle.util.Strings;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.junit.Test;

import com.google.common.collect.Maps;

public class SplitKeysTest {

	@Test
	public void decodeClassType() throws Exception {
		// given
		final Map<String, Object> record = Maps.newHashMap();
		record.put("identifier", "class.Application.description");
		record.put("description", "description of the class 'Application'");
		record.put("default", "Application");
		record.put("it", "Applicazione");
		record.put("ja", "アプリケーション");

		// when
		final String identifier = (String) record.get("identifier");
		final String type = Strings.split(identifier, '.')[0];
		final TranslatableElement elementType = TranslatableElement.of(type);

		// then
		assertTrue(elementType == TranslatableElement.CLASS);
	}

	@Test
	public void decodeAttributeClassType() throws Exception {
		// given
		final Map<String, Object> record = Maps.newHashMap();
		record.put("identifier", "attributeclass.Application.Code.description");

		// when
		final String identifier = (String) record.get("identifier");
		final String type = Strings.split(identifier, '.')[0];
		final TranslatableElement elementType = TranslatableElement.of(type);

		// then
		assertTrue(elementType == TranslatableElement.ATTRIBUTECLASS);
	}

	@Test
	public void decodeDomainClassType() throws Exception {
		// given
		final Map<String, Object> record = Maps.newHashMap();
		record.put("identifier", "domain.ApplicationComponent.directDescription");

		// when
		final String identifier = (String) record.get("identifier");
		final String type = Strings.split(identifier, '.')[0];
		final TranslatableElement elementType = TranslatableElement.of(type);

		// then
		assertTrue(elementType == TranslatableElement.DOMAIN);
	}

}
