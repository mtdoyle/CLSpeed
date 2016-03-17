import com.rabbitmq.client.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public class CLSpeed implements Runnable {
    String maxSpeed;
    String address;
    Channel channel;
    long deliveryTag;

    public CLSpeed() throws IOException, TimeoutException {
        Connection conn = getConnectionFactory();

        channel = conn.createChannel();

        channel.basicQos(1);

        channel.queueDeclare("clspeed", true, false, false, null);

        GetResponse response = channel.basicGet("clspeed", false);
        if (response == null) {
            //no message received
        } else {
            AMQP.BasicProperties props = response.getProps();
            byte[] body = response.getBody();
            this.deliveryTag = response.getEnvelope().getDeliveryTag();
            this.address = new String(body, "UTF-8");
        }
    }

    public void run(){
        checkAddress();
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnectionFactory() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost("192.168.1.211");
        factory.setPort(5672);
        return factory.newConnection();
    }

    public void checkAddress () {
        String[] choppedAddress = address.split(",");
        String submitAddress = choppedAddress[0] + ", " + choppedAddress[1] + ", MN " + choppedAddress[2];
        WebDriver webdriver = new FirefoxDriver();
        webdriver.get("http://www.centurylink.com/home/internet");
        webdriver.findElement(By.id("home-internet-speed-check")).click();
        webdriver.findElement(By.id("ctam_new-customer-link")).click();
        webdriver.findElement(By.id("ctam_nc-sfaddress")).sendKeys(submitAddress);
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        while (webdriver.findElements(By.xpath("/html/body/ul/li[1]/a")).size() < 1 && elapsedTime < 1){
            elapsedTime = (System.currentTimeMillis() - startTime)/1000;
        }
        if (webdriver.findElements(By.xpath("/html/body/ul/li[1]/a")).size() > 0){
            webdriver.findElements(By.xpath("/html/body/ul/li[1]/a")).get(0).click();
        }
        if (webdriver.findElements(By.id("ctam_nc-go")).size() > 0){
            if (webdriver.findElement(By.id("ctam_nc-go")).isDisplayed()){
                webdriver.findElement(By.id("ctam_nc-go")).click();
            }
        }
        if (webdriver.findElements(By.id("addressid2")).size() > 0){
            webdriver.findElements(By.id("addressid2")).get(0).click();
            webdriver.findElement(By.id("submitSecUnit")).click();
        }
        if (webdriver.getPageSource().contains("CenturyLink has fiber-connected Internet with speeds up to 1 Gig in your area")){
            webdriver.quit();
            displayBadAddress();
            return;
        }
        if (webdriver.getPageSource().contains("CenturyLink High-Speed Internet is not available in your area at this time, but we do have Internet options for you")){
            webdriver.quit();
            displayBadAddress();
            return;
        }
        if (webdriver.getCurrentUrl().contains("sorry.centurylink.com")){
            webdriver.quit();
            displayBadAddress();
            return;
        }
        if (webdriver.findElements(By.id("no-match-trillium-form")).size() > 0){
            webdriver.quit();
            displayBadAddress();
            return;
        }
        this.maxSpeed = webdriver.findElement(By.id("maxSpeed")).getAttribute("value").split(":")[0].replaceAll("\\D", "");
        System.out.println(maxSpeed + ": " + submitAddress);
        webdriver.quit();
        writeToDB();

    }
    private void writeToDB(){
        WriteToMySQL db = new WriteToMySQL(address, maxSpeed);
    }

    private void displayBadAddress(){
        System.out.println("Bad address: " + address);
    }
}

