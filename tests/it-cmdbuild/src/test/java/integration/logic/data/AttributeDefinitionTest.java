package integration.logic.data;

import static integration.logic.data.AttributesMatcher.hasAttributeWithName;
import static java.util.Arrays.asList;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_11;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_NN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import static org.cmdbuild.model.data.Attribute.AttributeTypeBuilder.*;
import org.cmdbuild.model.data.ClassOrder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AttributeDefinitionTest extends DataDefinitionLogicTest {

	private static final String CLASS_NAME = "foo";
	private static final String ANOTHER_CLASS_NAME = "bar";

	private static final String ATTRIBUTE_NAME = "bar";
	private static final String ANOTHER_ATTRIBUTE_NAME = "baz";

	private static final String TYPE_THAT_DOES_NOT_REQUIRE_PARAMS = "BOOLEAN";

	private static final String DESCRIPTION = "attribute's description";

	private static final String GROUP = "sample group";

	/**
	 * the previous attributes are Code, Description and Notes
	 */
	private static final int DEFAULT_ATTRIBUTE_INDEX = 4;

	private CMClass testClass;

	@Before
	public void createDefaultTest() throws Exception {
		testClass = dataDefinitionLogic().createOrUpdate(a(newClass(CLASS_NAME)));
	}

	@Test
	public void codeAndDescriptionAreDefaultAttributes() throws Exception {
		// given

		// when
		final Iterable<? extends CMAttribute> attributes = testClass.getAttributes();

		// then
		assertThat(attributes, hasAttributeWithName(CODE_ATTRIBUTE));
		assertThat(attributes, hasAttributeWithName(DESCRIPTION_ATTRIBUTE));
		assertThat(attributes, not(hasAttributeWithName("SurelyMissing")));
	}

	@Test
	public void codeAndDescriptionAreNotInheritedInClassesWithoutParent() throws Exception {
		// given

		// when
		final CMAttribute code = dataView().findClass(CLASS_NAME).getAttribute(CODE_ATTRIBUTE);
		final CMAttribute description = dataView().findClass(CLASS_NAME).getAttribute(DESCRIPTION_ATTRIBUTE);

		// then
		assertThat(code.isInherited(), equalTo(false));
		assertThat(description.isInherited(), equalTo(false));
	}

	@Test
	public void codeAndDescriptionAreInheritedInSubclasses() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newClass(ANOTHER_CLASS_NAME).withParent(testClass.getId())));

		// when
		final CMClass anotherClass = dataView().findClass(ANOTHER_CLASS_NAME);
		final CMAttribute code = anotherClass.getAttribute(CODE_ATTRIBUTE);
		final CMAttribute description = anotherClass.getAttribute(DESCRIPTION_ATTRIBUTE);

		// then
		assertThat(code.isInherited(), equalTo(true));
		assertThat(description.isInherited(), equalTo(true));
	}

	@Test
	public void codeAttributeIsActiveAsDefault() throws Exception {
		// given

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(CODE_ATTRIBUTE);

		// then
		assertThat(attribute.getName(), equalTo(CODE_ATTRIBUTE));
		assertThat(attribute.getDescription(), equalTo(CODE_ATTRIBUTE));
		assertThat(attribute.isActive(), equalTo(true));
	}

	@Test
	public void descriptionAttributeIsActiveAsDefault() throws Exception {
		// given

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(DESCRIPTION_ATTRIBUTE);

		// then
		assertThat(attribute.getName(), equalTo(DESCRIPTION_ATTRIBUTE));
		assertThat(attribute.getDescription(), equalTo(DESCRIPTION_ATTRIBUTE));
		assertThat(attribute.isActive(), equalTo(true));
	}

	@Test
	public void booleanAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("BOOLEAN")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(BooleanAttributeType.class));
	}

	@Test
	public void charAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("CHAR")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(CharAttributeType.class));
	}

	@Test
	public void dateAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("DATE")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(DateAttributeType.class));
	}

	@Test
	public void decimalAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("DECIMAL") //
						.withPrecision(5) //
						.withScale(2)));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(DecimalAttributeType.class));

		final DecimalAttributeType decimalAttributeType = (DecimalAttributeType) attribute.getType();
		assertThat(decimalAttributeType.scale, equalTo(2));
		assertThat(decimalAttributeType.precision, equalTo(5));
	}

	@Test
	public void doubleAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("DOUBLE")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(DoubleAttributeType.class));
	}

	@Ignore
	@Test
	public void foreignKeyAttributeCreatedAndRead() throws Exception {
		fail("TODO");
	}

	@Test
	public void ipAddressAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("INET")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(IpAddressAttributeType.class));
	}

	@Test
	public void integerAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("INTEGER")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(IntegerAttributeType.class));
	}

	@Ignore
	@Test
	public void linestringAttributeCreatedAndRead() throws Exception {
		fail("TODO");
	}

	@Test
	public void lookupAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("LOOKUP") //
						.withLookupType("AlfrescoCategory")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(LookupAttributeType.class));

		final LookupAttributeType lookupAttributeType = (LookupAttributeType) attribute.getType();
		assertThat(lookupAttributeType.getLookupTypeName(), equalTo("AlfrescoCategory"));
	}

	@Ignore
	@Test
	public void pointAttributeCreatedAndRead() throws Exception {
		fail("TODO");
	}

	@Ignore
	@Test
	public void polygonAttributeCreatedAndRead() throws Exception {
		fail("TODO");
	}

	@Test
	public void referenceAttributeCreatedAndRead() throws Exception {
		// given
		final CMClass anotherClass = dataDefinitionLogic().createOrUpdate(a(newClass(ANOTHER_CLASS_NAME)));
		final CMDomain domain = dataDefinitionLogic().create(a(newDomain("domain") //
				.withIdClass1(testClass.getId()) //
				.withIdClass2(anotherClass.getId()) //
				.withCardinality(CARDINALITY_N1.value()) //
				));
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("REFERENCE") //
						.withDomain(domain.getIdentifier().getLocalName())));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(ReferenceAttributeType.class));

		final ReferenceAttributeType referenceAttributeType = (ReferenceAttributeType) attribute.getType();
		assertThat(referenceAttributeType.getDomainName(), equalTo(domain.getIdentifier().getLocalName()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateReferenceForDomainWithCardinality_1_1() throws Exception {
		final CMClass anotherClass = dataDefinitionLogic().createOrUpdate(a(newClass(ANOTHER_CLASS_NAME)));
		final CMDomain domain = dataDefinitionLogic().create(a(newDomain("domain") //
				.withIdClass1(testClass.getId()) //
				.withIdClass2(anotherClass.getId()) //
				.withCardinality(CARDINALITY_11.value()) //
				));
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("REFERENCE") //
						.withDomain(domain.getIdentifier().getLocalName())));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateReferenceForDomainWithCardinality_N_N() throws Exception {
		final CMClass anotherClass = dataDefinitionLogic().createOrUpdate(a(newClass(ANOTHER_CLASS_NAME)));
		final CMDomain domain = dataDefinitionLogic().create(a(newDomain("domain") //
				.withIdClass1(testClass.getId()) //
				.withIdClass2(anotherClass.getId()) //
				.withCardinality(CARDINALITY_NN.value()) //
				));
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("REFERENCE") //
						.withDomain(domain.getIdentifier().getLocalName())));
	}

	@Test
	public void stringAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("STRING") //
						.withLength(42)));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(StringAttributeType.class));

		final StringAttributeType stringAttributeType = (StringAttributeType) attribute.getType();
		assertThat(stringAttributeType.length, equalTo(42));
	}

	@Test
	public void timeAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("TIME")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(TimeAttributeType.class));
	}

	@Test
	public void timestampAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("TIMESTAMP")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(DateTimeAttributeType.class));
	}

	@Test
	public void textAttributeCreatedAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("TEXT")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(TextAttributeType.class));
	}

	@Test
	public void textAttributeCreatedWithHtmlEditorAndRead() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType("TEXT") //
						.withEditorType("HTML")));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute, is(notNullValue(CMAttribute.class)));
		assertThat(attribute.getName(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.getOwner().getIdentifier().getLocalName(), equalTo(CLASS_NAME));
		assertThat(attribute.getType(), instanceOf(TextAttributeType.class));
		assertThat(attribute.getEditorType(), equalTo("HTML"));
	}

	@Test
	public void newlyCreatedAttributeIsActiveAsDefault() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ANOTHER_ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS) //
						.thatIsActive(false)));

		// when
		final CMClass _class = dataView().findClass(CLASS_NAME);

		// then
		assertThat(_class.getAttribute(ATTRIBUTE_NAME).isActive(), equalTo(true));
		assertThat(_class.getAttribute(ANOTHER_ATTRIBUTE_NAME).isActive(), equalTo(false));
	}

	@Test
	public void newlyCreatedAttributeIsNotDisplayableInListAsDefault() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ANOTHER_ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS) //
						.thatIsDisplayableInList(true)));

		// when
		final CMClass _class = dataView().findClass(CLASS_NAME);

		// then
		assertThat(_class.getAttribute(ATTRIBUTE_NAME).isDisplayableInList(), equalTo(false));
		assertThat(_class.getAttribute(ANOTHER_ATTRIBUTE_NAME).isDisplayableInList(), equalTo(true));
	}

	@Test
	public void newlyCreatedAttributeIsNotMandatoryAsDefault() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ANOTHER_ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS) //
						.thatIsMandatory(true)));

		// when
		final CMClass _class = dataView().findClass(CLASS_NAME);

		// then
		assertThat(_class.getAttribute(ATTRIBUTE_NAME).isMandatory(), equalTo(false));
		assertThat(_class.getAttribute(ANOTHER_ATTRIBUTE_NAME).isMandatory(), equalTo(true));
	}

	@Test
	public void newlyCreatedAttributeIsNotUniqueAsDefault() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ANOTHER_ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS) //
						.thatIsUnique(true)));

		// when
		final CMClass _class = dataView().findClass(CLASS_NAME);

		// then
		assertThat(_class.getAttribute(ATTRIBUTE_NAME).isUnique(), equalTo(false));
		assertThat(_class.getAttribute(ANOTHER_ATTRIBUTE_NAME).isUnique(), equalTo(true));
	}

	@Test
	public void onlyDescriptionStatusesAndModeCanBeChangedForAllAttributes() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute.getDescription(), equalTo(ATTRIBUTE_NAME));
		assertThat(attribute.isActive(), equalTo(true));
		assertThat(attribute.isDisplayableInList(), equalTo(false));
		assertThat(attribute.isMandatory(), equalTo(false));
		assertThat(attribute.isUnique(), equalTo(false));
		assertThat(attribute.getIndex(), equalTo(DEFAULT_ATTRIBUTE_INDEX));

		// but...

		// when
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withType(TEXT) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withDescription(DESCRIPTION) //
						.thatIsActive(false) //
						.thatIsDisplayableInList(true) //
						.thatIsMandatory(true) //
						.thatIsUnique(true) //
						.withIndex(10)));
		final CMAttribute updatedAttribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(updatedAttribute.getDescription(), equalTo(DESCRIPTION));
		assertThat(updatedAttribute.isActive(), equalTo(false));
		assertThat(updatedAttribute.isDisplayableInList(), equalTo(true));
		assertThat(updatedAttribute.isMandatory(), equalTo(true));
		assertThat(updatedAttribute.isUnique(), equalTo(true));
		// index is not changed
		assertThat(updatedAttribute.getIndex(), equalTo(DEFAULT_ATTRIBUTE_INDEX));
	}

	@Test
	public void newlyCreatedAttributeIsWritableAsDefault() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));

		// when
		final CMClass _class = dataView().findClass(CLASS_NAME);

		// then
		assertThat(_class.getAttribute(ATTRIBUTE_NAME).getMode(), equalTo(Mode.WRITE));
	}

	@Test
	public void newlyCreatedAttributeCanBeReadOnlyOrHidden() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS) //
						.withMode(Mode.READ)));
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ANOTHER_ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS) //
						.withMode(Mode.HIDDEN)));

		// when
		final CMClass _class = dataView().findClass(CLASS_NAME);

		// then
		assertThat(_class.getAttribute(ATTRIBUTE_NAME).getMode(), equalTo(Mode.READ));
		assertThat(_class.getAttribute(ANOTHER_ATTRIBUTE_NAME).getMode(), equalTo(Mode.HIDDEN));
	}

	@Test
	public void newlyCreatedAttributeIsNotInherited() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS) //
						.withMode(Mode.READ)));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute.isInherited(), equalTo(false));
	}

	@Test
	public void newlyCreatedAttributesHaveDefaultIndexThatCanBeChangedWithSpecificMethod() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute.getIndex(), equalTo(DEFAULT_ATTRIBUTE_INDEX));

		// but...

		// when
		dataDefinitionLogic().reorder( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withDescription(DESCRIPTION) //
						.withIndex(42)));
		final CMAttribute updatedAttribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(updatedAttribute.getIndex(), equalTo(42));
	}

	@Test
	public void newlyCreatedAttributesHaveDefaultZeroClassOrderThatCanBeChangedWithSpecificMethod() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute.getClassOrder(), equalTo(0));

		// but...

		// when
		dataDefinitionLogic().changeClassOrders( //
				testClass.getIdentifier().getLocalName(), //
				asList(ClassOrder.from(ATTRIBUTE_NAME, 42)));
		final CMAttribute updatedAttribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(updatedAttribute.getClassOrder(), equalTo(42));
	}

	@Test
	public void attributesCanBeGrouped() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS) //
						.withGroup(GROUP)));

		// when
		final CMAttribute attribute = dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME);

		// then
		assertThat(attribute.getGroup(), equalTo(GROUP));
	}

	@Test
	public void deletingUnexistingAttributeDoesNothing() throws Exception {
		// given
		// nothing

		// when
		dataDefinitionLogic().deleteOrDeactivate(
				a(newAttribute(ATTRIBUTE_NAME).withOwnerName(testClass.getIdentifier().getLocalName())));

		// then
		// nothing happens, but at least no errors
	}

	@Test
	public void deletingExistingAttributeWithNoData() throws Exception {
		// given
		dataDefinitionLogic().createOrUpdate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwnerName(testClass.getIdentifier().getLocalName()) //
						.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS)));

		// when
		dataDefinitionLogic().deleteOrDeactivate(
				a(newAttribute(ATTRIBUTE_NAME).withOwnerName(testClass.getIdentifier().getLocalName())));

		// then
		assertThat(dataView().findClass(CLASS_NAME).getAttribute(ATTRIBUTE_NAME), is(nullValue()));
	}

	@Ignore
	@Test
	public void deletingExistingAttributeWithDataSetsTheAttributeAsNoActiveAndThrowsException() throws Exception {
		fail("TODO");
	}

}
