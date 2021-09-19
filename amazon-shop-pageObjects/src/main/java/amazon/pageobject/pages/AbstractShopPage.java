package amazon.pageobject.pages;

import amazon.framework.util.ButtonWidget;
import amazon.framework.util.LoadingConstants;
import amazon.framework.util.WebElementHelper;
import amazon.framework.util.WebdriverHelper;
import com.google.common.base.Function;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static amazon.framework.util.DriverSessionHolder.getDriver;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractShopPage {
	protected final Log logger = LogFactory.getLog(AbstractShopPage.class);

	protected static String productName;
	private static final String XPATH_SELECTOR_TIME = "//div[contains(@class, 'timeslot_time') and contains(.,'%s')]";
	private static final String CSS_SET_MAIN_TICKET_BUTTON = ".print_button_container_for_expand_panel .button.setAsMainTicket a";
	private static final String CSS_SET_BENEFICIARY_BUTTON = ".print_button_container_for_expand_panel .button.ticket_set_beneficiary a";

	@FindBy(css = ".print_button_container_for_expand_panel .button.setAsMainTicket a")
	private List<WebElement> nbTicketSetMainTicketButtons;

	public static void releaseProductName() {
		AbstractShopPage.productName = null;
	}
	// This needs to be below timeout value on grids, 2 minutes is already a
	// lot...
	public static final int TIMEOUT_SEC_PAGE_LOAD = 60;

	private static final String ERROR_PAGE_TITLE = "Error Page";

	protected AbstractShopPage() {
		waitForPageReady();
		AjaxElementLocatorFactory ajaxFactory = new AjaxElementLocatorFactory(getDriver(), 60);
		PageFactory.initElements(ajaxFactory, this);
		waitForPageLoad();
		WebElementHelper.printCurrentURL();
	}

	protected AbstractShopPage(String productName) {
		AbstractShopPage.productName = productName;
		waitForPageReady();
		AjaxElementLocatorFactory ajaxFactory = new AjaxElementLocatorFactory(getDriver(), 60);
		PageFactory.initElements(ajaxFactory, this);
		waitForPageLoad();
		WebElementHelper.printCurrentURL();
	}

	static public <T extends AbstractShopPage> T constructContentObject(Class<T> objectClass) {
		T result = WebdriverHelper.constructPageObject(objectClass);
		result.waitForPageLoad();
		return result;
	}
	protected void waitForPageLoad() {
	}

	protected void waitForPageReady() {
		final CurrentPagePredicate predicate = new CurrentPagePredicate(getUrlRegex(), getPageTitleRegexp());
		try {
			new WebDriverWait(getDriver(), TIMEOUT_SEC_PAGE_LOAD).until(predicate);
			releaseProductName();
		} catch (TimeoutException te) {
			throw new TimeoutException(predicate.getPredicateLog(), te);
		}
		WebdriverHelper.waitUntilDocumentReady();
	}

	protected abstract String getUrlRegex();

	protected abstract String getPageTitleRegexp();

	/**
	 * Wait for the mask disappear.
	 *
	 * @param timeoutInSeconds : time to wait
	 */
	protected void waitForLoading(int timeoutInSeconds) {
		WebElementHelper.waitForElementInvisible(By.cssSelector("pleaseWaitDialog"), LoadingConstants.LOADING_TIMEOUT);
		final FluentWait<WebDriver> wait = new FluentWait<>(getDriver())
				.withTimeout(Duration.ofSeconds(timeoutInSeconds)).pollingEvery(Duration.ofSeconds(5))
				.ignoring(StaleElementReferenceException.class);
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(final WebDriver driver) {
				return WebElementHelper.findElements(By.cssSelector(".ui-widget-overlay")).isEmpty();
			}
		});
	}

	/**
	 * Wait for the mask disappear in 120 seconds.
	 */
	public void waitForLoading(final String idLocator) {
		WebElementHelper.waitForElementExist(By.id(idLocator));
	}

	protected void waitForLoad(final String cssSelector) {
		WebElementHelper.waitForElementExist(By.cssSelector(cssSelector));
	}

	/**
	 * Wait for the mask disappear in 60 seconds.
	 */
	public void waitForLoading() {
		waitForLoading(60);
	}

	protected void verifyTextPresentOnPage(boolean present) {
		Assert.assertEquals(present,
				WebElementHelper.isElementExisting(By.xpath("//body//*[contains(text(),'error')]")));

	}

	/**
	 * click in the header to reach login page (accessible from any shop page) when
	 * not logged in
	 */
	public LoginPage toLoginPage() {
		logger.info("Go to login page.");
		By signInLocator = By.cssSelector("#nav-link-accountList");
		new ButtonWidget(signInLocator).click();
		return new LoginPage();
	}

	public void verifyLoggedIn(String loginUser, boolean isLogged) {
		logger.info(String.format("Verify [%s] %s in.", loginUser, isLogged ? "logged" : "un-logged"));
		List<WebElement> element = WebElementHelper
				.findElements(By.xpath("//span[contains(@class,'item_name') and contains(., '" + loginUser + "')]"));

		if (isLogged) {
			assertThat(element).isNotEmpty();
		} else {
			assertThat(element).isEmpty();
		}
	}

	public boolean isUserLoggedIn() {
		List<WebElement> element = WebElementHelper.findElements(By.xpath("//*[@class='item item_email']"));
		return element.size() > 0;
	}

	private static class CurrentPagePredicate implements Function<WebDriver, Boolean> {
		private final String urlRegexp;
		private final String titleRegexp;
		private String predicateLog;

		CurrentPagePredicate(final String urlRegexp, final String titleRegexp) {
			this.urlRegexp = urlRegexp;
			this.titleRegexp = titleRegexp;
		}

		@SuppressWarnings("boxing")
		@Override
		public Boolean apply(final WebDriver input) {
			final String currentUrl = getDriver().getCurrentUrl();
			if (currentUrl.matches(urlRegexp)) {
				String title = getDriver().getTitle();
				String[] titleArray = title.split("\\|");
				if (titleArray[titleArray.length - 1].trim().matches(ERROR_PAGE_TITLE)) {
					throw new IllegalStateException("Unexpected error page, stop test. Was expecting : " + titleRegexp);
				}
				try {
					String targetTitle = titleArray[titleArray.length - 1];
					Boolean isMatching = false;
					if (targetTitle.trim().matches(titleRegexp) || targetTitle.contains(titleRegexp)) {
						isMatching = true;
					} else {
						// check keyword title match title regexp
						String[] keyWordTitles = targetTitle.split(" - ");
						for (String keyWordTitle : keyWordTitles) {
							if (keyWordTitle.trim().matches(titleRegexp)) {
								isMatching = true;
								break;
							}
						}

						// check title contains regexp keyword
						String[] keywordTitleRegexs = titleRegexp.split("\\|");
						for (String keywordTitleRegex : keywordTitleRegexs) {
							if (title.contains(keywordTitleRegex)) {
								isMatching = true;
								break;
							}
						}
					}
					if (isMatching == false) {
						setPredicateLog("Unmatching title after navigation, current title : " + title
								+ " , expecting : " + titleRegexp);
					}
					return isMatching;
				} catch (NullPointerException e) {
					return true;
				}
			} else {
				setPredicateLog(
						"Unmatching url after navigation, current url : " + currentUrl + " , expecting : " + urlRegexp);
				return false;
			}
		}

		String getPredicateLog() {
			return predicateLog;
		}

		void setPredicateLog(String printOut) {
			this.predicateLog = printOut;
		}
	}
}
