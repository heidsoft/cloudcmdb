package unit;

import static com.google.common.collect.Iterables.size;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.CMActivityMetadata;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlActivityWrapper;
import org.cmdbuild.workflow.xpdl.XpdlDocument;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttribute;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeMetadataFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeWidgetFactory;
import org.junit.Test;

public class XpdlActivityWrapperTest {

	private final XpdlActivity xpdlActivity;
	private final XpdlExtendedAttributeWidgetFactory widgetFactory;
	private final XpdlActivityWrapper wrapper;
	private final XpdlExtendedAttributeVariableFactory variableFactory;
	private final XpdlExtendedAttributeMetadataFactory metadataFactory;

	public XpdlActivityWrapperTest() {
		final XpdlDocument doc = new XpdlDocument("PKG");
		xpdlActivity = doc.createProcess("PRO").createActivity("ACT");
		widgetFactory = mock(XpdlExtendedAttributeWidgetFactory.class);
		variableFactory = mock(XpdlExtendedAttributeVariableFactory.class);
		metadataFactory = mock(XpdlExtendedAttributeMetadataFactory.class);
		wrapper = new XpdlActivityWrapper(xpdlActivity, variableFactory, metadataFactory, widgetFactory);
	}

	@Test
	public void extractsNoVariablesMetadataOrWidgetsIfNoExtendedAttributes() {
		assertThat(wrapper.getVariables(), hasSize(0));
		assertThat(size(wrapper.getMetadata()), equalTo(0));
		assertThat(wrapper.getWidgets(), hasSize(0));
		verify(widgetFactory, never()).createWidget(any(XpdlExtendedAttribute.class), any(CMValueSet.class));
		verify(variableFactory, never()).createVariable(any(XpdlExtendedAttribute.class));
	}

	@Test
	public void extractsNoVariablesForInvalidEntries() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");

		assertThat(wrapper.getVariables(), hasSize(0));
		verify(variableFactory, times(2)).createVariable(any(XpdlExtendedAttribute.class));
	}

	@Test
	public void variablesAreExtracted() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");
		when(variableFactory.createVariable(any(XpdlExtendedAttribute.class))).thenReturn(notNullVariableToProcess());

		assertThat(wrapper.getVariables(), hasSize(2));
		verify(variableFactory, times(2)).createVariable(any(XpdlExtendedAttribute.class));
	}

	@Test
	public void extractsNoMetadataForInvalidEntries() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");

		assertThat(size(wrapper.getMetadata()), equalTo(0));
		verify(metadataFactory, times(2)).createMetadata(any(XpdlExtendedAttribute.class));
	}

	@Test
	public void metadataAreExtracted() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");
		doReturn(notNullMetadata()) //
				.when(metadataFactory).createMetadata(any(XpdlExtendedAttribute.class));

		assertThat(size(wrapper.getMetadata()), equalTo(2));
		verify(metadataFactory, times(2)).createMetadata(any(XpdlExtendedAttribute.class));
	}

	@Test
	public void extractsNoWidgetsForInvalidEntries() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");

		assertThat(wrapper.getWidgets(), hasSize(0));
		verify(widgetFactory, times(2)).createWidget(any(XpdlExtendedAttribute.class), any(CMValueSet.class));
	}

	@Test
	public void widgetsAreExtracted() {
		xpdlActivity.addExtendedAttribute("SomeKey", "SomeValue");
		xpdlActivity.addExtendedAttribute("SomeOtherKey", "SomeOtherValue");
		when(widgetFactory.createWidget(any(XpdlExtendedAttribute.class), any(CMValueSet.class)))
				.thenReturn(notNullWidget());

		assertThat(wrapper.getWidgets(), hasSize(2));
		verify(widgetFactory, times(2)).createWidget(any(XpdlExtendedAttribute.class), any(CMValueSet.class));
	}

	/*
	 * Utils
	 */

	private CMActivityVariableToProcess notNullVariableToProcess() {
		return new CMActivityVariableToProcess("FakeName", false, false);
	}
	
	private CMActivityMetadata notNullMetadata() {
		return new CMActivityMetadata("FakeName", "FakeValue");
	}

	private CMActivityWidget notNullWidget() {
		return mock(CMActivityWidget.class);
	}
}
