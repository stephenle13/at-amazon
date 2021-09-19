package amazon.framework.core;

import amazon.framework.util.DriverSessionHolder;
import com.google.common.base.Stopwatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static amazon.framework.util.DriverSessionHolder.getDriver;

public abstract class AbstractWebDriverTestCase {

	private static final Log LOGGER = LogFactory.getLog(AbstractWebDriverTestCase.class);
	private Instant testStartTime;

	@Rule
	public final TestName testName = new TestName();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				getDriver().quit();
			} catch (Throwable t) {
				LOGGER.info("Shutdown hook failed", t);
			}
		}));

	}

	protected WebDriver getWebDriverWithoutRetrying(final WebDriverKind driverKind) {
		return getWebDriver(driverKind, false);
	}

	protected WebDriver getWebDriver(final WebDriverKind driverKind) {
		return getWebDriver(driverKind, true);
	}

	private WebDriver getWebDriver(final WebDriverKind driverKind, final boolean retry) {
		LOGGER.debug(String.format("Instantiate a new browser '%s'", driverKind));

		final WebDriver webDriver;
		webDriver = driverKind.getInstance();
		DriverSessionHolder.setDriver(webDriver, getDriverSessionIdentifier(), driverKind);
		return webDriver;
	}

	protected abstract String getDriverSessionIdentifier();

	protected void quitDriver() {
		try {
			DriverSessionHolder.quitDriver();
		} catch (final WebDriverException couldNotQuitWebDriver) {
			// Could not quit web driver
			LOGGER.warn("Could not quit driver", couldNotQuitWebDriver);
		} finally {
			DriverSessionHolder.reset();
		}
	}

	public enum WebDriverKind {

		Firefox() {
			@Override
			public WebDriver getInstance() {
				System.setProperty("webdriver.gecko.driver",
						"C:\\selenium\\drivers\\geckodriver_win32\\geckodriver.exe");
				final String date = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
				System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
				FirefoxOptions firefoxOptions = new FirefoxOptions();
				firefoxOptions.setProfile(getProfile());
				firefoxOptions.addPreference("dom.disable_beforeunload", true);
				return new FirefoxDriver(firefoxOptions);
			}

			@Override
			public DesiredCapabilities getCapabilities() {
				final DesiredCapabilities capabilities =
						new DesiredCapabilities(DesiredCapabilities.firefox().getBrowserName(),DesiredCapabilities.firefox().getVersion(), Platform.ANY);
				capabilities.setCapability("marionette", true);
				capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
				capabilities.setCapability(CapabilityType.HAS_NATIVE_EVENTS, false);
				capabilities.setCapability(FirefoxDriver.PROFILE, getProfile());
				capabilities.setCapability("TC", getClass().getSimpleName());
				return capabilities;
			}

			@Override
			public boolean isHeadless() {
				return false;
			}

			private FirefoxProfile getProfile() {
				final FirefoxProfile profile = new FirefoxProfile();
				profile.setPreference("browser.download.folderList", 2);

				final String date = new SimpleDateFormat("YYYY-MM-dd").format(new Date());
				profile.setPreference("webdriver.log.browser.file",
						"c:\\selenium\\logs\\firefox-browser-" + date + ".log");
				profile.setPreference("webdriver.log.driver.file",
						"c:\\selenium\\logs\\firefox-driver-" + date + ".log");
				profile.setPreference("webdriver.log.file", "c:\\selenium\\logs\\wd-log-" + date + ".log");

				System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "c:\\selenium\\logs\\firefox-log-" + date + ".log");
				return profile;
			}

		},

		IE() {
			@Override
			public WebDriver getInstance() {
				System.setProperty("webdriver.ie.driver", "C:\\selenium\\drivers\\iedriver\\IEDriverServer.exe");
				return new InternetExplorerDriver(getCapabilities());
			}

			@Override
			public DesiredCapabilities getCapabilities() {
				final DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
				capabilities.setJavascriptEnabled(true);
				return capabilities;
			}

			@Override
			public boolean isHeadless() {
				return false;
			}
		},

		Chrome() {
			@Override
			public WebDriver getInstance() {
				System.setProperty("webdriver.chrome.driver",
						"C:\\selenium\\drivers\\chromedriver_win32\\chromedriver93.exe");
				System.setProperty("webdriver.chrome.logfile", "C:\\selenium\\logs\\chromedriver.log");
				System.setProperty("webdriver.chrome.verboseLogging", "true");
				return new ChromeDriver(getCapabilities());
			}

			@Override
			public DesiredCapabilities getCapabilities() {
				final DesiredCapabilities capabilities = DesiredCapabilities.chrome();
				final java.util.HashMap<String, Object> prefs = new java.util.HashMap<>();
				final ChromeOptions options = new ChromeOptions();
				options.addArguments("--disable-notifications");
				options.setExperimentalOption("prefs", prefs);
				options.addArguments("--no-sandbox");
				return capabilities;
			}

			@Override
			public boolean isHeadless() {
				return false;
			}
		};

		protected abstract WebDriver getInstance();

		protected abstract DesiredCapabilities getCapabilities();

		public DesiredCapabilities getCapabilities(String testName) {
			return getCapabilities();
		}

		protected abstract boolean isHeadless();

		public FirefoxOptions getOptions() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class WebDriverSupplier implements Supplier<RemoteWebDriver> {

		private final int maxAttempts;
		private final URL hubUrl;
		private final WebDriverKind driverKind;

		WebDriverSupplier(WebDriverKind driverKind, URL hubUrl, int maxAttempts) {
			this.maxAttempts = maxAttempts;
			this.hubUrl = hubUrl;
			this.driverKind = driverKind;
		}

		@Override
		public RemoteWebDriver get() {
			int retryCounter = 0;
			while (retryCounter < maxAttempts) {
				Stopwatch sw = Stopwatch.createStarted();
				try {
					retryCounter++;
					final RemoteWebDriver driver = new RemoteWebDriver(hubUrl, driverKind.getCapabilities());
					LOGGER.info("[START_REMOTE] Attempt n° " + retryCounter + " to instantiate " + driverKind.name()
							+ " on " + hubUrl + " took " + sw.elapsed(TimeUnit.SECONDS) + " s");
					return driver;
				} catch (Exception ex) {
					LOGGER.error("[START_REMOTE] Attempt n° " + retryCounter + " on " + maxAttempts + " to instantiate "
							+ driverKind.name() + " on " + hubUrl + " failed after " + sw.elapsed(TimeUnit.SECONDS)
							+ " s");
					LOGGER.error(ex);
					if (retryCounter >= maxAttempts) {
						break;
					} else {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							LOGGER.error(e);
						}
					}
				}
			}
			throw new WebDriverException("Could not instantiate remote web driver on all of " + maxAttempts + " attempts");
		}
	}
}
