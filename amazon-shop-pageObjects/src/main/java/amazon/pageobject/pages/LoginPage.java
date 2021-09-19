package amazon.pageobject.pages;

import amazon.framework.util.WebElementHelper;
import amazon.pageobject.login.Credentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;

public class LoginPage extends AbstractShopPage {
    protected final Log logger = LogFactory.getLog(LoginPage.class);

    private static final By usernameLocator = By.id("ap_email");
    private static final By passwordInputLocator = By.id("ap_password");
    private static final By continueButtonLocator = By.id("continue");
    private static final By submitButtonLocator = By.id("signInSubmit");

    public void login(Credentials credentials) {
        logger.info(String.format("B2C login : %s/%s", credentials.getLogin(), credentials.getPassword()));
        WebElementHelper.sendKeysAndCheckLocated(usernameLocator, credentials.getLogin());
        WebElementHelper.click(continueButtonLocator);
        WebElementHelper.sendKeysAndCheckLocated(passwordInputLocator, credentials.getPassword());
        WebElementHelper.click(submitButtonLocator);
        waitForLoading();
    }

    @Override
    protected String getUrlRegex() {
        return ".*/ap/signin.*";
    }

    @Override
    protected String getPageTitleRegexp() {
        return "Amazon Sign-In";
    }

}
