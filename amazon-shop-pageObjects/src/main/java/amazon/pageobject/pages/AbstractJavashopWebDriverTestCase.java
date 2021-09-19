package amazon.pageobject.pages;

import amazon.framework.core.AbstractWebDriverTestCase;
import amazon.framework.util.ButtonWidget;
import amazon.framework.util.DriverSessionHolder;
import amazon.pageobject.login.Credentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import static amazon.framework.util.DriverSessionHolder.getDriver;

public class AbstractJavashopWebDriverTestCase extends AbstractWebDriverTestCase {
	protected final Log LOGGER = LogFactory.getLog(AbstractJavashopWebDriverTestCase.class);

	protected void navigateAndLogin(String url, final Credentials credentials) {
		LOGGER.info("Browsing to " + url);
		getDriver().navigate().to(url);
		LoginPage loginPage = toLoginPage();
		loginPage.login(credentials);
	}

	public LoginPage toLoginPage() {
		LOGGER.info("Go to login page.");
		By signInLocator = By.cssSelector("#nav-link-accountList");
		new ButtonWidget(signInLocator).click();
		return new LoginPage();
	}

	@Override
	protected String getDriverSessionIdentifier() {
		return null;
	}

	@Override
	protected WebDriver getWebDriver(WebDriverKind driverKind) {
		if (DriverSessionHolder.hasDriverSessionSet()) {
			WebDriverKind currentDriverKind = DriverSessionHolder.getDriverKind();
			if (!(currentDriverKind == driverKind)) {
				// Quit current driver, current browser does not match test needs
				LOGGER.info("Currently runnig webdriver does not match requested browser, quit current driver");
				quitDriver();
			} else {
				try {
					LOGGER.info("Currently running webdriver is matching test needs, try to reuse");
					getDriver().manage().deleteAllCookies();
					getDriver().navigate().refresh();
				} catch (WebDriverException we) {
					LOGGER.warn("Refreshing failed. Unexpected state, quit current driver", we);
					quitDriver();
				}
			}
		}
		if (!DriverSessionHolder.hasDriverSessionSet()) {
			super.getWebDriver(driverKind);
			getDriver().manage().window().maximize();
		}
		return getDriver();
	}
}
