package unit.logic.data.access.filter.json;

import static org.cmdbuild.logic.data.access.filter.model.Elements.all;
import static org.cmdbuild.logic.data.access.filter.model.Elements.attribute;
import static org.cmdbuild.logic.data.access.filter.model.Elements.oneOf;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.and;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.contains;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.endsWith;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.equalTo;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.greaterThan;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.in;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.isNull;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.lessThan;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.like;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.not;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.startsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.data.access.filter.json.JsonParser;
import org.cmdbuild.logic.data.access.filter.model.BuildableFilter;
import org.cmdbuild.logic.data.access.filter.model.BuildableFilter.Builder;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.logic.data.access.filter.model.Filter;
import org.junit.Test;

public class JsonParserTest {

	@Test
	public void attributeWithSimpleConditionAndContainOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"contain\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", contains("bar")))));
	}

	@Test
	public void attributeWithSimpleConditionAndEndOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"end\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", endsWith("bar")))));
	}

	@Test
	public void attributeWithSimpleConditionAndEqualOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"equal\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", equalTo("bar")))));
	}

	@Test
	public void attributeWithSimpleConditionAndGreaterOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"greater\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", greaterThan("bar")))));
	}

	@Test
	public void attributeWithSimpleConditionAndInOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"in\"," //
				+ "            \"value\": [\"bar\", \"baz\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", in("bar", "baz")))));
	}

	@Test
	public void attributeWithSimpleConditionAndIsNullOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"isnull\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", isNull()))));
	}

	@Test
	public void attributeWithSimpleConditionAndLowerOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"less\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", lessThan("bar")))));
	}

	@Test
	public void attributeWithSimpleConditionAndLikeOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"like\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", like("bar")))));
	}

	@Test
	public void attributeWithSimpleConditionAndBeginOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"begin\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", startsWith("bar")))));
	}

	@Test
	public void attributeWithSimpleConditionAndBetweenOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"between\"," //
				+ "            \"value\": [\"bar\", \"baz\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", and(greaterThan("bar"), lessThan("baz"))))));
	}

	@Test
	public void attributeWithSimpleConditionAndNotBeginOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"notbegin\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", not(startsWith("bar"))))));
	}

	@Test
	public void attributeWithSimpleConditionAndNotContainOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"notcontain\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", not(contains("bar"))))));
	}

	@Test
	public void attributeWithSimpleConditionAndNotEndOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"notend\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", not(endsWith("bar"))))));
	}

	@Test
	public void attributeWithSimpleConditionAndNotEqualOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"notequal\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", not(equalTo("bar"))))));
	}

	@Test
	public void attributeWithSimpleConditionAndIsNotNullOperator() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"isnotnull\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(attribute("foo", not(isNull())))));
	}

	@Test
	public void attributeWithAndCondition() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"and\": [" //
				+ "            {\"simple\": {" //
				+ "                \"attribute\": \"foo\"," //
				+ "                \"operator\": \"equal\"," //
				+ "                \"value\": [\"bar\"]}" //
				+ "            }," //
				+ "            {\"simple\": {" //
				+ "                \"attribute\": \"bar\"," //
				+ "                \"operator\": \"equal\"," //
				+ "                \"value\": [\"baz\"]}" //
				+ "            }" //
				+ "        ]" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(all(attribute("foo", equalTo("bar")), attribute("bar", equalTo("baz"))))));
	}

	@Test
	public void attributeWithOrCondition() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"or\": [" //
				+ "            {\"simple\": {" //
				+ "                \"attribute\": \"foo\"," //
				+ "                \"operator\": \"equal\"," //
				+ "                \"value\": [\"bar\"]}" //
				+ "            }," //
				+ "            {\"simple\": {" //
				+ "                \"attribute\": \"bar\"," //
				+ "                \"operator\": \"equal\"," //
				+ "                \"value\": [\"baz\"]}" //
				+ "            }" //
				+ "        ]" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(filterWith(oneOf(attribute("foo", equalTo("bar")), attribute("bar", equalTo("baz"))))));
	}

	@Test
	public void attributeWithMixedConditions() {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"and\": [" //
				+ "            {\"or\": [" //
				+ "                {\"simple\": {" //
				+ "                    \"attribute\": \"foo\"," //
				+ "                    \"operator\": \"equal\"," //
				+ "                    \"value\": [\"bar\"]}" //
				+ "                }," //
				+ "                {\"and\": [" //
				+ "                    {\"simple\": {" //
				+ "                        \"attribute\": \"bar\"," //
				+ "                        \"operator\": \"notequal\"," //
				+ "                        \"value\": [\"baz\"]}" //
				+ "                    }," //
				+ "                    {\"simple\": {" //
				+ "                        \"attribute\": \"baz\"," //
				+ "                        \"operator\": \"equal\"," //
				+ "                        \"value\": [\"foo\"]}" //
				+ "                    }" //
				+ "                ]}" //
				+ "            ]}," //
				+ "            {\"simple\": {" //
				+ "                \"attribute\": \"bar\"," //
				+ "                \"operator\": \"begin\"," //
				+ "                \"value\": [\"baz\"]}" //
				+ "            }" //
				+ "        ]" //
				+ "    }" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(
				filter,
				is(filterWith(all(
						oneOf(attribute("foo", equalTo("bar")),
								all(attribute("bar", not(equalTo("baz"))), attribute("baz", equalTo("foo")))),
						attribute("bar", startsWith("baz"))))));
	}

	@Test
	public void fullExample() throws Exception {
		// given
		final String json = "{" //
				+ "    \"attribute\": {" //
				+ "        \"simple\": {" //
				+ "            \"attribute\": \"foo\"," //
				+ "            \"operator\": \"equal\"," //
				+ "            \"value\": [\"bar\"]" //
				+ "        }" //
				+ "    }," //
				+ "    \"query\": \"blah blah blah\"" //
				+ "}";
		final JsonParser parser = new JsonParser(json);

		// when
		final Filter filter = parser.parse();

		// then
		assertThat(filter, is(a(filter() //
				.withAttribute(attribute("foo", equalTo("bar"))) //
				.withFullTextQuery("blah blah blah") //
				)));
	}

	private static Filter filterWith(final Element attribute) {
		return filter() //
				.withAttribute(attribute) //
				.build();
	}

	private static Builder filter() {
		return BuildableFilter.newInstance();
	}

	private static Filter a(final Builder builder) {
		return builder.build();
	}

}
