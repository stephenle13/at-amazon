package amazon.framework.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static amazon.framework.util.DriverSessionHolder.getDriver;

public class ButtonWidget {
	protected final Log logger = LogFactory.getLog(ButtonWidget.class);

	private final WebElement element;

	public ButtonWidget(final String id) {
		this(By.id(id));
	}

	public ButtonWidget(final WebElement element) {
		this.element = element;
	}

	public ButtonWidget(final By containerLocator) {
		this.element = WebElementHelper.waitAndGetElement(containerLocator);
	}

	public static ButtonWidget createByLink(By linkLocaltion) {
		WebElement container = WebElementHelper.waitAndGetElement(linkLocaltion).findElement(By.xpath(".."));
		return new ButtonWidget(container);
	}

	public void clickOnLink() {
		logger.info(String.format("Click on menu/link [%s]", element.getText()));
		WebElementHelper.click(element.findElement(By.tagName("a")));
	}

	public void click() {
		logger.info(String.format("Click on button [%s]", element.getText()));
		WebElementHelper.click(element);
	}

	public ButtonWidget waitForEnable() {
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(element));
		return this;
	}
}
