package integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static utils.XpdlTestUtils.randomName;

import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.xpdl.XpdlActivity;
import org.cmdbuild.workflow.xpdl.XpdlDocument.ScriptLanguage;
import org.cmdbuild.workflow.xpdl.XpdlException;
import org.cmdbuild.workflow.xpdl.XpdlProcess;
import org.junit.Test;

public class ConfigurationChangeTest extends AbstractRemoteSharkServiceTest {

	private static final String FAKE_SERVER_HOST = "foo.bar.baz";

	@Test
	public void startProcessFailsAfterReconfigurationWithWrongConfiguration() throws Exception {
		startSimpleProcess();
		try {
			changeConfiguration();
			startSimpleProcess();
			fail();
		} catch (final CMWorkflowException e) {
			e.printStackTrace();
			assertThat(e.getCause().getMessage(), containsString(UnknownHostException.class.getName()));
		} finally {
			revertConfiguration();
		}
	}

	private void startSimpleProcess() throws CMWorkflowException, XpdlException {
		final XpdlProcess process = xpdlDocument.createProcess(randomName());
		final XpdlActivity activity = process.createActivity(randomName());
		activity.setScriptingType(ScriptLanguage.JAVA, StringUtils.EMPTY);
		uploadXpdlAndStartProcess(process);
	}

	private void changeConfiguration() {
		configuration.setServerHost(FAKE_SERVER_HOST);
		configuration.notifyChange();
	}

	private void revertConfiguration() {
		configuration.setServerHost(SERVER_HOST);
		configuration.notifyChange();
	}

}
