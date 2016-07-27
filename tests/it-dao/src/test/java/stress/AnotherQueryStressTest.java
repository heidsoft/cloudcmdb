package stress;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.mockito.Mockito.mock;
import static utils.IntegrationTestUtils.NO_PARENT;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newSuperClass;
import static utils.IntegrationTestUtils.newTextAttribute;
import static utils.IntegrationTestUtils.withIdentifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.DataViewRowAndColumnPrivilegeFetcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;

import utils.IntegrationTestBase;
import utils.LoggingSupport;
import utils.ProfilerRule;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AnotherQueryStressTest extends IntegrationTestBase {

	@Rule
	public ProfilerRule testRule = new ProfilerRule();

	private static final Logger logger = LoggingSupport.logger;

	private static final String NAMESPACE = null;
	private static final String ROOT_CLASS_NAME = AnotherQueryStressTest.class.getSimpleName();

	private static final int DEFAULT_TIMEOUT = 300;

	private static final int LEVELS = 5;
	private static final int CLASSES_PER_LEVEL = 3;
	private static final double SUPER_CLASSES_RATIO = 0.7;
	private static final int TOTAL_CARDS = 1000;

	private static final Random random = new Random();

	private DBClass rootClass;
	private int classIndex;

	private final Map<CMClass, String> attributeNames = Maps.newHashMap();

	@Override
	protected DBDriver createTestDriver() {
		return super.createBaseDriver();
	}

	@Before
	public void createTestDataIfNeeded() throws Exception {
		final DBDriver pgDriver = dbDriver();
		rootClass = pgDriver.findClass(ROOT_CLASS_NAME, NAMESPACE);
		if (rootClass == null) {
			rootClass = dbDataView().create(newSuperClass(withIdentifier(ROOT_CLASS_NAME, NAMESPACE), NO_PARENT));
			createClassStructure(rootClass, 0);
			createCards();
		}
	}

	private void createClassStructure(final DBClass parent, final int level) {
		logger.info("creating class structure under class '{}' at level '{}'", parent.getName(), level);
		if (level < LEVELS) {
			for (int classIndex = 0; classIndex < CLASSES_PER_LEVEL; classIndex++) {
				final String className = ROOT_CLASS_NAME + "_" + level + "_" + classIndex + "_" + this.classIndex++;
				/*
				 * at least one superclass and one standard class
				 */
				final boolean isSuperclass = ((classIndex == 0) ? 0.0 : (classIndex == 1) ? 1.0 : Math.random()) < SUPER_CLASSES_RATIO;
				final DBClass currentClass;
				if (isSuperclass) {
					currentClass = dbDataView().create(newSuperClass(withIdentifier(className, NAMESPACE), parent));
					createClassStructure(currentClass, level + 1);
				} else {
					currentClass = dbDataView().create(newClass(withIdentifier(className, NAMESPACE), parent));
					final String attributeName = Integer.toString(this.classIndex);
					dbDataView().createAttribute(newTextAttribute(attributeName, currentClass));
					attributeNames.put(currentClass, attributeName);
				}
			}
		}
	}

	private void createCards() {
		logger.info("creating cards");
		final List<? extends CMClass> classes = Lists.newArrayList(rootClass.getLeaves());
		for (int cardIndex = 0; cardIndex < TOTAL_CARDS; cardIndex++) {
			final int classIndex = random.nextInt(classes.size());
			final CMClass currentClass = classes.get(classIndex);
			logger.info("creating card with index '{}' for class '{}'", cardIndex, currentClass.getName());
			dbDataView().createCardFor(currentClass) //
					.setCode(Integer.toString(cardIndex)) //
					.setDescription(Integer.toString(cardIndex)) //
					.set(attributeNames.get(currentClass), "foo") //
					.save();
		}
	}

	@Test
	public void mustBeTheFirstTest() throws Exception {
		// nothing to do
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void simpleQueryWithNoConditions() throws Exception {
		// when
		dbDataView().select(anyAttribute(rootClass)) //
				.from(rootClass) //
				.limit(20) //
				.run();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void simpleQueryWithOneConditionOnRandomSubclass() throws Exception {
		// given
		final List<DBClass> leaves = Lists.newArrayList(rootClass.getLeaves());
		final CMClass subclass = leaves.get(random.nextInt(leaves.size()));
		final String attributeName = fistNonPredefinedAttributeNameOf(subclass);

		// when
		dbDataView().select(anyAttribute(rootClass)) //
				.from(rootClass) //
				.where(condition(attribute(subclass, attributeName), eq("foo"))) //
				.limit(20) //
				.run();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void simpleQueryWithMoreConditionsOnRandomSubclasses() throws Exception {
		// given
		final List<DBClass> leaves = Lists.newArrayList(rootClass.getLeaves());
		final CMClass subclass1 = leaves.get(random.nextInt(leaves.size()));
		final String attributeName1 = fistNonPredefinedAttributeNameOf(subclass1);
		final CMClass subclass2 = leaves.get(random.nextInt(leaves.size()));
		final String attributeName2 = fistNonPredefinedAttributeNameOf(subclass2);
		final CMClass subclass3 = leaves.get(random.nextInt(leaves.size()));
		final String attributeName3 = fistNonPredefinedAttributeNameOf(subclass3);

		// when
		dbDataView().select(anyAttribute(rootClass)) //
				.from(rootClass) //
				.where(or( //
						condition(attribute(subclass1, attributeName1), eq("foo")), //
						and( //
						condition(attribute(subclass2, attributeName2), eq("foo")), //
								condition(attribute(subclass3, attributeName3), eq("foo")) //
						) //
				)) //
				.limit(20) //
				.run();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void simpleQueryOnFirstLeafWithSingleCondition() throws Exception {
		// given
		final CMClass target = rootClass.getLeaves().iterator().next();
		final String attributeName = fistNonPredefinedAttributeNameOf(target);

		// when
		dbDataView().select(anyAttribute(target)) //
				.from(target) //
				.where(condition(attribute(target, attributeName), eq("foo"))) //
				.limit(20) //
				.run();
	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void simpleQueryWithSingleConditionUsingUserDataView() throws Exception {
		// given
		final PrivilegeContext privilegeContext = new SystemPrivilegeContext();
		final RowAndColumnPrivilegeFetcher rowPrivilegeFetcher = new DataViewRowAndColumnPrivilegeFetcher( //
				dbDataView(), //
				privilegeContext, //
				mock(UserStore.class) //
		);
		final CMDataView userDataView = new UserDataView( //
				dbDataView(), //
				privilegeContext, rowPrivilegeFetcher, //
				operationUser() //
		);
		final List<DBClass> leaves = Lists.newArrayList(rootClass.getLeaves());
		final CMClass subclass = leaves.get(random.nextInt(leaves.size()));
		final String attributeName = fistNonPredefinedAttributeNameOf(subclass);

		// when
		userDataView.select(anyAttribute(rootClass)) //
				.from(rootClass) //
				.where(condition(attribute(subclass, attributeName), eq("foo"))) //
				.limit(20) //
				.run();
	}

	/*
	 * Utilities
	 */

	private String fistNonPredefinedAttributeNameOf(final CMClass subclass) {
		return from(subclass.getAllAttributes()) //
				.filter(NonPredefinedAttributes.of(subclass)) //
				.first() //
				.get() //
				.getName();
	}

	private static class NonPredefinedAttributes implements Predicate<CMAttribute> {

		public static NonPredefinedAttributes of(final CMClass type) {
			return new NonPredefinedAttributes(type);
		}

		private final Collection<String> attributes;

		public NonPredefinedAttributes(final CMClass type) {
			this.attributes = Arrays.asList( //
					type.getCodeAttributeName(), //
					type.getDescriptionAttributeName(), //
					"Notes" //
			);
		}

		@Override
		public boolean apply(final CMAttribute input) {
			return !attributes.contains(input.getName());
		}

	}

}
