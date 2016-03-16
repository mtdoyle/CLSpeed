import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 *
 */
public class CLSpeed implements Runnable {
    String maxSpeed;
    private final String address;

    public CLSpeed(String address){
        this.address = address;
    }

    public void run () {
        WebDriver webdriver = new FirefoxDriver();
        webdriver.get("http://www.centurylink.com/home/internet");
        webdriver.findElement(By.id("home-internet-speed-check")).click();
        webdriver.findElement(By.id("ctam_new-customer-link")).click();
        webdriver.findElement(By.id("ctam_nc-sfaddress")).sendKeys(address);
        while (webdriver.findElements(By.xpath("/html/body/ul/li[1]/a")).size() < 1){
        }
        webdriver.findElement(By.xpath("/html/body/ul/li[1]/a")).click();
        maxSpeed = webdriver.findElement(By.id("maxSpeed")).getAttribute("value").split(":")[0].replaceAll("\\D", "");
        webdriver.close();
        System.out.println(maxSpeed);
    }
}
