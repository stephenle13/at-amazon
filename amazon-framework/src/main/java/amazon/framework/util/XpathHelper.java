package amazon.framework.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class solves common issues with WebElement. <br>
 * Some listed issues which cannot be included in this helper, please see them as guidelines:
 * <li>In method com.secutix.boxoffice.selenium.page.frame.ResumeFileOperationsPopup.searchResumeFile we need to click a
 * tab. Each tab is a tag "li". When we click this tag, the element is invisible. This problem come from an invisible
 * tag "a" inside the "li". So, we should click another visible tag "a".</li>
 */
public class XpathHelper {
	private static final String CONTAINS_IGNORE_CASE =
			"contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜÉÈÊÀÁÂÒÓÔÙÚÛÇÅÏÕÑŒ','abcdefghijklmnopqrstuvwxyzäöüéèêàáâòóôùúûçåïõñœ'),'%s')";
	private static final String EQUALS_IGNORE_CASE =
			"translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜÉÈÊÀÁÂÒÓÔÙÚÛÇÅÏÕÑŒ','abcdefghijklmnopqrstuvwxyzäöüéèêàáâòóôùúûçåïõñœ')='%s'";
	private static final String CONTAINS = "contains(., '%s')";
	private static final String NOT_CONTAINS = "(not(contains(., '%s')))";
	private static final String EQUAL = ".='%s'";

	/***
	 * Ex: if addString contains ['], replace ['] to ["] original = //div[@id='option_list_box' and contains(.,'%s')]
	 * addString = I'm here return //div[@id="option_list_box" and contains(.,"%s")]
	 */
	public static String formatXpath(String original, String addString) {
		return addString.contains("'") ? original.replace("'", "\"") : original;
	}

	/***
	 * Return not null value array
	 */
	private static String[] filterNotNullItems(String... textInLineItems) {
		List<String> processedLineItems = new ArrayList<>();
		for (String lineItem : textInLineItems) {
			if (lineItem != null && !"".equals(lineItem)) {
				processedLineItems.add(lineItem);
			}
		}
		return processedLineItems.toArray(new String[0]);
	}

	/**
	 * @param andOr
	 *            = "and" or "or"
	 * @param lineItems
	 *            = {"a", "b", "I'm"}
	 * @return contains(., 'a') andOr contains(., 'b') andOr contains(., "I'm")
	 */
	private static String contains(String andOr, String... lineItems) {
		String[] items = filterNotNullItems(lineItems);
		String andContain = "";
		for (int i = 1; i < items.length; i++) {
			String xpath = formatXpath(CONTAINS, items[i]);
			xpath = String.format(" %s " + xpath, andOr, items[i]);
			andContain = String.format("%s %s", andContain, xpath);
		}
		String contains = formatXpath(CONTAINS, items[0]);
		return String.format(contains + "%s", items[0], andContain);
	}

	/**
	 * @param andOr
	 *            = "and" or "or"
	 * @param lineItems
	 *            = {"a", "b", "I'm"}
	 * @return not(contains(., 'a')) andOr not(contains(., 'b')) andOr not(contains(., "I'm"))
	 */
	private static String notContains(String andOr, String... lineItems) {
		String[] items = filterNotNullItems(lineItems);
		String andNotContain = "";
		for (int i = 1; i < items.length; i++) {
			String xpath = formatXpath(NOT_CONTAINS, items[i]);
			xpath = String.format(" %s " + xpath, andOr, items[i]);
			andNotContain = String.format("%s %s", andNotContain, xpath);
		}
		String notContains = formatXpath(NOT_CONTAINS, items[0]);
		return String.format(notContains + "%s", items[0], andNotContain);
	}

	/**
	 * @param lineItems
	 *            = {"a", "b", "I'm"}
	 * @return contains(., 'a') and contains(., 'b') and contains(., "I'm")
	 */
	public static String contains(String... lineItems) {
		return contains("and", lineItems);
	}

	public static String containsOr(String... lineItems) {
		return contains("or", lineItems);
	}

	public static String startsWith(String... lineItems) {
		String result = contains("and", lineItems);
		return result.replace("contains", "starts-with");
	}

	public static String endsWith(String str) {
		return String.format("//span[substring(., string-length(.)- string-length('%s') +1) = '%s']", str, str);
	}

	/**
	 * @param items
	 *            = {"a", "b", "I'm"}
	 * @return contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'a') and
	 *         contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'b') and
	 *         contains(translate (.,"ABCDEFGHIJKLMNOPQRSTUVWXYZ","abcdefghijklmnopqrstuvwxyz"),"i'm")
	 */
	public static String containsIgnoreCase(String... items) {
		return Lists.newArrayList(items).stream().filter(StringUtils::isNotEmpty).map(new Function<String, String>() {
			@Override
			public String apply(String item) {
				return String.format(formatXpath(CONTAINS_IGNORE_CASE, item),item.toLowerCase());
			}
		}).collect(Collectors.joining(" and "));
	}

	public static String equalsIgnoreCase(String... items) {
		String andContain = "";
		String itemToLowerCase = "";

		for (int i = 1; i < items.length; i++) {
			itemToLowerCase = items[i].toLowerCase();
			String xpath = formatXpath(EQUALS_IGNORE_CASE, itemToLowerCase);
			xpath = String.format(" and " + xpath, itemToLowerCase);
			andContain = String.format("%s %s", andContain, xpath);
		}
		itemToLowerCase = items[0].toLowerCase();
		String contains = formatXpath(EQUALS_IGNORE_CASE, itemToLowerCase);
		return String.format(contains + "%s", itemToLowerCase, andContain);
	}

	public static String equalTextValue(String itemText) {
		return String.format(formatXpath(EQUAL, itemText), itemText);
	}

	public static String notContains(String... lineItems) {
		return notContains("and", lineItems);
	}
}
