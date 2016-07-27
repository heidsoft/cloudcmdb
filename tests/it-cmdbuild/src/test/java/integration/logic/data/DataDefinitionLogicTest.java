package integration.logic.data;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.EntryType.ClassBuilder;
import org.junit.Before;

import utils.IntegrationTestBase;

public abstract class DataDefinitionLogicTest extends IntegrationTestBase {

	private DataDefinitionLogic dataDefinitionLogic;

	@Before
	public void createDataDefinitionLogic() throws Exception {
		// TODO add privileges management as database designer
		dataDefinitionLogic = new DefaultDataDefinitionLogic(dbDataView());
	}

	protected DataDefinitionLogic dataDefinitionLogic() {
		return dataDefinitionLogic;
	}

	protected CMDataView dataView() {
		return dbDataView();
	}

	/*
	 * Utilities
	 */

	public static EntryType a(final ClassBuilder classBuilder) {
		return classBuilder.build();
	}

	public static Domain a(final DomainBuilder domainBuilder) {
		return domainBuilder.build();
	}

	public static ClassBuilder newClass(final String name) {
		return EntryType.newClass() //
				.withName(name);
	}

	public static DomainBuilder newDomain(final String name) {
		return Domain.newDomain() //
				.withName(name);
	}

	public static Attribute a(final AttributeBuilder attributeBuilder) {
		return attributeBuilder.build();
	}

	public static AttributeBuilder newAttribute(final String name) {
		return Attribute.newAttribute() //
				.withName(name);
	}

}
