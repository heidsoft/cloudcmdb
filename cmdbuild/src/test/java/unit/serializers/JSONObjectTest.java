package unit.serializers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class JSONObjectTest {

	@Test
	public void testGetLong() throws JSONException {
		final String key = "key";
		final JSONObject obj = new JSONObject();
		obj.put(key, "");
		try {
			final Long value = obj.getLong(key);
			fail("The field " + key + " is not a long");
		} catch (final JSONException e) {
			assertEquals("", obj.get(key));
		}
	}

}
