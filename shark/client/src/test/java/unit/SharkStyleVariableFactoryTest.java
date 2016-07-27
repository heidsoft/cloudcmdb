package unit;

import static org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory.VARIABLE_PREFIX;
import static org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory.VariableSuffix.UPDATE;
import static org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory.VariableSuffix.UPDATEREQUIRED;
import static org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory.VariableSuffix.VIEW;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttribute;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeVariableFactory;
import org.junit.Test;

public class SharkStyleVariableFactoryTest {

	XpdlExtendedAttributeVariableFactory variableFactory = new SharkStyleXpdlExtendedAttributeVariableFactory();

	@Test
	public void returnsNullForInvalidEntries() {
		assertNull(createVariable("Rubbish", "Foo"));
		assertNull(createVariable(VARIABLE_PREFIX + VIEW, null));
	}

	@Test
	public void returnsVariableName() {
		assertThat(createVariable(VARIABLE_PREFIX + VIEW, "VarName").getName(), equalTo("VarName"));
	}

	@Test
	public void returnsVariableType() {
		assertThat(createVariable(VARIABLE_PREFIX + VIEW, "Foo").isWritable(), equalTo(false));
		assertThat(createVariable(VARIABLE_PREFIX + VIEW, "Foo").isMandatory(), equalTo(false));
		assertThat(createVariable(VARIABLE_PREFIX + UPDATE, "Bar").isWritable(), equalTo(true));
		assertThat(createVariable(VARIABLE_PREFIX + UPDATE, "Bar").isMandatory(), equalTo(false));
		assertThat(createVariable(VARIABLE_PREFIX + UPDATEREQUIRED, "Baz").isWritable(), equalTo(true));
		assertThat(createVariable(VARIABLE_PREFIX + UPDATEREQUIRED, "Baz").isMandatory(), equalTo(true));
	}

	/*
	 * Utils
	 */

	private CMActivityVariableToProcess createVariable(final String key, final String value) {
		return variableFactory.createVariable(new XpdlExtendedAttribute(key, value));
	}

}
