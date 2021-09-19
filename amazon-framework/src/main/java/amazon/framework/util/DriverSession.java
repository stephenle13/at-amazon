package amazon.framework.util;

import amazon.framework.core.AbstractWebDriverTestCase.WebDriverKind;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class DriverSession {

	private static final Log LOGGER = LogFactory.getLog(DriverSession.class);

	private WebDriver driver;

	private WebDriverKind driverKind;

	private String sessionIdentifier;
	private boolean gridSlow;

	public DriverSession(final WebDriver driver, final String identifier, WebDriverKind driverKind) {
		setDriver(driver);
		setDriverKind(driverKind);
		setSessionIdentifier(identifier);
	}

	public String getSessionIdentifier() {
		return sessionIdentifier;
	}

	public void setSessionIdentifier(final String identifier) {
		this.sessionIdentifier = identifier;
	}

	public WebDriver getDriver() {
		return driver;
	}

	private void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public boolean matches(final String sessionIdentifier) {
		return this.sessionIdentifier == sessionIdentifier;
	}

	public WebDriverKind getDriverKind() {
		return driverKind;
	}

	private void setDriverKind(WebDriverKind driverKind) {
		this.driverKind = driverKind;
	}

	public void resetCookies() {
		List<Cookie> cookies = Lists.newArrayList();
	}

	public void setGridSlow(final boolean gridSlow) {
		if (gridSlow) {
			LOGGER.info("Grid flagged as slow");
		}
		this.gridSlow = gridSlow;
	}
}
