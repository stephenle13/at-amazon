package amazon.pageobject.pages;

import amazon.framework.util.DateUtils;
import amazon.framework.util.JSComponentHelper;
import amazon.framework.util.WebElementHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.*;

public class ShopPage extends AbstractShopPage {

    protected final Log logger = LogFactory.getLog(LoginPage.class);

    private static final By searchByTypeDropdownBox = By.id("searchDropdownBox");
    private static final By productNameInput = By.id("twotabsearchtextbox");
    private static final By searchButton = By.id("nav-search-submit-button");
    private static final By sortByTypeDropdownBox = By.id("s-result-sort-select");

    @Override
    protected String getUrlRegex() {
        return ".*nav_ya_signin&.*";
    }

    @Override
    protected String getPageTitleRegexp() {
        return ".*Amazon.com.*";
    }

    public void selectProductType(final String type) {
        logger.info(String.format("Select product type: %s", type));
        WebElementHelper.click(searchByTypeDropdownBox);
        new Select(WebElementHelper.waitAndGetElement(searchByTypeDropdownBox)).selectByVisibleText(type);
    }

    public void inputProductName(final String productName) {
        logger.info(String.format("Enter product name: %s", productName));
        WebElementHelper.sendKeysAndCheck(WebElementHelper.waitForElementVisible(productNameInput), productName);
    }

    public void clickOnSearchButton() {
        logger.info("Click on search button");
        JSComponentHelper.click(searchButton);
    }

    public void filterByLanguage(final String language) {
        logger.info(String.format("Filer by language: %s", language));
        JSComponentHelper.scrollToElement(By.xpath(String.format("//*[@id=\"filters\"]/../..//span[contains(., '%s')]", language)));
        JSComponentHelper.click(By.xpath(String.format("//*[@id=\"filters\"]/../..//span[contains(., '%s')]", language)));
    }

    public void searchProduct(final String type, final String productName, final String language) {
        selectProductType(type);
        inputProductName(productName);
        clickOnSearchButton();
        waitForLoading();
        filterByLanguage(language);
    }

    public int verifyResultNumber() {
        int count = WebElementHelper.findElements(By.xpath("//div[contains(@class,'s-main-slot s-result-list s-search-results')]/div[contains(@class,'s-asin')]")).size();
        logger.info(String.format("No. of result per page: %s", count));
        return count;
    }

    public void sortBy(final String type) {
        logger.info(String.format("Select sort by: %s", type));
        WebElementHelper.click(sortByTypeDropdownBox);
        new Select(WebElementHelper.waitAndGetElement(sortByTypeDropdownBox)).selectByVisibleText(type);
        waitForLoading();
    }

    public void verifySortedPublicationDate() {
        logger.info("Try to get sorted list ..");
        List<WebElement> elements = WebElementHelper.waitAndGetElements(By.xpath("//div[@class='a-row' and .//span[contains(., 'by')]]"));

        List<String> listByString = new ArrayList<>();
        for(WebElement e : elements) {
            String productLineInfo = e.getText().trim();
            String publicationDateItem = productLineInfo.substring(productLineInfo.lastIndexOf(" | ")+1).replace("|", "").trim();
            listByString.add(publicationDateItem);
        }

        List<Date> listByDate = DateUtils.convertStringToDate(listByString);
        logger.info("Original listByDate: " + listByDate);

        List<Date> listBySortedDate = listByDate;
        Collections.sort(listBySortedDate, Comparator.reverseOrder());
        logger.info("Sorted list to compare: " + listBySortedDate);

        Assert.assertEquals(listBySortedDate, listByDate);
    }
}
