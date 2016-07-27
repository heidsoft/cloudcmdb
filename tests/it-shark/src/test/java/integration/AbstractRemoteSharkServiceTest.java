package integration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.cmdbuild.workflow.service.RemoteSharkService;
import org.cmdbuild.workflow.service.RemoteSharkServiceConfiguration;
import org.junit.BeforeClass;

import utils.AbstractRemoteWorkflowServiceTest;

public abstract class AbstractRemoteSharkServiceTest extends AbstractRemoteWorkflowServiceTest {

	protected static class ChangeableRemoteSharkServiceConfiguration implements RemoteSharkServiceConfiguration {

		private String serverHost = SERVER_HOST;
		private int serverPort = SERVER_PORT;
		private String webappName = WEBAPP_NAME;
		private String username = USERNAME;
		private String password = PASSWORD;

		private final Set<ChangeListener> changeListeners = new HashSet<RemoteSharkServiceConfiguration.ChangeListener>();

		@Override
		public String getServerUrl() {
			return String.format("http://%s:%d/%s", serverHost, serverPort, webappName);
		}

		public void setServerHost(final String serverHost) {
			this.serverHost = serverHost;
		}

		public void setServerPort(final int serverPort) {
			this.serverPort = serverPort;
		}

		public void setWebappName(final String webappName) {
			this.webappName = webappName;
		}

		@Override
		public String getUsername() {
			return username;
		}

		public void setUsername(final String username) {
			this.username = username;
		}

		@Override
		public String getPassword() {
			return password;
		}

		public void setPassword(final String password) {
			this.password = password;
		}

		@Override
		public void addListener(final ChangeListener listener) {
			changeListeners.add(listener);
		}

		public void notifyChange() {
			for (final ChangeListener changeListener : changeListeners) {
				changeListener.configurationChanged();
			}
		}

	}

	/**
	 * The Tomcat plugin deploys the webapp as the artifact id. It has to change
	 * when the artifact id changes.
	 */
	private static String WEBAPP_NAME = "it-shark";

	/**
	 * Defined in the {@code Shark.conf} configuration file.
	 */
	protected static File LOGFILE = new File(SystemUtils.getJavaIoTmpDir(), "it-shark-4.4.log");

	protected static final ChangeableRemoteSharkServiceConfiguration configuration = new ChangeableRemoteSharkServiceConfiguration();

	@Override
	protected File getLogFile() {
		return LOGFILE;
	}

	@BeforeClass
	public static void initWorkflowService() {
		ws = new RemoteSharkService(configuration);
	}

}
