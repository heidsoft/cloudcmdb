package unit.template.engine;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.junit.Test;

public class EngineBasedTemplateResolverTest {

	@Test
	public void nullStringResolvedAsNull() throws Exception {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance().build();

		// when
		final String value = tr.resolve(null);

		// then
		assertThat(value, is(nullValue()));
	}

	@Test
	public void resolvedAsNullWhenEngineIsMissing() throws Exception {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("param", 42), "e1") //
				.build();

		// when
		final String value = tr.resolve("XXX {e0:param} {e1:param}");

		// then
		assertThat(value, equalTo("XXX null 42"));
	}

	@Test
	public void resolvesAsNullWhenExpressionEvaluationWasUnsuccessful() throws Exception {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("param", "value"), "e1") //
				.build();

		// when
		final String value = tr.resolve("{e1:missing}");

		// then
		assertThat(value, equalTo("null"));
	}

	@Test
	public void multipleEnginesCanBeUsedWithinSameExpression() throws Exception {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("stringParam", "string param"), "e1") //
				.withEngine(engineWithParam("integerParam", Integer.valueOf(42)), "e2") //
				.build();

		// when
		final String value = tr.resolve("{e1:stringParam} -> {e2:integerParam}");

		// then
		assertThat(value, equalTo("string param -> 42"));
	}

	@Test
	public void variableNamesCanContainSpacesAndSpecialCharactersButNotCurlyBraces() throws Exception {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("this can BE a (variable]", "42"), "e1") //
				.build();

		// when
		final String value = tr.resolve("{e1:this can BE a (variable]}");

		// then
		assertThat(value, equalTo("42"));
	}

	@Test
	public void expressionWithNoTemplatesIsNotChanged() throws Exception {
		// given
		final String template = "A simple string";
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance().build();

		// when
		final String value = tr.resolve(template);

		// then
		assertThat(value, equalTo(template));
	}

	@Test
	public void nonTemplateTextWithinExpressionIsNotChanged() throws Exception {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("param", 42), "e1") //
				.build();

		// when
		final String value = tr.resolve("foo {e1:param} bar");

		// then
		assertThat(value, equalTo("foo 42 bar"));
	}

	@Test
	public void templateCanBeTheVariableOfAnotherTemplate() throws Exception {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("value", 42), "e0") //
				.withEngine(engineWithParam("param", "value"), "e1") //
				.build();

		// when
		final String value = tr.resolve("foo {e0:{e1:param}} bar");

		// then
		assertThat(value, equalTo("foo 42 bar"));
	}

	@Test
	public void backslashesAndDollarSignsCanBeUsed() throws Exception {
		// given
		final EngineBasedTemplateResolver tr = EngineBasedTemplateResolver.newInstance() //
				.withEngine(engineWithParam("contains_backslash", "foo \\ bar"), "e0") //
				.withEngine(engineWithParam("contains_dollar_sign", "baz$"), "e1") //
				.build();

		// when
		final String value = tr.resolve("{e0:contains_backslash} {e1:contains_dollar_sign}");

		// then
		assertThat(value, equalTo("foo \\ bar baz$"));
	}

	/*
	 * Utilities
	 */

	private Engine engineWithParam(final String name, final Object value) {
		final Engine engine = mock(Engine.class);
		when(engine.eval(name)).thenReturn(value);
		return engine;
	}

}
