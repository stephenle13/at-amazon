package amazon.b2c.searchProduct;

import amazon.framework.core.WebDriverSuppliers.SingleWebDriver;
import amazon.pageobject.pages.AbstractJavashopWebDriverTestCase;
import amazon.pageobject.login.Credentials;
import amazon.pageobject.pages.ShopPage;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class PaginatedTests extends AbstractJavashopWebDriverTestCase {
    /**
     * Pre-conditions: Provide an account to login Amazon page
     *
     * Scenario 1: Verify result list is paginated if there are more than 16 items
     * a. Perform a search with:
     *      i. Department: Books
     *      ii. Keyword: apple
     *      iii. Book Language: English
     * b. The Result displays exactly 16 items on each page.
     *
     * Scenario 2: Verify result list can be sorted on demand
     * a. Perform a search with:
     *      i. Department: Books
     *      ii. Keyword: apple
     *      iii. Book Language: English
     *      iv. Change sort option to Publication date
     * b. The Result is sorted by Publication date
     *
     */
    private static final String PRODUCT_TYPE = "Books";
    private static final String PRODUCT_NAME = "apple";
    private static final String LANGUAGE = "English";
    private static final String SORT_BY = "Publication Date";

    @Theory
    public void searchProductTests(@SingleWebDriver final WebDriverKind browser) throws InterruptedException {
        getWebDriver(browser);
        navigateAndLogin("https://www.amazon.com/", Credentials.LEPHUONGDY);
        ShopPage shopPage = new ShopPage();

        //Scenario 1:
        shopPage.searchProduct(PRODUCT_TYPE, PRODUCT_NAME, LANGUAGE);
        assertThat(shopPage.verifyResultNumber()).isEqualTo(16);

        //Scenario 2:
        shopPage.sortBy(SORT_BY);
        shopPage.verifySortedPublicationDate();

        System.out.println("Test finished successfully.");
    }
}
