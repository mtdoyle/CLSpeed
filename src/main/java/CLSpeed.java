import com.rabbitmq.client.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public class CLSpeed implements Runnable {
    String maxSpeed;
    String address;
    Connection conn;
    Channel channel;
    long deliveryTag;
    String actualAddress;
    String currDate;

    public CLSpeed(Connection conn, String currDate) throws IOException {
        this.conn = conn;
        this.currDate = currDate;
    }

    public CLSpeed() throws IOException, TimeoutException {
        this.conn = getConnectionFactory();
    }

    private void setUpConnection() throws IOException {
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

    public void run() {
        try {
            setUpConnection();
        } catch (IOException e) {
            System.out.println("ERROR ERROR");
            e.printStackTrace();
        }
        checkAddress();
        try {
            channel.basicAck(deliveryTag, false);
            channel.close();
        } catch (IOException e) {
            System.out.println("ERROR ERROR ERROR");
            e.printStackTrace();
        } catch (TimeoutException e) {
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
        FirefoxProfile profile = new FirefoxProfile(new File("/home/clspeed/.mozilla/firefox/pz9y9wa8.clspeed"));
        WebDriver webdriver = new FirefoxDriver(profile);
        //WebDriver webdriver = new FirefoxDriver();
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
            actualAddress = webdriver.findElements(By.xpath("/html/body/ul/li[1]/a")).get(0).getAttribute("innerHTML").replace("<b>","").replace("</b>","");
            webdriver.findElement(By.id("ctam_nc-sfaddress")).clear();
            webdriver.findElement(By.id("ctam_nc-sfaddress")).sendKeys(actualAddress);
            webdriver.findElement(By.id("ctam_nc-go")).click();
        }
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        while (webdriver.findElements(By.id("ctam_nc-go")).size() < 1 && elapsedTime < 5){
            elapsedTime = (System.currentTimeMillis() - startTime)/1000;
        }
        if (webdriver.findElement(By.id("ctam_nc-go")).isDisplayed()){
                webdriver.findElement(By.id("ctam_nc-go")).click();
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
        if (webdriver.findElements(By.id("no-match-trillium-form")).size() > 0){
            webdriver.quit();
            displayBadAddress();
            return;
        }
        if (webdriver.findElements(By.id("maxSpeed")).size() > 0){
            this.maxSpeed = webdriver.findElement(By.id("maxSpeed")).getAttribute("value").split(":")[0].replaceAll("M", "");
            System.out.println(maxSpeed + ": " + submitAddress);
            webdriver.quit();
            writeToDB(maxSpeed, currDate);
            return;
        }
        else {
            webdriver.quit();
            displayBadAddress();
            return;
        }

    }
    private void writeToDB(String speed, String currDate){
        String[] addressSplit = address.split(",");
        String[] actualAddressSplit = actualAddress.split(",");
        String street = actualAddressSplit[0];
        String city = actualAddressSplit[1];
        String zip = actualAddressSplit[2].split(" ")[1];
        String lat = addressSplit[3];
        String lon = addressSplit[4];
        String garbage = addressSplit[5];
        String state = "MN";

        String sql = String.format("insert into clspeed_%s " +
                        "(street, city, state, zip, speed, emm_lat, emm_lng, emm_acc)" +
                        "values ('%s', '%s', '%s', '%s', %s, %s, %s, '%s')",
                currDate, street, city, state, zip, speed, lat, lon, garbage);
        WriteToMySQL writeToMySQL = new WriteToMySQL();
        writeToMySQL.executeStatement(sql);
    }

    private void displayBadAddress(){
        System.out.println("Bad address: " + address);
        String sql = String.format("insert into badaddresses " +
                        "(badaddress)" +
                        "values ('%s')",
                address);
        WriteToMySQL writeToMySQL = new WriteToMySQL();
        writeToMySQL.executeStatement(sql);
    }
}

