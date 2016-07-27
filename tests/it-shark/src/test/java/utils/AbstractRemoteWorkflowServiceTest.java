package utils;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;

/**
 * 
 * Don't forget to initialize the variable {@code ws} in every subclass.
 * 
 */
public abstract class AbstractRemoteWorkflowServiceTest extends AbstractSharkServiceTest {

	protected static String USERNAME = "admin";
	protected static String PASSWORD = "enhydra";
	protected static String SERVER_HOST = "localhost";
	protected static int SERVER_PORT = 8080;

	/**
	 * Gets the log file associated with the Shark instance.
	 * 
	 * This file is defined in the {@code Shark.conf} configuration file.
	 * 
	 * @return the log file.
	 */
	protected abstract File getLogFile();

	@Before
	public void cleanLogFile() throws Exception {
		FileUtils.writeStringToFile(getLogFile(), EMPTY);
	}

	protected List<String> logLines() throws IOException {
		return FileUtils.readLines(getLogFile());
	}

}
