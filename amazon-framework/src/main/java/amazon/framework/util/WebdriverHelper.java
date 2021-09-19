package amazon.framework.util;

import com.google.common.base.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static amazon.framework.util.DriverSessionHolder.getDriver;

/**
 * Static function about {@link WebElement}
 *
 * @author Nicolas Rémond (nre)
 */
public class WebdriverHelper {


	private static final Log LOGGER = LogFactory.getLog(WebdriverHelper.class);

	/**
	 * Return the {@link WebElement} whose text CONTAINS the one in argument
	 *
	 * @param label the label to compare with
	 * @return the {@link WebElement} that has the good label
	 */
	public static WebElement findWebElementStrong(final List<WebElement> webElements, final String label) {

		WebElement result = findWebElement(webElements, label);
		if (result != null) {
			return result;
		}
		// Construct a convenient error message for debugging
		StringBuilder webLabels = new StringBuilder();
		boolean firstElement = true;
		for (final WebElement we : webElements) {
			if (firstElement) {
				firstElement = false;
			} else {
				webLabels.append(", ");
			}
			final String weLabel = we.getText().trim();
			webLabels.append("'").append(weLabel).append("'");
		}

		throw new NotFoundException(
				"No web element found with text='" + label + "' in the list of following web elements : " + webLabels);
	}

	public static WebElement findWebElement(final List<WebElement> webElements, final String label) {
		for (final WebElement we : webElements) {
			final String weLabel = we.getText().trim();
			String[] labels = weLabel.split("\n");
			// compare name product exactly, if not can select product in case there are
			// products contain name
			for (String wLabel : labels) {
				if (label.equals(wLabel)) {
					return we;
				}
			}
		}
		return null;
	}

	public static <P> P constructPageObject(final Class<P> pageObject) {
		try {
			return pageObject.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	private static <P> P constructPageObject(final Class<P> pageObject, final WebDriver driver) {
		try {
			return pageObject.getDeclaredConstructor(WebDriver.class).newInstance(driver);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e);
		}
	}

	@Deprecated
	public static <P extends LoadableComponent<P>> P initializePageObject(final Class<P> pageObject,
			final WebDriver driver) {
		return constructPageObject(pageObject, driver).get();
	}

	public static <T> T logTime(final String category, Supplier<T> supplier) {
		long start = System.currentTimeMillis();
		try {
			return supplier.get();
		} catch (RuntimeException e) {
			LOGGER.info("Unexpected error: " + category, e);
			throw e;
		} finally {
			final long time = System.currentTimeMillis() - start;
			LOGGER.debug(category + " time:" + time);
			if (time > 10_000) {
				LOGGER.info(category + " time:" + time);
			}
		}

	}

	public static void acceptAlert() {
		actionWithAlert(true);
	}
	
	public static void actionWithAlert(boolean isAccept) {
		if (isAlertPresent()) {
			final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(5))
					.withMessage("Time out while trying to cancel alert!");
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(final WebDriver driver) {
					try {
						if (isAccept) {
							LOGGER.info("Accepting alert... " + driver.switchTo().alert().getText());
							driver.switchTo().alert().accept();							
						}
						else {
							LOGGER.info("Cancelling alert... " + driver.switchTo().alert().getText());
							driver.switchTo().alert().dismiss();							
						}
						
					} catch (WebDriverException e) {
						// Accept alert failed randomly with this error. Retry until there is no alert
						// org.openqa.selenium.WebDriverException:
						// a.document.getElementsByTagName(...)[0] is undefined
						LOGGER.info("Error when working with alert. Retry ...", e);
						return false;
					}
					return true;
				}
			});
		}
	}

	/**
	 * 
	 * @return True if JavaScript Alert is present on the page otherwise false
	 */
	public static boolean isAlertPresent() {
		try {
			new WebDriverWait(getDriver(), 3)
					.ignoring(NoAlertPresentException.class)
					.until(ExpectedConditions.alertIsPresent());
			getDriver().switchTo().alert();
			LOGGER.info("There is an alert shown.");
			return true;
		} catch (NoAlertPresentException| NoSuchWindowException | TimeoutException e) {
			LOGGER.info("The alert is not shown. Ignored to continue test.");
			return false;
		}
	}
	
	/**
	 * 
	 * @return True if JavaScript Alert is present on the page within a timeout otherwise false
	 */
	public static boolean isAlertPresent(long timeout) {
		try {
			new WebDriverWait(getDriver(), timeout)
					.ignoring(NoAlertPresentException.class)
					.until(ExpectedConditions.alertIsPresent());
			getDriver().switchTo().alert();
			LOGGER.info("There is an alert shown.");
			return true;
		} catch (NoAlertPresentException| NoSuchWindowException | TimeoutException e) {
			return false;
		}
	}

	public static String getCurrentUrl() {
		try {
			return getDriver().getCurrentUrl();
		} catch (Exception e) {
			return String.format("Could not retrieve url from driver, got %s",
					e.getMessage() != null ? e.getMessage() : e.getClass().getName());
		}
	}

	/**
	 * For windows with same url, we have to identify to latest window.
	 */
	public static void switchToLatestWindow() {
		Set<String> windowHandles = getDriver().getWindowHandles();
		Iterator<String> windowIter = windowHandles.iterator();
		String window = "";
		while (windowIter.hasNext()) {
			window = windowIter.next();
		}

		LOGGER.info(String.format("Switch to latest window '%s'", window));
		getDriver().switchTo().window(window);

	}

	public static void waitUntilDocumentReady() {
		try {
			final FluentWait<WebDriver> wait = new FluentWait<>(getDriver())
					.withTimeout(Duration.ofSeconds(LoadingConstants.LOADING_TIMEOUT))
					.withMessage("Time out while waiting for document ready state").ignoring(WebDriverException.class);
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(final WebDriver driver) {
					return String.valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
							.equals("complete");
				}
			});
		} catch (Exception e) {
			LOGGER.warn("Cannot wait for document.readyState to complete. Continue test.", e);
		}

	}

	public static Cookie getCookieAndCloseBrowser(final WebDriver webDriver, String cookieName) {
		Cookie cookie = webDriver.manage().getCookieNamed(cookieName);
		webDriver.quit();
		return cookie;
	}

	/**
	 * <pre>
	 * For Selenium version 2.53 with issue
	 * org.openqa.selenium.InvalidCookieDomainException: You may only set cookies for the current domain.
	 * They changed something with cookie handling
	 * Solution: remove the leading '.' in domain cookie
	 * Use closeBrowser & addCookie
	 * </pre>
	 */
	public static void addCookie(final WebDriver newWebDriver, Cookie cookie) {
		if (cookie != null) {
			// New cookie on current url of driver to avoid InvalidCookieDomainException
			Cookie newCookie = new Cookie.Builder(cookie.getName(), cookie.getValue()).build();
			newWebDriver.manage().addCookie(newCookie);
		}
	}

	public static void switchToWindow(int windowNumber){
		Set<String> windows = getDriver().getWindowHandles();
		
		final Wait<WebDriver> wait =
				new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(60));
		wait.until(new Function<WebDriver, Boolean>() {
			@SuppressWarnings("unlikely-arg-type")
			@Override
			public Boolean apply(final WebDriver driver) {
				getDriver().switchTo().window((String) windows.toArray()[windowNumber]);
				return !getDriver().getWindowHandles().contains(windows);
			}
			
		});
	}

}
