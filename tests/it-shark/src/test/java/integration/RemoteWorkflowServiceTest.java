package integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static utils.XpdlTestUtils.randomName;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.cmdbuild.workflow.xpdl.XpdlPackageFactory;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.enhydra.jxpdl.elements.Package;
import org.junit.Test;

/**
 * Smoke tests to be reasonably sure that the web connection works just like the
 * local one. This is not tested throughly because we assume that it is going to
 * work just like the embedded Shark instance.
 */
@SuppressWarnings("serial")
public class RemoteWorkflowServiceTest extends AbstractRemoteSharkServiceTest {

	@Test
	public void packagesCanBeUploadedAndDownloaded() throws CMWorkflowException {
		Package pkg = xpdlDocument.getPkg();

		assertEquals(0, ws.getPackageVersions(pkg.getId()).length);

		pkg.setName("n1");
		upload(xpdlDocument);

		assertEquals(1, ws.getPackageVersions(pkg.getId()).length);

		pkg.setName("n2");
		upload(xpdlDocument);

		pkg.setName("n3");
		upload(xpdlDocument);

		assertEquals(3, ws.getPackageVersions(pkg.getId()).length);

		pkg = XpdlPackageFactory.readXpdl(ws.downloadPackage(pkg.getId(), "1"));
		assertThat(pkg.getName(), is("n1"));
	}

	/**
	 * Uses {@ref LookupType} because the service was initialized with the
	 * {@ref IdentityTypesConverter}.
	 */
	@Test
	public void lookupVariablesCanBeSaved() throws CMWorkflowException {
		final XpdlProcess process = xpdlDocument.createProcess(randomName());

		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();

		// TODO CMLookup it should be used!
		ws.setProcessInstanceVariables(procInstId, new HashMap<String, Object>() {
			{
				put("lookupVar", new LookupType(42, "type", "desc", "code"));
			}
		});

		final LookupType val = (LookupType) ws.getProcessInstanceVariables(procInstId).get("lookupVar");
		assertThat(val.getId(), is(42));
		assertThat(val.getType(), is("type"));
		assertThat(val.getDescription(), is("desc"));
		assertThat(val.getCode(), is("code"));
	}

	/**
	 * Uses {@ref ReferenceType} because the service was initialized with the
	 * {@ref IdentityTypesConverter}.
	 */
	@Test
	public void referenceVariablesCanBeSaved() throws CMWorkflowException {
		final XpdlProcess process = xpdlDocument.createProcess(randomName());

		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();

		// TODO CMLookup it should be used!
		ws.setProcessInstanceVariables(procInstId, new HashMap<String, Object>() {
			{
				put("referenceVar", new ReferenceType(42, 666, "desc"));
			}
		});

		final ReferenceType val = (ReferenceType) ws.getProcessInstanceVariables(procInstId).get("referenceVar");
		assertThat(val.getId(), is(42));
		assertThat(val.getIdClass(), is(666));
		assertThat(val.getDescription(), is("desc"));
	}

	@Test
	public void variablesNotDefinedInTheXpdlCanBeSettedAnyway() throws Exception {
		final XpdlProcess process = xpdlDocument.createProcess(randomName());
		process.createActivity(randomName());

		final String procInstId = uploadXpdlAndStartProcess(process).getProcessInstanceId();

		final Map<String, Object> settedVariables = new HashMap<String, Object>();
		settedVariables.put("UNDEFINED", "baz");
		ws.setProcessInstanceVariables(procInstId, settedVariables);

		final Map<String, Object> readVariables = ws.getProcessInstanceVariables(procInstId);
		assertThat((String) readVariables.get("UNDEFINED"), is("baz"));
	}
}
