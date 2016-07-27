package unit.common.collect;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class ChainablePutMapTest {

	private Map<String, String> delegate;
	private ChainablePutMap<String, String> chainablePutMap;

	@Before
	public void setUp() throws Exception {
		delegate = Maps.newHashMap();
		chainablePutMap = ChainablePutMap.of(delegate);
	}

	@Test
	public void chainablePutWorksInTheSameWayOfStandardPut() throws Exception {
		// when
		chainablePutMap //
				.chainablePut("foo", "foo") //
				.chainablePut("bar", "bar");

		// then
		assertThat(delegate, hasEntry("foo", "foo"));
		assertThat(delegate, hasEntry("bar", "bar"));
	}

	@Test
	public void chainablePutAllWorksInTheSameWayOfStandardPutAll() throws Exception {
		// given
		final Map<String, String> source = Maps.newHashMap();
		source.put("foo", "foo");
		source.put("bar", "bar");

		// when
		chainablePutMap //
				.chainablePutAll(source) //
				.chainablePut("baz", "baz");

		// then
		assertThat(delegate, hasEntry("foo", "foo"));
		assertThat(delegate, hasEntry("bar", "bar"));
		assertThat(delegate, hasEntry("baz", "baz"));
	}

}
