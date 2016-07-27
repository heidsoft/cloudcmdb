package unit.logic.taskmanager.task.email.mapper;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.task.email.mapper.EngineBasedMapper;
import org.cmdbuild.logic.taskmanager.task.email.mapper.EngineBasedMapper.Builder;
import org.cmdbuild.logic.taskmanager.task.email.mapper.KeyValueMapperEngine;
import org.cmdbuild.logic.taskmanager.task.email.mapper.Mapper;
import org.cmdbuild.logic.taskmanager.task.email.mapper.MapperEngine;
import org.junit.Test;

public class EngineBasedMapperTest {

	@Test(expected = NullPointerException.class)
	public void textIsMandatory() throws Exception {
		// given
		final MapperEngine engine = mock(MapperEngine.class);
		final Builder builder = EngineBasedMapper.newInstance() //
				.withEngine(engine);

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void engineIsMandatory() throws Exception {
		// given
		final Builder builder = EngineBasedMapper.newInstance() //
				.withText(null);

		// when
		builder.build();
	}

	@Test
	public void textCanBeEmpty() throws Exception {
		// given
		final MapperEngine engine = mock(MapperEngine.class);
		final Builder builder = EngineBasedMapper.newInstance() //
				.withText(EMPTY) //
				.withEngine(engine);

		// when
		builder.build();
	}

	@Test
	public void textCanBeBlank() throws Exception {
		// given
		final MapperEngine engine = mock(MapperEngine.class);
		final Builder builder = EngineBasedMapper.newInstance() //
				.withText(" \t") //
				.withEngine(engine);

		// when
		builder.build();
	}

	@Test
	public void lineFeedHandledCorrectly() throws Exception {
		// given
		final String text = EMPTY //
				+ "<key_init>foo</key_end> ... <value_init>F\nO\nO</value_end>   \n" //
				+ "<key_init>bar</key_end><value_init>B\r\nA\r\nR</value_end>...\n" //
				+ EMPTY;
		final Mapper processor = EngineBasedMapper.newInstance() //
				.withText(text) //
				.withEngine(KeyValueMapperEngine.newInstance() //
						.withKey("<key_init>", "</key_end>") //
						.withValue("<value_init>", "</value_end>") //
						.build()) //
				.build();

		// when
		final Map<String, String> processed = processor.map();

		// then
		assertThat(processed, hasEntry("foo", "F\nO\nO"));
		assertThat(processed, hasEntry("bar", "B\r\nA\r\nR"));
	}

	@Test
	public void keyValueTextSuccessfullyProcessed() throws Exception {
		// given
		final String text = EMPTY //
				+ "<key_init>foo</key_end> ... <value_init>FOO</value_end>   \n" //
				+ "... <key_init>bar</key_end><value_init>BAR</value_end>\n" //
				+ "<key_init>baz</key_end><value_init>BAZ</value_end>...\n" //
				+ EMPTY;
		final Mapper processor = EngineBasedMapper.newInstance() //
				.withText(text) //
				.withEngine(KeyValueMapperEngine.newInstance() //
						.withKey("<key_init>", "</key_end>") //
						.withValue("<value_init>", "</value_end>") //
						.build()) //
				.build();

		// when
		final Map<String, String> processed = processor.map();

		// then
		assertThat(processed, hasEntry("foo", "FOO"));
		assertThat(processed, hasEntry("bar", "BAR"));
		assertThat(processed, hasEntry("baz", "BAZ"));
	}

	@Test
	public void missingKeySkipsToNextIfPossible() throws Exception {
		// given
		final String text = EMPTY //
				+ "<key_init>foo</key_end><value_init>FOO</value_end>" //
				+ "<value_init>BAR</value_end>" //
				+ "<key_init>baz</key_end><value_init>BAZ</value_end>" //
				+ "<value_init>LOL</value_end>" //
				+ EMPTY;
		final Mapper processor = EngineBasedMapper.newInstance() //
				.withText(text) //
				.withEngine(KeyValueMapperEngine.newInstance() //
						.withKey("<key_init>", "</key_end>") //
						.withValue("<value_init>", "</value_end>") //
						.build()) //
				.build();

		// when
		final Map<String, String> processed = processor.map();

		// then
		assertThat(processed.size(), equalTo(2));
		assertThat(processed, hasEntry("foo", "FOO"));
		assertThat(processed, hasEntry("baz", "BAZ"));
	}

	@Test
	public void missingValueSkipsToNextIfPossible() throws Exception {
		// given
		final String text = EMPTY //
				+ "<key_init>foo</key_end><value_init>FOO</value_end>" //
				+ "<key_init>bar</key_end>" //
				+ "<key_init>baz</key_end><value_init>BAZ</value_end>" //
				+ "<key_init>lol</key_end>" //
				+ EMPTY;
		final Mapper processor = EngineBasedMapper.newInstance() //
				.withText(text) //
				.withEngine(KeyValueMapperEngine.newInstance() //
						.withKey("<key_init>", "</key_end>") //
						.withValue("<value_init>", "</value_end>") //
						.build()) //
				.build();

		// when
		final Map<String, String> processed = processor.map();

		// then
		assertThat(processed.size(), equalTo(2));
		assertThat(processed, hasEntry("foo", "FOO"));
		assertThat(processed, hasEntry("baz", "BAZ"));
	}

}
