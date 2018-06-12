package jcucumberng.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.paulhammant.ngwebdriver.ByAngular;

import cucumber.api.Scenario;

/**
 * This class handles actions for interacting with web applications using the
 * Selenium WebDriver.
 * 
 * @author Kat Rollo <rollo.katherine@gmail.com>
 */
public final class Selenium {
	private Selenium() {
		// Prevent instantiation
	}

	/**
	 * Returns the By object based on the value of the key from the ui-map. <br>
	 * <br>
	 * Example:<br>
	 * <br>
	 * key = expense.name.txt<br>
	 * <br>
	 * value = by-model:expense.name<br>
	 * <br>
	 * by = ByAngular.model()<br>
	 * <br>
	 * The colon (:) is the delimiter between the by-type (e.g. by-model) and the
	 * locator (e.g. expense.name) or substring after the colon.
	 * 
	 * @param key
	 *            the key from the ui-map
	 * @return By - the By object
	 * @throws IOException
	 */
	public static By by(String key) throws IOException {
		String value = PropsLoader.readLocator(key);
		String locator = value.substring(value.lastIndexOf(":") + 1);
		By by = null;
		// TODO Add By-types as needed
		if (value.contains("by-css")) {
			by = By.cssSelector(locator);
		} else if (value.contains("by-model")) {
			by = ByAngular.model(locator);
		} else if (value.contains("by-binding")) {
			by = ByAngular.binding(locator);
		}
		return by;
	}

	/**
	 * Returns a List of all Select elements.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param selectLocatorKey
	 *            the key from the ui-map
	 * @return List - the List of Select elements
	 * @throws IOException
	 */
	public static List<Select> getSelectElements(WebDriver driver, String selectLocatorKey) throws IOException {
		List<WebElement> elements = driver.findElements(Selenium.by(selectLocatorKey));
		List<Select> selectElements = new ArrayList<>();
		for (WebElement element : elements) {
			selectElements.add(new Select(element));
		}
		return selectElements;
	}

	/**
	 * Returns all text inside the body tag in HTML.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @return String - the text within HTML body tags
	 */
	public static String getBodyText(WebDriver driver) {
		return driver.findElement(By.tagName("body")).getText();
	}

	/**
	 * Scroll the screen vertically.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param yPosition
	 *            positive value to scroll down, negative value to scroll up
	 */
	public static void scrollVertical(WebDriver driver, int yPosition) {
		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
		jsExecutor.executeScript("scroll(0, " + yPosition + ");");
	}

	/**
	 * Enters text into a textfield or textarea.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param fieldLocatorKey
	 *            the key from the ui-map
	 * @param text
	 *            the text to be entered
	 * @throws IOException
	 */
	public static void enterText(WebDriver driver, String fieldLocatorKey, String text) throws IOException {
		WebElement field = driver.findElement(Selenium.by(fieldLocatorKey));
		field.clear();
		field.sendKeys(text);
	}

	/**
	 * Enters text into a textfield or textarea.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param field
	 *            the element of the textfield or textarea
	 * @param text
	 *            the text to be entered
	 * @throws IOException
	 */
	public static void enterText(WebDriver driver, WebElement field, String text) throws IOException {
		field.clear();
		field.sendKeys(text);
	}

	/**
	 * Selects value from a dropdown list by visible text.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param select
	 *            the Select element of the dropdown menu
	 * @param text
	 *            the text to be selected from the dropdown menu
	 */
	public static void selectFromDropMenuByText(WebDriver driver, Select select, String text) {
		select.selectByVisibleText(text);
	}

	/**
	 * Clicks an element on the web page.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param locatorKey
	 *            the key from the ui-map
	 * @throws IOException
	 */
	public static void clickElement(WebDriver driver, String locatorKey) throws IOException {
		WebElement element = driver.findElement(Selenium.by(locatorKey));
		element.click();
	}

	/**
	 * Clicks a nested element on the web page.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param locatorKeys
	 *            the keys from the ui-map pointing to the nested element
	 * @throws IOException
	 */
	public static void clickNestedElement(WebDriver driver, String... locatorKeys) throws IOException {
		By[] bys = new By[locatorKeys.length];
		By by = null;
		for (int ctr = 0; ctr < bys.length; ctr++) {
			by = Selenium.by(locatorKeys[ctr]);
			bys[ctr] = by;
		}
		WebElement element = driver.findElement(new ByChained(bys));
		element.click();
	}

	/**
	 * Opens a new window by clicking an element and switches to that window.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param childLocatorKey
	 *            the key from the ui-map for the element that opens the child
	 *            window
	 * @return String - the handle of the parent window before opening the child
	 *         window
	 */
	public static String openWindowByElement(WebDriver driver, String childLocatorKey) throws IOException {
		String parentHandle = driver.getWindowHandle(); // Save parent window
		WebElement clickableElement = driver.findElement(Selenium.by(childLocatorKey));
		clickableElement.click(); // Open child window
		WebDriverWait wait = new WebDriverWait(driver, 10); // Timeout in 10s
		boolean isChildWindowOpen = wait.until(ExpectedConditions.numberOfWindowsToBe(2));
		if (isChildWindowOpen) {
			Set<String> handles = driver.getWindowHandles();
			// Switch to child window
			for (String handle : handles) {
				driver.switchTo().window(handle);
				if (!parentHandle.equals(handle)) {
					break;
				}
			}
			driver.manage().window().maximize();
		}
		return parentHandle; // Returns parent window if need to switch back
	}

	/**
	 * Opens a new window by navigating to a URL and switches to that window.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param childUrl
	 *            the String URL that opens the child window
	 * @return String - the handle of the parent window before opening the child
	 *         window
	 */
	public static String openWindowByUrl(WebDriver driver, String childUrl) {
		String parentHandle = driver.getWindowHandle();
		driver.get(childUrl);
		WebDriverWait wait = new WebDriverWait(driver, 10);
		boolean isChildWindowOpen = wait.until(ExpectedConditions.numberOfWindowsToBe(2));
		if (isChildWindowOpen) {
			Set<String> handles = driver.getWindowHandles();
			for (String handle : handles) {
				driver.switchTo().window(handle);
				if (!parentHandle.equals(handle)) {
					break;
				}
			}
			driver.manage().window().maximize();
		}
		return parentHandle;
	}

	/**
	 * Switches to an existing open window by window title.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @param windowTitle
	 *            the title of the window
	 * @return String - the handle of the parent window before opening the child
	 *         window
	 */
	public static String switchToWindowByTitle(WebDriver driver, String windowTitle) {
		Set<String> handles = driver.getWindowHandles();
		String parentHandle = driver.getWindowHandle();
		if (1 < handles.size()) {
			for (String handle : handles) {
				driver.switchTo().window(handle);
				if (windowTitle.equalsIgnoreCase(driver.getTitle())) {
					break;
				}
			}
			driver.manage().window().maximize();
		}
		return parentHandle;
	}

	/**
	 * Captures the current screen. Stores images in /target/cucumber-screenshots/
	 * in PNG format.
	 * 
	 * @param driver
	 *            the Selenium WebDriver
	 * @throws IOException
	 */
	public static void captureScreenshot(WebDriver driver) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(System.getProperty("user.dir").replace("\\", "/"));
		builder.append("/target/cucumber-screenshots/sshot_");
		builder.append(System.currentTimeMillis());
		builder.append(".png");
		String screenshot = builder.toString();

		File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(srcFile, new File(screenshot));
	}

	/**
	 * Captures and embeds screenshot in generated HTML report.
	 * 
	 * @param scenario
	 *            the Scenario object
	 * @param driver
	 *            the Selenium WebDriver
	 */
	public static void embedScreenshot(WebDriver driver, Scenario scenario) {
		byte[] srcBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
		scenario.embed(srcBytes, "image/png");
	}

}
