package integration.dao;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.eq;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.networkContained;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.networkContainedOrEqual;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.networkContains;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.networkContainsOrEqual;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.networkRelationed;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.condition;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newAttribute;
import static utils.IntegrationTestUtils.newClass;

import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType.Type;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.where.OperatorAndValue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

public class NetworkAttributeTypeQueryTest extends IntegrationTestBase {

	private class HasNoRecords extends TypeSafeMatcher<CMQueryResult> {

		@Override
		protected boolean matchesSafely(final CMQueryResult item) {
			return item.isEmpty();
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("has no records");
		}

		@Override
		protected void describeMismatchSafely(final CMQueryResult item, final Description description) {
			description.appendText("has records");
		}

	}

	private class OnlyRecord extends TypeSafeMatcher<CMQueryResult> {

		private final Matcher<Object> matcher;

		public OnlyRecord(final Matcher<Object> matcher) {
			this.matcher = matcher;
		}

		@Override
		protected boolean matchesSafely(final CMQueryResult item) {
			final Object object = item.getOnlyRow().getCard(clazz).get(ATTRIBUTE_NAME);
			return matcher.matches(object);
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("matching " + matcher);
		}

		@Override
		protected void describeMismatchSafely(final CMQueryResult item, final Description description) {
			description.appendText("not matching " + matcher);
		}

	}

	private static final String CLASS_NAME = "foo";
	private static final String ATTRIBUTE_NAME = "bar";
	private static final Object HOST_WITHOUT_CLASS = "192.168.1.1";
	private static final Object HOST = HOST_WITHOUT_CLASS + "/32";
	private static final Object ANOTHER_HOST = "192.168.1.2/32";
	private static final Object SUBNET_24 = "192.168.1.0/24";
	private static final Object SUBNET_16 = "192.168.0.0/16";
	private static final Object ANOTHER_SUBNET_16 = "192.167.0.0/16";
	private static final Object SUBNET_8 = "192.0.0.0/8";

	private static final IpAddressAttributeType ATTRIBUTE_TYPE = new IpAddressAttributeType(Type.IPV4);

	private DBClass clazz;

	@Before
	public void createData() throws Exception {
		clazz = dbDataView().create(newClass(CLASS_NAME));
		dbDataView().createAttribute(newAttribute(ATTRIBUTE_NAME, ATTRIBUTE_TYPE, clazz));
	}

	private void cardWhereNetworkIs(final Object value) {
		dbDataView().createCardFor(clazz) //
				.set(ATTRIBUTE_NAME, value) //
				.save();
	}

	private CMQueryResult queryCardsWhere(final OperatorAndValue operatorAndValue) {
		return dbDataView() //
				.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(condition(attribute(clazz, ATTRIBUTE_NAME), operatorAndValue)) //
				.run();
	}

	private Matcher<CMQueryResult> isEmpty() {
		return new HasNoRecords();
	}

	private Matcher<CMQueryResult> hasOneCard(final Matcher<Object> matcher) {
		return new OnlyRecord(matcher);
	}

	@Test
	public void hostStrippedFromClassIf_32() throws Exception {
		// given
		cardWhereNetworkIs(HOST);

		// when/then
		assertThat(queryCardsWhere(eq(HOST)), hasOneCard(equalTo(HOST_WITHOUT_CLASS)));
	}

	@Test
	public void host() throws Exception {
		// given
		cardWhereNetworkIs(HOST);

		// when/then
		assertThat(queryCardsWhere(eq(HOST)), hasOneCard(equalTo(HOST_WITHOUT_CLASS)));
		assertThat(queryCardsWhere(eq(HOST_WITHOUT_CLASS)), hasOneCard(equalTo(HOST_WITHOUT_CLASS)));
		assertThat(queryCardsWhere(eq(ANOTHER_HOST)), isEmpty());
		assertThat(queryCardsWhere(eq(SUBNET_24)), isEmpty());
		assertThat(queryCardsWhere(networkContains(HOST)), isEmpty());
		assertThat(queryCardsWhere(networkContainsOrEqual(HOST)), hasOneCard(equalTo(HOST_WITHOUT_CLASS)));
		assertThat(queryCardsWhere(networkContains(SUBNET_24)), isEmpty());
		assertThat(queryCardsWhere(networkContainsOrEqual(SUBNET_24)), isEmpty());
		assertThat(queryCardsWhere(networkContained(HOST)), isEmpty());
		assertThat(queryCardsWhere(networkContainedOrEqual(HOST)), hasOneCard(equalTo(HOST_WITHOUT_CLASS)));
		assertThat(queryCardsWhere(networkContainedOrEqual(SUBNET_24)), hasOneCard(equalTo(HOST_WITHOUT_CLASS)));
		assertThat(queryCardsWhere(networkRelationed(HOST)), hasOneCard(equalTo(HOST_WITHOUT_CLASS)));
		assertThat(queryCardsWhere(networkRelationed(ANOTHER_HOST)), isEmpty());
		assertThat(queryCardsWhere(networkRelationed(SUBNET_24)), hasOneCard(equalTo(HOST_WITHOUT_CLASS)));
	}

	@Test
	public void subnet() throws Exception {
		// given
		cardWhereNetworkIs(SUBNET_16);

		// when/then
		assertThat(queryCardsWhere(eq(HOST)), isEmpty());
		assertThat(queryCardsWhere(eq(ANOTHER_HOST)), isEmpty());
		assertThat(queryCardsWhere(eq(SUBNET_24)), isEmpty());
		assertThat(queryCardsWhere(eq(SUBNET_16)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(eq(SUBNET_8)), isEmpty());
		assertThat(queryCardsWhere(networkContains(HOST)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkContainsOrEqual(HOST)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkContains(SUBNET_24)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkContainsOrEqual(SUBNET_24)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkContains(SUBNET_16)), isEmpty());
		assertThat(queryCardsWhere(networkContainsOrEqual(SUBNET_16)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkContains(ANOTHER_SUBNET_16)), isEmpty());
		assertThat(queryCardsWhere(networkContainsOrEqual(ANOTHER_SUBNET_16)), isEmpty());
		assertThat(queryCardsWhere(networkContains(SUBNET_8)), isEmpty());
		assertThat(queryCardsWhere(networkContainsOrEqual(SUBNET_8)), isEmpty());
		assertThat(queryCardsWhere(networkContained(HOST)), isEmpty());
		assertThat(queryCardsWhere(networkContainedOrEqual(HOST)), isEmpty());
		assertThat(queryCardsWhere(networkContained(SUBNET_24)), isEmpty());
		assertThat(queryCardsWhere(networkContainedOrEqual(SUBNET_24)), isEmpty());
		assertThat(queryCardsWhere(networkContained(SUBNET_16)), isEmpty());
		assertThat(queryCardsWhere(networkContainedOrEqual(SUBNET_16)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkContained(ANOTHER_SUBNET_16)), isEmpty());
		assertThat(queryCardsWhere(networkContainedOrEqual(ANOTHER_SUBNET_16)), isEmpty());
		assertThat(queryCardsWhere(networkContained(SUBNET_8)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkContainedOrEqual(SUBNET_8)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkRelationed(HOST)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkRelationed(SUBNET_24)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkRelationed(SUBNET_16)), hasOneCard(equalTo(SUBNET_16)));
		assertThat(queryCardsWhere(networkRelationed(ANOTHER_SUBNET_16)), isEmpty());
		assertThat(queryCardsWhere(networkRelationed(SUBNET_8)), hasOneCard(equalTo(SUBNET_16)));
	}

}
