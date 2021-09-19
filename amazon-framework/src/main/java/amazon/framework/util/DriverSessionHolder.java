package amazon.framework.util;

import amazon.framework.core.AbstractWebDriverTestCase.WebDriverKind;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class DriverSessionHolder {

	private static final Log LOGGER = LogFactory.getLog(DriverSessionHolder.class);

	// must use an inheritable thread local if using timeout attribute of @Test
	private static DriverSession driverSession;

	public static void setDriver(final WebDriver driver, final String identifier, WebDriverKind driverKind) {
		LOGGER.info("Set webDriver in threadlocal for " + identifier );
		driverSession = new DriverSession(driver, identifier, driverKind);
	}

	public static WebDriver getDriver() {
		assertThat(driverSession).isNotNull();
		return driverSession.getDriver();
	}

	public static WebDriverKind getDriverKind() {
		assertThat(driverSession).isNotNull();
		return driverSession.getDriverKind();
	}

	public static String getSessionIdentifier() {
		assertThat(driverSession).isNotNull();
		return driverSession.getSessionIdentifier();
	}

	public static void setSessionIdentifier(String identifier) {
		assertThat(driverSession).isNotNull();
		driverSession.setSessionIdentifier(identifier);
	}

	public static void resetCookies() {
		assertThat(driverSession).isNotNull();
		driverSession.resetCookies();
	}

	public static DriverSession getDriverSession() {
		return driverSession;
	}

	public static void reset() {
		LOGGER.info("Remove webDriver from threadlocal");
		driverSession = null;
	}

	public static void quitDriver() {
		LOGGER.info("Quit webDriver for " + getSessionIdentifier());
		Thread quitThread = null;
		quitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				getDriver().quit();
			}
		});
		quitThread.start();
		try {
			quitThread.join(45000); // This will wait max (param) seconds that the quit thread finishes
		} catch (InterruptedException e) {
			LOGGER.warn("Quit thread was interrupted", e);
		}

		if (quitThread.isAlive()) {
			// The quit thread is still alive, try to interrupt it and continue main thread
			final Exception exc = new Exception();
			exc.setStackTrace(quitThread.getStackTrace());
			LOGGER.warn("Quit thread is stuck", exc);
			quitThread.interrupt();
		} else {
			// The quit thread is finished
			LOGGER.info("Quit webDriver successfully.");
		}
	}

	public static boolean hasDriverSessionSet() {
		return driverSession != null;
	}

}
