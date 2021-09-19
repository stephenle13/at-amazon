package amazon.framework.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static amazon.framework.util.DriverSessionHolder.getDriver;
import static amazon.framework.util.DriverSessionHolder.getDriverSession;
import static org.junit.Assert.assertTrue;


/**
 * This class solves common issues with WebElement. <br>
 * Some listed issues which cannot be included in this helper, please see them as guidelines:
 * <li>In method com.secutix.boxoffice.selenium.page.frame.ResumeFileOperationsPopup. searchResumeFile we need to click
 * a tab. Each tab is a tag "li". When we click this tag, the element is invisible. This problem come from an invisible
 * tag "a" inside the "li". So, we should click another visible tag "a".</li>
 */
public class WebElementHelper {
	private static final Log LOGGER = LogFactory.getLog(WebElementHelper.class);

	// Hide the constructor
	private WebElementHelper() {
		super();
	}

	public static void printCurrentURL() {
		LOGGER.info(String.format("Current URL: %s", getDriver().getCurrentUrl()));
	}

	private static void sendKeysOneByOne(final WebElement element, final String text) {
		final int contentLength = element.getText().length();

		new Actions(getDriver()).moveToElement(element).click().perform();// set
																			// focus

		new Actions(getDriver()).moveToElement(element)
				.sendKeys(
						Keys.chord(Keys.END, StringUtils.repeat(String.valueOf(Keys.BACK_SPACE), contentLength), text))
				.perform();

		WebElement parent = element.findElement(By.xpath(".."));
		new Actions(getDriver()).click(parent).sendKeys(parent, Keys.TAB).perform();
	}

	public static void sendKeysOneByOneLocated(final By locator, final String text) {
		sendKeysOneByOne(findElement(locator), text);
	}

	/**
	 * This method makes sure the text is filled in an input correctly. It only support html input with attribute
	 * "value".
	 *
	 * @param element
	 * @param text
	 */
	public static void sendKeysAndCheck(final WebElement element, final String text) {
		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(30))
				.withMessage("Time out while trying to input text " + text);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				element.clear();
				element.sendKeys(text);
				JSComponentHelper.blur(element);
				if (text.equalsIgnoreCase(element.getAttribute("value"))) {
					return true;
				}
				LOGGER.info("Expected text='" + text + "' but the value is '" + element.getAttribute("value")
						+ "'. Retry to fill text");
				return false;
			}
		});
	}

	/**
	 * Same as sendKeysAndCheck(element, text)
	 *
	 * @param by
	 * @param text
	 */
	public static void sendKeysAndCheckLocated(final By by, final String text) {
		final WebElement element = waitAndGetElement(by, LoadingConstants.RENDER_ELEMENT);
		sendKeysAndCheck(element, text);
	}

	public static void sendKeysAtBeginning(final WebElement inputElement, final String value) {
		inputElement.clear();
		inputElement.sendKeys(Keys.HOME);
		inputElement.sendKeys(value);
		inputElement.sendKeys(Keys.TAB);
	}
	
	/**
	 * Some input need a blur and tab event to update the value
	 *
	 * @param value
	 * @param inputElement
	 */
	public static void sendKeysAndBlur(final WebElement inputElement, final String value) {
		inputElement.clear();
		inputElement.sendKeys(value);
		JSComponentHelper.blur(inputElement);
		inputElement.sendKeys(Keys.TAB);
	}

	public static void sendKeysAndBlurNotClear(final WebElement inputElement, final String value) {
		inputElement.sendKeys(value);
		JSComponentHelper.blur(inputElement);
		inputElement.sendKeys(Keys.TAB);
	}

	public static void sendKeysAndBlurLocated(final By by, final String value) {
		final WebElement inputElement = waitAndGetElement(by, LoadingConstants.RENDER_ELEMENT);
		sendKeysAndBlur(inputElement, value);
	}
	
	/**
	 * Ticketshop: can not send special keys, this function is used for these case
	 */
	public static void sendKeysByJsAndBlurLocated(final By by, final String value) {
		final WebElement inputElement = waitAndGetElement(by, LoadingConstants.RENDER_ELEMENT);
		sendKeysByJsAndBlur(inputElement, value);
	}
	
	/**
	 * Ticketshop, can not send special keys, this function is used for these case
	 */
	public static void sendKeysByJsAndBlur(final WebElement inputElement, final String value) {
		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(60))
				.withMessage("Time out while trying to input text " + value);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				inputElement.clear();
				JSComponentHelper.setValueJS(inputElement, value);
				JSComponentHelper.blur(inputElement);
				inputElement.sendKeys(Keys.TAB);
				if (inputElement.getAttribute("value").contentEquals(value)) {
					return true;
				}
				LOGGER.info("Expected text='" + value + "' but the value is '" + inputElement.getAttribute("value")
						+ "'. Retry to fill text");
				return false;
			}
		});
	}

	/**
	 * Wait and find element until it appears. This method intends to fix NoSuchElementException of
	 * http://jira-stx.elca.ch/jira/browse/STX-31933
	 *
	 * @param by
	 * @param timeout
	 * @return
	 */
	public static WebElement waitAndGetElement(final By by, long timeout) {
		waitForElementExist(by, timeout);
		return getDriver().findElement(by);
	}

	public static List<WebElement> waitAndGetElements(final By by, long timeout) {
		try {
			waitForElementExist(by, timeout);
		} catch (TimeoutException e) {
			LOGGER.info(String.format("element: %s not found => Return empty list", by));
		}
		return getDriver().findElements(by);
	}

	public static List<WebElement> waitAndGetElements(final By by) {
		return waitAndGetElements(by, LoadingConstants.RENDER_ELEMENT);
	}

    /**
	 * Wait and find element until it appears. Default timeout = 60s
	 *
	 * @param by
	 * @return
	 */
	public static WebElement waitAndGetElement(final By by) {
		return waitAndGetElement(by, LoadingConstants.RENDER_ELEMENT);
	}
	
    /**
	 * Wait element can be clicked.
	 * Appear timeout = 60s
	 * Clickable timeout = 60s
	 *
	 * @param by
	 * @return
	 */	
	public static void waitAndClickElement(final By by) {
		waitAndClick(by, LoadingConstants.RENDER_ELEMENT);
	}

    /**
     * Wait and find element until it appears. Return all attributes of the element as String
     *
     * @param by
     * @return String of attributes
     */
    public static String waitAndGetAllElementAttributes(final By by) {
        WebElement element = waitAndGetElement(by, LoadingConstants.RENDER_ELEMENT);
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();

        Object attributes = executor.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;", element);
        return attributes.toString();
    }


	private static String waitAndGetAllElementAttributes(final WebElement element) {
		JavascriptExecutor executor = (JavascriptExecutor) getDriver();

		Object attributes = executor.executeScript("var items = {}; for (index = 0; index < arguments[0].attributes.length; ++index) { items[arguments[0].attributes[index].name] = arguments[0].attributes[index].value }; return items;", element);
		return attributes.toString();
	}

    /**
     * Find out if element has attribute
     *
     * @param by
     * @param attribute attribute to check element has
     * @return true if element has the attribute
     */
    public static boolean elementHasAttribute(final By by, String attribute) {
        return waitAndGetAllElementAttributes(by).contains(attribute);
    }

	public static boolean elementHasAttribute(final WebElement element, String attribute) {
		return waitAndGetAllElementAttributes(element).contains(attribute);
	}

	/**
	 * Wait and find element from a parent until it appears. This method intends to fix NoSuchElementException of
	 * http://jira-stx.elca.ch/jira/browse/STX-31933
	 *
	 * @param by
	 *            : The locator of sub element. Note that if we use xpath, the prefix for finding is ".//"
	 * @param timeout
	 * @return
	 */
	public static WebElement waitAndGetElement(final WebElement parent, final By by, long timeout) {
		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.withMessage("Timeout while waiting element " + by);
		return wait.until(new Function<WebDriver, WebElement>() {
			@Override
			public WebElement apply(WebDriver driver) {
				List<WebElement> list = parent.findElements(by);
				return !list.isEmpty() ? findElement(parent, by) : null;
			}
		});
	}
	
	/**
	 * Wait and get element when the text is not blank
	 * Default time out
	 * This function is used to fix issue get text of element when it is blank
	 */
	public static WebElement waitAndGetElementUntilTextNotBlank(final By by) {
		return waitAndGetElementUntilTextNotBlank(by,  LoadingConstants.RENDER_ELEMENT);
	}
	
	public static WebElement waitAndGetElementUntilTextRender(final By by) {
		return waitAndGetElementUntilTextRender(by,  LoadingConstants.RENDER_ELEMENT);
	}
	
	
	/**
	 * Wait and get element when the text is not blank
	 * http://jira-stx.elca.ch/jira/browse/STX-86481
	 * This function is used to fix issue get text of element when it is blank
	 */
	public static WebElement waitAndGetElementUntilTextNotBlank(final By by, long timeout) {
		final FluentWait<WebDriver> wait = new FluentWait<WebDriver>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.withMessage("Timeout while waiting element has text" + by);
		return wait.until(new Function<WebDriver, WebElement>() {
			@Override
			public WebElement apply(WebDriver driver) {
				WebElement element = waitAndGetElement(by, timeout);
				if(JSComponentHelper.getValueJS(element).length() > 0) {
					return element;
				}
				else
				{
					return null;
				}
			}
		});
	}
	
	public static WebElement waitAndGetElementUntilTextRender(final By by, long timeout) {
		final FluentWait<WebDriver> wait = new FluentWait<WebDriver>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.withMessage("Timeout while waiting element has text" + by);
		return wait.until(new Function<WebDriver, WebElement>() {
			@Override
			public WebElement apply(WebDriver driver) {
				WebElement element = waitAndGetElement(by);
				if(element.getText() == null || element.getText() == "")
					return null;
				else return element;
			}
		});
	}

	public static List<WebElement> waitAndGetElements(final WebElement parent, final By by, long timeout) {
		return new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.withMessage(String.format("Timeout while waiting for elements %s located under %s", by, parent))
				.until(driver -> parent.findElements(by));
	}

	public static void waitForAlertAndAccept() {
		waitForAlertPresent().accept();
	}
	
	public static void waitForAlertAndAccept(String alertContent) {
		Alert alert = waitForAlertPresent();
		String actualMsg = alert.getText();
		assertTrue(String.format("Alert content does not have : %s\n\rAlert : %s", alertContent, actualMsg), actualMsg.contains(alertContent));
		alert.accept();
	}

	public static String waitForAlertAndGetText() {
		return waitForAlertPresent().getText();
	}

	private static Alert waitForAlertPresent() {
		new WebDriverWait(getDriver(), 20)
				.ignoring(NoAlertPresentException.class)
				.until(ExpectedConditions.alertIsPresent());
		return getDriver().switchTo().alert();
	}

	/**
	 * Some elements are never clickable. We can use {@link #waitAndGetElement(By)} then click().
	 */
	public static void waitAndClick(final By locator) {
		waitAndClick(locator, LoadingConstants.RENDER_ELEMENT);
	}

	/**
	 * Some elements are never clickable. We can use {@link #waitAndGetElement(By, long)} then click().
	 */
	public static void waitAndClick(final By locator, long timeout) {
		waitForElementExist(locator, timeout);
		waitForElementClickable(locator, timeout);
		click(getDriver().findElement(locator));
	}

	public static void waitForElementClickable(final By locator, long timeout) {
		new WebDriverWait(getDriver(), timeout).until(ExpectedConditions.elementToBeClickable(locator));
	}

	public static void waitForElementClickable(final By locator) {
		waitForElementClickable(locator, LoadingConstants.UPDATE_BUTTON);
	}
	
	public static void waitForElementClickable(final WebElement webelement) {
		new WebDriverWait(getDriver(), LoadingConstants.UPDATE_BUTTON).until(ExpectedConditions.elementToBeClickable(webelement));
	}

	public static void clickUntilOK(final By locator, long timeout, long poolingTime,
			final Predicate<WebDriver> condition) {
		FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.pollingEvery(Duration.ofSeconds(poolingTime)).withMessage("Failed to retry click.")
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				if (condition.apply(driver)) {
					return true;
				}
				try {
					getDriver().findElement(locator).click();
				} catch (WebDriverException e) {
					LOGGER.info("Cannot click the element because it disappears. Continue test." + e.getMessage());
					// return false;
				}
				return false;
			}
		});
	}

	public static WebElement findElement(final By by) {
		return getDriver().findElement(by);
	}

	public static WebElement findElement(final WebElement parent, final By by) {
		return parent.findElement(by);
	}

	public static List<WebElement> findElements(final By by) {
		long start = System.currentTimeMillis();
		try {
			return WebdriverHelper.logTime(by.toString(), () -> getDriver().findElements(by));
		}finally {
			if (System.currentTimeMillis() - start > 15_000) {
				getDriverSession(). setGridSlow(true);
			}
		}
	}

	public static List<WebElement> findElements(final WebElement parent, final By by) {
		return parent.findElements(by);
	}

	/**
	 * <b> Please use this carefully! </b> Click element is unstable on IE and Firefox. We have to get focus before
	 * click. This method intends to fix Selenium problem with click action. However, this action is hang out because of
	 * moveToElement when the element has disappeared (Selenium is hang out instead of StaleElementReferenceException).
	 * We are back to element.click() for those cases.
	 *
	 * @param element
	 */
	public static void click(final WebElement element) {
		// Try to use another way.
		try {
			element.click();
		} catch (WebDriverException ex) {
			LOGGER.info("Element is not clickable. Try to click by Javascript.");
			JavascriptExecutor executor = (JavascriptExecutor) getDriver();
			executor.executeScript("arguments[0].click()", element);
		}
	}

	public static void clickWithReleased(final WebElement element) {
		// To avoid "Cannot press more then one button or an already pressed
		// button"
		try {
			new Actions(getDriver()).release(element).perform();
		} catch (Exception e) {
			// No problem. Continue to click the element.
		}
		click(element);
	}

	public static void clickByJS(By locator) {
		JSComponentHelper.click(locator);
	}

	public static void clickByJS(WebElement element) {
		JSComponentHelper.click(element);
	}

	public static void click(final By locator) {
		click(findElement(locator));
	}

	/**
	 * Send key by TAB
	 */
	public static void sendKeysTab(final By locator) {
		findElement(locator).sendKeys(Keys.TAB);
	}

	public static void sendKeysTab(final WebElement inputElement) {
		inputElement.sendKeys(Keys.TAB);
	}

	/**
	 * Send key by Ctr+C
	 */
	public static void sendKeysCtrC(final WebElement inputElement) {
		inputElement.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.chord(Keys.CONTROL, "c"));
	}

	/**
	 * Send key by Ctr+V
	 */
	public static void sendKeysCtrV(final WebElement inputElement) {
		inputElement.clear();
		Actions action = new Actions(getDriver());
		inputElement.sendKeys(Keys.chord(Keys.CONTROL, "v"));
		action.build().perform();
	}

	public static void waitForElementExist(final By locator) {
		waitForElementExist(locator, LoadingConstants.LOADING_TIMEOUT);
	}

	public static void waitForElementExist(final By locator, final long timeout) {
		FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.ignoring(StaleElementReferenceException.class)
				.withMessage(String.format("Timed out after %s seconds waiting for element %s", timeout, locator));
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return !getDriver().findElements(locator).isEmpty();
			}
		});
	}
	
	public static void waitForElementExist(final By locator, final long timeout, final Duration interval) {
		FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.ignoring(StaleElementReferenceException.class)
				.pollingEvery(interval)
				.withMessage(String.format("Timed out after %s seconds waiting for element %s", timeout, locator));
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return !getDriver().findElements(locator).isEmpty();
			}
		});
	}

	public static WebElement waitForElementRefreshedAndGetIt(final WebElement element, final long timeout) {
		return new WebDriverWait(getDriver(), timeout)
				.pollingEvery(Duration.ofMillis(50))
				.ignoring(StaleElementReferenceException.class)
				.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(element)));
	}

	public static void waitForAnyElementExist(final By... locators) {
		waitForAnyElementExist(10L, locators);
	}

	private static void waitForAnyElementExist(final long timeout, final By... locators) {
		FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.ignoring(StaleElementReferenceException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				for (By locator : locators) {
					if (!getDriver().findElements(locator).isEmpty()) {
						return true;
					}
				}
				return false;
			}
		});
	}

	public static void waitForElementNotExist(final By locator, final long timeout) {
		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.withMessage("Element is still existing")
				.ignoring(StaleElementReferenceException.class);
		LOGGER.info(
				"[END wait for element invisible]. Please structure the test if there is too many waits continuously.");
	}

	/**
	 * Try to find the locator until it is not stale on screen. This implies that the element exists on screen.
	 */
	public static void waitForElementNotStale(final By locator, final long timeout) {
		final FluentWait<WebDriver> wait =
				new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout));
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					WebElement element = driver.findElement(locator);
					element.isEnabled();
					return true;
				} catch (StaleElementReferenceException e) {
					LOGGER.info("Waiting. Element is stale: " + locator);
				} catch (NoSuchElementException e) {
					LOGGER.info("Waiting. Element does not exist: " + locator);
				}
				return false;
			}
		});
	}

	/**
	 * Wait until the attribute contains the expected value
	 */
	public static void waitForElementAttributeContains(final By locator, final long timeout, final String attribute,
			final String expectedContainedValue) {
		waitForElementAttributeContainsOneOf(locator, timeout, attribute, expectedContainedValue);
	}

	/**
	 * Wait until the attribute contains one of the expected values
	 */
	private static void waitForElementAttributeContainsOneOf(final By locator, final long timeout,
															 final String attribute, final String... expectedContainedValues) {
		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(timeout))
				.ignoring(StaleElementReferenceException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				final String attributeValue = getDriver().findElement(locator).getAttribute(attribute);
				for (String expectedContainedValue : expectedContainedValues) {
					if (attributeValue.contains(expectedContainedValue)) {
						return true;
					}
				}
				return false;
			}
		});
	}
	
	/**
	 * Wait and find elements until elements equal texts
	 *
	 * @param by
	 * @return
	 */
	public static List<WebElement> waitAndGetElementsEqualValue(final By by, final String...inputTextList) {
		FluentWait<WebDriver> wait = new FluentWait<WebDriver>(getDriver()).withTimeout( Duration.ofSeconds(LoadingConstants.RENDER_ELEMENT_LONG_TIMEOUT))
				.withMessage("Failed to wait elements equal texts")
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				List<WebElement> matchingTextList = waitAndGetElements(by);
				Integer number = 0;
				for(String inputText : inputTextList) {
					inputText = inputText.toUpperCase();
					for(WebElement matchingText : matchingTextList) {
						String actualMatchingText = matchingText.getText().trim().toUpperCase();
						if(actualMatchingText.equals(inputText)) {
							number++;
							break;
						}
					}
				}
				
				if(number>= inputTextList.length) {
					return true;
				}
				else 
					return false;								
			}
		});
		
		return waitAndGetElements(by, LoadingConstants.RENDER_ELEMENT);
	}
	
	/**
	 * Wait elements until elements contain texts
	 *
	 * @param by
	 * @return
	 */
	public static List<WebElement> waitElementsContainValue(final By by, final String...inputTextList) {
		FluentWait<WebDriver> wait = new FluentWait<WebDriver>(getDriver()).withTimeout(Duration.ofSeconds(LoadingConstants.RENDER_ELEMENT_LONG_TIMEOUT))
				.withMessage("Failed to wait elements contain texts")
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				List<WebElement> matchingTextList = waitAndGetElements(by);
				Integer number = 0;
				for(String inputText : inputTextList) {
					inputText = inputText.toUpperCase();
					for(WebElement matchingText : matchingTextList) {
						String actualMatchingText = matchingText.getText().trim().toUpperCase();
						if(actualMatchingText.contains(inputText)) {
							number++;
							break;
						}
					}
				}
				
				if(number>= inputTextList.length) {
					return true;
				}
				else 
					return false;								
			}
		});
		
		return waitAndGetElements(by, LoadingConstants.RENDER_ELEMENT);
	}


	public static void waitForElementAttributesNotContains(final By locator, final String attribute, final String expectedNotContainedValues) {
		waitForElementAttributesNotContains(WebElementHelper.waitAndGetElement(locator), attribute, expectedNotContainedValues);
	}

	/**
	 * Wait until the attribute not contains expected values
	 */
	public static void waitForElementAttributesNotContains(WebElement webElement, String attribute, String expectedNotContainedValues) {
		new FluentWait<>(getDriver())
				.withTimeout(Duration.ofSeconds(10))
				.ignoring(StaleElementReferenceException.class)
				.until(d -> !webElement.getAttribute(attribute).contains(expectedNotContainedValues));
	}

	public static void waitForElementAttributeContains(WebElement webElement, String attribute, String expectedContainedValues) {
		new FluentWait<>(getDriver())
				.withTimeout(Duration.ofSeconds(10))
				.ignoring(StaleElementReferenceException.class)
				.until(d -> webElement.getAttribute(attribute).contains(expectedContainedValues));
	}

	private static boolean isElementInvisible(final By identifier) {
		try {
			List<WebElement> elements = getDriver().findElements(identifier);
			if (elements.isEmpty()) {
				return true;
			} else {
				String style = elements.get(0).getAttribute("style");
				return !elements.get(0).isDisplayed() || style.contains("visibility: hidden")
						|| style.contains("display: none");
			}
		} catch (Exception e) {
			LOGGER.info("Exception when checking mask." + e.getMessage());
			return false;
		}
	}

	public static WebElement waitForElementVisible(By locator) {
		return waitForElementVisible(locator, 60);
	}

	public static WebElement waitForElementVisible(WebElement element) {
		return waitForElementVisible(element, 60);
	}

	public static WebElement waitForElementVisible(By locator, long timeout) {
		return new WebDriverWait(getDriver(), timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	private static WebElement waitForElementVisible(WebElement element, long timeout) {
		return new WebDriverWait(getDriver(), timeout).until(ExpectedConditions.visibilityOf(element));
	}

	public static void waitForElementInvisible(By locator, long timeout) {
		waitForElementNotExist(locator, timeout);
	}

	public static void waitForElementInvisible(WebElement element, long timeout) {
		new WebDriverWait(getDriver(), timeout).until(ExpectedConditions.invisibilityOf(element));
	}

	public static void waitForElementStoppedMoving(final By locator, long timeout) {
		waitForElementStoppedMoving(getDriver().findElement(locator), timeout);
	}
	
	public static void waitForElementStoppedMoving(final WebElement webElement, long timeout) {
		new WebDriverWait(getDriver(), timeout).pollingEvery(Duration.ofSeconds(1))
		.until(new Function<WebDriver, Boolean>() {
			Point position;

			@Override
			public Boolean apply(WebDriver driver) {
				try {
					if (position != null && webElement.getLocation().equals(position)) {
						return true;
					}
					position = webElement.getLocation();
					return false;
				} catch (StaleElementReferenceException se) {
					return false;
				}
			}
		});
	}

	public static void hoverElement(WebElement webElement) {
		new Actions(getDriver()).moveToElement(webElement).perform();
	}

	public static void hoverElement(By by) {
		hoverElement(getDriver().findElement(by));
	}

	public static void moveToAndClick(final By locator) {
		waitForElementExist(locator);

		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(60))
				.ignoring(StaleElementReferenceException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				try {
					waitForElementClickable(locator, 30);
					new Actions(getDriver()).moveToElement(findElement(locator)).click()
							.build().perform();
					return true;
				} catch (WebDriverException e) {
					return false;
				}
			}
		});
	}

	public static void moveElement(By by, int moveX, int moveY) {
		new Actions(getDriver()).dragAndDropBy(getDriver().findElement(by), moveX, moveY).perform();
	}

	public static void moveElement(WebElement webElement, int moveX, int moveY) {
		new Actions(getDriver()).dragAndDropBy(webElement, moveX, moveY).perform();
	}

	public static void focusElement(By by) {
		new Actions(getDriver()).moveToElement(getDriver().findElement(by)).perform();
	}

	public static void focusElement(WebElement webElement) {
		new Actions(getDriver()).moveToElement(webElement).perform();
	}

	public static void focusAndClickElement(WebElement webElement) {
		new Actions(getDriver()).moveToElement(webElement).click().perform();
	}

	public static String getText(WebElement parent, By by) {
		List<WebElement> children = parent.findElements(by);
		return children.size() > 0 ? children.get(0).getText() : "";
	}

	public static void dragAndDrop(WebElement dragElement, WebElement dropToElement) {
		Actions dragAndDropAction = new Actions(getDriver());
		dragAndDropAction.clickAndHold(dragElement).pause(2000).moveToElement(dropToElement, 20, 20).pause(1000).release(dropToElement).build().perform();
	}

	public static void dragAndDrop(WebElement dragElement, int xOffset, int yOffset) {
		Actions dragAndDropAction = new Actions(getDriver());
		dragAndDropAction.dragAndDropBy(dragElement,xOffset, yOffset).build().perform();
	}
	
	public static void clickCoordinates(int xOffset, int yOffset) {
		Actions action = new Actions(getDriver());
		action.moveByOffset(xOffset, yOffset).click().build().perform();
	}

	public static void refresh() {
		getDriver().navigate().refresh();
	}
	
	public static void back() {
		getDriver().navigate().back();
	}
	
	public static String getTitlePageDiffersText(String value) {
		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofMinutes(2))
				.pollingEvery(Duration.ofSeconds(1));
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				if(getDriver().getTitle() != "" && !getDriver().getTitle().contains(value))
					return true;
				return false;
			}
		});
		String title = getDriver().getTitle();
		LOGGER.debug("Title : " + title);
		return title;
	}
	
	/**
	 * This method allows empty list.
	 */
	public static List<String> getListText(final By selector) {
		List<WebElement> list = getDriver().findElements(selector);
		List<String> result = new ArrayList<>();
		for (WebElement ele : list) {
			result.add(ele.getText());
		}
		return result;
	}

	public static void doubleClick(WebElement element) {
		final Actions action = new Actions(getDriver());
		action.doubleClick(element);
		action.perform();
	}

	public static void doubleClickByJS(final WebElement element) {
		// Try to use another way.
		try {			
			element.click();
			element.click();
		} catch (WebDriverException ex) {
			LOGGER.debug("Element is not clickable. Try to click by Javascript.");
			JavascriptExecutor executor = (JavascriptExecutor) getDriver();
			executor.executeScript("arguments[0].ondblclick()", element);
		}
	}

	public static void doubleClick(By elementLocator) {
		final Actions action = new Actions(getDriver());
		action.doubleClick(findElement(elementLocator));
		action.perform();
	}

	public static void waitAndDoubleClick(By elementLocator) {
		waitAndGetElement(elementLocator);
		doubleClick(elementLocator);
	}

	public static void clearCookie() {
		LOGGER.info("Clear cookie (instead of logging out)");
		getDriver().manage().deleteAllCookies();
	}

	public static boolean clickIfExist(By locator) {
		if (!findElements(locator).isEmpty()) {
			LOGGER.info(String.format("Click on [%s]", locator.toString()));
			WebElementHelper.findElement(locator).click();
			return true;
		}
		return false;
	}

	public static String waitAndGetText(final By by, final long timeout) {
		return waitAndGetElement(by, timeout).getText();
	}

	public static String waitAndGetText(final By by) {
		return waitAndGetText(by, LoadingConstants.RENDER_ELEMENT);
	}
	
	public static String getTextFirstElementDisplay(final By by) {
		return filterOutInvisibleElements(by).getText();
	}

	public static void clearAndSetText(By locator, String text) {
		WebElement input = waitAndGetElement(locator);
		input.clear();
		input.sendKeys(text);
	}

	public static String getAttributeValue(final By locator, final String attributeName) {
		waitForElementExist(locator);
		return getDriver().findElement(locator).getAttribute(attributeName);
	}

	public static String waitAndGetAttributeValue(final By locator, final String attributeName) {
		LOGGER.info(String.format("Wait for [%s]", locator.toString()));

		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(90))
				.ignoring(NoSuchElementException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				if (!driver.findElements(locator).isEmpty()) {
					return !"".equals(driver.findElements(locator).get(0).getAttribute(attributeName));
				}
				return false;
			}
		});

		return getDriver().findElement(locator).getAttribute(attributeName);
	}

	public static String getAttributeValue(final WebElement element, String attributeName) {
		return element.getAttribute(attributeName);
	}

	public static void waitForURLContains(final String... texts) {
		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver())
				.withTimeout(Duration.ofSeconds(LoadingConstants.LOADING_TIMEOUT)).ignoring(NoSuchElementException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				for (String text : texts) {
					if(!driver.getCurrentUrl().contains(text)) return false;
				}
				return true;
			}
		});
	}

	public static void maximizeWindow() {
		getDriver().manage().window().maximize();
	}
	
	public static void setSizeWindow(int width, int height) {
		 Dimension n = new Dimension(width, height);  
		 getDriver().manage().window().setSize(n);
	}

	public static void tryToClickUntilDisappear(final By locator) {
		final FluentWait<WebDriver> wait =
				new FluentWait<>(getDriver()).withTimeout(Duration.ofSeconds(LoadingConstants.RENDER_ELEMENT))
				.pollingEvery(LoadingConstants.LOADING_POLLING, TimeUnit.SECONDS);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				if (!driver.findElements(locator).isEmpty()) {
					LOGGER.info("Click the element");
					driver.findElement(locator).click();
				}
				return driver.findElements(locator).isEmpty();
			}
		});
	}

	/**
	 * Waiting explicitly some seconds is NEVER a good solution. Please check with development team to have mask on
	 * screen until it's available to work.
	 */
	@Deprecated
	public static void waitExplicitly(final long timeInSeconds) {
		try {
			Thread.sleep(timeInSeconds * 1000);
		} catch (InterruptedException e) {
			LOGGER.info("Try to wait in " + timeInSeconds + " but failed.", e);
		}
	}

	public static void switchToDefaultContent() {
		getDriver().switchTo().defaultContent();
	}

	public static void moveToAndSendKeys(final By locator, final int positionIndexFrom0, final String value) {
		WebElement element = WebElementHelper.waitAndGetElements(locator).get(positionIndexFrom0);
		new Actions(getDriver()).moveToElement(element).click().sendKeys(value).perform();
	}

	public static Boolean isElementExisting(final By locator) {
		return getDriver().findElements(locator).size() > 0;
	}
	
	public static Boolean isElementDisplayed(final By locator) {
		return getDisplayedElements(locator).size() > 0;
	}

	/**
	 * This methods scrolls to element and from there scroll by an offset
	 *
	 * @param element
	 *            to scroll to
	 */
	public static void scrollToElement(WebElement element, int xoffset, int yoffset) {
		new Actions(getDriver()).moveToElement(element).perform();
		JSComponentHelper.excuteJavaScript(String.format("window.scrollBy(%s,%s)", xoffset, yoffset));
	}

	/**
	 * To be used when there are more than one element, but other elements aren't visible
	 * @return WebElement displayed element satisfying provided locator or throws exception if nothing is found
	 */
	public static WebElement filterOutInvisibleElements(final By by) {
		return new WebDriverWait(getDriver(), LoadingConstants.RENDER_ELEMENT)
		.ignoring(StaleElementReferenceException.class)
		.withMessage(String.format("No element By %s was found or no such element was displayed", by))
		.until(driver -> findElements(by)
				.stream()
				.filter(WebElement::isDisplayed)
				.findFirst().orElse(null));
	}
	
	public static WebElement filterOutInvisibleElements(WebElement parent, final By by) {
		return new WebDriverWait(getDriver(), LoadingConstants.RENDER_ELEMENT)
		.ignoring(StaleElementReferenceException.class)
		.withMessage(String.format("No element By %s was found or no such element was displayed", by))
		.until(driver -> findElements(parent, by)
				.stream()
				.filter(WebElement::isDisplayed)
				.findFirst().orElse(null));
	}

	public static List<WebElement> filterOutElementsBySize(final By by) {
		if(WebElementHelper.findElements(by).isEmpty())
		new RuntimeException(String.format("No element By %s was found or no such element was displayed", by));
		return WebElementHelper.findElements(by).stream().filter(element -> element.getSize().getWidth()>0 && element.getSize().getHeight()>0).collect(Collectors.toList());

	}
	
	public static List<String> getDisplayedElements(final By by) {
		List<WebElement> newList = WebElementHelper.findElements(by).stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
		return newList.stream().map(el -> el.getText()).collect(Collectors.toList());
	}
	
	public static List<String> getVisibleElements(final By by) {
		List<WebElement> newList = WebElementHelper.findElements(by).stream().filter(WebElement::isEnabled).collect(Collectors.toList());
		return newList.stream().map(el -> el.getText()).collect(Collectors.toList());
	}

	public static List<WebElement> waitForElementsListSize(final By by, int size, final long timeout) {

		return new WebDriverWait(getDriver(), timeout)
				.pollingEvery(Duration.ofSeconds(2))
				.ignoring(StaleElementReferenceException.class)
				.until(ExpectedConditions.numberOfElementsToBe(by,size));

	}

	public static WebElement waitForElementValueIsNotEmpty(By by){
		new WebDriverWait(getDriver(), 10)
				.pollingEvery(Duration.ofSeconds(2))
				.ignoring(StaleElementReferenceException.class)
				.until(e->!JSComponentHelper.getValueJS
						(WebElementHelper
								.waitAndGetElement(by))
						.isEmpty());
		return WebElementHelper.waitAndGetElement(by);
	}

	public static WebElement waitForElementTextIsNotEmpty(By by){
		new WebDriverWait(getDriver(), 10)
				.pollingEvery(Duration.ofSeconds(2))
				.ignoring(StaleElementReferenceException.class)
				.until(e->!WebElementHelper
								.waitAndGetElement(by)
						.getText()
						.isEmpty());
		return WebElementHelper.waitAndGetElement(by);
	}

	public static WebElement waitForElementTextHasText(By by, String text){
		new WebDriverWait(getDriver(), 10)
				.pollingEvery(Duration.ofSeconds(2))
				.ignoring(StaleElementReferenceException.class)
				.until(e->WebElementHelper
						.waitAndGetElement(by)
						.getText()
						.contains(text));
		return WebElementHelper.waitAndGetElement(by);
	}
	
	public static void doubleClickByJSOnSeatMap(WebElement canvas) {
		JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		executor.executeScript("var evt = document.createEvent('MouseEvents');"+ "evt.initMouseEvent('dblclick',true, true, window, 0, -20, -20, -20, -20, false, false, false, false, 0,null);"+ "arguments[0].dispatchEvent(evt);", canvas);
	}
	
	public static void scrollElementOnTop(WebElement element) {
		JavascriptExecutor je = (JavascriptExecutor) getDriver();
		je.executeScript("arguments[0].scrollIntoView({ block: 'start', inline: 'start' });",element);
	}
	
	public static void scrollElementOnDown(WebElement element) {
		JavascriptExecutor je = (JavascriptExecutor) getDriver();
		je.executeScript("arguments[0].scrollIntoView({ block: 'start', inline: 'end' });",element);
	}
	
	public static void waitForElementVisibleAndClickJs(By by) {
		waitForElementVisible(by);
		clickByJS(by);
	}
	
	public static void waitForLoadElement() {
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(getDriver(), 10);
        wait.until(pageLoadCondition);
    }
	
	public static void waitForElementDisplay(final By locator) {
		@SuppressWarnings("deprecation")
		final FluentWait<WebDriver> wait =
				new FluentWait<>(getDriver()).pollingEvery(LoadingConstants.LOADING_POLLING, TimeUnit.SECONDS) .withTimeout(Duration.ofSeconds(LoadingConstants.RENDER_ELEMENT));
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete") && (ExpectedConditions.visibilityOfElementLocated(locator) != null);
			}
		});
	
	}
	
	public static boolean verifyElementIsFocus(WebElement element) {
		 if(element.equals(getDriver().switchTo().activeElement()))
            return true;
		 else 
			 return false;
	}
	
	public static void pressKey(Keys key) {
		Actions builder = new Actions(getDriver());
	    Action select= builder
	    		.sendKeys(key)
	            .build();
	    select.perform();
	}
	
	public static void pressCombinationKey(Keys keyHold, Keys keyPress) {
		Actions builder = new Actions(getDriver());
	    Action select= builder
	    		.keyDown(keyHold)
	    		.sendKeys(keyPress)
	    		.keyUp(keyHold)
	            .build();
	    select.perform();
	}
	
	public static void scrollWindow(int offsetX, int offsetY) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		String expression = "window.scrollBy(" + offsetX + "," + offsetY + ")";
		js.executeScript(expression);
	}
	
	public static void rightClickOnElement(String elementID, int offsetX, int offsetY){
		WebElement element  = WebElementHelper.findElement(By.id(elementID));
		Actions actions = new Actions(getDriver());
		actions
				.moveToElement(element, offsetX, offsetY)
				.contextClick(element)
				.build()
				.perform();
	}
	
	public static void rightClickOnElement(String elementID){
		Actions actions = new Actions(getDriver());
		WebElement elementLocator = waitAndGetElement(By.id(elementID));
		actions.contextClick(elementLocator).perform();
	}
	
	public static void waitDatalayerContainsTexts(List<String> listText) {
		final FluentWait<WebDriver> wait =
				new FluentWait<>(getDriver()).pollingEvery(30, TimeUnit.SECONDS) .withTimeout(Duration.ofMinutes(2));
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				String dataLayer = String.valueOf(JSComponentHelper.excuteJavaScript("return window.dataLayer"));
				
				Boolean isContainAll = true;
				for(String text : listText) {
					Boolean isContain = dataLayer.contains(text);
					if(isContain == false) {
						LOGGER.debug("Datalayer :" + dataLayer);
						LOGGER.info("DataLayer does not contains : " + text);
					}
					isContainAll &= dataLayer.contains(text);
				}
				return isContainAll;
			}
		});
	}
	
}
