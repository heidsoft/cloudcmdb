package integration.dao.driver.postgres.query;

import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;

import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

public class QueryCreatorTest extends IntegrationTestBase {

	private DBClass newClass;

	@Before
	public void createNewClass() {
		newClass = dbDataView().create(newClass("foo"));
	}

	@Test
	public void shouldBuildQueryWithDefinedAttributes() {
		// given
		final String codeAttributeName = newClass.getCodeAttributeName();
		final String CLASS_ALIAS = "root";

		// when
		final String generatedQuery = new QueryCreator(dbDataView() //
				.select(codeAttributeName) //
				.from(newClass, as(CLASS_ALIAS)) //
				.build()) //
				.getQuery();

		// then
		assertThat(generatedQuery, containsString("SELECT\n" + CLASS_ALIAS + ".\"" + codeAttributeName + "\""));
		assertThat(generatedQuery, containsString("FROM " + newClass.getName() + " " + "AS " + CLASS_ALIAS));
	}

}
