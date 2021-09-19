package amazon.framework.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.openqa.selenium.*;

import static amazon.framework.util.DriverSessionHolder.getDriver;

/**
 * This class helps to work with javascript. Eg. execute a javascript command, getId, click, blur
 */
public class JSComponentHelper {
	private static final Log LOGGER = LogFactory.getLog(JSComponentHelper.class);
	public static String getValueJS(final String id) {
		if (StringUtils.isNotBlank(id)) {
			return String.valueOf(excuteJavaScript("return document.getElementById('" + id + "').value;"));
		} else {
			return null;
		}
	}

	public static String getValueJS(final WebElement webElement) {
		final String id = getId(webElement);
		if (StringUtils.isNotBlank(id)) {
			return String.valueOf(excuteJavaScript("return document.getElementById('" + id + "').value;"));
		} else {
			return null;
		}
	}

	public static void setValueJS(final WebElement webElement, String value) {
		final String id = getId(webElement);
		setValueJS(id, value);
	}

	public static void setValueJS(final String id, String value) {
		if (StringUtils.isNotBlank(id)) {
			String scriptText = XpathHelper.formatXpath("return document.getElementById('%s').value='%s';", value);
			excuteJavaScript(String.format(scriptText, id, value));
		} else {
			throw new InvalidElementStateException("The element should have an id to be set with this method");
		}
	}

	public static void setValueJS(final By locator, String value) {
		setValueJS(WebElementHelper.waitAndGetElement(locator), value);
	}

	public static void clearAndSetValueJS(final By locator, String value) {
		WebElement element = WebElementHelper.waitAndGetElement(locator);
		element.clear();
		setValueJS(element, value);
	}

	public static Object excuteJavaScript(final String script) {
		ObjectAssert<WebDriver> assertDriver = Assertions.assertThat(getDriver());
		assertDriver.isNotNull();
		assertDriver.isInstanceOf(JavascriptExecutor.class);
		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) getDriver();
		return javascriptExecutor.executeScript(script);

	}

	public static String getId(WebElement webElement) {
		Assertions.assertThat(webElement).isNotNull();
		return webElement.getAttribute("id");
	}

	/**
	 * Blur the input element to have fields update
	 *
	 * @param webElement
	 */
	public static void blur(WebElement webElement) {
		final String id = getId(webElement);
		if (StringUtils.isNotBlank(id)) {
			excuteJavaScript("return document.getElementById('" + id + "').blur();");
		}
	}

	public static void click(WebElement webElement) {
		final String id = getId(webElement);
		click(id);
	}

	public static void click(String id) {
		LOGGER.info(String.format("Button id [%s]", id));
		if (StringUtils.isNotBlank(id)) {
			LOGGER.info(String.format("Click by JavaScript on button id [%s]", id));
			excuteJavaScript("return document.getElementById('" + id + "').click();");
		}
	}

	public static void fireExtJsEvent(final String elementId, final String eventName, final String value) {
		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) getDriver();
		String script = "Ext.getCmp('%s').fireEvent('%s', %s);";
		javascriptExecutor.executeScript(String.format(script, elementId, eventName, value));
	}

	public static void scrollToElementByID(String elementId) {
		scrollToElement(By.id(elementId));
	}

	public static void scrollToElement(By locator) {
		JavascriptExecutor je = (JavascriptExecutor) getDriver();
		WebElement element = getDriver().findElement(locator);
		je.executeScript("arguments[0].scrollIntoView(true);", element);
	}

	public static void scrollToElement(WebElement webElement) {
		JavascriptExecutor je = (JavascriptExecutor) getDriver();
		je.executeScript("arguments[0].scrollIntoView(true);", webElement);
	}

	public static void zoomOnCanvasBlock(String iframeId, String mainBlock, String subBlock) {
		JavascriptExecutor je = (JavascriptExecutor) getDriver();
		je.executeScript(String.format("%s.zoomToBlock('%s', '%s');", iframeId, mainBlock, subBlock));
	}

	public static void selectSkybox(String iframeId, String skyboxId) {
		JavascriptExecutor je = (JavascriptExecutor) getDriver();
		je.executeScript(String.format("%s.selectSkybox('%s');", iframeId, skyboxId));
	}

	public static void click(By locator) {
		JavascriptExecutor executor = (JavascriptExecutor) getDriver();
		executor.executeScript("arguments[0].click()", getDriver().findElement(locator));
	}
	
	public static void sendMouseDownEvent(WebElement element) {
		String javaScript = "var evObj = document.createEvent('MouseEvents');"
                + "evObj.initMouseEvent(\"mousedown\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
                + "arguments[0].dispatchEvent(evObj);";
        ((JavascriptExecutor) getDriver()).executeScript(javaScript, element);
	}
	
	public static void mouseHover(WebElement element) {
		String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover',	true, false); " +
				"arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
        ((JavascriptExecutor) getDriver()).executeScript(mouseOverScript, element);
	}
}
